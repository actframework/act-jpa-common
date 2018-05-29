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
import act.app.DbServiceManager;
import act.util.Stateless;
import org.osgl.inject.NamedProvider;
import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;

import javax.inject.Provider;
import javax.persistence.EntityManager;

@Stateless
public class EntityManagerProvider implements
        NamedProvider<EntityManager>, Provider<EntityManager> {

    private static final Logger LOGGER = LogManager.get(EntityManagerProvider.class);

    private static final String DEFAULT = DbServiceManager.DEFAULT;

    @Override
    public EntityManager get() {
        return get(DEFAULT);
    }

    @Override
    public EntityManager get(String s) {
        return JPAContext.em(svc(s));
    }

    private JPAService svc(String s) {
        JPAService svc = Act.app().dbServiceManager().dbService(s);
        if (null == svc) {
            LOGGER.warn("service not found: %s; Will try load default service.");
            if (!DEFAULT.equalsIgnoreCase(s)) {
                return svc(DEFAULT);
            }
        }
        return svc;
    }

}
