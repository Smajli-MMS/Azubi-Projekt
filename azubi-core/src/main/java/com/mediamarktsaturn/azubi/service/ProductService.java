package com.mediamarktsaturn.azubi.service;

import com.mediamarktsaturn.azubi.Category;
import com.mediamarktsaturn.azubi.dto.ProductDto;
import com.mediamarktsaturn.azubi.entity.SalesProduct;
import com.mediamarktsaturn.azubi.entity.Stock;
import com.mediamarktsaturn.azubi.repository.SalesProductRepository;
import com.mediamarktsaturn.azubi.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private SalesProductRepository salesProductRepository;

    @Autowired
    private StockRepository stockRepository;

    // GET /products/{id}
    public Optional<com.mediamarktsaturn.azubi.SalesProduct> getProductById(Integer id) {
        return salesProductRepository.findById(id)
                .map(this::convertToSalesProduct);
    }

    // GET /products
    public List<com.mediamarktsaturn.azubi.SalesProduct> getAllProducts() {
        return salesProductRepository.findAll()
                .stream()
                .map(this::convertToSalesProduct)
                .toList();
    }

    // POST /products
    public com.mediamarktsaturn.azubi.SalesProduct createProduct(ProductDto productDto) {
        var entity = convertToEntity(productDto);
        entity.setCreated(LocalDateTime.now());
        entity.setUpdated(LocalDateTime.now());

        var savedEntity = salesProductRepository.save(entity);
        return convertToSalesProduct(savedEntity);
    }

    // PUT /products/{id}
    public Optional<com.mediamarktsaturn.azubi.SalesProduct> updateProduct(Integer id, ProductDto productDto) {
        return salesProductRepository.findById(id)
                .map(existingEntity -> {
                    updateEntityFromDto(existingEntity, productDto);
                    existingEntity.setUpdated(LocalDateTime.now());
                    var savedEntity = salesProductRepository.save(existingEntity);
                    return convertToSalesProduct(savedEntity);
                });
    }

    // GET /products/{id}/stock
    public Optional<Integer> getStockByProductId(Integer productId) {
        return stockRepository.findByProductId(productId)
                .map(Stock::getStock);
    }

    // Hilfsmethoden für Konvertierung
    private com.mediamarktsaturn.azubi.SalesProduct convertToSalesProduct(SalesProduct entity) {
        var salesProduct = new com.mediamarktsaturn.azubi.SalesProduct();
        salesProduct.setId(entity.getProductId());
        salesProduct.setName(entity.getName());
        salesProduct.setCategory(mapProductGroupToCategory(entity.getProductGroupId()));
        salesProduct.setPrice(entity.getPrice().doubleValue()); // BigDecimal zu double
        salesProduct.setCreated(entity.getCreated());
        salesProduct.setUpdated(entity.getUpdated());
        return salesProduct;
    }

    private SalesProduct convertToEntity(ProductDto dto) {
        var entity = new SalesProduct();
        entity.setProductId(dto.getProductId());
        entity.setName(dto.getName());
        entity.setProductGroupId(dto.getProductGroupId());
        entity.setPrice(dto.getPrice()); // Direkt BigDecimal verwenden
        entity.setCreated(LocalDateTime.now());
        entity.setUpdated(LocalDateTime.now());
        return entity;
    }

    private void updateEntityFromDto(SalesProduct entity, ProductDto dto) {
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getProductGroupId() != null) {
            entity.setProductGroupId(dto.getProductGroupId());
        }
        if (dto.getPrice() != null) {
            entity.setPrice(dto.getPrice()); // Direkt BigDecimal
        }
    }

    private Category mapProductGroupToCategory(Integer productGroupId) {
        return switch (productGroupId) {
            case 1 -> Category.SMARTPHONE;
            case 2 -> Category.TV;
            case 4 -> Category.WASHINGMASHINE;
            default -> Category.SMARTPHONE;
        };
    }

    public boolean deleteProduct(Integer id) {
        if (salesProductRepository.existsById(id)) {
            salesProductRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public void extendedDebugStock(Integer productId) {
        System.out.println("=== EXTENDED DEBUG FOR PRODUCT ID: " + productId + " ===");

        List<Stock> allStocks = stockRepository.findAll();
        System.out.println("Total stocks in database: " + allStocks.size());

        for (Stock stock : allStocks) {
            System.out.println("Stock - ProductId: " + stock.getProductId() +
                    ", Stock: " + stock.getStock() +
                    ", LastChange: " + stock.getLastChange());
        }

        Optional<Stock> foundStock = stockRepository.findByProductId(productId);
        if (foundStock.isPresent()) {
            Stock stock = foundStock.get();
            System.out.println("FOUND STOCK:");
            System.out.println("- ProductId: " + stock.getProductId());
            System.out.println("- Stock Value: " + stock.getStock());
            System.out.println("- LastChange: " + stock.getLastChange());
        } else {
            System.out.println("NO STOCK FOUND for productId: " + productId);
        }
    }
}
