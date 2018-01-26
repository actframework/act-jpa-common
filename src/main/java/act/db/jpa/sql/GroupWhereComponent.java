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

public class GroupWhereComponent extends WhereComponentBase {
    private List<WhereComponent> subComponents;
    private LogicOperator op;

    public GroupWhereComponent(LogicOperator operator, List<WhereComponent> subComponents) {
        E.illegalArgumentIf(subComponents.isEmpty(), "sub components cannot be empty");
        this.subComponents = $.notNull(subComponents);
        this.op = $.notNull(operator);
    }

    public GroupWhereComponent(LogicOperator operator, WhereComponent... subComponents) {
        E.illegalArgumentIf(0 == subComponents.length, "sub components cannot be empty");
        this.subComponents = C.listOf(subComponents);
        this.op = $.notNull(operator);
    }

    @Override
    public void print(SqlDialect dialect, StringBuilder builder, AtomicInteger paramId) {
        WhereComponent first = subComponents.get(0);
        first.print(dialect, builder, paramId);
        int sz = subComponents.size();
        for (int i = 1; i < sz; ++i) {
            op.print(builder);
            subComponents.get(i).print(dialect, builder, paramId);
        }
    }

    private void printSubComponent(SqlDialect dialect, WhereComponent subComponent, StringBuilder builder, AtomicInteger paramId) {
        boolean subIsGroup = subComponent instanceof GroupWhereComponent;
        if (subIsGroup) {
            builder.append("(");
        }
        subComponent.print(dialect, builder, paramId);
        if (subIsGroup) {
            builder.append(")");
        }
    }
}
