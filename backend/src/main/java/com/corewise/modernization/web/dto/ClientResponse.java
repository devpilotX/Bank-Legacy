package com.corewise.modernization.web.dto;

import com.corewise.modernization.domain.model.Client;
import java.time.OffsetDateTime;

/** What we tell the frontend about a client. */
public record ClientResponse(
    Long id,
    String name,
    String status,
    String notes,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(client.getId(), client.getName(), client.getStatus(),
            client.getNotes(), client.getCreatedAt(), client.getUpdatedAt());
    }
}
