package com.app.order_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemId implements Serializable {
    private Integer productId;
    private Integer orderId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItemId)) return false;
        OrderItemId that = (OrderItemId) o;
        return productId.equals(that.productId) && orderId.equals(that.orderId);
    }

    @Override
    public int hashCode() {
        int result = productId.hashCode();
        result = 31 * result + orderId.hashCode();
        return result;
    }
}
