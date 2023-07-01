package com.vinsguru.common.orchestrator;

import com.vinsguru.common.messages.Request;
import com.vinsguru.common.messages.Response;
import org.reactivestreams.Publisher;

public interface ResponseProcessor<T extends Response> {

    Publisher<Request> process(T response);

}
