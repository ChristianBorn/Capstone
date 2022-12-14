package de.ffmjava.capstone.backend.clients;

import de.ffmjava.capstone.backend.clients.model.Client;
import de.ffmjava.capstone.backend.horses.model.Horse;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends MongoRepository<Client, String> {
    boolean existsByOwnsHorseContains(Horse horseOfUpdatedClient);
}
