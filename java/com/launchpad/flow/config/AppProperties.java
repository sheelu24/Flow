package com.launchpad.flow.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private final Jwt jwt = new Jwt();
    private final Crypto crypto = new Crypto();
    private final Cors cors = new Cors();
    private final Execution execution = new Execution();

    public Jwt getJwt() {
        return jwt;
    }

    public Crypto getCrypto() {
        return crypto;
    }

    public Cors getCors() {
        return cors;
    }

    public Execution getExecution() {
        return execution;
    }

    public static class Jwt {
        private String secret;
        private long ttlMinutes = 720;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getTtlMinutes() {
            return ttlMinutes;
        }

        public void setTtlMinutes(long ttlMinutes) {
            this.ttlMinutes = ttlMinutes;
        }
    }

    public static class Crypto {
        private String secret;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:5173");

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class Execution {
        private int maxSteps = 200;
        private int maxDelaySeconds = 300;
        private int retryCount = 2;

        public int getMaxSteps() {
            return maxSteps;
        }

        public void setMaxSteps(int maxSteps) {
            this.maxSteps = maxSteps;
        }

        public int getMaxDelaySeconds() {
            return maxDelaySeconds;
        }

        public void setMaxDelaySeconds(int maxDelaySeconds) {
            this.maxDelaySeconds = maxDelaySeconds;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public void setRetryCount(int retryCount) {
            this.retryCount = retryCount;
        }
    }
}

