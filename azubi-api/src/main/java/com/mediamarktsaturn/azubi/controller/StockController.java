package com.mediamarktsaturn.azubi.controller;

import com.mediamarktsaturn.azubi.dto.StockDto;
import com.mediamarktsaturn.azubi.entity.Stock;
import com.mediamarktsaturn.azubi.repository.StockJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/stock")  // ← Zurück zum ursprünglichen Mapping
public class StockController {

    @Autowired
    private StockJpaRepository stockRepository;

    // GET /api/stock - Alle Bestände abrufen
    @GetMapping
    public ResponseEntity<List<StockDto>> getAllStock() {
        List<Stock> stocks = stockRepository.findAll();
        List<StockDto> stockDtos = new ArrayList<>();

        for (Stock stock : stocks) {
            stockDtos.add(convertToDto(stock));
        }

        return ResponseEntity.ok(stockDtos);
    }

    // GET /api/stock/{productId} - Bestand für spezifisches Produkt
    @GetMapping("/{productId}")
    public ResponseEntity<StockDto> getStockByProductId(@PathVariable Integer productId) {
        Optional<Stock> stock = stockRepository.findByProductId(productId);

        if (stock.isPresent()) {
            StockDto dto = convertToDto(stock.get());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/stock/low-stock?threshold=10 - Produkte mit niedrigem Bestand
    @GetMapping("/low-stock")
    public ResponseEntity<List<StockDto>> getLowStock(@RequestParam(defaultValue = "10") Integer threshold) {
        List<Stock> lowStockItems = stockRepository.findByStockLessThan(threshold);
        List<StockDto> stockDtos = new ArrayList<>();

        for (Stock stock : lowStockItems) {
            stockDtos.add(convertToDto(stock));
        }

        return ResponseEntity.ok(stockDtos);
    }

    // GET /api/stock/available - Nur verfügbare Artikel (Bestand > 0)
    @GetMapping("/available")
    public ResponseEntity<List<StockDto>> getAvailableStock() {
        List<Stock> availableStock = stockRepository.findByStockGreaterThan(0);
        List<StockDto> stockDtos = new ArrayList<>();

        for (Stock stock : availableStock) {
            stockDtos.add(convertToDto(stock));
        }

        return ResponseEntity.ok(stockDtos);
    }

    // Helper method to convert Entity to DTO
    private StockDto convertToDto(Stock entity) {
        return new StockDto(
                entity.getProductId(),
                entity.getStock(),
                entity.getLastChange()
        );
    }

    @GetMapping("/debug")
    public ResponseEntity<String> debugStock() {
        List<Stock> allStocks = stockRepository.findAll();
        StringBuilder debug = new StringBuilder();
        debug.append("Total stocks found: ").append(allStocks.size()).append("\n");

        for (Stock stock : allStocks) {
            debug.append("ProductID: ").append(stock.getProductId())
                    .append(", Stock: ").append(stock.getStock())
                    .append(", LastChange: ").append(stock.getLastChange())
                    .append("\n");
        }

        return ResponseEntity.ok(debug.toString());
    }
}
