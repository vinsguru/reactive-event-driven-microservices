package com.vinsguru.shipping.common.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ScheduleRequest(UUID orderId,
                              Integer productId,
                              Integer customerId,
                              Integer quantity) {
}
