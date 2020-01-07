/*
 * Copyright (c) 2020 - Felipe Desiderati
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.herd.common.data.jpa;

import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.util.ClassUtils;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Classe abstrata para entidades JPA. Não utilizamos a classe {@link AbstractPersistable} pois a mesma
 * somente permite a configuração de um identificador usando {@link GenerationType#AUTO} (Valor padrão,
 * permite que o provedor de persistência escolha a estratégia mais adequada de acordo com o banco de
 * dados).
 * <p>
 * Ao invés, aqui usamos a configuração {@link GenerationType#IDENTITY} (Informamos ao provedor de
 * persistência que os valores a serem atribuídos ao identificador único serão gerados pela coluna de
 * auto incremento do banco de dados. Assim, um valor para o identificador é gerado para cada registro
 * inserido no banco. Alguns bancos de dados podem não suportar essa opção).
 */
@MappedSuperclass
@SuppressWarnings("unused")
public abstract class AbstractPersistableIdentity<I extends Serializable> implements Persistable<I>, Identity<I> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private I id;

    @Override
    public I getId() {
        return id;
    }

    protected void setId(final I id) {
        this.id = id;
    }

    /**
     * Precisa ser {@link Transient} para garantir que o provedor JPA não reclame de um 'setter' faltante.
     *
     * @see Persistable#isNew()
     */
    @Transient // DATAJPA-622
    public boolean isNew() {
        return null == getId();
    }

    @Override
    public String toString() {
        return String.format("Entity of type %s with id: %s", getClass().getName(), getId());
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (!getClass().equals(ClassUtils.getUserClass(obj))) {
            return false;
        }

        AbstractPersistableIdentity<?> that = (AbstractPersistableIdentity<?>) obj;
        return null != getId() && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode += null == getId() ? 0 : getId().hashCode() * 31;
        return hashCode;
    }
}

