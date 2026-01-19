package com.abhishek.ecommerce.payment.gateway.razorpay.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public final class RazorpaySignatureVerifier {

    private RazorpaySignatureVerifier() {}

    public static boolean verify(String orderId, String paymentId, String providedSignature, String secret) {
        if (orderId == null || paymentId == null || providedSignature == null || secret == null) return false;
        String payload = orderId + "|" + paymentId;
        String expected = hmacSha256Hex(payload, secret);
        return constantTimeEquals(expected, providedSignature);
    }

    private static String hmacSha256Hex(String data, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return toHex(hash);
        } catch (Exception e) {
            // Treat as verification failure; caller decides response
            return "";
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}

