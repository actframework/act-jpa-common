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

import act.Act;
import act.app.App;
import act.db.DB;
import act.db.Dao;
import act.db.DbService;
import act.db.jpa.sql.DefaultSqlDialect;
import act.db.jpa.sql.SQL;
import act.db.jpa.sql.SqlDialect;
import act.db.meta.EntityClassMetaInfo;
import act.db.meta.EntityFieldMetaInfo;
import act.db.meta.EntityMetaInfoRepo;
import act.db.sql.DataSourceConfig;
import act.db.sql.SqlDbService;
import org.osgl.$;
import org.osgl.util.C;
import org.osgl.util.E;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.persistence.*;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

public abstract class JPAService extends SqlDbService {

    protected EntityMetaInfoRepo entityMetaInfoRepo;
    // map sql expression to SQL instance
    protected ConcurrentMap<SQLKey, SQL> sqlCache = new ConcurrentHashMap<>();

    private Map<String, NamedQuery> namedQueries = new HashMap<>();

    EntityManagerFactory emFactory;

    public JPAService(final String dbId, final App app, final Map<String, String> config) {
        super(dbId, app, config);
    }

    @Override
    protected void dataSourceProvided(DataSource dataSource, DataSourceConfig dataSourceConfig) {
        super.dataSourceProvided(dataSource, dataSourceConfig);
        String dbId = id();
        entityMetaInfoRepo = app().entityMetaInfoRepo().forDb(dbId);
        this.emFactory = createEntityManagerFactory(dbId, dataSource);
    }

    @Override
    protected DataSource createDataSource() {
        throw E.unsupport();
    }

    @Override
    protected boolean supportDdl() {
        return true;
    }

    @Override
    public <DAO extends Dao> DAO defaultDao(Class<?> modelType) {
        Class<?> idType = findModelIdTypeByAnnotation(modelType, Id.class);
        E.illegalArgumentIf(null == idType, "Cannot find out Dao for model type[%s]: unable to identify the ID type", modelType);
        return (DAO)newDao(idType, modelType);
    }

    @Override
    public <DAO extends Dao> DAO newDaoInstance(Class<DAO> aClass) {
        E.illegalArgumentIfNot(isValidDaoClass(aClass), "The class is not a JPA Dao: %s", aClass);
        JPADao dao = $.cast(app().getInstance(aClass));
        dao.setJPAService(this);
        return $.cast(dao);
    }

    @Override
    public Class<? extends Annotation> entityAnnotationType() {
        return Entity.class;
    }

    public SQL getSQL(SQL.Type type, Class entityClass, String expression, String... columns) {
        SQLKey key = new SQLKey(type, entityClass, expression, columns);
        SQL sql = sqlCache.get(key);
        if (null == sql) {
            String entityName = entityName(entityClass);
            E.illegalArgumentIf(null == entityName, "cannot find entity name for " + entityClass);
            sql = SQL.Parser.parse(type, entityName, expression, columns);
            sqlCache.putIfAbsent(key, sql);
        }
        return sql;
    }

    // --- Implement SqlDialect

    // --- Eof Implement SqlDialect

    /**
     * Create an new Dao for given id type and model type.
     *
     * @param idType
     *      the type of the ID field
     * @param modelType
     *      the type of the model
     * @param <ID_TYPE>
     *      the generic type of the ID class
     * @param <MODEL_TYPE>
     *      the generic type of the model class
     * @return
     *      an new Dao instance
     */
    protected <ID_TYPE, MODEL_TYPE> JPADao<ID_TYPE, MODEL_TYPE> newDao(Class<ID_TYPE> idType, Class<MODEL_TYPE> modelType) {
        return new JPADao<>(idType, modelType, this);
    }

    /**
     * Returns the base class of the Dao supported by the JPA service.
     * @return the Dao base class
     */
    protected Class<? extends JPADao> baseDaoClass() {
        return JPADao.class;
    }

    /**
     * Returns the class of a {@link javax.persistence.spi.PersistenceProvider}
     * implementation.
     *
     * For example a `HibernateService` shall return `HibernatePersistenceProvider`.
     *
     * @return the persistence provider class
     */
    protected abstract Class<? extends PersistenceProvider> persistenceProviderClass();

    /**
     * Create an {@link EntityManagerFactory} using {@link PersistenceUnitInfo}.
     * @param persistenceUnitInfo the {@link PersistenceUnitInfo}
     * @return an {@link EntityManagerFactory} instance.
     */
    protected abstract EntityManagerFactory createEntityManagerFactory(PersistenceUnitInfo persistenceUnitInfo);

    protected <DAO extends Dao> boolean isValidDaoClass(Class<DAO> aClass) {
        return baseDaoClass().isAssignableFrom(aClass);
    }

    /**
     * Sub class shall fill in specific properties that are required to build the
     * {@link PersistenceUnitInfo}.
     *
     * For example, a hibernate service might have the following code:
     *
     * ```java
     * properties.put("javax.persistence.provider", "org.hibernate.ejb.HibernatePersistence");
     * properties.put(org.hibernate.jpa.AvailableSettings.LOADED_CLASSES, managedClasses);
     * properties.put("hibernate.dialect", getDefaultDialect(dbConfig, dbConfig.get("db.driver")));
     * ...
     * return properties;
     * ```
     *
     * @param properties the properties contains common JPA configuration
     * @return the properties with specific service configurations added in.
     */
    protected Properties processProperties(Properties properties) {
        return properties;
    }

    protected List<Class> entityClasses() {
        return C.list(entityMetaInfoRepo.entityClasses());
    }

    protected void registerNamedQuery(NamedQuery nq) {
        namedQueries.put(nq.name(), nq);
    }

    protected NamedQuery namedQuery(String name) {
        return namedQueries.get(name);
    }

    String lastModifiedColumn(Class<?> modelClass) {
        EntityFieldMetaInfo fieldInfo = classInfo(modelClass).lastModifiedAtField();
        return null == fieldInfo ? null : fieldInfo.columnName();
    }

    String createdColumn(Class<?> modelClass) {
        EntityFieldMetaInfo fieldInfo = classInfo(modelClass).createdAtField();
        return null == fieldInfo ? null : fieldInfo.columnName();
    }

    String idColumn(Class<?> modelClass) {
        EntityFieldMetaInfo fieldInfo = classInfo(modelClass).idField();
        return null == fieldInfo ? null : fieldInfo.columnName();
    }

    Field idField(Class<?> modelClass) {
        EntityFieldMetaInfo fieldInfo = classInfo(modelClass).idField();
        return null == fieldInfo ? null : $.fieldOf(modelClass, fieldInfo.fieldName());
    }

    String columnName(Field field) {
        return classInfo(field.getDeclaringClass()).fieldInfo(field.getName()).columnName();
    }

    String entityName(Class<?> modelClass) {
        return classInfo(modelClass).entityName();
    }

    SqlDialect dialect() {
        return DefaultSqlDialect.INSTANCE;
    }

    EntityManager createEntityManager() {
        return emFactory.createEntityManager();
    }

    private EntityClassMetaInfo classInfo(Class<?> modelClass) {
        return entityMetaInfoRepo.classMetaInfo(modelClass);
    }

    private EntityManagerFactory createEntityManagerFactory(String dbName, DataSource dataSource) {
        PersistenceUnitInfoImpl persistenceUnitInfo = persistenceUnitInfo(dbName);
        persistenceUnitInfo.setNonJtaDataSource(dataSource);
        return createEntityManagerFactory(persistenceUnitInfo);
    }

    private PersistenceUnitInfoImpl persistenceUnitInfo(String dbName) {
        Properties properties = properties();
        properties = processProperties(properties);
        List<Class> managedClasses = C.list(entityMetaInfoRepo.entityClasses());
        return persistenceUnitInfo(
                persistenceProviderClass().getName(),
                dbName, managedClasses, mappingFiles(), properties);
    }

    private Properties properties() {
        Properties properties = new Properties();
        properties.putAll(config.rawConf);
        properties.put("javax.persistence.transaction", "RESOURCE_LOCAL");
        return properties;
    }

    private List<String> mappingFiles() {
        String mappingFiles = config.rawConf.get(JPAPlugin.CONF_MAPPING_FILES);
        return null == mappingFiles ? C.<String>list() : C.list(mappingFiles);
    }

    private PersistenceUnitInfoImpl persistenceUnitInfo(
            String persistenceProviderClass,
            String persistenceUnitName,
            List<Class> managedClasses,
            List<String> mappingFileNames,
            Properties properties
    ) {
        return new PersistenceUnitInfoImpl(persistenceProviderClass,
                persistenceUnitName, managedClasses, mappingFileNames, properties, app().classLoader());
    }

    private void postDaoConstructor(JPADao dao) {
        E.tbd("Set JPA context to dao");
    }

    public static JPAService findByModelClass(Class<?> modelClass) {
        DB db = modelClass.getAnnotation(DB.class);
        String serviceId = null == db ? DB.DEFAULT : db.value();
        DbService service = Act.app().dbServiceManager().dbService(serviceId);
        return service instanceof JPAService ? (JPAService) service : null;
    }

    private static class SQLKey {
        private Class entityClass;
        private String expression;
        private SQL.Type type;
        private List<String> columns;

        public SQLKey(SQL.Type type, Class entityClass, String expression, String... columns) {
            this.entityClass = entityClass;
            this.expression = expression;
            this.type = type;
            this.columns = C.listOf(columns);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SQLKey sqlKey = (SQLKey) o;
            return type == sqlKey.type &&
                    $.eq(expression, sqlKey.expression) &&
                    $.eq(columns, sqlKey.columns) &&
                    $.eq(entityClass, sqlKey.entityClass);
        }

        @Override
        public int hashCode() {

            return $.hc(entityClass, expression, type, columns);
        }
    }

}
