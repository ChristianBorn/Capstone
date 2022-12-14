package de.ffmjava.capstone.backend.clients;

import de.ffmjava.capstone.backend.clients.model.Client;
import de.ffmjava.capstone.backend.horses.model.Horse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClientServiceTest {

    private final ClientRepository mockRepository = mock(ClientRepository.class);
    private final ClientService service = new ClientService(mockRepository);

    @Test
    void getAllClients() {
        //Given
        //When
        when(mockRepository.findAll()).thenReturn(List.of());
        //Then
        List<Client> actual = service.getAllClients();
        List<Client> expected = List.of();
        assertEquals(expected, actual);
    }

    @Test
    void addNewClient_AndExpectClient() {
        //Given
        Client newClient = new Client(null, "name", List.of());
        //When
        doReturn(newClient.withId(UUID.randomUUID().toString())).when(mockRepository).save(any());
        //Then
        Client actual = service.addNewClient(newClient);
        Client expected = newClient.withId(actual.id());
        assertEquals(expected, actual);
    }

    @Test
    void deleteClient_AndExpectSuccess() {
        //Given
        String idToDelete = "1";
        //When
        when(mockRepository.existsById(idToDelete)).thenReturn(true);
        doNothing().when(mockRepository).deleteById(idToDelete);
        //Then
        assertTrue(service.deleteClient(idToDelete));
        verify(mockRepository).existsById(idToDelete);
    }

    @Test
    void deleteClient_AndExpectException_404() {
        //Given
        String idToDelete = "1";
        //When
        when(mockRepository.existsById(idToDelete))
                .thenReturn(false);
        doNothing().when(mockRepository).deleteById(idToDelete);
        //Then
        try {
            service.deleteClient(idToDelete);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Kein Eintrag f??r die gegebene ID gefunden", e.getMessage());
            verify(mockRepository).existsById(idToDelete);
        }
    }

    @Test
    void updateClient_WithoutPreownedHorse_AndExpectSuccess_200() {
        //Given
        Client newClient = new Client("id", "name", List.of());
        //When
        when(mockRepository.existsById("id")).thenReturn(true);
        when(mockRepository.findByOwnsHorseContains(any())).thenReturn(null);
        //Then
        assertTrue(service.updateClient(newClient));
    }

    @Test
    void updateClient_WithPreownedHorse_AndExpectSuccess_200() {
        //Given
        Horse ownedHorse = new Horse("id", "name", "owner", List.of());
        Horse horseToAdd = new Horse("id2", "name2", "owner2", List.of());
        Client oldClient = new Client("id", "name", List.of(ownedHorse));
        Client newClient = new Client("id", "name", List.of(ownedHorse, horseToAdd));
        //When
        when(mockRepository.existsById("id")).thenReturn(true);
        when(mockRepository.findByOwnsHorseContains(ownedHorse)).thenReturn(oldClient);
        //Then
        assertTrue(service.updateClient(newClient));
    }

    @Test
    void updateClient_NoOwnership_AndExpectSuccess_200() {
        //Given
        Client newClient = new Client("id", "name", List.of());
        //When
        when(mockRepository.existsById("id")).thenReturn(true);
        //Then
        assertTrue(service.updateClient(newClient));
    }

    @Test
    void updateClient_AndExpectSuccess_201() {
        //Given
        Client newClient = new Client("id", "name", List.of());
        //When
        when(mockRepository.existsById("id")).thenReturn(false);
        when(mockRepository.findByOwnsHorseContains(any())).thenReturn(null);
        //Then
        assertFalse(service.updateClient(newClient));
    }

    @Test
    void updateClient_AndExpectException_alreadyOwned() {
        //Given
        Horse horseToAdd = new Horse("id", "name", "owner", List.of());
        Client newClient = new Client("id", "name", List.of(horseToAdd));
        Client foundClient = new Client("1", "name2", List.of(horseToAdd));
        //When
        when(mockRepository.existsById("id")).thenReturn(false);
        when(mockRepository.findByOwnsHorseContains(horseToAdd)).thenReturn(foundClient);
        //Then
        try {
            service.updateClient(newClient);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("One or more horses are already owned", e.getMessage());
        }
    }

    @Test
    void updateClient_AndExpectException_duplicate() {
        //Given
        Horse horseToAdd = new Horse("id", "name", "owner", List.of());
        Client newClient = new Client("id", "name", List.of(horseToAdd, horseToAdd));
        //When
        when(mockRepository.existsById("id")).thenReturn(false);
        when(mockRepository.findByOwnsHorseContains(horseToAdd)).thenReturn(null);
        //Then
        try {
            service.updateClient(newClient);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("A horse can only be owned by one person", e.getMessage());
        }
    }
}