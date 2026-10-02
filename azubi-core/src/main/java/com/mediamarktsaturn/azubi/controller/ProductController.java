package com.mediamarktsaturn.azubi.controller;

import com.mediamarktsaturn.azubi.dto.ProductDto;
import com.mediamarktsaturn.azubi.entity.SalesProduct;
import com.mediamarktsaturn.azubi.entity.Stock;
import com.mediamarktsaturn.azubi.repository.SalesProductRepository;
import com.mediamarktsaturn.azubi.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private SalesProductRepository productRepository;

    @Autowired
    private StockRepository stockRepository;

    // GET /api/products/{id} - Product by ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Integer id) {
        Optional<SalesProduct> product = productRepository.findById(id);

        if (product.isPresent()) {
            ProductDto dto = convertToDto(product.get());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/products - All Products
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<SalesProduct> products = productRepository.findAll();
        List<ProductDto> productDtos = products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(productDtos);
    }

    // POST /api/products - Create new Product
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        try {
            // ✅ KORRIGIERT: Vollständiger Konstruktor mit allen Werten
            SalesProduct entity = new SalesProduct(
                    productDto.getName(),
                    productDto.getProductGroupId(),
                    productDto.getPrice()
            );
            entity.setProductId(productDto.getProductId());

            SalesProduct savedProduct = productRepository.save(entity);
            ProductDto responseDto = convertToDto(savedProduct);

            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT /api/products/{id} - Update existing Product
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Integer id,
                                                    @RequestBody ProductDto productDto) {
        Optional<SalesProduct> existingProduct = productRepository.findById(id);

        if (existingProduct.isPresent()) {
            SalesProduct entity = existingProduct.get();

            // Update fields
            if (productDto.getName() != null) {
                entity.setName(productDto.getName());
            }
            if (productDto.getProductGroupId() != null) {
                entity.setProductGroupId(productDto.getProductGroupId());
            }
            if (productDto.getPrice() != null) {
                entity.setPrice(productDto.getPrice());
            }

            SalesProduct updatedProduct = productRepository.save(entity);
            ProductDto responseDto = convertToDto(updatedProduct);

            return ResponseEntity.ok(responseDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/products/{id}/stock - Stock value for Product
    @GetMapping("/{id}/stock")
    public ResponseEntity<StockResponse> getProductStock(@PathVariable Integer id) {
        Optional<SalesProduct> product = productRepository.findById(id);

        if (product.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // ✅ KORRIGIERT: Verwende findByProductId statt findById
        Optional<Stock> stock = stockRepository.findByProductId(id);

        StockResponse response = new StockResponse();
        response.setProductId(id);
        response.setProductName(product.get().getName());
        response.setStock(stock.map(Stock::getStock).orElse(0));
        response.setLastChange(stock.map(Stock::getLastChange).orElse(null));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        var product = productRepository.findById(id);

        if (product.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        productRepository.delete(product.get());
        return ResponseEntity.ok().build();
    }

    // Helper method to convert Entity to DTO
    private ProductDto convertToDto(SalesProduct entity) {
        return new ProductDto(
                entity.getProductId(),
                entity.getName(),
                entity.getProductGroupId(),
                entity.getPrice(),
                entity.getCreated(),
                entity.getUpdated()
        );
    }

    // GET /api/products/with-stock - Alle Produkte mit Bestandsinfo
    @GetMapping("/with-stock")
    public ResponseEntity<List<ProductDto>> getAllProductsWithStock() {
        List<SalesProduct> products = productRepository.findAll();

        List<ProductDto> productDtos = products.stream()
                .map(product -> {
                    ProductDto dto = convertToDto(product);

                    // ✅ Stock hinzufügen
                    stockRepository.findByProductId(product.getProductId())
                            .ifPresentOrElse(
                                    stock -> {
                                        dto.setStockQuantity(stock.getStock());
                                        dto.setStockLastChange(stock.getLastChange());
                                    },
                                    () -> dto.setStockQuantity(0)
                            );

                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(productDtos);
    }



    // Inner class for Stock Response
    public static class StockResponse {
        private Integer productId;
        private String productName;
        private Integer stock;
        private LocalDateTime lastChange;

        // Getters und Setters
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

        public LocalDateTime getLastChange() {
            return lastChange;
        }

        public void setLastChange(LocalDateTime lastChange) {
            this.lastChange = lastChange;
        }
    }
}
