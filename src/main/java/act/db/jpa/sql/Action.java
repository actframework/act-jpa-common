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

import org.osgl.$;
import org.osgl.util.C;
import org.osgl.util.E;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Action implements SqlPart {
    protected final String entityName;

    public Action(String entityName) {
        this.entityName = $.notNull(entityName);
    }

    public static class Select extends Action {
        private List<String> columns;

        public Select(String entityName, List<String> columns) {
            super(entityName);
            this.columns = $.notNull(columns);
        }

        public Select(String entityName, String... columns) {
            super(entityName);
            this.columns = C.listOf(columns);
        }

        @Override
        public void print(SqlDialect dialect, StringBuilder builder, AtomicInteger paramId) {
            int len = columns.size();
            if (0 == len) {
                builder.append("FROM ").append(entityName);
            } else {
                builder.append("SELECT ").append(columns.get(0));
                for (int i = 1; i < len; ++i) {
                    builder.append(", ").append(columns.get(i));
                }
                builder.append(" FROM ").append(entityName);
            }
        }
    }

    public static class Delete extends Action {
        public Delete(String entityName) {
            super(entityName);
        }

        @Override
        public void print(SqlDialect dialect, StringBuilder builder, AtomicInteger paramId) {
            builder.append("DELETE FROM ").append(entityName);
        }
    }

    public static class Count extends Action {
        public Count(String entityName) {
            super(entityName);
        }

        @Override
        public void print(SqlDialect dialect, StringBuilder builder, AtomicInteger paramId) {
            builder.append("SELECT COUNT(*) FROM ").append(entityName);
        }
    }

    public static class Update extends Action {
        private List<String> columns;

        public Update(String entityName, List<String> columns) {
            super(entityName);
            E.illegalArgumentIf(columns.isEmpty(), "columns expected for Update action");
            this.columns = $.notNull(columns);
        }

        public Update(String entityName, String... columns) {
            super(entityName);
            E.illegalArgumentIf(0 == columns.length, "columns expected for Update action");
            this.columns = C.listOf(columns);
        }

        @Override
        public void print(SqlDialect dialect, StringBuilder builder, AtomicInteger paramId) {
            builder.append("UPDATE ").append(entityName);
            for (String column : columns) {
                builder.append(" SET ").append(column).append(" = ?").append(paramId.incrementAndGet());
            }
        }
    }
}
