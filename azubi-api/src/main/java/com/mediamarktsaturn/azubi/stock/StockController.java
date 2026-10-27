package com.mediamarktsaturn.azubi.stock;

import com.mediamarktsaturn.azubi.core.stock.StockService;
import com.mediamarktsaturn.azubi.core.stock.model.Stock;
import com.mediamarktsaturn.azubi.generated.api.StockApi;
import com.mediamarktsaturn.azubi.generated.model.StockApiResponseDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class StockController implements StockApi {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    // GET /api/stock - Alle Bestände abrufen
    @Override
    public ResponseEntity<List<StockApiResponseDTO>> getAllStock() {

        List<Stock> stocks = stockService.getAllStock();

        var response = stocks.stream()
                .map(this::convertToDto)
                .toList();

        return ResponseEntity.ok(response);
    }

    // GET /api/stock/{productId}
    @Override
    public ResponseEntity<StockApiResponseDTO> getStockByProductId(
            Integer productId) {

        Optional<Stock> stock = stockService.getStockByProductId(productId);

        return stock
                .map(value -> ResponseEntity.ok(convertToDto(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /api/stock/low-stock?threshold=10
    @Override
    public ResponseEntity<List<StockApiResponseDTO>> getLowStock(
            Integer threshold) {

        List<Stock> stocks = stockService.getLowStock(threshold);

        var response = stocks.stream()
                .map(this::convertToDto)
                .toList();

        return ResponseEntity.ok(response);
    }

    // GET /api/stock/available
    @Override
    public ResponseEntity<List<StockApiResponseDTO>> getAvailableStock() {

        List<Stock> stocks = stockService.getAvailableStock();

        var response = stocks.stream()
                .map(this::convertToDto)
                .toList();

        return ResponseEntity.ok(response);
    }

    private StockApiResponseDTO convertToDto(Stock stock) {
        return new StockApiResponseDTO()
                .productId(stock.getProductId())
                .stock(stock.getStock())
                .lastChange(stock.getLastChange());
    }
}
