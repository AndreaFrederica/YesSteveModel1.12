package com.elfmcys.yesstevemodel.client.model.processor;

@FunctionalInterface
public interface TriFunction<A, B, C, R> {
    R apply(A a, B b, C c);
}
