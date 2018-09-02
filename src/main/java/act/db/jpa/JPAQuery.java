package act.db.jpa;

/*-
 * #%L
 * ACT JPA Common Module
 * %%
 * Copyright (C) 2018 ActFramework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static act.db.jpa.sql.SQL.Type.*;

import act.db.Dao;
import act.db.jpa.sql.SQL;
import org.osgl.$;
import org.osgl.util.E;

import java.util.*;
import javax.persistence.*;

public class JPAQuery<MODEL_TYPE> implements Query, Dao.Query<MODEL_TYPE, JPAQuery<MODEL_TYPE>> {

    private JPAService svc;
    private EntityManager em;
    private Class entityClass;
    private SQL.Type type;
    private String[] columns;
    private String expression;
    private Integer offset;
    private Integer limit;
    private String[] orderByList;
    private Map<String, Object> hints = new HashMap<>();
    private FlushModeType flushMode;
    private LockModeType lockMode;
    private Query q;
    private Map<Integer, Object> params = new HashMap<>();
    private Map<Integer, $.T2<Calendar, TemporalType>> calendarParams = new HashMap<>();
    private Map<Integer, $.T2<Date, TemporalType>> dateParams = new HashMap<>();

    public JPAQuery(JPAService svc, EntityManager em, Class entityClass, SQL.Type type, String expression, String... columns) {
        this.svc = $.requireNotNull(svc);
        this.em = $.requireNotNull(em);
        this.entityClass = $.requireNotNull(entityClass);
        this.type = $.requireNotNull(type);
        this.expression = $.requireNotNull(expression);
        this.columns = columns;
    }

    private JPAQuery(JPAQuery copy, SQL.Type type) {
        this.svc = copy.svc;
        this.type = type;
        this.entityClass = copy.entityClass;
        this.type = copy.type;
        this.expression = copy.expression;
        this.columns = copy.columns;
        this.offset = copy.offset;
        this.limit = copy.limit;
        this.orderByList = copy.orderByList;
        this.hints = copy.hints;
        this.flushMode = copy.flushMode;
        this.lockMode = copy.lockMode;
        this.em = JPADao.readOnly(type) ? JPAContext.em(svc, true) : JPAContext.emWithTx(svc);
        this.params = new HashMap<>(copy.params);
        this.calendarParams = new HashMap<>(copy.calendarParams);
        this.dateParams = new HashMap<>(copy.dateParams);
    }

    public JPAQuery<MODEL_TYPE> asFind() {
        return as(FIND);
    }

    public JPAQuery<MODEL_TYPE> asDelete() {
        return as(DELETE);
    }

    public JPAQuery<MODEL_TYPE> asUpdate() {
        if (UPDATE == this.type) {
            return this;
        }
        throw E.unsupport("Cannot convert other query types to UPDATE");
    }

    public JPAQuery<MODEL_TYPE> asCount() {
        return as(COUNT);
    }

    public JPAQuery<MODEL_TYPE> as(SQL.Type type) {
        if (type == this.type) {
            return this;
        } else {
            JPAQuery<MODEL_TYPE> copy = new JPAQuery<>(this, type);
            copy.type = type;
            return copy;
        }
    }

    // -- act Dao.Query --

    @Override
    public JPAQuery<MODEL_TYPE> offset(int pos) {
        E.illegalArgumentIf(pos < 0, "Invalid offset position: " + pos);
        offset = pos;
        if (null != q) {
            q.setFirstResult(pos);
        }
        return this;
    }

    @Override
    public JPAQuery<MODEL_TYPE> limit(int limit) {
        E.illegalArgumentIf(limit < 1, "Invalid limit size: " + limit);
        this.limit = limit;
        if (null != q) {
            q.setMaxResults(limit);
        }
        return this;
    }

    @Override
    public JPAQuery<MODEL_TYPE> orderBy(String... fieldList) {
        if (null != q) {
            JPAQuery newQuery = new JPAQuery(this, this.type);
            newQuery.orderByList = fieldList;
            return newQuery;
        } else {
            orderByList = fieldList;
            return this;
        }
    }

    /**
     * Alias of {@link #first()}
     * @return the first result found
     */
    public MODEL_TYPE findOne() {
        return first();
    }

    @Override
    public MODEL_TYPE first() {
        try {
            limit(1);
            List<MODEL_TYPE> list = fetch();
            return null == list || list.isEmpty() ? null : list.get(0);
        } catch (NoResultException e) {
            return null;
        } finally {
            q = null;
        }
    }

    @Override
    public List<MODEL_TYPE> fetch() {
        return $.cast(q().getResultList());
    }

    @Override
    public long count() {
        if (type != SQL.Type.COUNT) {
            return asCount().count();
        }
        Number n = (Number) q().getSingleResult();
        return n.longValue();
    }

    public int delete() {
        if (type != SQL.Type.DELETE) {
            return asDelete().delete();
        }
        return executeUpdate();
    }

    // -- EOF act Dao.Query --

    // -- JPA Query --

    @Override
    public List getResultList() {
        return q().getResultList();
    }

    @Override
    public Object getSingleResult() {
        return q().getSingleResult();
    }

    @Override
    public int executeUpdate() {
        return q().executeUpdate();
    }

    @Override
    public Query setMaxResults(int maxResult) {
        return limit(maxResult);
    }

    @Override
    public int getMaxResults() {
        return limit;
    }

    @Override
    public Query setFirstResult(int startPosition) {
        return offset(startPosition);
    }

    @Override
    public int getFirstResult() {
        return offset;
    }

    @Override
    public Query setHint(String hintName, Object value) {
        hints.put(hintName, value);
        if (null != q) {
            q.setHint(hintName, value);
        }
        return this;
    }

    @Override
    public Map<String, Object> getHints() {
        return hints;
    }

    @Override
    public <T> Query setParameter(Parameter<T> param, T value) {
        q().setParameter(param, value);
        return this;
    }

    @Override
    public Query setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        q().setParameter(param, value, temporalType);
        return this;
    }

    @Override
    public Query setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        q().setParameter(param, value, temporalType);
        return this;
    }

    @Override
    public Query setParameter(String name, Object value) {
        q().setParameter(name, value);
        return this;
    }

    @Override
    public Query setParameter(String name, Calendar value, TemporalType temporalType) {
        q().setParameter(name, value, temporalType);
        return this;
    }

    @Override
    public Query setParameter(String name, Date value, TemporalType temporalType) {
        q().setParameter(name, value, temporalType);
        return this;
    }

    @Override
    public Query setParameter(int position, Object value) {
        if (null != q) {
            q.setParameter(position, value);
        }
        params.put(position, value);
        return this;
    }

    @Override
    public Query setParameter(int position, Calendar value, TemporalType temporalType) {
        if (null != q) {
            q.setParameter(position, value, temporalType);
        }
        calendarParams.put(position, $.T2(value, temporalType));
        return this;
    }

    @Override
    public Query setParameter(int position, Date value, TemporalType temporalType) {
        if (null != q) {
            q.setParameter(position, value, temporalType);
        }
        dateParams.put(position, $.T2(value, temporalType));
        return this;
    }

    @Override
    public Set<Parameter<?>> getParameters() {
        return q().getParameters();
    }

    @Override
    public Parameter<?> getParameter(String name) {
        return q().getParameter(name);
    }

    @Override
    public <T> Parameter<T> getParameter(String name, Class<T> type) {
        return q().getParameter(name, type);
    }

    @Override
    public Parameter<?> getParameter(int position) {
        return q().getParameter(position);
    }

    @Override
    public <T> Parameter<T> getParameter(int position, Class<T> type) {
        return q().getParameter(position, type);
    }

    @Override
    public boolean isBound(Parameter<?> param) {
        return q().isBound(param);
    }

    @Override
    public <T> T getParameterValue(Parameter<T> param) {
        return q().getParameterValue(param);
    }

    @Override
    public Object getParameterValue(String name) {
        return q().getParameterValue(name);
    }

    @Override
    public Object getParameterValue(int position) {
        return q().getParameterValue(position);
    }

    @Override
    public Query setFlushMode(FlushModeType flushMode) {
        this.flushMode = $.requireNotNull(flushMode);
        if (null != q) {
            q.setFlushMode(flushMode);
        }
        return this;
    }

    @Override
    public FlushModeType getFlushMode() {
        return flushMode;
    }

    @Override
    public Query setLockMode(LockModeType lockMode) {
        this.lockMode = $.requireNotNull(lockMode);
        if (null != q) {
            q.setLockMode(lockMode);
        }
        return this;
    }

    @Override
    public LockModeType getLockMode() {
        return lockMode;
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return q().unwrap(cls);
    }

    // -- EOF JPA Query --
    
    private Query q() {
        if (null == q) {
            NamedQuery nq = svc.namedQuery(expression);
            if (null != nq) {
                q = em.createNamedQuery(expression);
            } else {
                q = em.createQuery(svc.getSQL(type, entityClass, expression, columns).withOrderBy(orderByList).rawSql(svc.dialect()));
            }
            if (null != lockMode) {
                q.setLockMode(lockMode);
            }
            if (null != flushMode) {
                q.setFlushMode(flushMode);
            }
            if (null != limit) {
                q.setMaxResults(limit);
            }
            if (null != offset) {
                q.setFirstResult(offset);
            }
            for (Map.Entry<Integer, Object> entry : params.entrySet()) {
                q.setParameter(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<Integer, $.T2<Calendar, TemporalType>> entry : calendarParams.entrySet()) {
                $.T2<Calendar, TemporalType> t2 = entry.getValue();
                q.setParameter(entry.getKey(), t2._1, t2._2);
            }
            for (Map.Entry<Integer, $.T2<Date, TemporalType>> entry : dateParams.entrySet()) {
                $.T2<Date, TemporalType> t2 = entry.getValue();
                q.setParameter(entry.getKey(), t2._1, t2._2);
            }
            for (Map.Entry<String, Object> entry : hints.entrySet()) {
                q.setHint(entry.getKey(), entry.getValue());
            }
        }
        return q;
    }

}
