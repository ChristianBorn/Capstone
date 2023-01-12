package de.ffmjava.capstone.backend.horses;

import de.ffmjava.capstone.backend.horses.model.HorseDTO;
import de.ffmjava.capstone.backend.model.FormError;
import de.ffmjava.capstone.backend.user.CustomApiErrorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/horses/")
@RequiredArgsConstructor
public class HorseController {

    private final HorseService service;

    @GetMapping
    public List<HorseDTO> getAllHorses() {
        return service.getAllHorses();
    }

    @PutMapping
    public ResponseEntity<Object> updateHorse(@Valid @RequestBody HorseDTO updatedHorse, Errors errors) {
        ResponseEntity<Object> errorMessage = CustomApiErrorHandler.handlePossibleErrors(errors);
        if (errorMessage != null) return errorMessage;
        if (!updatedHorse.consumptionList()
                .stream()
                .filter(consumption -> consumption.dailyConsumption().compareTo(BigDecimal.ZERO) < 1).toList()
                .isEmpty()) {
            return new ResponseEntity<>(new FormError("Der Wert muss größer als 0 sein", "dailyConsumption"),
                    HttpStatus.BAD_REQUEST);
        }
       try {
            HorseDTO horseToReturn = service.updateHorse(updatedHorse);
            if (horseToReturn.id().equals(updatedHorse.id())) {
                return new ResponseEntity<>(horseToReturn, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(horseToReturn, HttpStatus.CREATED);
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHorse(@PathVariable String id) {
        try {
            service.deleteHorse(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
