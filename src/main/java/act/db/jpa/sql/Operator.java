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

/**
 * SQL where clause component operator.
 */
public enum Operator {
    EQ("=", "eq"),
    NE("<>", "ne", "neq"),
    NOT_NULL("not_null", "notNull") {
        @Override
        public void print(SqlDialect dialect, StringBuilder build, String column, AtomicInteger ordinalId, String entityAliasPrefix) {
            build.append(entityAliasPrefix).append(column).append(" IS NOT NULL");
        }
    },
    IS_NULL("is_null", "isNull") {
        @Override
        public void print(SqlDialect dialect, StringBuilder build, String column, AtomicInteger ordinalId, String entityAliasPrefix) {
            build.append(entityAliasPrefix).append(column).append(" IS NULL");
        }
    },
    LT("<", "lt", "lessThan", "less_than"),
    LTE("<=", "lte", "lessThanOrEqualTo", "less_than_or_equal_to"),
    GT(">", "gt", "greaterThan", "greater_than"),
    GTE(">=", "gte", "greaterThanOrEqualTo", "greater_than_or_equal_to"),
    BETWEEN("between") {
        @Override
        public void print(SqlDialect dialect, StringBuilder build, String column, AtomicInteger ordinalId, String entityAliasPrefix) {
            build.append(entityAliasPrefix).append(column).append(" < ?").append(ordinalId.incrementAndGet()).append(" AND ").append(column).append(" > ?").append(ordinalId.incrementAndGet());
        }
    },
    LIKE("like") {
        @Override
        public void print(SqlDialect dialect, StringBuilder build, String column, AtomicInteger ordinalId, String entityAliasPrefix) {
            build.append(entityAliasPrefix).append(column).append(" LIKE ?").append(ordinalId.incrementAndGet());
        }
    },
    ILIKE("ilike") {
        @Override
        public void print(SqlDialect dialect, StringBuilder build, String column, AtomicInteger ordinalId, String entityAliasPrefix) {
            String fnLower = dialect.lowerCaseFunction();
            build.append(fnLower).append("(").append(entityAliasPrefix).append(column).append(") LIKE ")
                    .append(fnLower).append("(CONCAT('%', ?")
                    .append(ordinalId.incrementAndGet()).append(", '%'))");
        }
    };

    private String op;
    Operator(String op, String... aliases) {
        this.op = op;
        SQL.registerOperator(op, this);
        for (String alias : aliases) {
            SQL.registerOperator(alias, this);
        }
    }

    public void print(SqlDialect dialect, StringBuilder build, String column, AtomicInteger ordinalId, String entityAliasPrefix) {
        build.append(entityAliasPrefix).append(column).append(" ").append(op).append(" ?").append(ordinalId.incrementAndGet());
    }
}
