package com.distribute.products.dto.request;

import java.util.List;
import com.distribute.products.kafka.event.Item;

public class CalcTotalAmontRequest {
    public List<Item> items;
}
