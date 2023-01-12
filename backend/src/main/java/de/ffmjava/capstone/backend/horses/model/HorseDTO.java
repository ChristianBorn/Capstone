package de.ffmjava.capstone.backend.horses.model;

import de.ffmjava.capstone.backend.clients.model.Client;
import lombok.With;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
@With
public record HorseDTO(
        @NotNull
        String id,
        @NotBlank(message = "Feld \"Name\" darf nicht leer sein")
        String name,
        Client owner,
        List<Consumption> consumptionList
) {
}
