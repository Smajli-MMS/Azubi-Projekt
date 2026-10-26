package com.mediamarktsaturn.azubi.stock;

import com.mediamarktsaturn.azubi.core.stock.StockService;
import com.mediamarktsaturn.azubi.core.stock.model.Stock;
import com.mediamarktsaturn.azubi.stock.model.StockApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/stock")
public class StockController {

    private StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    // GET /api/stock - Alle Bestände abrufen
    @GetMapping
    public ResponseEntity<List<StockApiResponse>> getAllStock() {

        List<Stock> stocks = stockService.getAllStock();

        var response = stocks.stream()
                .map(this::convertToDto)
                .toList();

        return ResponseEntity.ok(response);
    }

    // GET /api/stock/{productId}
    @GetMapping("/{productId}")
    public ResponseEntity<StockApiResponse> getStockByProductId(
            @PathVariable Integer productId) {

        Optional<Stock> stock = stockService.getStockByProductId(productId);

        return stock
                .map(value -> ResponseEntity.ok(convertToDto(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /api/stock/low-stock?threshold=10
    @GetMapping("/low-stock")
    public ResponseEntity<List<StockApiResponse>> getLowStock(
            @RequestParam(defaultValue = "10") Integer threshold) {

        List<Stock> stocks = stockService.getLowStock(threshold);

        var response = stocks.stream()
                .map(this::convertToDto)
                .toList();

        return ResponseEntity.ok(response);
    }

    // GET /api/stock/available
    @GetMapping("/available")
    public ResponseEntity<List<StockApiResponse>> getAvailableStock() {

        List<Stock> stocks = stockService.getAvailableStock();

        var response = stocks.stream()
                .map(this::convertToDto)
                .toList();

        return ResponseEntity.ok(response);
    }

    private StockApiResponse convertToDto(Stock stock) {
        return new StockApiResponse(
                stock.getProductId(),
                stock.getStock(),
                stock.getLastChange()
        );
    }
}