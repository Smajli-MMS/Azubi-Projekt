package com.mediamarktsaturn.azubi.market;

import com.mediamarktsaturn.azubi.core.market.MarketService;
import com.mediamarktsaturn.azubi.core.market.model.Market;
import com.mediamarktsaturn.azubi.core.market.model.MarketStock;
import com.mediamarktsaturn.azubi.core.product.model.SalesProduct;
import com.mediamarktsaturn.azubi.generated.api.MarketsApi;
import com.mediamarktsaturn.azubi.generated.model.MarketApiResponseDTO;
import com.mediamarktsaturn.azubi.generated.model.MarketAvailabilityDTO;
import com.mediamarktsaturn.azubi.generated.model.MarketOverviewDTO;
import com.mediamarktsaturn.azubi.generated.model.ProductStockInAllMarketsDTO;
import com.mediamarktsaturn.azubi.generated.model.ProductWithMarketStockDTO;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class MarketController implements MarketsApi {

    private final MarketService marketService;

    // TODO implementieren der spec im frontend - Copy spec yaml in das frontend project. Add npm library for autogenerating openapi and autogenerate clients / interfaces- Nutzen der clietns und interfaces.

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @Override
    public ResponseEntity<List<MarketApiResponseDTO>> getAllMarkets() {
        List<Market> markets = marketService.getAllMarkets();
        var response = markets.stream().map(MarketApiMapper::convertToDto).toList();
        return ResponseEntity.ok(response);
    }

    // GET /api/markets/{marketId} - Spezifischen Markt abrufen
    @Override
    public ResponseEntity<MarketApiResponseDTO> getMarketById(Integer marketId) {
        return marketService.getMarket(marketId)
                .map(market -> ResponseEntity.ok(MarketApiMapper.convertToDto(market)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /api/markets/{marketId}/products - Alle Produkte eines Marktes mit Bestand
    @Override
    public ResponseEntity<List<ProductWithMarketStockDTO>> getProductsByMarket(Integer marketId) {
        List<MarketStock> marketStocks = marketService.getStockByMarket(marketId);
        List<ProductWithMarketStockDTO> products = new ArrayList<>();

        for (MarketStock stock : marketStocks) {
            ProductWithMarketStockDTO dto = convertToProductWithStockDto(stock);
            if (dto != null) {
                products.add(dto);
            }
        }

        return ResponseEntity.ok(products);
    }

    // GET /api/markets/{marketId}/products/available - Nur verfügbare Produkte eines Marktes
    @Override
    public ResponseEntity<List<ProductWithMarketStockDTO>> getAvailableProductsByMarket(Integer marketId) {
        List<MarketStock> availableStocks = marketService.getAvailableStockByMarket(marketId);
        List<ProductWithMarketStockDTO> products = new ArrayList<>();

        for (MarketStock stock : availableStocks) {
            ProductWithMarketStockDTO dto = convertToProductWithStockDto(stock);
            if (dto != null) {
                products.add(dto);
            }
        }

        return ResponseEntity.ok(products);
    }

    // GET /api/markets/overview - Marktübersicht mit Produktanzahl
    @Override
    public ResponseEntity<List<MarketOverviewDTO>> getMarketsOverview() {
        List<Market> markets = marketService.getAllMarkets();
        List<MarketOverviewDTO> overview = new ArrayList<>();

        for (Market market : markets) {
            List<MarketStock> availableStock = marketService.getAvailableStockByMarket(market.getMarketId());

            MarketOverviewDTO marketInfo = new MarketOverviewDTO();
            marketInfo.setMarketId(market.getMarketId());
            marketInfo.setMarketName(market.getMarketName());
            marketInfo.setLocation(market.getLocation());
            marketInfo.setPostalCode(market.getPostalCode());
            marketInfo.setAvailableProducts(availableStock.size());

            int totalStock = 0;
            for (MarketStock stock : availableStock) {
                totalStock += stock.getStock();
            }
            marketInfo.setTotalStock(totalStock);

            overview.add(marketInfo);
        }

        return ResponseEntity.ok(overview);
    }

    // GET /api/markets/products/{productId} - Bestand eines Produkts in allen Märkten
    @Override
    public ResponseEntity<ProductStockInAllMarketsDTO> getProductStockInAllMarkets(Integer productId) {
        List<MarketStock> allStocks = marketService.getStockForProduct(productId);

        Optional<SalesProduct> productOpt = marketService.getProduct(productId);
        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SalesProduct product = productOpt.get();

        List<MarketAvailabilityDTO> marketAvailability = new ArrayList<>();
        int totalStock = 0;

        for (MarketStock stock : allStocks) {
            Optional<Market> marketOpt = marketService.getMarket(stock.getMarketId());
            if (marketOpt.isPresent()) {
                Market market = marketOpt.get();

                MarketAvailabilityDTO marketInfo = new MarketAvailabilityDTO();
                marketInfo.setMarketId(market.getMarketId());
                marketInfo.setMarketName(market.getMarketName());
                marketInfo.setLocation(market.getLocation());
                marketInfo.setStock(stock.getStock());
                marketInfo.setAvailable(stock.getStock() > 0);

                marketAvailability.add(marketInfo);
                totalStock += stock.getStock();
            }
        }

        ProductStockInAllMarketsDTO result = new ProductStockInAllMarketsDTO()
                .productId(productId)
                .productName(product.getName())
                .price(BigDecimal.valueOf(product.getPrice()))
                .totalStock(totalStock)
                .availableInMarkets(marketAvailability);

        return ResponseEntity.ok(result);
    }

    // GET /api/markets/{marketId}/stock/{productId} - Spezifischer Bestand
    @Override
    public ResponseEntity<Integer> getSpecificStock(Integer marketId, Integer productId) {
        Optional<MarketStock> marketStock = marketService.getSpecificStock(marketId, productId);
        return ResponseEntity.ok(marketStock.map(MarketStock::getStock).orElse(0));
    }

    // Helper Method
    private ProductWithMarketStockDTO convertToProductWithStockDto(MarketStock marketStock) {
        Optional<SalesProduct> productOpt = marketService.getProduct(marketStock.getProductId());
        Optional<Market> marketOpt = marketService.getMarket(marketStock.getMarketId());

        if (productOpt.isPresent() && marketOpt.isPresent()) {
            return MarketApiMapper.convertToProductWithStockDto(marketStock, productOpt.get(), marketOpt.get());
        }

        return null;
    }
}
