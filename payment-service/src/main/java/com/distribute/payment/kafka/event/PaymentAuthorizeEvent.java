
package com.distribute.payment.kafka.event;

import lombok.*;
import java.math.BigDecimal;



@Builder
public record PaymentAuthorizeEvent( 
    Integer orderId,    
    BigDecimal amount
) {}
