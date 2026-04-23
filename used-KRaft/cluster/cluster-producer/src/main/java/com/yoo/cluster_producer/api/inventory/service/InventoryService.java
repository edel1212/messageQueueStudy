package com.yoo.cluster_producer.api.inventory.service;


import com.yoo.cluster_producer.api.inventory.dto.InventoryDto;

public interface InventoryService {
    String TOPIC = "inventory.request";

    void sendMessage(InventoryDto dto);
}
