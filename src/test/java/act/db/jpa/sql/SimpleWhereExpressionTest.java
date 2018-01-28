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

import static act.db.jpa.sql.Operator.EQ;

import org.junit.Test;

public class SimpleWhereExpressionTest extends SqlPartTestBase {

    SimpleWhereExpression exp;

    @Test
    public void testEq() {
        exp = new SimpleWhereExpression("name", EQ);
        exp.print(DefaultSqlDialect.INSTANCE, buf, paramCounter, entityAliasPrefix);
        eq("U.name = ?1");
    }

}
