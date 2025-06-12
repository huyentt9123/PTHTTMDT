package com.ecom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.springframework.web.bind.annotation.RequestMethod;
import org.json.JSONObject;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/momo")
public class MomoController {
    // Thông tin test Momo (sandbox)
    private static final String endpoint = "https://test-payment.momo.vn/v2/gateway/api/create";
    private static final String partnerCode = "MOMONPMB20210629";
    private static final String accessKey = "Q2XhhSdgpKUlQ4Ky";
    private static final String secretKey = "k6B53GQKSjktZGJBK2MyrDa7w9S6RyCf";
    private static final String returnUrl = "http://localhost:8081/momo/return";
    private static final String notifyUrl = "http://localhost:8081/momo/notify";

    @RequestMapping(value = "/payment", method = {RequestMethod.GET, RequestMethod.POST})
    public RedirectView createMomoPayment(@RequestParam long amount, @RequestParam(required = false) String orderInfo, Model model) throws Exception {
        String orderId = UUID.randomUUID().toString();
        String requestId = UUID.randomUUID().toString();
        String orderInfoStr = orderInfo != null ? orderInfo : "Thanh toan don hang EcomStore";
        String requestType = "captureWallet";
        String orderType = "momo_wallet";

        // Raw data for signature (đúng thứ tự)
        String rawSignature = "accessKey=" + accessKey +
                "&amount=" + ((long)amount) +
                "&extraData=" + "" +
                "&ipnUrl=" + notifyUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfoStr +
                "&orderType=" + orderType +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + returnUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        String signature = hmacSHA256(rawSignature, secretKey);

        // Build request body
        JSONObject json = new JSONObject();
        json.put("partnerCode", partnerCode);
        json.put("accessKey", accessKey);
        json.put("requestId", requestId);
        json.put("amount", (long)amount);
        json.put("orderId", orderId);
        json.put("orderInfo", orderInfoStr);
        json.put("redirectUrl", returnUrl);
        json.put("ipnUrl", notifyUrl);
        json.put("lang", "vi");
        json.put("extraData", "");
        json.put("requestType", requestType);
        json.put("orderType", orderType);
        json.put("signature", signature);

        // Send request to Momo
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(json.toString().getBytes());
        os.flush();
        os.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject responseJson = new JSONObject(response.toString());
        String payUrl = responseJson.getString("payUrl");

        return new RedirectView(payUrl);
    }

    @GetMapping("/return")
    public String momoReturn(@RequestParam Map<String, String> params, Model model) {
        // Xử lý kết quả redirect từ Momo (thành công/thất bại)
        model.addAttribute("params", params);
        return "momo_return"; // Tạo file momo_return.html để hiển thị kết quả
    }

    @PostMapping("/notify")
    @ResponseBody
    public String momoNotify(@RequestParam Map<String, String> params) {
        // Xử lý notify từ Momo (cập nhật trạng thái đơn hàng)
        // TODO: Kiểm tra signature, cập nhật trạng thái đơn hàng
        return "{" + "\"message\":\"success\"}";
    }

    // Hàm tạo chữ ký HMAC SHA256
    private String hmacSHA256(String data, String key) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        hmacSHA256.init(secretKeySpec);
        byte[] bytes = hmacSHA256.doFinal(data.getBytes());
        StringBuilder hash = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hash.append('0');
            hash.append(hex);
        }
        return hash.toString();
    }
} 