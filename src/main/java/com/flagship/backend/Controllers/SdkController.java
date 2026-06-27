package com.flagship.backend.Controllers;

import com.flagship.backend.DTO.SdkFlagDTO;
import com.flagship.backend.Entities.FeatureFlag;
import com.flagship.backend.Entities.TargetingRule;
import com.flagship.backend.Entities.User;
import com.flagship.backend.Respositories.FeatureFlagRepository;
import com.flagship.backend.Respositories.UserRepository;
import com.flagship.backend.Services.EvaluateUserService;
import com.flagship.backend.Services.SuccessHandlerService;
import com.flagship.backend.Services.ValidateOwnershipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class SdkController {

    private final ValidateOwnershipService validateOwnershipService;
    private final EvaluateUserService evaluateUserService;
    private final SuccessHandlerService successHandlerService;
    private final UserRepository userRepository;
    private final FeatureFlagRepository featureFlagRepository;

    public SdkController(ValidateOwnershipService validateOwnershipService, EvaluateUserService evaluateUserService, SuccessHandlerService successHandlerService, UserRepository userRepository, FeatureFlagRepository featureFlagRepository) {
        this.validateOwnershipService = validateOwnershipService;
        this.evaluateUserService = evaluateUserService;
        this.successHandlerService = successHandlerService;
        this.userRepository = userRepository;
        this.featureFlagRepository = featureFlagRepository;
    }

    @GetMapping("/config")
    public ResponseEntity<?> getConfig(@RequestHeader("X-API-Key") String apiKey) {
        Optional<User> optionalUser = userRepository.findUserByApiKey(apiKey);

        if (optionalUser.isEmpty()) return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid API key");

        List<FeatureFlag> featureFlags = featureFlagRepository.findFeatureFlagsByOwner(optionalUser.get().getUsername());
        List<SdkFlagDTO> result = new ArrayList<>();

        for (FeatureFlag featureFlag : featureFlags) {
            Map<String, List<String>> targetingRules = new HashMap<>();
            for (TargetingRule rule : featureFlag.getTargetingRules()) {
                if (!rule.isEnabled()) continue;
                targetingRules.computeIfAbsent(rule.getRuleKey(), k -> new ArrayList<>())
                              .addAll(rule.getAllowedValues());
            }
            result.add(new SdkFlagDTO(
                    featureFlag.getId(),
                    featureFlag.getFlagName(),
                    featureFlag.isEnabled(),
                    featureFlag.getRolloutPercent(),
                    targetingRules,
                    featureFlag.getSeed()
            ));
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/evaluate/{id}")
    public ResponseEntity<?> evaluate(@RequestHeader("X-API-Key") String apiKey, @PathVariable String id, @RequestParam String userId, @RequestParam Map<String, String> allParams) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid flag ID format");
        }
        validateOwnershipService.validate(apiKey, uuid);

        Map<String, String> userAttributes = new HashMap<>(allParams);
        userAttributes.remove("userId");

        return ResponseEntity.ok(evaluateUserService.evaluate(uuid, userId, userAttributes));
    }

    @PostMapping("/success")
    public ResponseEntity<?> success(@RequestHeader("X-API-Key") String apiKey, @RequestParam String userId) {
        successHandlerService.handleSuccess(apiKey, userId);
        return ResponseEntity.ok("Success handled");
    }
}
