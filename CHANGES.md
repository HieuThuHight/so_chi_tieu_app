# GlassFinance – thay đổi

## Xóa các file cũ (đã được thay thế)
- data/repository/TransactionRepository.kt  -> FinanceRepository.kt
- data/viewmodel/TransactionViewModel.kt + TransactionViewModelFactory.kt -> FinanceViewModel.kt + FinanceViewModelFactory.kt

## Lỗi build đã sửa
- GlassFinanceApp.kt thiếu import (LocalContext, viewModel, AppDatabase...)
- TransactionsScreen khai báo `transactions` 2 lần, thiếu import collectAsState / TransactionViewModel
- Dashboard lồng Scaffold thứ 2 (double inset)

## Làm theo tiến trình
1. Tổng quan: số dư, thu chi tháng này, giao dịch gần đây (dữ liệu thật từ Room)
2. Giao dịch: lịch sử theo ngày, tìm kiếm không dấu, lọc Thu/Chi, thêm/sửa/xóa
3. Thống kê: biểu đồ cột 6 tháng, biểu đồ tròn theo danh mục, báo cáo nhanh
4. Cài đặt: giao diện, ngân hàng, thiết bị, đồng bộ/xuất CSV, bảo mật, dữ liệu
Chức năng bên trong: Danh mục (form + thống kê), Ngân sách (trong Thống kê), Tiết kiệm (trong Tổng quan)

## Lưu ý
- Room DB nâng lên v2 (thêm bảng budgets, savings_goals) và dùng fallbackToDestructiveMigration -> mở app lần đầu sẽ xóa dữ liệu cũ. Cần viết Migration thật trước khi phát hành.
- Chưa có build.gradle mới: không thêm thư viện nào.


## Đợt cập nhật: đọc thông báo, Lịch/Báo cáo, chống trùng
- Sửa lỗi build FinanceRepository: thêm vào TransactionDao `deleteByBankName`, `getPending`, `markSynced`, `getByFingerprints`.
- ui/debts/DebtsScreen.kt thực chất chứa bản SettingsScreen mới (có ô link Web App, độ trong kính) → đã hợp nhất vào ui/settings/SettingsScreen.kt và xóa file trùng. (Màn Sổ vay/nợ vẫn chưa có UI, ViewModel đã sẵn addDebt/payDebt/debts.)
- Đọc thông báo ngân hàng: parser/BankNotificationParser.kt (hiện có VCB), notification/BankNotificationListener.kt, đăng ký service trong AndroidManifest. Bật quyền ở Cài đặt → "Tự động đọc thông báo ngân hàng".
- Thông báo chưa nhận diện (giống tin biến động số dư) được lưu cục bộ, có nút "Sao chép" trong Cài đặt để lấy mẫu viết parser MB/Techcombank.
- Chống trùng: (1) cùng thông báo bị báo lại → bỏ qua nhờ fingerprint; (2) đã có bản Gmail/OCR cùng loại + số tiền trong ±15 phút → không lưu bản thông báo; (3) khi sheet kéo về bản Gmail/OCR mà đang có bản thông báo → gộp (xóa bản thông báo, giữ danh mục người dùng đã chọn).
- Tab mới "Lịch": lịch tháng (tổng thu/chi từng ngày, chạm ngày để xem giao dịch) + Báo cáo theo Tuần/Tháng/Quý/Năm (so với kỳ trước, biểu đồ cột, chi theo danh mục).
- Độ trong suốt của thẻ kính (slider trong Cài đặt) giờ có tác dụng thật.
- Thêm unit test: src/test/.../BankNotificationParserTest.kt, PeriodStatsTest.kt.


## Đợt cập nhật: bỏ Google Script, lưu SQL cục bộ, xuất / nhập dữ liệu
- Xóa hẳn gói `sync/` (SheetsSyncClient, SyncModels), ô "Link Web App", mã thiết bị, tự đồng bộ khi mở app, quyền INTERNET. App không còn dùng mạng.
- Mọi dữ liệu nằm trong một cơ sở dữ liệu SQLite (Room) trên máy. DB nâng v2 -> v3 bằng Migration thật (KHÔNG xóa dữ liệu): thêm bảng `debts`, `debt_payments`. Bỏ `fallbackToDestructiveMigration`.
- Sổ vay / nợ giờ lưu trong SQL (trước đây sống nhờ sheet) + màn hình mới `ui/debts/DebtsScreen.kt` (thêm khoản, ghi trả, hạn trả, quá hạn, lịch sử trả). Mở từ thẻ "Sổ vay / nợ" ở Tổng quan.
- Xuất dữ liệu (Cài đặt -> Dữ liệu & sao lưu), người dùng tự chọn nơi lưu:
  - CSV (có BOM UTF-8, Excel đọc đúng tiếng Việt)
  - JSON sao lưu đầy đủ (giao dịch, ngân sách, tiết kiệm, vay/nợ) - nhập lại được
  - SQL (CREATE TABLE + INSERT + view v_monthly_summary), mở bằng DB Browser for SQLite
  - Tệp .db SQLite nguyên bản
- Nhập từ JSON theo kiểu gộp: bỏ qua dữ liệu trùng, không xóa dữ liệu đang có.
- Xóa các cột/giá trị đồng bộ khỏi giao diện (dòng "Đồng bộ" ở chi tiết giao dịch...). Cột `externalId`, `syncStatus` vẫn giữ trong bảng để không phải đổi schema.
- File thay đổi chính: data/database/*, data/repository/FinanceRepository.kt, data/backup/*, data/viewmodel/*, data/preferences/SettingsStore.kt, ui/settings/SettingsScreen.kt, ui/dashboard/DashboardScreen.kt, ui/GlassFinanceApp.kt, MainActivity.kt, AndroidManifest.xml.


## Logo mới
- Icon app (adaptive icon): nền gradient navy -> xanh thương hiệu, đồng xu "kính" + 3 cột tăng dần (cột xanh mint = tăng trưởng).
- res/drawable/ic_launcher_background.xml, ic_launcher_foreground.xml, ic_launcher_monochrome.xml (Themed icon Android 13+), mipmap-anydpi-v26/*, mipmap-*/ic_launcher*.webp.
- branding/: glassfinance_logo.svg (bản gốc), master_1024.png, playstore_512.png (icon cho Google Play).


## Đọc được thông báo MB Bank
- parser/BankNotificationParser.kt: thêm `MbNotificationParser` (định dạng `TK xxx|GD: +70,000VND 20/11/26 15:21 | SD: ...|TU: ...|ND: ...`) và đăng ký vào `BankNotificationParsers`. Trước đây chỉ có VCB nên tin MB bị bỏ vào "Mẫu thông báo chưa nhận diện" và không thành giao dịch.
- Fingerprint chống trùng: `notif:mb:<giây>:<±số tiền>:<số dư>` (không có số dư thì dùng hash nội dung).
- Thêm unit test MB trong BankNotificationParserTest.kt.

## Đọc thông báo Techcombank + lưu toàn bộ phần sau số tiền vào nội dung
- Thêm `TechcombankNotificationParser` (dạng `Tài khoản <số> / Số tiền GD: -2,333 / Số dư: ... / <nội dung>`). Thông báo không có giờ nên lấy giờ nhận thông báo; mã chống trùng không phụ thuộc giờ.
- MB Bank và Techcombank: mọi thứ đứng sau số tiền (số dư, người gửi, nội dung, mã tham chiếu...) được lưu nguyên vào nội dung giao dịch. VCB giữ nguyên (nội dung lấy từ phần Ref).
- Chấp nhận cả dấu trừ Unicode (−, –).
- `UnrecognizedNotificationLog.looksLikeBankMessage` nhận cả tin không có chữ "VND" nhưng có "Số tiền GD".
- Techcombank mặc định đang TẮT: bật ở Cài đặt → ngân hàng.

## Không lấy số dư (MB Bank, Techcombank)
- Số dư (SD / "Số dư:") bị cắt khỏi nội dung và `balanceAfter = null`; app chỉ ghi nhận thu/chi và số tiền.
- Vì không còn dùng số dư, mã chống trùng của MB/Techcombank dùng giờ nhận thông báo + nội dung (có mã tham chiếu): cùng thông báo bị báo lại vẫn trùng, còn hai khoản cùng số tiền trong một phút vẫn được lưu riêng.

## Techcombank dạng thông báo đẩy, chỉ còn sao lưu JSON, liquid glass + chuyển động
- Techcombank: đọc thêm dạng thông báo đẩy (tiêu đề `+ VND 2,031`, nội dung `Tài khoản: ... Số dư: VND ... <nội dung>`). Số dư vẫn bị cắt khỏi nội dung.
- Cài đặt → Dữ liệu & sao lưu: bỏ các nút xuất CSV / SQL / .db, chỉ còn Xuất JSON và Nhập JSON. (Các lớp xuất khác vẫn còn trong code, chỉ không hiện nút.)
- GlassCard: thêm vệt sáng chéo như phản chiếu kính; thẻ bấm được có hiệu ứng nhún (`rememberPressScale`).
- Nút ＋ bật/tắt bằng scale + fade và nhún khi bấm; biểu tượng tab đang chọn phóng to bằng spring; chuyển tab trượt nhẹ theo hướng + mờ dần (thay Crossfade); thanh điều hướng bỏ tonalElevation, độ đặc theo Cài đặt.

## Liquid glass + bố cục + nhập tay danh mục "Khác"
- GlassBackground: thêm 4 quầng màu mềm trôi chậm phía sau (kính phải có thứ để nhìn xuyên qua), nên thanh "Độ trong suốt của kính" giờ thấy rõ tác dụng.
- GlassCard: vệt sáng chéo, viền sáng bên trong (sáng góc trên trái), viền ngoài mảnh, nhún khi bấm.
- GlassNavBar: thanh điều hướng dạng viên kính nổi; viên chọn trượt bằng spring và giãn nhẹ khi di chuyển; chữ/biểu tượng đổi màu mượt. Thay NavigationBar mặc định.
- Tổng quan: thẻ tổng số dư có vệt sáng + viền kính.
- Cài đặt: sắp lại thứ tự (Giao diện → Đọc thông báo → Ngân hàng → Sao lưu → Bảo mật → Thiết bị → Dữ liệu); ngân hàng thành chip gọn; thêm khung xem trước thẻ kính ngay dưới thanh trượt.
- Form sửa / thêm giao dịch: bấm "Khác" thì hiện ô nhập tên danh mục, bắt buộc nhập mới lưu được (Categories.OTHER).

## Tiêu đề ngày trong danh sách giao dịch
- DayHeader (TransactionsScreen): bỏ dải nền gần như đặc phủ ngang màn hình, thay bằng viên kính bo tròn trong suốt (độ đặc theo Cài đặt, bằng 70%), có viền sáng.
- DayHeader to hơn (chữ titleLarge / bodyLarge / titleSmall, đệm 18x14dp, bo 20dp); danh sách giao dịch dùng một khoảng cách đều 8dp giữa mọi phần tử (Arrangement.spacedBy), bỏ đệm lẻ của từng dòng.
- DayHeader: thêm topGap 16dp (trừ nhóm đầu) để tiêu đề ngày lùi xuống, cách giao dịch của ngày trước.


## Sửa lỗi "không lưu được giao dịch từ thông báo"
- Listener giờ đọc luôn các thông báo đang nằm trong khay ngay khi kết nối (trước đây chỉ đọc tin đến SAU khi bật quyền nên tin "vừa xong" bị lỡ). Có nút "🔄 Quét lại" trong Cài đặt.
- Parser MB Bank: cho phép TK và GD ngăn bằng `|`, xuống dòng hoặc khoảng trắng.
- Thêm nhật ký chẩn đoán (NotificationActivityLog): số thông báo đã nhận, lần gần nhất, kết quả từng tin (Đã lưu / Trùng / Ngân hàng đang tắt / Chưa nhận diện / Lỗi). Không lưu nội dung thông báo.
- Test mới cho mẫu MB ngày 21/11 (+29,000 VND).
- Cài đặt → Giao diện: 3 nút chế độ chia đều chiều ngang; thanh trượt độ trong suốt có con trượt chạy theo tay, có 13 nấc.
- Gỡ bộ đếm "Thông báo đã nhận" / "Lần nhận gần nhất". Nhật ký chỉ còn: lần kết nối của listener và kết quả các tin có dáng ngân hàng (giờ, tên app, kết quả; không lưu nội dung).

## 2026-11-23 – Kéo thanh điều hướng bằng nhấn giữ
- Thêm `detectDragGesturesAfterLongPress` cho vùng thanh `GlassNavBar`.
- Khi nhấn giữ và kéo ngang, tab được chọn cập nhật theo ô mà ngón tay đi qua; viên kính chọn tab tiếp tục dùng spring animation hiện có.
- Giữ nguyên thao tác chạm thông thường, callback `onSelect`, danh sách tab và luồng điều hướng của ứng dụng.
- Chưa xác nhận compile trong môi trường xử lý vì Gradle Wrapper cần tải Gradle 9.4.1 nhưng môi trường không truy cập được `services.gradle.org`.

## 2026-11-23 – Thanh điều hướng liquid glass mới (kiểu Apple Music)
- `GlassNavBar` viết lại: viên chọn tab là kính thật (khúc xạ riêng, ánh màu chủ đạo), nhấn xuống thì phồng lên (press-to-bulge), đặt tay và kéo thì viên chạy theo ngón tay, thả tay thì spring về đúng ô.
- Gộp chạm + kéo vào một `pointerInput` (bỏ `clickable` từng mục và `detectDragGesturesAfterLongPress`).
- `GlassFinanceApp`: thanh được vẽ nổi ngoài `Scaffold`, lấy backdrop ghi cả nền lẫn nội dung (`navBackdrop`); `bottomBar` chỉ còn `GlassNavBarSpacer()` để chừa chỗ. Ẩn/hiện bằng trượt + mờ.
- Chưa build được trong môi trường xử lý (không có mạng để tải Gradle): cần Sync + Make Project trong Android Studio.
- Cập nhật: bỏ khung viên thuốc bao quanh thanh; các tab trôi lơ lửng trên nền, chỉ viên chọn tab giữ hiệu ứng liquid glass.

## 2026-11-23 – Nút Cài đặt chuyển lên Tổng quan, thanh điều hướng gọn 4 tab
- Tổng quan: thêm nút ⚙️ ngay cạnh nút mắt (ẩn/hiện số dư) → mở màn Cài đặt (`SETTINGS_TAB`). Nút Back quay về Tổng quan như cũ.
- Thanh điều hướng chỉ còn 4 tab (Tổng quan, Giao dịch, Lịch, Thống kê); thanh căn giữa, bề rộng = số tab × `GlassNavCellWidth`, có khung kính viên thuốc ôm sát. Khi đang ở Cài đặt, viên chọn tab ẩn đi.
- Chưa build được trong môi trường xử lý: cần Sync + Make Project.

## 2026-11-23 – Thanh điều hướng dài, lơ lửng; chỉ chuyển màn khi nhấc tay
- Thanh dài hết bề ngang (tối đa 560dp), hình chữ nhật bo góc 26dp; viên chọn tab bo 20dp.
- Thanh lơ lửng phía trên nội dung: bỏ chỗ chừa của `bottomBar`, các màn hình tăng đệm đáy lên 120dp để cuộn hết vẫn không bị che; nút ＋ nâng lên 72dp.
- Nhấn giữ và kéo: viên kính chạy theo ngón tay nhưng chỉ điều hướng sang màn hình mới khi nhấc tay (trước đây đổi tab ngay khi kéo qua).
- Chưa build được trong môi trường xử lý: cần Sync + Make Project.

## 2026-11-23 – Chỉnh độ mờ / trong suốt của thanh điều hướng giống Apple Music
- Khung thanh: blur 18dp + vibrancy, khúc xạ nhẹ (lens 6/14dp), lớp phủ surface alpha = glass×0.40 + lớp trắng mờ; viền sáng mảnh. Thanh trượt "Độ trong suốt của kính" vẫn điều khiển độ đặc.
- Viên chọn: blur 8dp, ánh màu chủ đạo đậm hơn thanh một chút, thêm lớp sáng nhẹ, đậm lên khi nhấn.
- Chữ/biểu tượng tab chưa chọn đổi sang `onSurface` đậm (tương phản cao như ảnh tham chiếu).
- Chưa build được trong môi trường xử lý: cần Sync + Make Project.

## 2026-11-23 – Sửa chữ/thẻ lọt sắc nét vào thanh điều hướng
- Nội dung màn hình mờ dần về trong suốt ngay phía trên thanh (`GlassFinanceApp`), nên khi cuộn hay chuyển tab, thẻ/chữ không còn lọt sắc nét vào trong thanh.
- Thanh đặc hơn (glass×0.62). Hiệu ứng nền gắn theo phiên bản Android: blur từ API 31, khúc xạ từ API 33; máy cũ hơn dùng nền mờ đục thay thế.
- Chưa build được trong môi trường xử lý: cần Sync + Make Project.

## 2026-11-23 – Hoàn tác lớp mờ dần, sửa lại hiệu ứng kính của thanh
- Gỡ lớp mờ dần nội dung phía trên thanh (làm thanh nhìn như bản ban đầu); nội dung chạy xuống dưới thanh như ảnh mẫu.
- Sửa nghi vấn gốc: `.clip()` đặt trước `drawBackdrop` có thể làm backdrop không lấy được nền (thẻ GlassCard không có clip trước và chạy đúng). Đã bỏ clip trước, chỉ giữ clip sau.
- Lớp phủ thanh giảm còn glass×0.45 để thấy blur của nội dung phía sau.
- Chưa build được trong môi trường xử lý: cần Sync + Make Project.

## 2026-11-23 – Tinh chỉnh theo hướng của SimpMusic
- Viên chọn tab: squash & stretch (trượt thì dài ra và thấp xuống, nhấn thì phồng) + quầng sáng tròn khi nhấn.
- Khung thanh dài: bỏ khúc xạ (lens) để không bị đường nối trên viên dài, chỉ giữ blur + vibrancy; lens chỉ còn ở viên chọn.
- Kính luôn là phần tử anh em (sibling) của nguồn backdrop, không lồng bên trong (đã đúng sẵn).
- Chưa build được trong môi trường xử lý: cần Sync + Make Project.

## v1.1 (versionCode 2)
- Cài đặt: thêm thanh "Độ mờ thanh điều hướng" (10%-90%) cho thanh điều hướng dưới cùng.
- Thanh điều hướng và viên chọn tab bo tròn hết cỡ (kiểu Apple Music); blur nền tăng lên 28dp để nội dung cuộn phía sau không đọc xuyên qua được.
- Cài đặt > Tự động đọc thông báo ngân hàng: chỉ hiện 3 tin gần nhất, có nút "Xem thêm" / "Thu gọn".
- Phiên bản app 1.0 -> 1.1 (hiện ở Cài đặt > Thiết bị > Phiên bản app).


## 2026-11-23 – Phiên bản 1.2
- Thanh điều hướng: độ đặc mặc định giảm còn 6% (một nửa bản trước), blur chỉ còn 2dp, lớp phủ trắng mỏng hơn → nhìn xuyên thấy nội dung phía sau. Khóa lưu cài đặt đổi sang `nav_bar_opacity_v3` để mặc định mới có hiệu lực.
- Thanh điều hướng to hơn: cao 64 → 76dp, biểu tượng 20 → 24sp, chữ 11 → 12sp. Nút ＋ nâng lên 84dp; đệm đáy các màn 120 → 136dp để cuộn hết không bị che.
- Tiêu đề ngày trong danh sách giao dịch: nền đậm, gần như đặc khi cuộn.
- Thông báo "Đã lưu giao dịch" khi app ghi giao dịch từ thông báo ngân hàng (xin quyền POST_NOTIFICATIONS trên Android 13+).
- Sửa nút "Quét lại" / "Làm mới": quét thẳng khay thông báo thay vì requestRebind() (không có tác dụng khi listener đã kết nối).
- versionCode 2 → 3, versionName 1.1 → 1.2.
