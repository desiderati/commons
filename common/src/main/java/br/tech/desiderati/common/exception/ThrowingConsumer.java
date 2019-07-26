/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.exception;

import java.util.function.Consumer;

@FunctionalInterface
@SuppressWarnings("unused")
public interface ThrowingConsumer<T> {

    void apply(T t) throws Exception;

    static <T> Consumer<T> unchecked(ThrowingConsumer<T> f) {
        return t -> {
            try {
                f.apply(t);
            } catch (Exception ex) {
                ThrowingConsumer.sneakyThrow(ex);
            }
        };
    }

    @SuppressWarnings("unchecked")
    static <T extends Exception> void sneakyThrow(Exception t) throws T {
        throw (T) t;
    }
}
