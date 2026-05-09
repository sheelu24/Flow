package com.launchpad.flow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchpad.flow.domain.IntegrationProvider;
import com.launchpad.flow.domain.User;
import com.launchpad.flow.domain.UserIntegration;
import com.launchpad.flow.dto.IntegrationDtos.GitHubRequest;
import com.launchpad.flow.dto.IntegrationDtos.GmailRequest;
import com.launchpad.flow.repository.UserIntegrationRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IntegrationService {
    private final UserIntegrationRepository integrationRepository;
    private final CryptoService cryptoService;
    private final ObjectMapper objectMapper;

    public IntegrationService(
        UserIntegrationRepository integrationRepository,
        CryptoService cryptoService,
        ObjectMapper objectMapper
    ) {
        this.integrationRepository = integrationRepository;
        this.cryptoService = cryptoService;
        this.objectMapper = objectMapper;
    }

    public List<UserIntegration> list(User user) {
        return integrationRepository.findByUser_IdOrderByProviderAsc(user.getId());
    }

    @Transactional
    public UserIntegration saveGmail(User user, GmailRequest request) {
        Map<String, Object> credentials = Map.of(
            "clientId", request.clientId(),
            "clientSecret", request.clientSecret(),
            "refreshToken", request.refreshToken(),
            "fromEmail", request.fromEmail()
        );
        return upsert(user, IntegrationProvider.GMAIL, "Gmail", credentials);
    }

    @Transactional
    public UserIntegration saveGitHub(User user, GitHubRequest request) {
        Map<String, Object> credentials = Map.of(
            "token", request.token(),
            "defaultOwner", request.defaultOwner() == null ? "" : request.defaultOwner(),
            "defaultRepo", request.defaultRepo() == null ? "" : request.defaultRepo()
        );
        return upsert(user, IntegrationProvider.GITHUB, "GitHub", credentials);
    }

    public Map<String, Object> credentials(User user, IntegrationProvider provider, Long integrationId) {
        UserIntegration integration = integrationId == null
            ? integrationRepository.findByUser_IdAndProviderAndActiveTrue(user.getId(), provider)
                .orElseThrow(() -> AppException.badRequest(provider + " integration is not configured"))
            : integrationRepository.findByIdAndUser_IdAndActiveTrue(integrationId, user.getId())
                .filter(candidate -> candidate.getProvider() == provider)
                .orElseThrow(() -> AppException.badRequest(provider + " integration is not configured"));
        try {
            return objectMapper.readValue(
                cryptoService.decrypt(integration.getEncryptedCredentialJson()),
                new TypeReference<>() {
                }
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored integration credentials are invalid", exception);
        }
    }

    private UserIntegration upsert(User user, IntegrationProvider provider, String name, Map<String, Object> credentials) {
        try {
            UserIntegration integration = integrationRepository
                .findByUser_IdAndProviderAndActiveTrue(user.getId(), provider)
                .orElseGet(UserIntegration::new);
            integration.setUser(user);
            integration.setProvider(provider);
            integration.setName(name);
            integration.setActive(true);
            integration.setEncryptedCredentialJson(cryptoService.encrypt(objectMapper.writeValueAsString(credentials)));
            return integrationRepository.save(integration);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to store integration credentials", exception);
        }
    }
}
