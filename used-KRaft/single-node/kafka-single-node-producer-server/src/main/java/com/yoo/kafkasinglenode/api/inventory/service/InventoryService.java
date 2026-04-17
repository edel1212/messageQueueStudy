package com.yoo.kafkasinglenode.api.inventory.service;

import com.yoo.kafkasinglenode.api.inventory.dto.InventoryDto;

public interface InventoryService {
    String TOPIC = "inventory.request";

    void sendMessage(InventoryDto dto);
}
