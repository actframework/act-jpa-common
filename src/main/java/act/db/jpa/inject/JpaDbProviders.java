package act.db.jpa.inject;

/*-
 * #%L
 * ACT JPA Common Module
 * %%
 * Copyright (C) 2018 - 2019 ActFramework
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
import act.db.jpa.JPAService;
import act.db.sql.SqlDbService;
import org.osgl.inject.Genie;
import org.osgl.inject.NamedProvider;

import javax.inject.Provider;

public class JpaDbProviders {
    private static Provider<JPAService> SQL_JPA_SVC_PROVIDER = new Provider<JPAService>() {
        @Override
        public JPAService get() {
            return NAMED_JPA_DB_SVC_PROVIDER.get(DB.DEFAULT);
        }
    };

    private static NamedProvider<JPAService> NAMED_JPA_DB_SVC_PROVIDER = new NamedProvider<JPAService>() {
        @Override
        public JPAService get(String name) {
            return Act.app().dbServiceManager().dbService(name);
        }
    };

    public static void classInit(App app) {
        Genie genie = app.getInstance(Genie.class);
        genie.registerProvider(JPAService.class, SQL_JPA_SVC_PROVIDER);
        genie.registerNamedProvider(JPAService.class, NAMED_JPA_DB_SVC_PROVIDER);
    }
}
