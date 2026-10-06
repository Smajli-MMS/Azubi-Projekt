package com.mediamarktsaturn.azubi.controller;

import com.mediamarktsaturn.azubi.dto.MarketDto;
import com.mediamarktsaturn.azubi.dto.ProductWithMarketStockDto;
import com.mediamarktsaturn.azubi.entity.Market;
import com.mediamarktsaturn.azubi.entity.MarketStock;
import com.mediamarktsaturn.azubi.product.entity.SalesProductEntity;
import com.mediamarktsaturn.azubi.repository.MarketJpaRepository;
import com.mediamarktsaturn.azubi.repository.MarketStockJpaRepository;
import com.mediamarktsaturn.azubi.product.repository.SalesProductJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/markets")
public class MarketController {

    @Autowired
    private MarketJpaRepository marketRepository;

    @Autowired
    private MarketStockJpaRepository marketStockRepository;

    @Autowired
    private SalesProductJpaRepository salesProductRepository;

    // GET /api/markets - Alle Märkte abrufen
    @GetMapping
    public ResponseEntity<List<MarketDto>> getAllMarkets() {
        List<Market> markets = marketRepository.findAll();
        List<MarketDto> marketDtos = new ArrayList<>();

        for (Market market : markets) {
            marketDtos.add(convertToDto(market));
        }

        return ResponseEntity.ok(marketDtos);
    }

    // GET /api/markets/{marketId} - Spezifischen Markt abrufen
    @GetMapping("/{marketId}")
    public ResponseEntity<MarketDto> getMarketById(@PathVariable Integer marketId) {
        Optional<Market> market = marketRepository.findById(marketId);

        if (market.isPresent()) {
            MarketDto dto = convertToDto(market.get());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/markets/{marketId}/products - Alle Produkte eines Marktes mit Bestand
    @GetMapping("/{marketId}/products")
    public ResponseEntity<List<ProductWithMarketStockDto>> getProductsByMarket(@PathVariable Integer marketId) {
        List<MarketStock> marketStocks = marketStockRepository.findByMarketId(marketId);
        List<ProductWithMarketStockDto> products = new ArrayList<>();

        for (MarketStock stock : marketStocks) {
            ProductWithMarketStockDto dto = convertToProductWithStockDto(stock);
            if (dto != null) {
                products.add(dto);
            }
        }

        return ResponseEntity.ok(products);
    }

    // GET /api/markets/{marketId}/products/available - Nur verfügbare Produkte eines Marktes
    @GetMapping("/{marketId}/products/available")
    public ResponseEntity<List<ProductWithMarketStockDto>> getAvailableProductsByMarket(@PathVariable Integer marketId) {
        List<MarketStock> availableStocks = marketStockRepository.findAvailableStockByMarket(marketId);
        List<ProductWithMarketStockDto> products = new ArrayList<>();

        for (MarketStock stock : availableStocks) {
            ProductWithMarketStockDto dto = convertToProductWithStockDto(stock);
            if (dto != null) {
                products.add(dto);
            }
        }

        return ResponseEntity.ok(products);
    }

    // GET /api/markets/overview - Marktübersicht mit Produktanzahl
    @GetMapping("/overview")
    public ResponseEntity<List<Map<String, Object>>> getMarketsOverview() {
        List<Market> markets = marketRepository.findAll();
        List<Map<String, Object>> overview = new ArrayList<>();

        for (Market market : markets) {
            List<MarketStock> availableStock = marketStockRepository.findAvailableStockByMarket(market.getMarketId());

            Map<String, Object> marketInfo = new HashMap<>();
            marketInfo.put("marketId", market.getMarketId());
            marketInfo.put("marketName", market.getMarketName());
            marketInfo.put("location", market.getLocation());
            marketInfo.put("postalCode", market.getPostalCode());
            marketInfo.put("availableProducts", availableStock.size());

            // Gesamtbestand berechnen
            int totalStock = 0;
            for (MarketStock stock : availableStock) {
                totalStock += stock.getStock();
            }
            marketInfo.put("totalStock", totalStock);

            overview.add(marketInfo);
        }

        return ResponseEntity.ok(overview);
    }

    // GET /api/markets/products/{productId} - Bestand eines Produkts in allen Märkten
    @GetMapping("/products/{productId}")
    public ResponseEntity<Map<String, Object>> getProductStockInAllMarkets(@PathVariable Integer productId) {
        List<MarketStock> allStocks = marketStockRepository.findAllStockForProduct(productId);

        // Produktinformationen laden
        Optional<SalesProductEntity> productOpt = salesProductRepository.findById(productId);
        if (!productOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        SalesProductEntity product = productOpt.get();

        List<Map<String, Object>> marketAvailability = new ArrayList<>();
        int totalStock = 0;

        for (MarketStock stock : allStocks) {
            Optional<Market> marketOpt = marketRepository.findById(stock.getMarketId());
            if (marketOpt.isPresent()) {
                Market market = marketOpt.get();

                Map<String, Object> marketInfo = new HashMap<>();
                marketInfo.put("marketId", market.getMarketId());
                marketInfo.put("marketName", market.getMarketName());
                marketInfo.put("location", market.getLocation());
                marketInfo.put("stock", stock.getStock());
                marketInfo.put("available", stock.getStock() > 0);

                marketAvailability.add(marketInfo);
                totalStock += stock.getStock();
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("productId", productId);
        result.put("productName", product.getName());
        result.put("price", product.getPrice());
        result.put("totalStock", totalStock);
        result.put("availableInMarkets", marketAvailability);

        return ResponseEntity.ok(result);
    }

    // GET /api/markets/{marketId}/stock/{productId} - Spezifischer Bestand
    @GetMapping("/{marketId}/stock/{productId}")
    public ResponseEntity<Integer> getSpecificStock(@PathVariable Integer marketId, @PathVariable Integer productId) {
        Optional<MarketStock> marketStock = marketStockRepository.findByMarketIdAndProductId(marketId, productId);

        if (marketStock.isPresent()) {
            return ResponseEntity.ok(marketStock.get().getStock());
        } else {
            return ResponseEntity.ok(0); // Kein Bestand = 0
        }
    }

    // Helper Methods
    private MarketDto convertToDto(Market market) {
        return new MarketDto(
                market.getMarketId(),
                market.getMarketName(),
                market.getLocation(),
                market.getPostalCode()
        );
    }

    private ProductWithMarketStockDto convertToProductWithStockDto(MarketStock marketStock) {
        Optional<SalesProductEntity> productOpt = salesProductRepository.findById(marketStock.getProductId());
        Optional<Market> marketOpt = marketRepository.findById(marketStock.getMarketId());

        if (productOpt.isPresent() && marketOpt.isPresent()) {
            SalesProductEntity product = productOpt.get();
            Market market = marketOpt.get();

            return new ProductWithMarketStockDto(
                    product.getProductId(),
                    product.getName(),
                    product.getPrice(),
                    product.getProductGroupId(),
                    marketStock.getStock(),
                    market.getMarketName(),
                    market.getMarketId()
            );
        }

        return null;
    }
}
