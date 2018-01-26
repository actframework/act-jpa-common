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

public interface WhereComponent extends SqlPart {

    WhereComponent EMPTY = new WhereComponent() {
        @Override
        public WhereComponent not() {
            return EMPTY;
        }

        @Override
        public WhereComponent and(WhereComponent... otherWhereComponents) {
            return new GroupWhereComponent(LogicOperator.AND, otherWhereComponents);
        }

        @Override
        public WhereComponent or(WhereComponent... otherWhereComponents) {
            return new GroupWhereComponent(LogicOperator.OR, otherWhereComponents);
        }

        @Override
        public void print(SqlDialect dialect, StringBuilder builder, AtomicInteger paramId) {
        }

        @Override
        public void printWithLead(SqlDialect dialect, StringBuilder builder, AtomicInteger paramId) {
        }
    };

    void printWithLead(SqlDialect dialect, StringBuilder builder, AtomicInteger paramId);


    /**
     * Return negative expression of this where component
     *
     * @return the negative of this where component
     */
    WhereComponent not();

    /**
     * Return a {@link GroupWhereComponent} with the first
     * sub component be this where component followed by
     * `otherWhereComponents` concatenated by {@link LogicOperator#AND}
     *
     * @param otherWhereComponents other where components
     * @return an `AND` group component
     */
    WhereComponent and(WhereComponent... otherWhereComponents);


    /**
     * Return a {@link GroupWhereComponent} with the first
     * sub component be this where component followed by
     * `otherWhereComponents` concatenated by {@link LogicOperator#OR}
     *
     * @param otherWhereComponents other where components
     * @return an `OR` group component
     */
    WhereComponent or(WhereComponent... otherWhereComponents);
}
