package com.flagship.backend.Controllers;

import com.flagship.backend.DTO.CreateFlagRequest;
import com.flagship.backend.DTO.FlagDTO;
import com.flagship.backend.DTO.TargetingRuleDTO;
import com.flagship.backend.DTO.UpdateFlagRequest;
import com.flagship.backend.Entities.FeatureFlag;
import com.flagship.backend.Entities.TargetingRule;
import com.flagship.backend.Respositories.FeatureFlagRepository;
import com.flagship.backend.Services.*;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/flags")
public class FlagController {

    private final CreateFlagService createFlagService;
    private final UpdateFlagService updateFlagService;
    private final DeleteFlagService deleteFlagService;
    private final ResetUsersService resetUsersService;
    private final ValidateOwnershipService validateOwnershipService;
    private final FeatureFlagRepository featureFlagRepository;

    public FlagController(CreateFlagService createFlagService, UpdateFlagService updateFlagService, DeleteFlagService deleteFlagService, ResetUsersService resetUsersService, ValidateOwnershipService validateOwnershipService, FeatureFlagRepository featureFlagRepository) {
        this.createFlagService = createFlagService;
        this.updateFlagService = updateFlagService;
        this.deleteFlagService = deleteFlagService;
        this.resetUsersService = resetUsersService;
        this.validateOwnershipService = validateOwnershipService;
        this.featureFlagRepository = featureFlagRepository;
    }

    @PostMapping
    public ResponseEntity<String> createFlag(@Valid @RequestBody CreateFlagRequest createFlagRequest, @AuthenticationPrincipal UserDetails userDetails) {
        createFlagService.createFlag(createFlagRequest, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body("Flag created");
    }

    @GetMapping
    public ResponseEntity<?> getFlags(@AuthenticationPrincipal UserDetails userDetails) {
        List<FeatureFlag> featureFlags = featureFlagRepository.findFeatureFlagsByOwner(userDetails.getUsername());
        List<FlagDTO> result = new ArrayList<>();

        for (FeatureFlag featureFlag : featureFlags) {
            List<TargetingRuleDTO> targetingRules = new ArrayList<>();
            for (TargetingRule rule : featureFlag.getTargetingRules()) {
                if (!rule.isEnabled()) continue;
                TargetingRuleDTO dto = new TargetingRuleDTO();
                dto.setKey(rule.getRuleKey());
                dto.setValues(rule.getAllowedValues());
                dto.setEnabled(true);
                targetingRules.add(dto);
            }
            result.add(new FlagDTO(
                    featureFlag.getId(),
                    featureFlag.getFlagName(),
                    featureFlag.getDescription(),
                    featureFlag.isEnabled(),
                    featureFlag.getRolloutPercent(),
                    targetingRules,
                    featureFlag.getFlagConversions(),
                    featureFlag.getControlConversions()
            ));
        }

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateFlag(@RequestBody UpdateFlagRequest updateFlagRequest, @PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        validateOwnershipService.validateByUsername(userDetails.getUsername(), id);
        updateFlagService.updateFlag(updateFlagRequest, id);
        return ResponseEntity.ok("Flag updated");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFlag(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        validateOwnershipService.validateByUsername(userDetails.getUsername(), id);
        deleteFlagService.deleteFlag(id);
        return ResponseEntity.ok("Flag deleted");
    }

    @PostMapping("/{id}/reset")
    public ResponseEntity<String> resetUsers(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        validateOwnershipService.validateByUsername(userDetails.getUsername(), id);
        resetUsersService.resetUsers(id);
        return ResponseEntity.ok("Flag seed reset");
    }
}
