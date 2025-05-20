package iuh.fit.se.orderservice.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VNPayUtils {
    public static String buildQuery(Map<String, String> vnp_Params, String secretKey, String payUrl) {
        // Lấy danh sách các key, sắp xếp theo thứ tự alphabet
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        // Duyệt qua các field và build chuỗi hashData, query
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);

            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Build hash data
                hashData.append(fieldName).append("=");
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append("=");
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                // Thêm ký tự & nếu còn phần tử tiếp theo
                if (itr.hasNext()) {
                    hashData.append("&");
                    query.append("&");
                }
            }
        }

        // Tạo chữ ký bảo mật
        String vnp_SecureHash = hmacSHA512(secretKey, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);

        // Tạo URL thanh toán cuối cùng
        String paymentUrl = payUrl + "?" + query.toString();

        // Log để kiểm tra
        System.out.println("Hash Data: " + hashData);
        System.out.println("Query String: " + query);
        System.out.println("Payment URL: " + paymentUrl);

        return paymentUrl;
    }

    public static String hmacSHA512(final String key, final String data) {
        try {

            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            return "";
        }
    }
}
