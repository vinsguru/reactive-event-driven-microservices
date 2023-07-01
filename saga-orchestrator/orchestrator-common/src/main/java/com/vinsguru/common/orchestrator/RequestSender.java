package com.vinsguru.common.orchestrator;

import com.vinsguru.common.messages.Request;
import org.reactivestreams.Publisher;

import java.util.UUID;

public interface RequestSender {

    Publisher<Request> send(UUID id);

}
