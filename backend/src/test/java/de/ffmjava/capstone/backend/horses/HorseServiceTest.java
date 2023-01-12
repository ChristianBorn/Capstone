package de.ffmjava.capstone.backend.horses;

import de.ffmjava.capstone.backend.clients.ClientRepository;
import de.ffmjava.capstone.backend.clients.model.Client;
import de.ffmjava.capstone.backend.horses.model.Consumption;
import de.ffmjava.capstone.backend.horses.model.Horse;
import de.ffmjava.capstone.backend.horses.model.HorseDTO;
import de.ffmjava.capstone.backend.stock.StockRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HorseServiceTest {

    private final HorseRepository mockHorseRepository = mock(HorseRepository.class);
    private final StockRepository mockStockRepository = mock(StockRepository.class);
    private final ClientRepository mockClientRepository = mock(ClientRepository.class);
    private final HorseService service = new HorseService(mockHorseRepository, mockStockRepository, mockClientRepository);

    @Test
    void getAllHorses_returnEmptyList() {
        //Given
        //When
        when(mockHorseRepository.findAll()).thenReturn(List.of());
        List<HorseDTO> expected = List.of();
        List<HorseDTO> actual = service.getAllHorses();
        //Then
        assertEquals(expected, actual);
    }
    @Test
    void getAllHorses_withOwnerNotPresent() {
        //Given
        Horse retrievedHorse = new Horse("id", "name", "", List.of());
        HorseDTO returnedHorse = new HorseDTO("id", "name", null, List.of());
        //When
        when(mockHorseRepository.findAll()).thenReturn(List.of(retrievedHorse));
        when(mockClientRepository.findById("")).thenReturn(Optional.empty());
        List<HorseDTO> expected = List.of(returnedHorse);
        List<HorseDTO> actual = service.getAllHorses();
        //Then
        assertEquals(expected, actual);
    }
    @Test
    void getAllHorses_withOwnerPresent() {
        //Given
        Horse retrievedHorse = new Horse("id", "name", "id", List.of());
        Client retrievedClient = new Client("id", "name", List.of("id"));
        HorseDTO returnedHorse = new HorseDTO("id", "name", retrievedClient, List.of());
        //When
        when(mockHorseRepository.findAll()).thenReturn(List.of(retrievedHorse));
        when(mockClientRepository.findById("id")).thenReturn(Optional.of(retrievedClient));
        List<HorseDTO> expected = List.of(returnedHorse);
        List<HorseDTO> actual = service.getAllHorses();
        //Then
        assertEquals(expected, actual);
    }

    @Test
    void addNewHorse_AndExpectHorse_200() {
        //Given
        Horse newHorse = new Horse(null, "name", "owner", null);
        //When
        when(mockHorseRepository.save(any())).thenReturn(newHorse.withId("1"));
        //Then
        Horse actual = service.addNewHorse(newHorse);
        Horse expected = newHorse.withId("1");
        assertEquals(expected, actual);
    }

    @Test
    void deleteHorse_AndExpectSuccess() {
        //Given
        String idToDelete = "1";
        //When
        when(mockHorseRepository.existsById(idToDelete)).thenReturn(true);
        doNothing().when(mockHorseRepository).deleteById(idToDelete);
        //Then
        assertTrue(service.deleteHorse(idToDelete));
        verify(mockHorseRepository).existsById(idToDelete);
    }

    @Test
    void deleteHorse_AndExpectException_404() {
        //Given
        String idToDelete = "1";
        //When
        when(mockHorseRepository.existsById(idToDelete))
                .thenReturn(false);
        doNothing().when(mockHorseRepository).deleteById(idToDelete);
        //Then
        try {
            service.deleteHorse(idToDelete);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Kein Eintrag für die gegebene ID gefunden", e.getMessage());
            verify(mockHorseRepository).existsById(idToDelete);
        }
    }

    @Test
    void UpdateHorse_AndExpectSuccess_201() {
        //Given
        Client owner = new Client("1", "name", List.of("1"));
        HorseDTO newHorse = new HorseDTO("1", "name", owner,
                List.of(new Consumption("1", "name", new BigDecimal("0"))));
        Horse savedHorse = Horse.createHorseFromDTO(newHorse);
        //When
        when(mockStockRepository.existsById("1")).thenReturn(true);
        when(mockHorseRepository.existsById("id")).thenReturn(false);
        when(mockHorseRepository.save(any())).thenReturn(savedHorse.withId(UUID.randomUUID().toString()));
        //Then
        HorseDTO actual = service.updateHorse(newHorse);
        HorseDTO expected = newHorse.withId(actual.id());
        assertEquals(expected, actual);
    }

    @Test
    void UpdateHorse_AndExpectSuccess_200() {
        //Given
        Client owner = new Client("1", "name", List.of("1"));
        HorseDTO newHorse = new HorseDTO("1", "name", owner,
                List.of(new Consumption("1", "name", new BigDecimal("0"))));
        Horse horseToSave = new Horse("1", "name", "1", List.of(new Consumption("1", "name", new BigDecimal("0"))));
        //When
        when(mockStockRepository.existsById("1")).thenReturn(true);
        when(mockHorseRepository.existsById("1")).thenReturn(true);
        //Then
        HorseDTO actual = service.updateHorse(newHorse);
        HorseDTO expected = newHorse;
        assertEquals(expected, actual);
        verify(mockHorseRepository).save(horseToSave);

    }

    @Test
    void UpdateHorse_AndExpectException_400() {
        //Given
        Client owner = new Client("1", "name", List.of("1"));
        HorseDTO newHorse = new HorseDTO("id", "name", owner,
                List.of(new Consumption("1", "name", new BigDecimal("0")),
                        new Consumption("1", "name", new BigDecimal("0"))));
        //When
        //Then
        try {
            service.updateHorse(newHorse);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("IDs of consumptionItems must be unique for every horse", e.getMessage());
        }
    }

    @Test
    void UpdateHorse_NoMatchingStockItem_AndExpectException_400() {
        //Given
        Client owner = new Client("1", "name", List.of("1"));
        HorseDTO newHorse = new HorseDTO("id", "name", owner,
                List.of(new Consumption("1", "name", new BigDecimal("0")),
                        new Consumption("2", "name", new BigDecimal("0"))));
        //When
        when(mockStockRepository.existsById("1")).thenReturn(false);
        //Then
        try {
            service.updateHorse(newHorse);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Consumption item not in stock", e.getMessage());
        }
    }
}
