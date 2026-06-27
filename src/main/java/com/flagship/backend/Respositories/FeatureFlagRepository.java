package com.flagship.backend.Respositories;

import com.flagship.backend.Entities.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, UUID> {
    Optional<FeatureFlag> findFeatureFlagById(UUID id);
    List<FeatureFlag> findFeatureFlagsByOwner(String owner);
    boolean existsByOwnerAndFlagName(String owner, String flagName);
}
