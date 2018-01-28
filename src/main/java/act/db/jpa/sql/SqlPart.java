package act.db.jpa.sql;

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

import java.util.concurrent.atomic.AtomicInteger;

public interface SqlPart {

    /**
     * Print string representation to `StringBuilder`
     *
     * @param dialect
     *         SQL dialect
     * @param builder
     *         The StringBuilder
     * @param paramId
     *         The parameter Id counter
     * @param entityAliasPrefix
     *         The entity alias prefix, e.g. Apple's alias might be `a`
     */
    void print(SqlDialect dialect, StringBuilder builder, AtomicInteger paramId, String entityAliasPrefix);

    class Util {
        public static String entityAlias(String entityName) {
            return entityName.substring(0, 1);
        }
    }

}
