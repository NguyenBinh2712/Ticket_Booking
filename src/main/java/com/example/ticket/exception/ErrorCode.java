package com.example.ticket.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // System
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(9000, "Invalid message key", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(9001, "Invalid request", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(9002, "Validation error", HttpStatus.BAD_REQUEST),

    // Auth & User
    USER_EXISTED(1001, "Email đã được sử dụng", HttpStatus.CONFLICT),
    USER_NOT_EXISTED(1002, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    PASSWORD_INVALID(1003, "Mật khẩu phải có ít nhất 8 ký tự", HttpStatus.BAD_REQUEST),
    INCORRECT_PASSWORD(1004, "Sai mật khẩu", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1005, "Chưa xác thực", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1006, "Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN),
    INVALID_TOKEN(1007, "Token không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED),
    USER_LOCKED(1008, "Tài khoản đã bị khóa", HttpStatus.FORBIDDEN),
    INVALID_ROLE_FOR_ACTION(1009, "Vai trò tài khoản không phù hợp với hành động này", HttpStatus.FORBIDDEN),
    GOOGLE_LOGIN_NO_PASSWORD(1010, "Tài khoản đăng nhập bằng Google không thể dùng mật khẩu", HttpStatus.BAD_REQUEST),
    OAUTH_PROVIDER_ERROR(1011, "Xác thực Google thất bại", HttpStatus.BAD_REQUEST),

    // Producer / Venue Profile
    PRODUCER_PROFILE_EXISTED(1100, "Bạn đã có hồ sơ nhà sản xuất", HttpStatus.CONFLICT),
    PRODUCER_PROFILE_NOT_FOUND(1101, "Không tìm thấy hồ sơ nhà sản xuất", HttpStatus.NOT_FOUND),
    VENUE_PROFILE_EXISTED(1102, "Bạn đã có hồ sơ trung tâm/địa điểm", HttpStatus.CONFLICT),
    VENUE_PROFILE_NOT_FOUND(1103, "Không tìm thấy hồ sơ địa điểm", HttpStatus.NOT_FOUND),
    PROFILE_NOT_VERIFIED(1104, "Hồ sơ chưa được xác minh, không thể thực hiện hành động này", HttpStatus.FORBIDDEN),
    PROFILE_ALREADY_REVIEWED(1105, "Hồ sơ đã được xử lý trước đó", HttpStatus.CONFLICT),
    PROFILE_REJECTED(1106, "Hồ sơ đã bị từ chối", HttpStatus.FORBIDDEN),

    // Room & Seat
    ROOM_NOT_FOUND(1200, "Không tìm thấy phòng", HttpStatus.NOT_FOUND),
    ROOM_NAME_DUPLICATED(1201, "Tên phòng đã tồn tại trong địa điểm này", HttpStatus.CONFLICT),
    ROOM_IN_MAINTENANCE(1202, "Phòng đang bảo trì, không thể sử dụng", HttpStatus.BAD_REQUEST),
    SEAT_TYPE_NOT_FOUND(1203, "Không tìm thấy loại ghế", HttpStatus.NOT_FOUND),
    SEAT_NOT_FOUND(1204, "Không tìm thấy ghế", HttpStatus.NOT_FOUND),
    SEAT_POSITION_DUPLICATED(1205, "Vị trí ghế đã tồn tại trong phòng này", HttpStatus.CONFLICT),
    INVALID_ROOM_LAYOUT(1206, "Số hàng/cột không hợp lệ", HttpStatus.BAD_REQUEST),
    VENUE_NOT_VERIFIED(1108, "Địa điểm chưa được xác minh, không thể cấu hình phòng", HttpStatus.FORBIDDEN),
    SEAT_MERGE_INVALID(1109, "Ghế đôi phải là 2 ghế liền kề cùng hàng, chưa bị gộp hay vô hiệu", HttpStatus.BAD_REQUEST),

    // Event
    EVENT_NOT_FOUND(1300, "Không tìm thấy sự kiện", HttpStatus.NOT_FOUND),
    EVENT_DETAIL_NOT_FOUND(1301, "Không tìm thấy thông tin chi tiết sự kiện", HttpStatus.NOT_FOUND),
    EVENT_NOT_EDITABLE(1302, "Sự kiện đang ở trạng thái không cho phép chỉnh sửa", HttpStatus.BAD_REQUEST),
    EVENT_NOT_OWNED_BY_PRODUCER(1303, "Sự kiện không thuộc quyền quản lý của bạn", HttpStatus.FORBIDDEN),
    INVALID_EVENT_STATUS_TRANSITION(1400, "Không thể chuyển sang trạng thái này từ trạng thái hiện tại", HttpStatus.BAD_REQUEST),
    PRODUCER_NOT_VERIFIED(1110, "Hồ sơ nhà sản xuất chưa được xác minh, không thể tạo sự kiện", HttpStatus.FORBIDDEN),
    EVENT_NOT_PUBLISHED(1304, "Sự kiện chưa được công khai", HttpStatus.NOT_FOUND),
    //  Event workflow / state machine
    EVENT_NOT_SUBMITTED(1401, "Sự kiện chưa được gửi duyệt", HttpStatus.BAD_REQUEST),
    EVENT_NOT_READY_TO_PUBLISH(1402, "Sự kiện chưa đủ điều kiện để công khai (thiếu hợp đồng xác nhận)", HttpStatus.BAD_REQUEST),
    EVENT_ALREADY_PUBLISHED(1403, "Sự kiện đã được công khai trước đó", HttpStatus.CONFLICT),
    EVENT_CANCELLED(1404, "Sự kiện đã bị hủy", HttpStatus.BAD_REQUEST),

    // Contract
    CONTRACT_NOT_FOUND(1500, "Không tìm thấy hợp đồng", HttpStatus.NOT_FOUND),
    CONTRACT_SHARE_PERCENT_INVALID(1501, "Tổng phần trăm chia doanh thu phải bằng 100%", HttpStatus.BAD_REQUEST),
    CONTRACT_ALREADY_RESPONDED(1502, "Hợp đồng đã được phản hồi trước đó", HttpStatus.CONFLICT),
    CONTRACT_NOT_OWNED_BY_VENUE(1503, "Hợp đồng không thuộc địa điểm của bạn", HttpStatus.FORBIDDEN),
    CONTRACT_ROOM_REQUIRED(1504, "Cần chọn phòng cụ thể khi chấp nhận hợp đồng", HttpStatus.BAD_REQUEST),
    CONTRACT_STATUS_INVALID_FOR_ACTION(1505, "Trạng thái hợp đồng không cho phép hành động này", HttpStatus.BAD_REQUEST),
     SHOWTIME_TIME_OVERLAPPED(1601, "Khung giờ bị trùng với suất khác trong cùng phòng", HttpStatus.CONFLICT),
    ROOM_NOT_OWNED_BY_VENUE(1207, "Phòng không thuộc địa điểm của bạn", HttpStatus.FORBIDDEN),
    // Showtime
    SHOWTIME_NOT_FOUND(1600, "Không tìm thấy suất chiếu/suất diễn", HttpStatus.NOT_FOUND),
    SHOWTIME_ALREADY_STARTED(1602, "Suất chiếu đã bắt đầu, không thể chỉnh sửa", HttpStatus.BAD_REQUEST),
    SHOWTIME_CANCELLED(1603, "Suất chiếu đã bị hủy", HttpStatus.BAD_REQUEST),

    // Voucher
    VOUCHER_NOT_FOUND(1700, "Mã giảm giá không tồn tại", HttpStatus.NOT_FOUND),
    VOUCHER_EXPIRED(1701, "Mã giảm giá đã hết hạn", HttpStatus.BAD_REQUEST),
    VOUCHER_OUT_OF_STOCK(1702, "Mã giảm giá đã hết lượt sử dụng", HttpStatus.BAD_REQUEST),
    VOUCHER_MIN_ORDER_NOT_MET(1703, "Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã", HttpStatus.BAD_REQUEST),
    VOUCHER_DISABLED(1704, "Mã giảm giá hiện không khả dụng", HttpStatus.BAD_REQUEST),

    // Booking & Seat Hold
    SEAT_ALREADY_HELD(1800, "Ghế đang được người khác giữ", HttpStatus.CONFLICT),
    SEAT_ALREADY_BOOKED(1801, "Ghế đã được đặt", HttpStatus.CONFLICT),
    SEAT_HOLD_NOT_FOUND(1802, "Không tìm thấy phiên giữ ghế, có thể đã hết hạn", HttpStatus.BAD_REQUEST),
    SEAT_HOLD_EXPIRED(1803, "Thời gian giữ ghế đã hết hạn", HttpStatus.BAD_REQUEST),
    SEAT_HOLD_NOT_OWNED(1804, "Ghế này không phải do bạn giữ", HttpStatus.FORBIDDEN),
    BOOKING_NOT_FOUND(1805, "Không tìm thấy đơn đặt vé", HttpStatus.NOT_FOUND),
    BOOKING_NOT_OWNED_BY_USER(1806, "Đơn đặt vé không thuộc về bạn", HttpStatus.FORBIDDEN),
    BOOKING_ALREADY_CONFIRMED(1807, "Đơn đặt vé đã được xác nhận trước đó", HttpStatus.CONFLICT),
    BOOKING_ALREADY_CANCELLED(1808, "Đơn đặt vé đã bị hủy", HttpStatus.BAD_REQUEST),
    BOOKING_CANCEL_NOT_ALLOWED(1809, "Đã quá thời hạn cho phép hủy vé", HttpStatus.BAD_REQUEST),
    NO_SEATS_SELECTED(1810, "Chưa chọn ghế nào", HttpStatus.BAD_REQUEST),
    RATE_LIMIT_EXCEEDED(1811, "Bạn thao tác quá nhanh, vui lòng thử lại sau", HttpStatus.TOO_MANY_REQUESTS),
    REJECT_REASON_REQUIRED(1107, "Cần nhập lý do khi từ chối hồ sơ", HttpStatus.BAD_REQUEST),
    SEAT_NOT_IN_ROOM(1807, "Ghế không thuộc phòng của suất chiếu này", HttpStatus.BAD_REQUEST),
    SEAT_INACTIVE(1808, "Ghế này không thể đặt (lối đi hoặc đã bị vô hiệu)", HttpStatus.BAD_REQUEST),
    SHOWTIME_NOT_AVAILABLE(1809, "Suất chiếu không còn khả dụng để đặt vé", HttpStatus.BAD_REQUEST),

    // Payment
    PAYMENT_NOT_FOUND(1900, "Không tìm thấy giao dịch thanh toán", HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_PROCESSED(1901, "Giao dịch đã được xử lý trước đó", HttpStatus.CONFLICT),
    PAYMENT_SIGNATURE_INVALID(1902, "Chữ ký giao dịch không hợp lệ", HttpStatus.BAD_REQUEST),
    PAYMENT_AMOUNT_MISMATCH(1903, "Số tiền thanh toán không khớp với đơn hàng", HttpStatus.BAD_REQUEST),
    PAYMENT_GATEWAY_ERROR(1905, "Lỗi kết nối tới cổng thanh toán", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_PAYMENT_METHOD(1906, "Phương thức thanh toán không hợp lệ", HttpStatus.BAD_REQUEST),
    BOOKING_EXPIRED(1810, "Đơn đặt vé đã hết hạn thanh toán", HttpStatus.BAD_REQUEST),
    REVENUE_TRANSACTION_NOT_FOUND(2000, "Không tìm thấy giao dịch doanh thu", HttpStatus.NOT_FOUND),
    TRANSACTION_ALREADY_SETTLED(2005, "Giao dịch đã được đối soát, không thể hoàn tác", HttpStatus.CONFLICT),
    PAYMENT_FAILED(1904, "Thanh toán thất bại", HttpStatus.BAD_REQUEST),
    // Revenue & Settlement
    SETTLEMENT_NOT_FOUND(2001, "Không tìm thấy đợt đối soát", HttpStatus.NOT_FOUND),
    SETTLEMENT_ALREADY_PAID(2002, "Đợt đối soát này đã được thanh toán", HttpStatus.CONFLICT),
    SETTLEMENT_PERIOD_INVALID(2003, "Khoảng thời gian đối soát không hợp lệ", HttpStatus.BAD_REQUEST),
    SETTLEMENT_NO_TRANSACTIONS(2004, "Không có giao dịch nào trong kỳ đối soát này", HttpStatus.BAD_REQUEST),

    // Review & Notification
    REVIEW_NOT_ALLOWED(2100, "Bạn cần tham gia sự kiện này trước khi đánh giá", HttpStatus.FORBIDDEN),
    REVIEW_ALREADY_EXISTED(2101, "Bạn đã đánh giá sự kiện này rồi", HttpStatus.CONFLICT),
    REVIEW_NOT_FOUND(2102, "Không tìm thấy đánh giá", HttpStatus.NOT_FOUND),
    NOTIFICATION_NOT_FOUND(2103, "Không tìm thấy thông báo", HttpStatus.NOT_FOUND),


    // Otp
    INVALID_OTP(2200,"OTP NOT VALID",HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(2201,"OTP EXPIRED",HttpStatus.BAD_REQUEST),
    OTP_SEND_TOO_FAST(2202, "Please wait before requesting another OTP",HttpStatus.TOO_MANY_REQUESTS),
    OTP_LIMIT_EXCEEDED(2203, "OTP request limit exceeded",HttpStatus.TOO_MANY_REQUESTS),

    // File
    FILE_UPLOAD_FAILED(2300,"File upload failed",HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_EMPTY(2301,"File empty",HttpStatus.BAD_REQUEST),

    ;

    private int code;
    private String message;
    private HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}