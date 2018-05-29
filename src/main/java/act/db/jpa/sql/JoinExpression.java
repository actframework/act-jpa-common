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

import org.osgl.util.S;

import java.util.concurrent.atomic.AtomicInteger;

public class JoinExpression implements SqlPart {

    private String joinTarget;

    private String onPrefix;

    private String onMyField;

    private String onField;

    public JoinExpression(String joinTarget) {
        this.joinTarget = S.requireNotBlank(joinTarget).trim();
    }

    public JoinExpression on(String modelPrefix, String field, String myField) {
        this.onPrefix = S.ensure(S.requireNotBlank(modelPrefix).trim()).endWith(".");
        this.onField = S.requireNotBlank(field).trim();
        if (S.notBlank(myField)) {
            this.onMyField = myField.trim();
        } else {
            this.onMyField = onField;
        }
        return this;
    }

    @Override
    public void print(SqlDialect dialect, StringBuilder builder, AtomicInteger paramId, String entityAliasPrefix) {
        builder.append(" JOIN ")
                .append(entityAliasPrefix)
                .append(joinTarget)
                .append(" ")
                .append(joinTarget);
        if (null != onPrefix) {
            builder.append(" ON ")
                    .append(joinTarget).append(".").append(onMyField)
                    .append(" = ").append(onPrefix).append(onField);
        }
    }
}
