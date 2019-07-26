/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.data.mongodb;

import br.tech.desiderati.common.data.jpa.NullSafeJpaRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class NullSafeMongoRepository {

    @Pointcut("target(org.springframework.data.mongodb.repository.MongoRepository)")
    public void repositoryMethods() {
        // AspectJ configuration
    }

    @Around("repositoryMethods()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        return NullSafeJpaRepository.processParameterAndReturnNullIfNull(joinPoint);
    }
}
