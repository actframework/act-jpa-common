package act.db.jpa.util;

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
import act.db.DbManager;
import act.db.DbService;
import act.db.TimestampGenerator;
import act.db.meta.EntityClassMetaInfo;
import act.db.meta.EntityFieldMetaInfo;
import act.db.meta.EntityMetaInfoRepo;
import act.db.meta.MasterEntityMetaInfoRepo;
import act.util.Stateless;
import org.osgl.$;
import org.osgl.Osgl;
import org.osgl.util.E;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@Stateless
public class TimestampAuditor {

    private Map<Class, $.Visitor> createdAtLookup = new HashMap<>();
    private Map<Class, $.Visitor> lastModifiedAtLookup = new HashMap<>();

    public TimestampAuditor() {
        buildLookups();
    }

    @PrePersist
    public void prePersist(Object entity) {
        Class<?> entityType = entity.getClass();
        apply(createdAtLookup.get(entityType), entity);
        apply(lastModifiedAtLookup.get(entityType), entity);
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        Class<?> entityType = entity.getClass();
        apply(lastModifiedAtLookup.get(entityType), entity);
    }

    private void buildLookups() {
        App app = Act.app();
        MasterEntityMetaInfoRepo masterRepo = app.entityMetaInfoRepo();
        final DbManager dbManager = Act.dbManager();
        for (DbService db : app.dbServiceManager().registeredServices()) {
            EntityMetaInfoRepo repo = masterRepo.forDb(db.id());
            for (Class entityClass : repo.entityClasses()) {
                EntityClassMetaInfo classInfo = repo.classMetaInfo(entityClass);
                EntityFieldMetaInfo fieldInfo = classInfo.createdAtField();
                if (null != fieldInfo) {
                    final Field field = $.notNull($.fieldOf(entityClass, fieldInfo.fieldName()));
                    field.setAccessible(true);
                    createdAtLookup.put(entityClass, new TimestampFieldVisitor(field, dbManager));
                }
                fieldInfo = classInfo.lastModifiedAtField();
                if (null != fieldInfo) {
                    Field field = $.notNull($.fieldOf(entityClass, fieldInfo.fieldName()));
                    lastModifiedAtLookup.put(entityClass, new TimestampFieldVisitor(field, dbManager));
                }
            }
        }
    }

    private static void apply($.Visitor visitor, Object entity) {
        if (null != visitor) {
            visitor.visit(entity);
        }
    }

    private static class TimestampFieldVisitor extends $.Visitor {
        final TimestampGenerator tsGen;
        final Field field;
        public TimestampFieldVisitor(Field field, DbManager dbManager) {
            this.tsGen = dbManager.timestampGenerator(field.getType());
            field.setAccessible(true);
            this.field = field;
        }

        @Override
        public void visit(Object entity) throws Osgl.Break {
            try {
                field.set(entity, tsGen.now());
            } catch (IllegalAccessException e) {
                throw E.unexpected(e);
            }
        }
    }

}
