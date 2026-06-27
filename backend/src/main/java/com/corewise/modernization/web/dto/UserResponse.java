package com.corewise.modernization.web.dto;

import com.corewise.modernization.domain.model.User;
import java.time.OffsetDateTime;

/** What we tell the frontend about a user. The password hash is never included. */
public record UserResponse(
    Long id,
    String email,
    String fullName,
    String role,
    String status,
    OffsetDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRole(),
            user.getStatus(),
            user.getCreatedAt());
    }
}
