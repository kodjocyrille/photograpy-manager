package com.giovanni.photograpy_manager.service;

import com.giovanni.photograpy_manager.domain.client.Client;
import com.giovanni.photograpy_manager.domain.client.EventType;
import com.giovanni.photograpy_manager.dto.ClientForm;
import com.giovanni.photograpy_manager.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    public List<Client> findAll() {
        return clientRepository.findByArchivedFalseOrderByCreatedAtDesc();
    }

    public List<Client> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        return clientRepository.search(query.trim());
    }

    public List<Client> findByServiceType(EventType type) {
        return clientRepository.findByServiceTypeAndArchivedFalse(type);
    }

    public Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable : " + id));
    }

    public boolean existsByEmail(String email) {
        return clientRepository.existsByEmail(email);
    }

    @Transactional
    public Client create(ClientForm form) {
        Client client = Client.builder()
                .clientCode(generateClientCode())
                .fullName(form.getFullName())
                .phone(form.getPhone())
                .email(form.getEmail())
                .serviceType(form.getServiceType())
                .eventDate(form.getEventDate())
                .build();
        return clientRepository.save(client);
    }

    @Transactional
    public Client update(Long id, ClientForm form) {
        Client client = findById(id);
        client.setFullName(form.getFullName());
        client.setPhone(form.getPhone());
        client.setEmail(form.getEmail());
        client.setServiceType(form.getServiceType());
        client.setEventDate(form.getEventDate());
        return clientRepository.save(client);
    }

    @Transactional
    public void archive(Long id) {
        Client client = findById(id);
        client.setArchived(true);
        clientRepository.save(client);
    }

    public long count() {
        return clientRepository.countByArchivedFalse();
    }

    public String exportCsv() {
        List<Client> clients = findAll();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("Code,Nom,Téléphone,Email,Prestation,Date événement");
        for (Client c : clients) {
            pw.printf("%s,%s,%s,%s,%s,%s%n",
                    c.getClientCode(), c.getFullName(), c.getPhone(),
                    c.getEmail(), c.getServiceType().getLabel(),
                    c.getEventDate());
        }
        return sw.toString();
    }

    private String generateClientCode() {
        long count = clientRepository.count() + 1;
        return String.format("CLI-%d-%04d", Year.now().getValue(), count);
    }
}
