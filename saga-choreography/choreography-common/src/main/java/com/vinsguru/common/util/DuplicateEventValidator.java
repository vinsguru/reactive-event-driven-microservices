package com.vinsguru.common.util;

import com.vinsguru.common.exception.EventAlreadyProcessedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class DuplicateEventValidator {

    private static final Logger log = LoggerFactory.getLogger(DuplicateEventValidator.class);

    public static Function<Mono<Boolean>, Mono<Void>> emitErrorForRedundantProcessing() {
        return mono -> mono
                .flatMap(b -> b ? Mono.error(new EventAlreadyProcessedException()) : Mono.empty())
                .doOnError(EventAlreadyProcessedException.class, ex -> log.warn("Duplicate event"))
                .then();
    }

    public static <T> Mono<T> validate(Mono<Boolean> eventValidationPublisher, Mono<T> eventProcessingPublisher){
        return eventValidationPublisher
                .transform(emitErrorForRedundantProcessing())
                .then(eventProcessingPublisher);
    }

    /*

            DuplicateEventValidator.validate(  isPresentInDB(some-id), process(some-id) )
                                .doOnNext(...)
                                .map(...)
                                ...
                                ...

     */


}
