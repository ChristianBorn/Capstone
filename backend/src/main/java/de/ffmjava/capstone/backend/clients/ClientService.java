package de.ffmjava.capstone.backend.clients;

import de.ffmjava.capstone.backend.clients.model.Client;
import de.ffmjava.capstone.backend.clients.model.ClientDTO;
import de.ffmjava.capstone.backend.horses.HorseRepository;
import de.ffmjava.capstone.backend.horses.model.Horse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final HorseRepository horseRepository;

    public List<ClientDTO> getAllClients() {
        List<Client> retrievedClients = clientRepository.findAll();
        List<ClientDTO> clientsToReturn = new ArrayList<>();
        for (Client client : retrievedClients) {
            List<Horse> ownedHorses = new ArrayList<>();
            horseRepository.findAllById(client.ownsHorse()).forEach(ownedHorses::add);
            clientsToReturn.add(ClientDTO.createDTOFromClient(client, ownedHorses));
        }
        return clientsToReturn;
    }

    public Client addNewClient(ClientDTO newClient) {
        return clientRepository.save(Client.createClientFromDTO(newClient).withId(UUID.randomUUID().toString()));
    }

    public boolean deleteClient(String id) {
        if (!clientRepository.existsById(id)) {
            throw new IllegalArgumentException("Kein Eintrag für die gegebene ID gefunden");
        }
        clientRepository.deleteById(id);
        return true;
    }

    public boolean updateClient(ClientDTO updatedClient) throws IllegalArgumentException {
        boolean clientExists = clientRepository.existsById(updatedClient.id());
        if (!updatedClient.ownsHorse().isEmpty()) {
            List<String> assignedHorses = updatedClient.ownsHorse()
                    .stream().map(Horse::id)
                    .distinct().toList();
            if (updatedClient.ownsHorse().size() != assignedHorses.size()) {
                throw new IllegalArgumentException("A horse can only be owned by one person");
            }
            for (String horseOfUpdatedClientId : assignedHorses) {
                Client foundClient = clientRepository.findByOwnsHorseContains(horseOfUpdatedClientId);
                if (foundClient != null && !foundClient.id().equals(updatedClient.id())) {
                    throw new IllegalArgumentException("One or more horses are already owned");
                }
                Optional<Horse> retrievedHorse = horseRepository.findById(horseOfUpdatedClientId);
                if (retrievedHorse.isPresent()) {
                    Horse horseToUpdate = retrievedHorse.get();
                    horseRepository.save(horseToUpdate.withOwner(updatedClient.id()));
                }
                else {
                    throw new IllegalArgumentException("Horse with <ID> does not exist"
                            .replace("<ID>", horseOfUpdatedClientId));
                }
            }        }
        if (!clientExists) {
            clientRepository.save(Client.createClientFromDTO(updatedClient).withId(UUID.randomUUID().toString()));
        } else {
            clientRepository.save(Client.createClientFromDTO(updatedClient));
        }
        return clientExists;
    }
}
