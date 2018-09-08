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

import static act.Act.app;
import static act.app.DbServiceManager.DEFAULT;
import static act.db.jpa.sql.SQL.Type.*;

import act.Act;
import act.app.ActionContext;
import act.app.DbServiceManager;
import act.db.DB;
import act.db.DaoBase;
import act.db.DbService;
import act.db.Model;
import act.db.jpa.sql.SQL;
import act.inject.param.NoBind;
import act.util.General;
import org.osgl.$;
import org.osgl.util.E;
import org.osgl.util.S;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;

@General
@NoBind
public class JPADao<ID_TYPE, MODEL_TYPE> extends DaoBase<ID_TYPE, MODEL_TYPE, JPAQuery<MODEL_TYPE>> {

    private volatile JPAService _jpa;
    protected String entityName;
    protected String createdColumn;
    protected String lastModifiedColumn;
    protected String idColumn;
    protected Field idField;
    private String qIdList;

    public JPADao(Class<ID_TYPE> idType, Class<MODEL_TYPE> modelType, JPAService jpa) {
        super(idType, modelType);
        setJPAService(jpa);
    }

    public JPADao() {
        DB db = modelType().getAnnotation(DB.class);
        String svcId = null == db ? DEFAULT : db.value();
        DbServiceManager dbm = Act.app().dbServiceManager();
        DbService dbService = dbm.dbService(svcId);
        E.invalidConfigurationIf(null == dbService, "cannot find db service by id: %s", svcId);
        E.unexpectedIfNot(dbService instanceof JPAService, "expected JPAService, found: " + dbService.getClass().getSimpleName());
        setJPAService((JPAService) dbService);
    }

    public JPAService jpa() {
        if (null != _jpa) {
            return _jpa;
        }
        synchronized (this) {
            if (null == _jpa) {
                DB db = modelType().getAnnotation(DB.class);
                String dbId = null == db ? DEFAULT : db.value();
                _jpa = app().dbServiceManager().dbService(dbId);
            }
        }
        return _jpa;
    }

    @Override
    public MODEL_TYPE findById(ID_TYPE id) {
        return emForRead().find(modelClass, id);
    }

    @Override
    public MODEL_TYPE findLatest() {
        E.unsupportedIf(null == createdColumn, "no CreatedAt column defined");
        return q().orderBy(createdColumn).first();
    }

    @Override
    public MODEL_TYPE findLastModified() {
        E.unsupportedIf(null == lastModifiedColumn, "no LastModifiedAt column defined");
        return q().orderBy(lastModifiedColumn).first();
    }

    @Override
    public Iterable<MODEL_TYPE> findBy(String expression, Object... values) throws IllegalArgumentException {
        return q(expression, values).fetch();
    }

    @Override
    public MODEL_TYPE findOneBy(String expression, Object... values) throws IllegalArgumentException {
        return q(expression, values).first();
    }

    @Override
    public Iterable<MODEL_TYPE> findByIdList(Collection<ID_TYPE> idList) {
        return q(qIdList, idList).fetch();
    }

    @Override
    public MODEL_TYPE reload(MODEL_TYPE entity) {
        emForRead().refresh(entity);
        return entity;
    }

    @Override
    public ID_TYPE getId(MODEL_TYPE entity) {
        if (entity instanceof Model) {
            return $.cast(((Model) entity)._id());
        }
        return null == idField ? null : (ID_TYPE) $.getFieldValue(entity, idField);
    }

    @Override
    public long countBy(String expression, Object... values) throws IllegalArgumentException {
        return createCountQuery(expression, values).count();
    }

    @Override
    public long count() {
        return q(COUNT).count();
    }

    @Override
    public MODEL_TYPE save(MODEL_TYPE entity) {
        EntityManager em = emForWrite();
        _save(entity, em);
        return entity;
    }

    private void _save(MODEL_TYPE entity, EntityManager em) {
        if (!em.contains(entity)) {
            em.persist(entity);
        } else {
            em.merge(entity);
        }
    }

    @Override
    public void save(MODEL_TYPE entity, String fieldList, Object... values) {
        prepareForWrite();
        values = $.concat(values, getId(entity));
        JPAQuery<MODEL_TYPE> q = createUpdateQuery(fieldList, idColumn, values);
        q.executeUpdate();
    }

    @Override
    public List<MODEL_TYPE> save(Iterable<MODEL_TYPE> entities) {
        List<MODEL_TYPE> list = new ArrayList<>();
        EntityManager em = emForWrite();
        _save(entities, list, em);
        return list;
    }

    private void _save(Iterable<MODEL_TYPE> entities, List<MODEL_TYPE> list, EntityManager em) {
        int count = 0;
        for (MODEL_TYPE entity : entities) {
            _save(entity, em);
            // TODO: make `20` configurable
            if (++count % 20 == 0) {
                em.flush();
                em.clear();
            }
            list.add(entity);
        }
    }

    @Override
    public void delete(MODEL_TYPE entity) {
        EntityManager em = emForWrite();
        em.remove(entity);
        em.flush();
    }

    @Override
    public void delete(JPAQuery<MODEL_TYPE> query) {
        query.delete();
    }

    @Override
    public void deleteById(ID_TYPE id) {
        delete(q(DELETE, idColumn, id));
    }

    @Override
    public void deleteBy(String expression, Object... values) throws IllegalArgumentException {
        delete(q(DELETE, expression, values));
    }

    @Override
    public void deleteAll() {
        q(DELETE).delete();
    }

    @Override
    public void drop() {
        deleteAll();
    }

    @Override
    public JPAQuery<MODEL_TYPE> q() {
        return q(FIND);
    }

    public JPAQuery<MODEL_TYPE> q(SQL.Type type) {
        return q(type, "");
    }

    @Override
    public JPAQuery<MODEL_TYPE> createQuery() {
        return q();
    }

    @Override
    public JPAQuery<MODEL_TYPE> q(String expression, Object... values) {
        return q(FIND, expression, values);
    }

    public JPAQuery<MODEL_TYPE> q(SQL.Type type, String expression, Object... values) {
        E.unsupportedIf(SQL.Type.UPDATE == type, "UPDATE not supported in q() API");
        JPAService jpa = jpa();

        JPAQuery<MODEL_TYPE> q = new JPAQuery<>(jpa, em(jpa, readOnly(type)), modelClass, type, expression);
        int len = values.length;
        for (int i = 0; i < len; ++i) {
            q.setParameter(i + 1, values[i]);
        }
        return q;
    }


    /**
     * Create a Find query.
     *
     * E.g.
     *
     * ```
     * List<User> users = userDao.createQuery("age <,gender", ageLimit, Gender.MALE).fetch
     * ```
     *
     * @param expression the expression to filter records to be selected.
     * @param values the parameter values used in the expression
     * @return a Find query
     */
    @Override
    public JPAQuery<MODEL_TYPE> createQuery(String expression, Object... values) {
        return q(expression, values);
    }

    public JPAQuery<MODEL_TYPE> createFindQuery(String expression, Object... values) {
        return q(FIND, expression, values);
    }

    public JPAQuery<?> createFindQuery(String fieldList, String expression, Object... values) {
        String[] columns = fieldList.split(S.COMMON_SEP);
        JPAService jpa = jpa();
        JPAQuery<?> q = new JPAQuery<>(jpa, em(jpa, true), modelClass, SQL.Type.FIND, expression, columns);
        int len = values.length;
        for (int i = 0; i < len; ++i) {
            q.setParameter(i + 1, values[i]);
        }
        return q;
    }

    /**
     * Create a delete query.
     * @param expression the expression to filter the records to be deleted.
     * @param values the parameter values used in the expression
     * @return a delete query
     */
    public JPAQuery<MODEL_TYPE> createDeleteQuery(String expression, Object... values) {
        return q(DELETE, expression, values);
    }

    public JPAQuery<MODEL_TYPE> createUpdateQuery(String fieldList, String expression, Object... values) {
        String[] columns = fieldList.split(S.COMMON_SEP);
        JPAService jpa = jpa();
        JPAQuery<MODEL_TYPE> q = new JPAQuery<>(jpa, JPAContext.em(jpa, false), modelClass, SQL.Type.UPDATE, expression, columns);
        int len = values.length;
        for (int i = 0; i < len; ++i) {
            q.setParameter(i + 1, values[i]);
        }
        return q;
    }

    public JPAQuery<MODEL_TYPE> createCountQuery(String expression, Object... values) {
        return q(COUNT, expression, values);
    }

    public EntityManager em() {
        return emForWrite();
    }

    public EntityManager emForRead() {
        ActionContext ctx = ActionContext.current();
        boolean readWrite = null != ctx && ctx.req().method().unsafe();
        return JPAContext.em(jpa(), !readWrite);
    }

    private void prepareForWrite() {
        emForWrite();
    }

    EntityManager emForWrite() {
        return JPAContext.emWithTx(jpa());
    }

    void setJPAService(JPAService jpa) {
        Class<MODEL_TYPE> modelType = modelType();
        this.entityName = jpa.entityName(modelType);
        this.createdColumn = jpa.createdColumn(modelType);
        this.lastModifiedColumn = jpa.lastModifiedColumn(modelType);
        this.idColumn = jpa.idColumn(modelType);
        this.qIdList = S.fmt("%s in ", idColumn);
        this.idField = jpa.idField(modelType);
        this._jpa = jpa;
    }

    private void apply($.Visitor<MODEL_TYPE> visitor, MODEL_TYPE entity) {
        if (null != visitor) {
            visitor.apply(entity);
        }
    }

    private EntityManager em(JPAService jpa, boolean readOnly) {
        return readOnly ? JPAContext.em(jpa, true) : JPAContext.emWithTx(jpa);
    }

    public static boolean readOnly(SQL.Type type) {
        ActionContext actionContext = ActionContext.current();
        if (null != actionContext && actionContext.req().method().unsafe()) {
            return false;
        }
        return type.readOnly();
    }

}
