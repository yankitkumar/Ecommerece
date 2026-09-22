package com.checkoutline.inventory.controller;

import com.checkoutline.inventory.model.Inventory;
import com.checkoutline.inventory.repository.InventoryRepository;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryRepository inventoryRepository;

    public InventoryController(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @GetMapping("/{productId}")
    public Inventory getStock(@PathVariable String productId) {
        return inventoryRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No stock record for " + productId));
    }

    /** Seeds or tops up stock for a product — an admin/back-office operation, not part of checkout. */
    @PutMapping("/{productId}")
    public Inventory addStock(@PathVariable String productId, @RequestBody StockRequest request) {
        Inventory inventory = inventoryRepository.findById(productId)
                .orElseGet(() -> new Inventory(productId, 0));
        inventory.addStock(request.availableQty());
        return inventoryRepository.save(inventory);
    }

    public record StockRequest(@NotBlank String productId, @Min(0) int availableQty) {}
}
