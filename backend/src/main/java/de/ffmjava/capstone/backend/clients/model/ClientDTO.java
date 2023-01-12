package de.ffmjava.capstone.backend.clients.model;

import de.ffmjava.capstone.backend.horses.model.Horse;
import lombok.With;

import javax.validation.constraints.NotBlank;
import java.util.List;
@With
public record ClientDTO(
        String id,
        @NotBlank(message = "Feld \"Name\" darf nicht leer sein")
        String name,
        List<Horse> ownsHorse
) {
        public static ClientDTO createDTOFromClient(Client client, List<Horse> ownedHorses) {
                return new ClientDTO(client.id(),
                        client.name(),
                        ownedHorses);
        }
}
