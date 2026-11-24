package com.example.glassfinance.parser

import com.example.glassfinance.data.model.TransactionType
import com.example.glassfinance.utils.foldVietnamese
import java.text.Normalizer
import java.time.LocalDateTime
import java.time.ZoneId

/** Giao dịch đọc được từ một thông báo ngân hàng. */
data class ParsedTransaction(
    val bank: String,            // phải trùng tên trong SupportedBanks (vd "VCB")
    val isIncome: Boolean,
    val amount: Long,            // luôn dương
    val timeMillis: Long,
    val content: String,
    val balanceAfter: Long?,
    /** Mã chống trùng khi cùng một thông báo được hệ thống báo lại nhiều lần. */
    val fingerprint: String
) {
    val type: String get() = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
}

interface BankNotificationParser {
    /** Trả về null nếu thông báo này không thuộc ngân hàng của parser. */
    fun parse(title: String, text: String, postTime: Long): ParsedTransaction?
}

/**
 * Danh sách parser. Muốn thêm ngân hàng: viết một object implement [BankNotificationParser]
 * (xem [VcbNotificationParser]) rồi thêm vào list này.
 *
 * Đã có: VCB, MB Bank, Techcombank.
 * Thông báo có chữ "VND" mà chưa nhận diện được sẽ được app lưu lại ở Cài đặt →
 * "Mẫu thông báo chưa nhận diện" để bạn sao chép gửi đi.
 */
object BankNotificationParsers {
    private val parsers: List<BankNotificationParser> = listOf(
        VcbNotificationParser,
        MbNotificationParser,
        TechcombankNotificationParser
    )

    fun parse(title: String, text: String, postTime: Long): ParsedTransaction? {
        val cleanText = normalize(text)
        if (cleanText.isBlank()) return null
        val cleanTitle = normalize(title)
        for (p in parsers) {
            val r = runCatching { p.parse(cleanTitle, cleanText, postTime) }.getOrNull()
            if (r != null) return r
        }
        return null
    }

    /** Gộp NFC (dấu tiếng Việt), đổi NBSP/xuống dòng thành khoảng trắng. */
    internal fun normalize(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFC)
            .replace('\u00A0', ' ')
            .replace('\u202F', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()
}

/**
 * VCB Digibank, ví dụ:
 *   Số dư TK VCB 10xxxxx6 +3,210 VND lúc 19-11-2026 13:12:04. Số dư 5,0xx,xxx VND.
 *   Ref DO VAN HIEU chuyen tien#SP#0200970422111913120420 26FWOR457546.5390.84674.131204
 * Dấu + là tiền vào, dấu - là tiền ra.
 */
object VcbNotificationParser : BankNotificationParser {

    private val MAIN = Regex(
        """Số dư TK VCB\s+(\S+)\s+([+-])\s*([\d.,]+)\s*VND\s+lúc\s+(\d{1,2})\s*-\s*(\d{1,2})\s*-\s*(\d{4})\s+(\d{1,2})\s*:\s*(\d{2})\s*:\s*(\d{2})""",
        RegexOption.IGNORE_CASE
    )
    private val BALANCE = Regex("""Số dư\s+([\d.,]+)\s*VND""", RegexOption.IGNORE_CASE)
    private val REF = Regex("""\bRef\s+(.+)$""", RegexOption.IGNORE_CASE)
    private val VN_ZONE: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")

    override fun parse(title: String, text: String, postTime: Long): ParsedTransaction? {
        val m = MAIN.find(text) ?: return null
        val (_, sign, rawAmount, dd, mm, yyyy, hh, mi, ss) = m.destructured
        val amount = digits(rawAmount) ?: return null
        if (amount <= 0L) return null

        val time = runCatching {
            LocalDateTime.of(yyyy.toInt(), mm.toInt(), dd.toInt(), hh.toInt(), mi.toInt(), ss.toInt())
                .atZone(VN_ZONE).toInstant().toEpochMilli()
        }.getOrDefault(postTime)

        // "Số dư <số>" đầu tiên là câu "Số dư TK VCB ..." nên chỉ lấy khớp nằm SAU phần giao dịch.
        val balance = BALANCE.find(text, m.range.last + 1)?.groupValues?.get(1)?.let(::digits)

        val ref = REF.find(text)?.groupValues?.get(1)?.trim().orEmpty()
        // Phần sau "#" là mã tra soát nội bộ của ngân hàng, không cần hiện cho người dùng.
        val content = ref.substringBefore('#').trim().ifBlank { "Giao dịch VCB" }

        val isIncome = sign == "+"
        return ParsedTransaction(
            bank = "VCB",
            isIncome = isIncome,
            amount = amount,
            timeMillis = time,
            content = content,
            balanceAfter = balance,
            fingerprint = "notif:vcb:${time / 1000}:${if (isIncome) "+" else "-"}$amount:${balance ?: 0}"
        )
    }

    private fun digits(s: String): Long? = s.filter { it.isDigit() }.toLongOrNull()
}

/**
 * MB Bank, ví dụ (các trường ngăn bằng dấu |):
 *   TK 33xxx888|GD: +70,000VND 20/11/26 15:21 | SD: 1,234,567VND|TU: HOANG THI THANH HIEN - 1234258|ND: Hoang Thanh Hien chuyen khoan nhanh qua Zalo Q28H79VH/308064
 * Dấu + là tiền vào, dấu - là tiền ra. Phần SD (số dư), TU (người gửi) và ND (nội dung) có thể thiếu.
 * Số dư không được lấy.
 * Ngày giờ dạng dd/MM/yy HH:mm (có thể có giây, năm có thể 2 hoặc 4 chữ số).
 * Mọi thứ đứng sau ngày giờ được lưu vào nội dung giao dịch (trừ số dư).
 */
object MbNotificationParser : BankNotificationParser {

    private val MAIN = Regex(
        """TK\s+[^\s|]+[\s|]*GD:\s*([+\-\u2212\u2013])\s*([\d.,]+)\s*VN[DĐ]\s*(\d{1,2})\s*/\s*(\d{1,2})\s*/\s*(\d{2,4})\s+(\d{1,2})\s*:\s*(\d{2})(?:\s*:\s*(\d{2}))?""",
        RegexOption.IGNORE_CASE
    )
    // Số dư (SD) không được lấy: cắt khỏi nội dung, chỉ giữ số tiền thu/chi.
    private val BALANCE = Regex("""\bSD:\s*[\d.,]+\s*VN[DĐ]\s*\|?""", RegexOption.IGNORE_CASE)
    private val VN_ZONE: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")

    override fun parse(title: String, text: String, postTime: Long): ParsedTransaction? {
        val m = MAIN.find(text) ?: return null
        val g = m.groupValues
        val isIncome = g[1] == "+"
        val amount = digits(g[2]) ?: return null
        if (amount <= 0L) return null

        val year = g[5].toInt().let { if (it < 100) 2000 + it else it }
        val time = runCatching {
            LocalDateTime.of(
                year, g[4].toInt(), g[3].toInt(),
                g[6].toInt(), g[7].toInt(), g[8].toIntOrNull() ?: 0
            ).atZone(VN_ZONE).toInstant().toEpochMilli()
        }.getOrDefault(postTime)

        // Mọi thứ đứng sau ngày giờ (TU, ND...) được lưu nguyên vào nội dung, trừ số dư.
        val content = BALANCE.replace(text.substring(m.range.last + 1), "")
            .replace(Regex("\\s+"), " ")
            .trim(' ', '|')
            .ifBlank { "Giao dịch MB Bank" }

        // Tin MB không có giây và không dùng số dư, nên thêm giờ nhận thông báo + nội dung để
        // hai khoản cùng số tiền trong một phút không bị coi là một. Cùng thông báo bị báo lại thì vẫn trùng.
        val tail = "${postTime / 1000}:${content.filterNot { it.isWhitespace() }.hashCode()}"
        return ParsedTransaction(
            bank = "MB Bank",
            isIncome = isIncome,
            amount = amount,
            timeMillis = time,
            content = content,
            balanceAfter = null,
            fingerprint = "notif:mb:${time / 1000}:${if (isIncome) "+" else "-"}$amount:$tail"
        )
    }

    private fun digits(s: String): Long? = s.filter { it.isDigit() }.toLongOrNull()
}

/**
 * Techcombank, có 2 dạng (các dòng của thông báo được gộp thành một):
 *  1) Thông báo đẩy: tiêu đề "+ VND 2,031", nội dung
 *       Tài khoản: 190776920 Số dư: VND 14,498 DO VAN HIEU chuyen tien
 *  2) Màn hình chi tiết trong app Techcombank:
 *       Tài khoản 19077692080010 Số tiền GD: -2,333 Số dư: 12,467 DO VAN HIEU chuyen tien. Ma tham chieu 257292
 * Dấu + là tiền vào, dấu - là tiền ra. Thông báo không có ngày giờ nên lấy giờ nhận thông báo.
 * Mọi thứ đứng sau số tài khoản/số tiền (nội dung, mã tham chiếu...) được lưu vào nội dung giao dịch;
 * số dư không được lấy.
 */
object TechcombankNotificationParser : BankNotificationParser {

    // Dạng 2: số tiền nằm trong nội dung.
    private val DETAIL = Regex(
        """Tài khoản:?\s*(\d+)\s+Số tiền GD:\s*([+\-\u2212\u2013])\s*([\d.,]+)\s*(?:VN[DĐ])?""",
        RegexOption.IGNORE_CASE
    )
    // Dạng 1: số tiền nằm ở tiêu đề, số tài khoản ở đầu nội dung.
    private val TITLE = Regex("""^\s*([+\-\u2212\u2013])\s*VN[DĐ]\s*([\d.,]+)""", RegexOption.IGNORE_CASE)
    private val ACCOUNT = Regex("""^\s*Tài khoản:?\s*(\d+)""", RegexOption.IGNORE_CASE)

    // Số dư không được lấy: cắt khỏi nội dung, chỉ giữ số tiền thu/chi.
    private val BALANCE = Regex("""Số dư:?\s*(?:VN[DĐ]\s*)?[\d.,]+\s*(?:VN[DĐ])?""", RegexOption.IGNORE_CASE)

    override fun parse(title: String, text: String, postTime: Long): ParsedTransaction? {
        val sign: String
        val amountText: String
        val account: String
        val tail: String

        val d = DETAIL.find(text)
        if (d != null) {
            account = d.groupValues[1]
            sign = d.groupValues[2]
            amountText = d.groupValues[3]
            tail = text.substring(d.range.last + 1)
        } else {
            val t = TITLE.find(title) ?: return null
            val a = ACCOUNT.find(text) ?: return null
            account = a.groupValues[1]
            sign = t.groupValues[1]
            amountText = t.groupValues[2]
            tail = text.substring(a.range.last + 1)
        }

        val isIncome = sign == "+"
        val amount = digits(amountText) ?: return null
        if (amount <= 0L) return null

        val content = BALANCE.replace(tail, " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .ifBlank { "Giao dịch Techcombank" }

        // Giờ nhận thông báo + nội dung giúp phân biệt các khoản cùng số tiền.
        return ParsedTransaction(
            bank = "Techcombank",
            isIncome = isIncome,
            amount = amount,
            timeMillis = postTime,
            content = content,
            balanceAfter = null,
            fingerprint = "notif:tcb:$account:${if (isIncome) "+" else "-"}$amount:${postTime / 1000}:${content.hashCode()}"
        )
    }

    private fun digits(s: String): Long? = s.filter { it.isDigit() }.toLongOrNull()
}

/** Đoán danh mục từ nội dung chuyển khoản; không đoán được thì "Khác". Người dùng vẫn sửa được. */
object CategoryGuesser {

    private val expenseRules = listOf(
        "Ăn uống" to listOf("an uong", "com ", "pho ", "bun ", "cafe", "ca phe", "tra sua", "highlands", "starbucks", "grabfood", "shopeefood", "nha hang"),
        "Di chuyển" to listOf("grab", "xanh sm", "taxi", "xang", "gui xe", "be car"),
        "Hóa đơn" to listOf("tien dien", "tien nuoc", "internet", "wifi", "vnpt", "viettel", "vinaphone", "mobifone", "tien nha", "thue nha", "hoa don"),
        "Mua sắm" to listOf("shopee", "lazada", "tiki", "tiktok shop"),
        "Học tập" to listOf("hoc phi", "khoa hoc", "tien hoc"),
        "Giải trí" to listOf("netflix", "spotify", "youtube", "cgv", "steam", "game"),
        "Sức khỏe" to listOf("thuoc", "benh vien", "phong kham", "bao hiem y te")
    )

    private val incomeRules = listOf(
        "Lương" to listOf("luong", "salary"),
        "Thưởng" to listOf("thuong", "bonus"),
        "Làm thêm" to listOf("lam them", "freelance", "cong viec")
    )

    fun guess(isIncome: Boolean, content: String): String {
        val folded = " " + content.foldVietnamese() + " "
        val rules = if (isIncome) incomeRules else expenseRules
        return rules.firstOrNull { (_, keys) -> keys.any { folded.contains(it) } }?.first ?: "Khác"
    }
}
