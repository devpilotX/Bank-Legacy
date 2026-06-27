package com.corewise.modernization.web;

import com.corewise.modernization.domain.model.Client;
import com.corewise.modernization.service.ClientService;
import com.corewise.modernization.web.dto.ClientRequest;
import com.corewise.modernization.web.dto.ClientResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * The banks we work for. Anyone signed in can look; only admins can add or change
 * a client, since clients are an admin responsibility.
 */
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clients;

    public ClientController(ClientService clients) {
        this.clients = clients;
    }

    @GetMapping
    public List<ClientResponse> list() {
        return clients.list().stream().map(ClientResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ClientResponse get(@PathVariable Long id) {
        return ClientResponse.from(clients.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ClientResponse create(@Valid @RequestBody ClientRequest request) {
        Client created = clients.create(request.name(), request.status(), request.notes());
        return ClientResponse.from(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ClientResponse update(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        Client updated = clients.update(id, request.name(), request.status(), request.notes());
        return ClientResponse.from(updated);
    }
}
