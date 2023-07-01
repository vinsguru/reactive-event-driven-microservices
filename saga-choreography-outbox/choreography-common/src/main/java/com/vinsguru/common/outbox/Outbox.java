package com.vinsguru.common.outbox;

import com.vinsguru.common.events.DomainEvent;
import lombok.Builder;

@Builder
public record Outbox<T extends DomainEvent>(Long correlationId,
                                            T event) {
}
