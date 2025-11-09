package com.distribute.products.service;

import com.distribute.products.entity.Product;
import com.distribute.products.kafka.event.Item;
import com.distribute.products.kafka.producer.ProductProducer;
import com.distribute.products.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final OutboxService outboxService;
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

        // ✅ Save STOCK_RESERVE_SUCCEEDED event to outbox (Debezium will publish this to Kafka)
        outboxService.saveStockUpdatedEvent(orderId, items, "STOCK_RESERVE_SUCCEEDED", orderId.toString());

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

        // ✅ Save event to outbox (Debezium will publish this to Kafka)
        outboxService.saveStockReleasedEvent(orderId, items, orderId.toString());
    }

    @Transactional
    public void saveStockReserveFailed(Integer orderId, List<Item> items, String reason) {
        // Save STOCK_RESERVE_FAILED event to outbox with transaction
        // Debezium will publish this event to Kafka
        outboxService.saveStockUpdatedEvent(orderId, items, "STOCK_RESERVE_FAILED", orderId.toString());
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
