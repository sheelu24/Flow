package com.launchpad.flow.repository;

import com.launchpad.flow.domain.IntegrationProvider;
import com.launchpad.flow.domain.UserIntegration;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserIntegrationRepository extends JpaRepository<UserIntegration, Long> {
    List<UserIntegration> findByUser_IdOrderByProviderAsc(Long userId);

    Optional<UserIntegration> findByUser_IdAndProviderAndActiveTrue(Long userId, IntegrationProvider provider);

    Optional<UserIntegration> findByIdAndUser_IdAndActiveTrue(Long id, Long userId);
}
