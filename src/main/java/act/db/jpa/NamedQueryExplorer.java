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

import act.app.App;
import act.app.DbServiceManager;
import act.db.DbService;
import act.db.meta.EntityMetaInfoRepo;
import act.db.meta.MasterEntityMetaInfoRepo;

import java.util.Set;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

class NamedQueryExplorer {

    void explore(App app) {
        DbServiceManager mgr = app.dbServiceManager();
        MasterEntityMetaInfoRepo repo = app.entityMetaInfoRepo();
        for (DbService svc: mgr.registeredServices()) {
            String dbId = svc.id();
            EntityMetaInfoRepo repo0 = repo.forDb(dbId);
            if (null != repo0 && svc instanceof JPAService) {
                lookupIn((JPAService) svc, repo0);
            }
        }
    }

    private void lookupIn(JPAService svc, EntityMetaInfoRepo repo) {
        Set<Class> managedClasses = repo.entityClasses();
        for (Class c : managedClasses) {
            lookupIn(c, svc);
        }
    }

    private void lookupIn(Class<?> entityClass, JPAService svc) {
        if (null == entityClass || Object.class == entityClass) {
            return;
        }
        NamedQuery namedQuery = entityClass.getAnnotation(NamedQuery.class);
        if (null != namedQuery) {
            svc.registerNamedQuery(namedQuery);
        }
        NamedQueries namedQueries = entityClass.getAnnotation(NamedQueries.class);
        if (null != namedQueries) {
            for (NamedQuery nq : namedQueries.value()) {
                svc.registerNamedQuery(nq);
            }
        }
        lookupIn(entityClass.getSuperclass(), svc);
    }

}
