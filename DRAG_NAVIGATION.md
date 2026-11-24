# GlassNavBar – kéo tab bằng nhấn giữ

## Cách sử dụng
1. Mở ứng dụng và tới thanh điều hướng dưới màn hình.
2. Đặt ngón tay lên một mục bất kỳ.
3. Nhấn giữ một chút đến khi thao tác kéo được nhận diện.
4. Giữ ngón tay trên màn hình và kéo ngang qua các mục khác.
5. Tab đang nằm dưới ngón tay sẽ được chọn; nội dung màn hình và viên kính chỉ báo chuyển tab bằng animation hiện có.

## Thay đổi kỹ thuật
`GlassNavBar` gắn `pointerInput` lên toàn bộ hàng điều hướng và dùng `detectDragGesturesAfterLongPress`. Tọa độ X của ngón tay được chia theo chiều rộng từng ô để tính chỉ số tab, sau đó gọi callback `onSelect(index)` hiện có.

Tap thông thường vẫn được xử lý bởi `clickable` trên từng mục. Không thay đổi `selectedTab`, `AnimatedContent`, ViewModel, Room hay các chức năng tài chính.
