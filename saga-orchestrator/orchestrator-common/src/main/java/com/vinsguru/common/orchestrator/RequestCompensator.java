package com.vinsguru.common.orchestrator;

import com.vinsguru.common.messages.Request;
import org.reactivestreams.Publisher;

import java.util.UUID;

public interface RequestCompensator {

    Publisher<Request> compensate(UUID id);

}
