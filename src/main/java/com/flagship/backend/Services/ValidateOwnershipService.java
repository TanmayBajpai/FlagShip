package com.flagship.backend.Services;

import com.flagship.backend.Entities.FeatureFlag;
import com.flagship.backend.Entities.User;
import com.flagship.backend.Exceptions.ForbiddenException;
import com.flagship.backend.Exceptions.InvalidApiKeyException;
import com.flagship.backend.Exceptions.InvalidFlagIdException;
import com.flagship.backend.Respositories.FeatureFlagRepository;
import com.flagship.backend.Respositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ValidateOwnershipService {

    private final UserRepository userRepository;
    private final FeatureFlagRepository featureFlagRepository;

    public ValidateOwnershipService(UserRepository userRepository, FeatureFlagRepository featureFlagRepository) {
        this.userRepository = userRepository;
        this.featureFlagRepository = featureFlagRepository;
    }

    public void validate(String apiKey, UUID id) {
        Optional<FeatureFlag> optionalFlag = featureFlagRepository.findFeatureFlagById(id);
        Optional<User> optionalUser = userRepository.findUserByApiKey(apiKey);

        if (optionalFlag.isEmpty()) throw new InvalidFlagIdException();
        if (optionalUser.isEmpty()) throw new InvalidApiKeyException();
        if (!optionalFlag.get().getOwner().equals(optionalUser.get().getUsername())) throw new ForbiddenException();
    }

    public void validateByUsername(String username, UUID id) {
        Optional<FeatureFlag> optionalFeatureFlag = featureFlagRepository.findFeatureFlagById(id);
        if (optionalFeatureFlag.isEmpty()) throw new InvalidFlagIdException();
        if (!optionalFeatureFlag.get().getOwner().equals(username)) throw new ForbiddenException();
    }
}
