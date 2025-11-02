package com.distribute.products.controller;

import com.distribute.products.dto.request.CalcTotalAmontRequest;
import com.distribute.products.dto.request.UpdateStockRequest;
import com.distribute.products.dto.response.ApiResponse;
import com.distribute.products.entity.Product;
import com.distribute.products.kafka.event.Item;
import com.distribute.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductController {

    @Autowired
    private final ProductService productService;

    @GetMapping()
    public List<Product> findAllProducts() {
        return productService.findAllProducts();
    }

    @PostMapping()
    public ResponseEntity<Product> save(@RequestBody Product product) {

        return ResponseEntity.ok(
                productService.createProduct(product));
    }

    @PostMapping("/total_amount")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateTotalAmount(@RequestBody CalcTotalAmontRequest request) {
        ApiResponse<BigDecimal> apiResponse = new ApiResponse<>(200, "Total amount calculated successfully",
                productService.calculateTotalAmount(request.items));
        return ResponseEntity.ok(apiResponse);
    }

}
