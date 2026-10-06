package com.mediamarktsaturn.azubi.product.controller;

import com.mediamarktsaturn.azubi.core.product.ProductService;
import com.mediamarktsaturn.azubi.core.product.model.SalesProduct;
import com.mediamarktsaturn.azubi.entity.Stock;
import com.mediamarktsaturn.azubi.product.model.ProductApiDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET /api/products/{id} - Product by ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductApiDto> getProductById(@PathVariable Integer id) {
        if (id < 0) {
            throw new IllegalArgumentException("Product id must be greater than 0");
        }

        Optional<SalesProduct> product = productService.getProductByArticleNumber(id);

        if (product.isPresent()) {
            return ResponseEntity.ok(enrichWithStock(product.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/products - All Products
    @GetMapping
    public ResponseEntity<List<ProductApiDto>> getAllProducts() {
        List<ProductApiDto> dtos = productService.getAllProducts().stream()
                .map(ProductApiMapper::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // POST /api/products - Create new Product
    @PostMapping
    public ResponseEntity<ProductApiDto> createProduct(@RequestBody ProductApiDto productApiDto) {
        try {
            SalesProduct domain = ProductApiMapper.convertToDomain(productApiDto);
            SalesProduct saved = productService.createProduct(domain);
            return ResponseEntity.status(HttpStatus.CREATED).body(ProductApiMapper.convertToDto(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT /api/products/{id} - Update existing Product
    @PutMapping("/{id}")
    public ResponseEntity<ProductApiDto> updateProduct(@PathVariable Integer id,
                                                       @RequestBody ProductApiDto productApiDto) {
        SalesProduct domain = ProductApiMapper.convertToDomain(productApiDto);

        return productService.updateProduct(id, domain)
                .map(updated -> ResponseEntity.ok(ProductApiMapper.convertToDto(updated)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /api/products/{id}/stock - Stock value for Product
    @GetMapping("/{id}/stock")
    public ResponseEntity<StockResponse> getProductStock(@PathVariable Integer id) {
        Optional<SalesProduct> product = productService.getProductByArticleNumber(id);

        if (product.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Stock> stock = productService.getStock(id);

        StockResponse response = new StockResponse();
        response.setProductId(id);
        response.setProductName(product.get().getName());
        response.setStock(stock.map(Stock::getStock).orElse(0));
        response.setLastChange(stock.map(Stock::getLastChange).orElse(null));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        boolean deleted = productService.deleteProduct(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    // GET /api/products/with-stock - Alle Produkte mit Bestandsinfo
    @GetMapping("/with-stock")
    public ResponseEntity<List<ProductApiDto>> getAllProductsWithStock() {
        List<ProductApiDto> dtos = productService.getAllProducts().stream()
                .map(this::enrichWithStock)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    private ProductApiDto enrichWithStock(SalesProduct product) {
        ProductApiDto dto = ProductApiMapper.convertToDto(product);

        productService.getStock(product.getId()).ifPresentOrElse(
                stock -> {
                    dto.setStockQuantity(stock.getStock());
                    dto.setStockLastChange(stock.getLastChange());
                },
                () -> dto.setStockQuantity(0)
        );

        return dto;
    }

    // Inner class for Stock Response
    public static class StockResponse {
        private Integer productId;
        private String productName;
        private Integer stock;
        private java.time.LocalDateTime lastChange;

        public Integer getProductId() {
            return productId;
        }

        public void setProductId(Integer productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Integer getStock() {
            return stock;
        }

        public void setStock(Integer stock) {
            this.stock = stock;
        }

        public java.time.LocalDateTime getLastChange() {
            return lastChange;
        }

        public void setLastChange(java.time.LocalDateTime lastChange) {
            this.lastChange = lastChange;
        }
    }
}
