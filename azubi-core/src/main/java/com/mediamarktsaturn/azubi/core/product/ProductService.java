package com.mediamarktsaturn.azubi.core.product;

import com.mediamarktsaturn.azubi.core.product.model.SalesProduct;
import com.mediamarktsaturn.azubi.entity.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private SalesProductRepository salesProductRepository;

    @Autowired
    private StockRepository stockRepository;

    public Optional<SalesProduct> getProductByArticleNumber(Integer id) {
        return salesProductRepository.getSalesProductByArticleNumber(id);
    }

    public List<SalesProduct> getAllProducts() {
        return salesProductRepository.findAll();
    }

    public SalesProduct createProduct(SalesProduct product) {
        return salesProductRepository.save(product);
    }

    public Optional<SalesProduct> updateProduct(Integer id, SalesProduct updated) {
        return salesProductRepository.getSalesProductByArticleNumber(id)
                .map(existing -> {
                    if (updated.getName() != null) {
                        existing.setName(updated.getName());
                    }
                    if (updated.getCategory() != null) {
                        existing.setCategory(updated.getCategory());
                    }
                    existing.setPrice(updated.getPrice());
                    return salesProductRepository.save(existing);
                });
    }

    public boolean deleteProduct(Integer id) {
        if (salesProductRepository.existsById(id)) {
            salesProductRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<Stock> getStock(Integer productId) {
        return stockRepository.findByProductId(productId);
    }
}
