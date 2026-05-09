package com.launchpad.flow.dto;

import jakarta.validation.constraints.NotBlank;

public final class IntegrationDtos {
    private IntegrationDtos() {
    }

    public record GmailRequest(
        @NotBlank String clientId,
        @NotBlank String clientSecret,
        @NotBlank String refreshToken,
        @NotBlank String fromEmail
    ) {
    }

    public record GitHubRequest(
        @NotBlank String token,
        String defaultOwner,
        String defaultRepo
    ) {
    }
}

