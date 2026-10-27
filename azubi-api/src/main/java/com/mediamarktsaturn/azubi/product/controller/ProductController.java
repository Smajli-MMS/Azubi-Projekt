package com.mediamarktsaturn.azubi.product.controller;

import com.mediamarktsaturn.azubi.core.product.ProductService;
import com.mediamarktsaturn.azubi.core.product.dto.UpdateProductDto;
import com.mediamarktsaturn.azubi.core.product.model.SalesProduct;
import com.mediamarktsaturn.azubi.core.stock.StockService;
import com.mediamarktsaturn.azubi.core.stock.model.Stock;
import com.mediamarktsaturn.azubi.generated.api.ProductsApi;
import com.mediamarktsaturn.azubi.generated.model.ProductApiDTO;
import com.mediamarktsaturn.azubi.generated.model.ProductStockResponseDTO;
import com.mediamarktsaturn.azubi.generated.model.UpdateProductApiRequestDTO;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class ProductController implements ProductsApi {

    private final ProductService productService;
    private final StockService stockService;

    public ProductController(ProductService productService, StockService stockService) {
        this.productService = productService;
        this.stockService = stockService;
    }

    // GET /api/products/{id} - Product by ID
    @Override
    public ResponseEntity<ProductApiDTO> getProductById(Integer id) {
        if (id < 0) {
            throw new IllegalArgumentException("Product id must be greater than 0");
        }

        Optional<SalesProduct> product = productService.getProductByArticleNumber(id);

        return product
                .map(salesProduct -> ResponseEntity.ok(enrichWithStock(salesProduct)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /api/products - All Products
    @Override
    public ResponseEntity<List<ProductApiDTO>> getAllProducts() {
        List<ProductApiDTO> dtos = productService.getAllProducts()
                .stream()
                .map(ProductApiMapper::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // POST /api/products - Create new Product
    @Override
    public ResponseEntity<ProductApiDTO> createProduct(ProductApiDTO productApiDto) {
        try {
            SalesProduct domain = ProductApiMapper.convertToDomain(productApiDto);
            SalesProduct saved = productService.createProduct(domain);
            return ResponseEntity.status(HttpStatus.CREATED).body(ProductApiMapper.convertToDto(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT /api/products/{id} - Update existing Product
    @Override
    public ResponseEntity<ProductApiDTO> updateProduct(Integer id,
                                                       UpdateProductApiRequestDTO updateProductRequest) {
        UpdateProductDto dto = ProductApiMapper.convertToDomain(updateProductRequest);

        return productService.updateProduct(id, dto)
                .map(updated -> ResponseEntity.ok(ProductApiMapper.convertToDto(updated)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /api/products/{id}/stock - Stock value for Product
    @Override
    public ResponseEntity<ProductStockResponseDTO> getProductStock(Integer id) {
        Optional<SalesProduct> product = productService.getProductByArticleNumber(id);

        if (product.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Stock> stock = stockService.getStockByProductId(id);

        ProductStockResponseDTO response = new ProductStockResponseDTO();
        response.setProductId(id);
        response.setProductName(product.get().getName());
        response.setStock(stock.map(Stock::getStock).orElse(0));
        response.setLastChange(stock.map(Stock::getLastChange).orElse(null));

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteProduct(Integer id) {
        boolean deleted = productService.deleteProduct(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }

    // GET /api/products/with-stock - Alle Produkte mit Bestandsinfo
    @Override
    public ResponseEntity<List<ProductApiDTO>> getAllProductsWithStock() {
        List<ProductApiDTO> dtos = productService.getAllProducts()
                .stream()
                .map(this::enrichWithStock)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    private ProductApiDTO enrichWithStock(SalesProduct product) {
        ProductApiDTO dto = ProductApiMapper.convertToDto(product);

        stockService.getStockByProductId(product.getId()).ifPresentOrElse(
                stock -> {
                    ProductApiMapper.setStockQuantity(dto, stock.getStock());
                    dto.setStockLastChange(stock.getLastChange());
                },
                () -> ProductApiMapper.setStockQuantity(dto, 0)
        );

        return dto;
    }

}
