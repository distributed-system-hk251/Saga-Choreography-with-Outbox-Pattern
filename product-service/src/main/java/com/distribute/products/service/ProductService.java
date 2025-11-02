package com.distribute.products.service;

import com.distribute.products.entity.Product;
import com.distribute.products.kafka.event.Item;
import com.distribute.products.kafka.producer.ProductProducer;
import com.distribute.products.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    // private final ProductProducer productProducer;

    public List<Product> findAllProducts(){
        return productRepository.findAll();
    }

    public Product createProduct(Product product){
        return productRepository.save(product);
    }

    @Transactional
    public void updateStocks(Integer orderId, List<Item> items ){
        for (Item item : items) {
            Product p = productRepository.findByIdForUpdate(item.getProductId())
                    .orElseThrow(() ->
                            new RuntimeException("Product not found: " + item.getProductId()));


            if (p.getStock() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + item.getProductId());
            }

            p.setStock(p.getStock() - item.getQuantity());
            productRepository.save(p);          
        }

        return ;
    }

    @Transactional
    public void releaseStocks(Integer orderId, List<Item> items) {
        for (Item item : items) {
            Product p = productRepository.findByIdForUpdate(item.getProductId())
                    .orElseThrow(() ->
                            new RuntimeException("Product not found: " + item.getProductId()));

            // Add back the reserved stock
            p.setStock(p.getStock() + Math.abs(item.getQuantity()));
            productRepository.save(p);
        }
    }

    public BigDecimal calculateTotalAmount(List<Item> items) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Item item : items) {
            Product p = productRepository.findById(item.getProductId())
                    .orElseThrow(() ->
                            new RuntimeException("Product not found: " + item.getProductId()));
            BigDecimal total = p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(total);
        }
        return totalAmount;
    }

}
