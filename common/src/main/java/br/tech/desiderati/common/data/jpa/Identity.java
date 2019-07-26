/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.data.jpa;

import java.io.Serializable;

public interface Identity<I extends Serializable> extends Serializable {

    /**
     * @return O identificador da entidade.
     */
    I getId();

}
