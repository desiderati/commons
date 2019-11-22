/*
 * Copyright (c) 2019 - Felipe Desiderati
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
package br.tech.desiderati.common.data.jpa;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.data.domain.Persistable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

/**
 * Usar esta classe quando for definir um repositório de dados personalizado!
 */
@Repository
@Transactional(readOnly = true)
@SuppressWarnings("unused")
public abstract class AbstractRepository<E extends Persistable<?>> {

    @PersistenceContext
    private EntityManager entityManager;

    private Class<E> entityClass;

    private String getEntityName() {
        return getEntityClass().getName();
    }

    @SuppressWarnings("unchecked")
    private Class<E> getEntityClass() {
        if (entityClass == null) {
            entityClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        }
        return entityClass;
    }

    protected Query createQuery(StringBuilder hql) {
        return createQuery(hql.toString());
    }

    protected Query createQuery(String hql) {
        return entityManager.createQuery(hql);
    }

    protected Query createQuery(StringBuilder hql, Map<String, Object> parameters) {
        return createQuery(hql.toString(), parameters);
    }

    protected Query createQuery(String hql, Map<String, Object> parameters) {
        Query query = entityManager.createQuery(hql);
        if (parameters != null) {
            setParameters(query, parameters);
        }
        return query;
    }

    protected Query createNativeQuery(String sql) {
        return createNativeQuery(sql, null);
    }

    protected Query createNativeQuery(String sql, Map<String, Object> parameters) {
        Query sqlQuery = entityManager.createNativeQuery(sql);
        if (parameters != null) {
            setParameters(sqlQuery, parameters);
        }
        return sqlQuery;
    }

    private void setParameters(Query query, Map<String, Object> parameters) {
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (entry.getValue() != null) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
        }
    }

    public void update(E entity, String... fields) {
        if (fields == null || fields.length == 0) {
            throw new IllegalStateException("Fields can not be null or empty!");
        }

        StringBuilder hql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        hql.append("update ").append(getEntityName()).append(" set ");
        try {
            for (int i = 0; i < fields.length; i++) {
                String field = fields[i];
                if (i > 0) {
                    hql.append(", ");
                }

                hql.append(field);
                hql.append(" = ");

                Object object = PropertyUtils.getProperty(entity, field);
                if (object == null) {
                    hql.append("null");
                } else {
                    hql.append(":").append(field);
                    params.put(field, object);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        hql.append(" where id = :id");
        params.put("id", entity.getId());

        Query query = createQuery(hql, params);
        query.executeUpdate();
    }

    public void deleteById(Long... ids) {
        StringBuilder hql = new StringBuilder();
        hql.append("delete from ").append(getEntityName()).append(" ");
        hql.append("where id ").append(ids.length == 1 ? "= :id" : "in :ids");

        Query query = createQuery(hql);
        if (ids.length == 1) {
            query.setParameter("id", ids[0]);
        } else {
            query.setParameter("ids", ids);
        }
        query.executeUpdate();
    }
}
