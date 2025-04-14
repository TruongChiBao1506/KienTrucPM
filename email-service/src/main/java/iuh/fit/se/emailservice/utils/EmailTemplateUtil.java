package iuh.fit.se.emailservice.utils;

import iuh.fit.se.emailservice.entities.Order;
import iuh.fit.se.emailservice.entities.User;
import iuh.fit.se.emailservice.feign.UserServiceClient;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class EmailTemplateUtil {
    public static String buildEmailContent(Order order, User user, StringBuilder productTable) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            background-color: #f4f4f4;
                            margin: 0;
                            padding: 0;
                        }
                        .email-container {
                            width: 100%%;
                            max-width: 600px;
                            margin: 0 auto;
                            background-color: #ffffff;
                            padding: 20px;
                            border: 1px solid #ddd;
                            border-radius: 8px;
                        }
                        .header {
                            text-align: center;
                            padding-bottom: 20px;
                            border-bottom: 1px solid #eee;
                        }
                        .header img {
                            width: 150px;
                        }
                        .content {
                            padding: 20px 0;
                        }
                        .content h2 {
                            color: #4CAF50;
                        }
                        .order-info {
                            background-color: #f9f9f9;
                            padding: 10px;
                            margin-bottom: 20px;
                            border-radius: 8px;
                        }
                        .products-table {
                            width: 100%%;
                            border-collapse: collapse;
                            margin-bottom: 20px;
                        }
                        .products-table th {
                            background-color: #f2f2f2;
                            padding: 10px;
                            text-align: left;
                        }
                        .products-table td {
                            padding: 10px;
                            border-bottom: 1px solid #eee;
                        }
                        .products-table td:last-child {
                            text-align: right;
                        }
                        .amount-section {
                            padding: 10px 0;
                            border-top: 1px solid #eee;
                        }
                        .amount-section table {
                            width: 100%%;
                        }
                        .amount-section td {
                            padding: 5px 0;
                        }
                        .total-amount {
                            color: #E91E63;
                            font-size: 18px;
                            font-weight: bold;
                        }
                        .footer {
                            text-align: center;
                            padding-top: 20px;
                            font-size: 12px;
                            color: #666;
                        }
                        .footer p {
                            margin: 5px 0;
                        }
                        .footer hr {
                            border: none;
                            border-top: 1px solid #eee;
                            margin: 10px 0;
                        }
                    </style>
                </head>
                <body>
                    <div class="email-container">
                        <div class="header">
                            <img src="cid:logo" alt="Logo" width='1200' height='50'>
                            <h2 style="margin: 0;">Xác nhận đơn hàng #%s</h2>
                        </div>
                        
                        <div class="content">
                            <h2>Xin chào %s,</h2>
                            <p>Cảm ơn bạn đã đặt hàng tại cửa hàng của chúng tôi!</p>
                            
                            <div class="order-info">
                                <h3 style="margin-top: 0;">Thông tin đơn hàng:</h3>
                                <p><strong>Mã đơn hàng:</strong> #%d</p>
                                <p><strong>Ngày đặt hàng:</strong> %s</p>
                                <p><strong>Phương thức thanh toán:</strong> %s</p>
                            </div>

                            <h3>Chi tiết đơn hàng</h3>
                            <table class="products-table">
                                <thead>
                                    <tr>
                                        <th>Sản phẩm</th>
                                        <th style="text-align: right;">Màu</th>
                                        <th style="text-align: right;">Số lượng</th>
                                        <th style="text-align: right;">Giá</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    %s
                                </tbody>
                            </table>

                            <div class="amount-section">
                                <table style="width: 100%%;">
                                    <tr>
                                        <td>Tạm tính:</td>
                                        <td style="text-align: right;">%s</td>
                                    </tr>
                                    <tr>
                                        <td>Phí vận chuyển:</td>
                                        <td style="text-align: right;">0đ</td>
                                    </tr>
                                    <tr>
                                        <td style="font-weight: bold; font-size: 18px;">Tổng cộng:</td>
                                        <td style="text-align: right;"><span class="total-amount">%s</span></td>
                                    </tr>
                                </table>
                            </div>
                        </div>

                        <div class="footer">
                            <p><strong>Cần hỗ trợ?</strong></p>
                            <p>Email: support@yourstore.com | Hotline: 096.689.4644</p>
                            <p>Địa chỉ: 112 Cao Thắng, Quận 3 – HCM</p>
                            <hr>
                            <p>© 2024 Eyeglasses Store. All rights reserved.</p>
                            <p style="color: #999;">Email này được gửi tự động, vui lòng không trả lời.</p>
                        </div>
                    </div>
                </body>
                </html>
            """.formatted(
                order.getOrderNumber(),
                user.getFullname(),
                order.getId(),
                order.getOrderDate().toString(),
                order.getPaymentMethod(),
                productTable.toString(),
                formatCurrency(order.getTotalAmount()),
                formatCurrency(order.getTotalAmount())
        );
    }
    public static void sendHtmlEmailWithInlineImage(JavaMailSender mailSender, String to, String subject,
                                                    String htmlContent, String imagePath, String contentId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom("sendingemaileventhub@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            ClassPathResource image = new ClassPathResource(imagePath);
            helper.addInline(contentId, image);

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    public static String formatCurrency(double amount) {
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return currencyFormat.format(amount) + "đ";
    }
}
