package de.ffmjava.capstone.backend.horses.model;

import lombok.With;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotBlank;
import java.util.List;

@With
public record Horse(
        @Id
        String id,
        @NotBlank(message = "Feld \"Name\" darf nicht leer sein")
        String name,
        String owner,
        List<Consumption> consumptionList
) {
    public static Horse createHorseFromDTO(HorseDTO horseTransferObject) {
        String ownerId;
        if (horseTransferObject.owner() == null) {
            ownerId = "";
        }
        else {
            ownerId = horseTransferObject.owner().id();
        }
        return new Horse(horseTransferObject.id(),
                horseTransferObject.name(),
                ownerId,
                horseTransferObject.consumptionList());
    }
}
