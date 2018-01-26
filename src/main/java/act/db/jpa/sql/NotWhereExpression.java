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

import java.util.concurrent.atomic.AtomicInteger;

public class NotWhereExpression extends WhereComponentBase {

    private WhereComponent target;

    public NotWhereExpression(WhereComponent target) {
        this.target = $.notNull(target);
    }

    @Override
    public void print(SqlDialect dialect, StringBuilder builder, AtomicInteger paramId) {
        builder.append("NOT (");
        target.print(dialect, builder, paramId);
        builder.append(")");
    }

    @Override
    public WhereComponent not() {
        return target;
    }

}
