package com.mediamarktsaturn.azubi.core.product;

import com.mediamarktsaturn.azubi.core.product.dto.UpdateProductDto;
import com.mediamarktsaturn.azubi.core.product.model.SalesProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private SalesProductRepository salesProductRepository;

    public Optional<SalesProduct> getProductByArticleNumber(Integer id) {
        return salesProductRepository.getSalesProductByArticleNumber(id);
    }

    public List<SalesProduct> getAllProducts() {
        return salesProductRepository.findAll();
    }

    public SalesProduct createProduct(SalesProduct product) {
        return salesProductRepository.save(product);
    }

    public Optional<SalesProduct> updateProduct(Integer id, UpdateProductDto updateData) {
        return salesProductRepository.getSalesProductByArticleNumber(id)
                .map(existing -> updateSalesProduct(existing, updateData))
                .map(updated -> salesProductRepository.save(updated));
    }

    private SalesProduct updateSalesProduct(SalesProduct salesProduct, UpdateProductDto updateData) {
        if (updateData.getName() != null) {
            salesProduct.setName(updateData.getName());
        }
        if (updateData.getCategory() != null) {
            salesProduct.setCategory(updateData.getCategory());
        }
        if (updateData.getPrice() != null) {
            salesProduct.setPrice(updateData.getPrice());
        }
        return salesProduct;
    }

    public boolean deleteProduct(Integer id) {
        if (salesProductRepository.existsById(id)) {
            salesProductRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
