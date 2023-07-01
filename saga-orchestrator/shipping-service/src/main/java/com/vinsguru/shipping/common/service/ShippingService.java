package com.vinsguru.shipping.common.service;

import com.vinsguru.shipping.common.dto.ScheduleRequest;
import com.vinsguru.shipping.common.dto.ShipmentDto;
import reactor.core.publisher.Mono;

public interface ShippingService {

    Mono<ShipmentDto> schedule(ScheduleRequest request);

}

