package com.flagship.backend.Services;

import com.flagship.backend.Entities.FeatureFlag;
import com.flagship.backend.Entities.User;
import com.flagship.backend.Exceptions.InvalidApiKeyException;
import com.flagship.backend.Respositories.FeatureFlagRepository;
import com.flagship.backend.Respositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SuccessHandlerService {

    private final FeatureFlagRepository featureFlagRepository;
    private final UserRepository userRepository;
    private final EvaluateUserService evaluateUserService;

    public SuccessHandlerService(FeatureFlagRepository featureFlagRepository, UserRepository userRepository, EvaluateUserService evaluateUserService) {
        this.featureFlagRepository = featureFlagRepository;
        this.userRepository = userRepository;
        this.evaluateUserService = evaluateUserService;
    }

    @Transactional
    public void handleSuccess(String apiKey, String userId) {

        Optional<User> userOptional = userRepository.findUserByApiKey(apiKey);

        if (userOptional.isEmpty()) throw new InvalidApiKeyException();

        List<FeatureFlag> featureFlagList = featureFlagRepository.findFeatureFlagsByOwner(userOptional.get().getUsername());

        for (FeatureFlag featureFlag : featureFlagList) {
            if (evaluateUserService.evaluateBucketOnly(featureFlag, userId)) {
                featureFlag.setFlagConversions(featureFlag.getFlagConversions() + 1);
            } else {
                featureFlag.setControlConversions(featureFlag.getControlConversions() + 1);
            }
            featureFlagRepository.save(featureFlag);
        }
    }
}
