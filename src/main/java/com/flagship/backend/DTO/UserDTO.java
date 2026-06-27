package com.flagship.backend.DTO;

import com.flagship.backend.Entities.User;

public record UserDTO(String username, Long id, String apiKey) {
    public static UserDTO from(User user) {
        return new UserDTO(user.getUsername(), user.getId(), user.getApiKey());
    }
}
