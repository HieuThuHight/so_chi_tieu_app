package com.example.glassfinance.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class BankNotificationParserTest {

    private val vcbIn = "Số dư TK VCB 10123456 +3,210 VND lúc 19-11-2026 13:12:04. " +
            "Số dư 5,012,345 VND. Ref DO VAN HIEU chuyen tien#SP#02009704221119131204202 6FWOR457546.5390.84674.131204"

    @Test fun vcb_tienVao() {
        val p = BankNotificationParsers.parse("Thông báo VCB", vcbIn, 0L)
        assertNotNull(p)
        p!!
        assertEquals("VCB", p.bank)
        assertTrue(p.isIncome)
        assertEquals(3210L, p.amount)
        assertEquals(5_012_345L, p.balanceAfter)
        assertEquals("DO VAN HIEU chuyen tien", p.content)
        val t = Instant.ofEpochMilli(p.timeMillis).atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
        assertEquals("2026-11-19T13:12:04", t.toLocalDateTime().toString())
    }

    @Test fun vcb_tienRa() {
        val p = BankNotificationParsers.parse(
            "", "Số dư TK VCB 10123456 -150,000 VND lúc 20-11-2026 08:05:30. Số dư 4,862,345 VND. Ref THANH TOAN HIGHLANDS COFFEE", 0L
        )!!
        assertFalse(p.isIncome)
        assertEquals(150_000L, p.amount)
        assertEquals("Ăn uống", CategoryGuesser.guess(p.isIncome, p.content))
    }

    @Test fun vcb_ngatDongVaNbspVanDoc() {
        val wrapped = "Số dư TK VCB 10123456\n+3,210\u00A0VND lúc 19-11-\n2026 13:12:04. Số dư 5,012,345 VND. Ref DO VAN\nHIEU chuyen tien#SP#0200"
        assertEquals(
            BankNotificationParsers.parse("", vcbIn, 0L)!!.fingerprint,
            BankNotificationParsers.parse("", wrapped, 0L)!!.fingerprint
        )
    }

    private val mbIn = "TK 33xxx888|GD: +70,000VND 20/11/26 15:21 | SD: 1,234,567VND|" +
            "TU: HOANG THI THANH HIEN - 1234258|ND: Hoang Thanh Hien chuyen khoan nhanh qua Zalo Q28H79VH/308064"

    @Test fun mb_tienVao() {
        val p = BankNotificationParsers.parse("Thông báo biến động số dư", mbIn, 0L)
        assertNotNull(p)
        p!!
        assertEquals("MB Bank", p.bank)
        assertTrue(p.isIncome)
        assertEquals(70_000L, p.amount)
        assertNull(p.balanceAfter)
        assertEquals(
            "TU: HOANG THI THANH HIEN - 1234258|ND: Hoang Thanh Hien chuyen khoan nhanh qua Zalo Q28H79VH/308064",
            p.content
        )
        assertFalse(p.content.contains("1,234,567"))
        val t = Instant.ofEpochMilli(p.timeMillis).atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
        assertEquals("2026-11-20T15:21", t.toLocalDateTime().toString())
    }

    @Test fun mb_tienRa_namBonChuSo_khongCoTuVaSoDu() {
        val p = BankNotificationParsers.parse(
            "", "TK 56xxx613|GD: -20,000VND 21/11/2026 09:05:30|ND: DO VAN HIEU chuyen tien", 0L
        )!!
        assertFalse(p.isIncome)
        assertEquals(20_000L, p.amount)
        assertEquals(null, p.balanceAfter)
        val t = Instant.ofEpochMilli(p.timeMillis).atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
        assertEquals("2026-11-21T09:05:30", t.toLocalDateTime().toString())
    }

    @Test fun mb_ngatDongVaNbspVanDoc() {
        val wrapped = "TK 33xxx888|GD: +70,000VND\n20/11/26 15:21 | SD: 1,234,567VND|\u00A0TU: HOANG THI THANH HIEN - 1234258\n" +
                "|ND: Hoang Thanh Hien chuyen khoan nhanh qua Zalo Q28H79VH/308064"
        assertEquals(
            BankNotificationParsers.parse("", mbIn, 0L)!!.fingerprint,
            BankNotificationParsers.parse("", wrapped, 0L)!!.fingerprint
        )
    }

    @Test fun mb_khongNhamVoiVcb() {
        assertEquals("VCB", BankNotificationParsers.parse("", vcbIn, 0L)!!.bank)
        assertNull(BankNotificationParsers.parse("", "Mã OTP của bạn là 123456, không chia sẻ cho ai", 0L))
    }

    private val tcbOut = "Tài khoản 19077692080010\nSố tiền GD: -2,333\nSố dư: 12,467\n" +
            "DO VAN HIEU chuyen tien. Ma tham chieu 257292"

    @Test fun techcombank_tienRa_luuToanBoSauSoTienVaoNoiDung() {
        val p = BankNotificationParsers.parse("Biến động số dư", tcbOut, 1_000_000L)
        assertNotNull(p)
        p!!
        assertEquals("Techcombank", p.bank)
        assertFalse(p.isIncome)
        assertEquals(2_333L, p.amount)
        assertNull(p.balanceAfter)
        assertEquals(1_000_000L, p.timeMillis)
        assertEquals("DO VAN HIEU chuyen tien. Ma tham chieu 257292", p.content)
    }

    @Test fun techcombank_tienVao_baoLaiCungGioThiTrung_khacGioThiKhongTrung() {
        val inText = "Tài khoản 19077692080010 Số tiền GD: +50,000 Số dư: 62,467 NGUYEN VAN A chuyen tien"
        val a = BankNotificationParsers.parse("", inText, 1_000L)!!
        val b = BankNotificationParsers.parse("", inText, 1_000L)!!
        val c = BankNotificationParsers.parse("", inText, 9_000L)!!
        assertTrue(a.isIncome)
        assertEquals(50_000L, a.amount)
        assertEquals("NGUYEN VAN A chuyen tien", a.content)
        assertEquals(a.fingerprint, b.fingerprint)
        assertFalse(a.fingerprint == c.fingerprint)
    }

    @Test fun techcombank_thongBaoDay_soTienONguTieuDe() {
        val p = BankNotificationParsers.parse(
            "+ VND 2,031",
            "Tài khoản: 190776920\nSố dư: VND 14,498\nDO VAN HIEU chuyen tien",
            5_000L
        )
        assertNotNull(p)
        p!!
        assertEquals("Techcombank", p.bank)
        assertTrue(p.isIncome)
        assertEquals(2_031L, p.amount)
        assertNull(p.balanceAfter)
        assertEquals("DO VAN HIEU chuyen tien", p.content)
        assertEquals(5_000L, p.timeMillis)
    }

    @Test fun techcombank_thongBaoDay_tienRa() {
        val p = BankNotificationParsers.parse("- VND 15,000", "Tài khoản: 190776920 Số dư: VND 1,498 Tra tien an", 0L)!!
        assertFalse(p.isIncome)
        assertEquals(15_000L, p.amount)
        assertEquals("Tra tien an", p.content)
    }

    @Test fun mb_mauThat_khongCoKhoangTrangTruocSD() {
        val p = BankNotificationParsers.parse(
            "Thông báo biến động số dư",
            "TK 56xxx613|GD: +12,134VND 20/11/26 15:03 |SD: 12,995VND|TU: DO VAN HIEU - 104316|" +
                "ND: MBVCB.17136108666.321726.DO VAN HIEU chuyen tien.CT tu 1043118976 DO VAN HIEU toi 54613 DO VAN HIEU tai MB- Ma GD ACSP/ dy321726",
            0L
        )!!
        assertEquals("MB Bank", p.bank)
        assertTrue(p.isIncome)
        assertEquals(12_134L, p.amount)
        assertFalse(p.content.contains("12,995"))
        assertTrue(p.content.startsWith("TU: DO VAN HIEU - 104316|ND: MBVCB."))
    }

    @Test fun thongBaoKhac_traVeNull() {
        assertNull(BankNotificationParsers.parse("Zalo", "Bạn có tin nhắn mới", 0L))
    }

    @Test fun doanDanhMuc() {
        assertEquals("Lương", CategoryGuesser.guess(true, "CONG TY ABC TRA LUONG T11"))
        assertEquals("Khác", CategoryGuesser.guess(true, "DO VAN HIEU chuyen tien"))
    }

    @Test fun mb_mauAnh_21_11_tienVao29k() {
        val p = BankNotificationParsers.parse(
            "Thông báo biến động số dư",
            "TK 91xxx58|GD: +29,000VND 21/11/26 17:06 |SD: 1,234,567VND|TU: HOANG THI THANH HOA - 0211000258458| " +
                "ND: Qahgwu5644 MBBMS01195548 1 MBVCB.1715394175.513076.VQRLOAMB20260226170300211.CT tu 0211000258458 " +
                "HOANG THI THANH HOA toi VQRQAHGWU5644 PH",
            0L
        )!!
        assertEquals("MB Bank", p.bank)
        assertTrue(p.isIncome)
        assertEquals(29_000L, p.amount)
        assertFalse(p.content.contains("1,234,567"))
        val t = Instant.ofEpochMilli(p.timeMillis).atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
        assertEquals("2026-11-21T17:06", t.toLocalDateTime().toString())
    }

    @Test fun mb_taiKhoanVaGdNganBangXuongDongVanDoc() {
        val p = BankNotificationParsers.parse(
            "", "TK 91xxx58\nGD: +29,000VND\n21/11/26 17:06 |SD: 1,234,567VND|TU: A - 1|ND: B", 0L
        )!!
        assertEquals("MB Bank", p.bank)
        assertEquals(29_000L, p.amount)
    }
}
