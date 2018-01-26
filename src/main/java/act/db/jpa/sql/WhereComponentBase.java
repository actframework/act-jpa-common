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

import org.osgl.util.C;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class WhereComponentBase implements WhereComponent {

    @Override
    public void printWithLead(SqlDialect dialect, StringBuilder builder, AtomicInteger paramId) {
        builder.append(" WHERE ");
        print(dialect, builder, paramId);
    }

    @Override
    public WhereComponent not() {
        return new NotWhereExpression(this);
    }

    @Override
    public WhereComponent and(WhereComponent... otherWhereComponents) {
        return new GroupWhereComponent(LogicOperator.AND, C.newListOf(otherWhereComponents).prepend(this));
    }

    @Override
    public WhereComponent or(WhereComponent... otherWhereComponents) {
        return new GroupWhereComponent(LogicOperator.OR, C.newListOf(otherWhereComponents).prepend(this));
    }
}
