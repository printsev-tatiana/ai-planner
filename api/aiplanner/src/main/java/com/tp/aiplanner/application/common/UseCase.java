package com.tp.aiplanner.application.common;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface UseCase<I, O> {

    CompletableFuture<O> execute(I input);
}
