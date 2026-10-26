package com.mediamarktsaturn.azubi.market;

import com.mediamarktsaturn.azubi.core.market.MarketService;
import com.mediamarktsaturn.azubi.core.market.model.Market;
import com.mediamarktsaturn.azubi.core.product.model.SalesProduct;
import com.mediamarktsaturn.azubi.market.model.MarketApiResponse;
import com.mediamarktsaturn.azubi.market.model.ProductWithMarketStockDto;
import com.mediamarktsaturn.azubi.core.market.model.MarketStock;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/markets")
public class MarketController {

    // TODO Replace Map with actual Response Dto
    // TODO Think about if the logic here fits best to either Market, or Stock, or MarketStock

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    // GET /api/markets - Alle Märkte abrufen
    @GetMapping
    public ResponseEntity<List<MarketApiResponse>> getAllMarkets() {
        List<Market> markets = marketService.getAllMarkets();
        var response = markets.stream().map(MarketApiMapper::convertToDto).toList();
        return ResponseEntity.ok(response);
    }

    // GET /api/markets/{marketId} - Spezifischen Markt abrufen
    @GetMapping("/{marketId}")
    public ResponseEntity<MarketApiResponse> getMarketById(@PathVariable Integer marketId) {
        return marketService.getMarket(marketId)
                .map(market -> ResponseEntity.ok(MarketApiMapper.convertToDto(market)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /api/markets/{marketId}/products - Alle Produkte eines Marktes mit Bestand
    @GetMapping("/{marketId}/products")
    public ResponseEntity<List<ProductWithMarketStockDto>> getProductsByMarket(@PathVariable Integer marketId) {
        List<MarketStock> marketStocks = marketService.getStockByMarket(marketId);
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
        List<MarketStock> availableStocks = marketService.getAvailableStockByMarket(marketId);
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
        List<Market> markets = marketService.getAllMarkets();
        List<Map<String, Object>> overview = new ArrayList<>();

        for (Market market : markets) {
            List<MarketStock> availableStock = marketService.getAvailableStockByMarket(market.getMarketId());

            Map<String, Object> marketInfo = new HashMap<>();
            marketInfo.put("marketId", market.getMarketId());
            marketInfo.put("marketName", market.getMarketName());
            marketInfo.put("location", market.getLocation());
            marketInfo.put("postalCode", market.getPostalCode());
            marketInfo.put("availableProducts", availableStock.size());

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
        List<MarketStock> allStocks = marketService.getStockForProduct(productId);

        Optional<SalesProduct> productOpt = marketService.getProduct(productId);
        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SalesProduct product = productOpt.get();

        List<Map<String, Object>> marketAvailability = new ArrayList<>();
        int totalStock = 0;

        for (MarketStock stock : allStocks) {
            Optional<Market> marketOpt = marketService.getMarket(stock.getMarketId());
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
        Optional<MarketStock> marketStock = marketService.getSpecificStock(marketId, productId);
        return ResponseEntity.ok(marketStock.map(MarketStock::getStock).orElse(0));
    }

    // Helper Method
    private ProductWithMarketStockDto convertToProductWithStockDto(MarketStock marketStock) {
        Optional<SalesProduct> productOpt = marketService.getProduct(marketStock.getProductId());
        Optional<Market> marketOpt = marketService.getMarket(marketStock.getMarketId());

        if (productOpt.isPresent() && marketOpt.isPresent()) {
            return MarketApiMapper.convertToProductWithStockDto(marketStock, productOpt.get(), marketOpt.get());
        }

        return null;
    }
}
