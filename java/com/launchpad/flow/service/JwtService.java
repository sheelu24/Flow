package com.launchpad.flow.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchpad.flow.config.AppProperties;
import com.launchpad.flow.domain.User;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    public JwtService(AppProperties appProperties, ObjectMapper objectMapper) {
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
    }

    public String create(User user) {
        try {
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", user.getId().toString());
            payload.put("email", user.getEmail());
            payload.put("iat", Instant.now().getEpochSecond());
            payload.put("exp", Instant.now().plusSeconds(appProperties.getJwt().getTtlMinutes() * 60).getEpochSecond());

            String encodedHeader = encode(objectMapper.writeValueAsBytes(header));
            String encodedPayload = encode(objectMapper.writeValueAsBytes(payload));
            String signed = encodedHeader + "." + encodedPayload;
            return signed + "." + sign(signed);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to create JWT", exception);
        }
    }

    public Optional<Long> validate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return Optional.empty();
            }

            String signed = parts[0] + "." + parts[1];
            if (!constantTimeEquals(sign(signed), parts[2])) {
                return Optional.empty();
            }

            Map<String, Object> payload = objectMapper.readValue(DECODER.decode(parts[1]), new TypeReference<>() {
            });
            Number exp = (Number) payload.get("exp");
            if (exp == null || Instant.now().getEpochSecond() > exp.longValue()) {
                return Optional.empty();
            }
            return Optional.of(Long.parseLong(payload.get("sub").toString()));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private String encode(byte[] bytes) {
        return ENCODER.encodeToString(bytes);
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return encode(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}

