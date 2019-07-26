/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.data.jpa;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class NullSafeJpaRepository {

    @Pointcut("target(org.springframework.data.jpa.repository.JpaRepository)")
    public void repositoryMethods() {
        // Apenas uma configuração do ASPECTJ, não é necessário implementação.
    }

    @Around("repositoryMethods()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        return NullSafeJpaRepository.processParameterAndReturnNullIfNull(joinPoint);
    }

    public static Object processParameterAndReturnNullIfNull(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args.length != 1) {
            return joinPoint.proceed();
        }

        Object obj = args[0];
        if (obj == null || (obj instanceof Iterable && !((Iterable) obj).iterator().hasNext())) {
            return null;
        }

        return joinPoint.proceed();
    }
}
