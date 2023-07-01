package com.vinsguru.common.demo;

import java.util.Objects;
import java.util.function.Function;

class MessageHandlerImpl<I, O> implements MessageHandler<I, O> {

    private final I input;
    private O output;

    public MessageHandlerImpl(I input) {
        this.input = input;
    }

    @Override
    public <R> MessageHandler<I, O> onMessage(Class<R> type, Function<R, O> function) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(function);
        if(Objects.isNull(output) && type.isInstance(input)){
            this.output = function.apply((R) input);
        }
        return this;
    }

    @Override
    public O handle() {
        return this.output;
    }

}
