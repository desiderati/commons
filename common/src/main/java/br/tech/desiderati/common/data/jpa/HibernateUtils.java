/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.data.jpa;

import lombok.experimental.UtilityClass;
import org.hibernate.Hibernate;

@UtilityClass
@SuppressWarnings("unused")
public class HibernateUtils {

    @SuppressWarnings("unchecked")
    public static <T, S extends T> S unproxy(T object) {
        return (S) Hibernate.unproxy(object);
    }
}
