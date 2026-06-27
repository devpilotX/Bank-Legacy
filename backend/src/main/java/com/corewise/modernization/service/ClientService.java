package com.corewise.modernization.service;

import com.corewise.modernization.common.BadRequestException;
import com.corewise.modernization.common.ResourceNotFoundException;
import com.corewise.modernization.domain.model.Client;
import com.corewise.modernization.repository.ClientRepository;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creating and updating the banks we work for. */
@Service
public class ClientService {

    public static final String STATUS_ACTIVE = "active";
    private static final Set<String> ALLOWED_STATUSES = Set.of("prospect", "active", "archived");

    private final ClientRepository clients;

    public ClientService(ClientRepository clients) {
        this.clients = clients;
    }

    @Transactional
    public Client create(String name, String status, String notes) {
        String chosen = (status == null || status.isBlank()) ? STATUS_ACTIVE : status;
        requireValidStatus(chosen);
        return clients.save(new Client(name.trim(), chosen, trimToNull(notes)));
    }

    @Transactional(readOnly = true)
    public List<Client> list() {
        return clients.findAll(Sort.by("name"));
    }

    @Transactional(readOnly = true)
    public Client getById(Long id) {
        return clients.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("There is no client with id " + id + "."));
    }

    @Transactional
    public Client update(Long id, String name, String status, String notes) {
        Client client = getById(id);
        if (name != null && !name.isBlank()) {
            client.setName(name.trim());
        }
        if (status != null && !status.isBlank()) {
            requireValidStatus(status);
            client.setStatus(status);
        }
        client.setNotes(trimToNull(notes));
        return clients.save(client);
    }

    private void requireValidStatus(String status) {
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new BadRequestException("Client status must be prospect, active, or archived.");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
