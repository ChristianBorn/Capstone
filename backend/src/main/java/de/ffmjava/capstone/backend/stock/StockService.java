package de.ffmjava.capstone.backend.stock;

import de.ffmjava.capstone.backend.stock.model.StockItem;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository repository;

    public List<StockItem> getAllStockItems() {
        return repository.findAll();
    }

    public boolean deleteStockItem(String id) throws ResponseStatusException {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Kein Eintrag für die gegebene ID gefunden");
        }
        repository.deleteById(id);
        return true;

    }

    public ResponseEntity<Object> addNewStockItem(StockItem newStockItem) {
        if (repository.existsByName(newStockItem.name())) {
            return new ResponseEntity<>("Der angegebene Name ist bereits vergeben", HttpStatus.CONFLICT);
        }
        StockItem newStockItemWithId = newStockItem.withId(UUID.randomUUID().toString());
        return new ResponseEntity<>(repository.save(newStockItemWithId), HttpStatus.CREATED);
    }

    public ResponseEntity<Object> updateStockItem(StockItem updatedStockItem) {
        boolean stockItemExists = repository.existsById(updatedStockItem.id());
        repository.save(updatedStockItem);
        if (stockItemExists) {
            return new ResponseEntity<>(updatedStockItem, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(updatedStockItem, HttpStatus.CREATED);
        }
    }

    public StockItem getStockItemById(String id) {
        return repository.getById(id);
    }
}
