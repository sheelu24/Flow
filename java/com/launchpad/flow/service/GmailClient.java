package com.launchpad.flow.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GmailClient {
    private final HttpJsonClient httpJsonClient;

    public GmailClient(HttpJsonClient httpJsonClient) {
        this.httpJsonClient = httpJsonClient;
    }

    public Map<String, Object> sendEmail(Map<String, Object> credentials, String to, String subject, String body)
        throws Exception {
        String accessToken = refreshAccessToken(credentials);
        String from = credentials.get("fromEmail").toString();
        String raw = "From: " + from + "\r\n"
            + "To: " + to + "\r\n"
            + "Subject: " + subject + "\r\n"
            + "Content-Type: text/plain; charset=UTF-8\r\n\r\n"
            + body;

        Map<String, Object> request = Map.of(
            "raw", Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8))
        );
        return httpJsonClient.postJson("https://gmail.googleapis.com/gmail/v1/users/me/messages/send", accessToken, request);
    }

    private String refreshAccessToken(Map<String, Object> credentials) throws Exception {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("client_id", credentials.get("clientId").toString());
        form.put("client_secret", credentials.get("clientSecret").toString());
        form.put("refresh_token", credentials.get("refreshToken").toString());
        form.put("grant_type", "refresh_token");
        Map<String, Object> response = httpJsonClient.postForm("https://oauth2.googleapis.com/token", form);
        Object token = response.get("access_token");
        if (token == null) {
            throw AppException.badRequest("Gmail OAuth response did not include an access token");
        }
        return token.toString();
    }
}

