package com.mediamarktsaturn.azubi;

import com.mediamarktsaturn.azubi.core.market.MarketService;
import com.mediamarktsaturn.azubi.core.product.ProductService;
import com.mediamarktsaturn.azubi.core.product.model.Category;
import com.mediamarktsaturn.azubi.core.product.model.SalesProduct;
import com.mediamarktsaturn.azubi.core.stock.StockService;
import com.mediamarktsaturn.azubi.core.stock.model.Stock;
import com.mediamarktsaturn.azubi.market.MarketController;
import com.mediamarktsaturn.azubi.product.controller.ProductController;
import com.mediamarktsaturn.azubi.stock.StockController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import com.mediamarktsaturn.azubi.core.market.model.Market;
import com.mediamarktsaturn.azubi.core.market.model.MarketStock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ApiContractTest {
    private MarketService markets;
    private ProductService products;
    private StockService stock;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        markets = mock(MarketService.class);
        products = mock(ProductService.class);
        stock = mock(StockService.class);
        mvc = MockMvcBuilders.standaloneSetup(
                new MarketController(markets),
                new ProductController(products, stock), new StockController(stock)).build();
    }

    @ParameterizedTest
    @CsvSource({
            "/api/markets, 200", "/api/markets/1, 404",
            "/api/markets/1/products, 200", "/api/markets/1/products/available, 200",
            "/api/markets/overview, 200", "/api/markets/products/1, 404",
            "/api/markets/1/stock/1, 200", "/api/products, 200",
            "/api/products/1, 404", "/api/products/1/stock, 404",
            "/api/products/with-stock, 200", "/api/stock, 200",
            "/api/stock/1, 404", "/api/stock/low-stock, 200", "/api/stock/available, 200"
    })
    void generatedInterfacesExposeExistingGetRoutes(String path, int expectedStatus) throws Exception {
        mvc.perform(get(path)).andExpect(status().is(expectedStatus));
    }

    @Test
    void generatedQueryParameterKeepsDefaultAndExplicitThreshold() throws Exception {
        mvc.perform(get("/api/stock/low-stock")).andExpect(status().isOk());
        verify(stock).getLowStock(10);
        mvc.perform(get("/api/stock/low-stock").param("threshold", "3")).andExpect(status().isOk());
        verify(stock).getLowStock(3);
    }

    @Test
    void generatedRequestBodySupportsCreateUpdateAndDelete() throws Exception {
        when(products.createProduct(any())).thenAnswer(call -> call.getArgument(0));
        String json = "{\"name\":\"Phone\",\"productGroupId\":1,\"price\":99.95}";
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("Phone"))
                .andExpect(jsonPath("$.price").value(99.95));
        when(products.updateProduct(eq(1), any())).thenReturn(
                Optional.of(new SalesProduct(1, "Updated", Category.SMARTPHONE, 99.95)));
        mvc.perform(put("/api/products/1").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Updated"));
        verify(products).updateProduct(eq(1), argThat(dto -> "Phone".equals(dto.getName())
                && dto.getPrice().doubleValue() == 99.95));
        when(products.deleteProduct(1)).thenReturn(true);
        mvc.perform(delete("/api/products/1")).andExpect(status().isOk());
        mvc.perform(delete("/api/products/2")).andExpect(status().isNotFound());
    }

    @Test
    void generatedMarketDtosRetainOverviewAndAvailabilityJson() throws Exception {
        Market market = new Market("Ingolstadt", "Ingolstadt", "85049");
        market.setMarketId(1);
        MarketStock available = new MarketStock(1, 7, 3);
        SalesProduct phone = new SalesProduct(7, "Phone", Category.SMARTPHONE, 99.95);
        when(markets.getAllMarkets()).thenReturn(List.of(market));
        when(markets.getMarket(1)).thenReturn(Optional.of(market));
        when(markets.getProduct(7)).thenReturn(Optional.of(phone));
        when(markets.getStockForProduct(7)).thenReturn(List.of(available));
        when(markets.getAvailableStockByMarket(1)).thenReturn(List.of(available));
        mvc.perform(get("/api/markets/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.postalCode").value("85049"));
        mvc.perform(get("/api/markets/overview")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].availableProducts").value(1))
                .andExpect(jsonPath("$[0].totalStock").value(3));
        mvc.perform(get("/api/markets/1/products/available")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(7))
                .andExpect(jsonPath("$[0].productGroupId").value(1))
                .andExpect(jsonPath("$[0].price").value(99.95));
        mvc.perform(get("/api/markets/products/7")).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStock").value(3))
                .andExpect(jsonPath("$.availableInMarkets[0].marketName").value("Ingolstadt"))
                .andExpect(jsonPath("$.availableInMarkets[0].available").value(true));
    }

    @ParameterizedTest
    @CsvSource({"0, OUT_OF_STOCK", "1, LOW_STOCK", "5, LOW_STOCK", "6, AVAILABLE"})
    void generatedDtoRetainsCalculatedStockStatus(int quantity, String expectedStatus) throws Exception {
        when(products.getProductByArticleNumber(1)).thenReturn(
                Optional.of(new SalesProduct(1, "Phone", Category.SMARTPHONE, 99.95)));
        Stock value = mock(Stock.class);
        when(value.getStock()).thenReturn(quantity);
        when(value.getLastChange()).thenReturn(LocalDateTime.of(2026, 10, 27, 12, 0));
        when(stock.getStockByProductId(1)).thenReturn(Optional.of(value));
        mvc.perform(get("/api/products/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(quantity))
                .andExpect(jsonPath("$.stockStatus").value(expectedStatus))
                .andExpect(jsonPath("$.articleNumber").value("XX-0000001"))
                .andExpect(jsonPath("$.stockLastChange").exists());
    }
}
