/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.exception;

@FunctionalInterface
@SuppressWarnings("unused")
public interface ThrowingRunnable {

    void run() throws Exception;

    static Runnable uncheckedRunnable(ThrowingRunnable f) {
        return () -> {
            try {
                f.run();
            } catch (Exception ex) {
                ThrowingRunnable.sneakyThrow(ex);
            }
        };
    }

    @SuppressWarnings("unchecked")
    static <E extends Exception> void sneakyThrow(Exception e) throws E {
        throw (E) e;
    }
}
