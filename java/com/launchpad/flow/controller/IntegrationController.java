package com.launchpad.flow.controller;

import com.launchpad.flow.config.CurrentUser;
import com.launchpad.flow.dto.DtoMapper;
import com.launchpad.flow.dto.IntegrationDtos.GitHubRequest;
import com.launchpad.flow.dto.IntegrationDtos.GmailRequest;
import com.launchpad.flow.service.IntegrationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/integrations")
public class IntegrationController {
    private final IntegrationService integrationService;
    private final DtoMapper mapper;

    public IntegrationController(IntegrationService integrationService, DtoMapper mapper) {
        this.integrationService = integrationService;
        this.mapper = mapper;
    }

    @GetMapping
    Object list() {
        return integrationService.list(CurrentUser.require()).stream().map(mapper::integration).toList();
    }

    @PutMapping("/gmail")
    Object saveGmail(@Valid @RequestBody GmailRequest request) {
        return mapper.integration(integrationService.saveGmail(CurrentUser.require(), request));
    }

    @PutMapping("/github")
    Object saveGitHub(@Valid @RequestBody GitHubRequest request) {
        return mapper.integration(integrationService.saveGitHub(CurrentUser.require(), request));
    }

    @PostMapping("/gmail")
    Object postGmail(@Valid @RequestBody GmailRequest request) {
        return saveGmail(request);
    }

    @PostMapping("/github")
    Object postGitHub(@Valid @RequestBody GitHubRequest request) {
        return saveGitHub(request);
    }
}

