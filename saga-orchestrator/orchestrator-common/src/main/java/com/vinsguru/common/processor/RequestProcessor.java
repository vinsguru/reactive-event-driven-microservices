package com.vinsguru.common.processor;

import com.vinsguru.common.messages.Request;
import com.vinsguru.common.messages.Response;
import reactor.core.publisher.Mono;

public interface RequestProcessor<T extends Request, R extends Response> {

    Mono<R> process(T request);

}
