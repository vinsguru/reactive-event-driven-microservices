package com.vinsguru.common.demo;

import java.util.function.Function;

public interface MessageHandler<I, O> {

   <R> MessageHandler<I, O> onMessage(Class<R> type, Function<R, O> function);

   O handle();

   static <I, O> MessageHandler<I, O> create(I input){
       return new MessageHandlerImpl<>(input);
   }

}
