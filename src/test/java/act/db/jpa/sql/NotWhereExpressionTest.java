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

import static act.db.jpa.sql.Operator.BETWEEN;
import static act.db.jpa.sql.Operator.EQ;

import org.junit.Before;
import org.junit.Test;

public class NotWhereExpressionTest extends SqlPartTestBase {

    WhereComponent component;

    @Before
    public void prepare2() {
        component = new SimpleWhereExpression("name", EQ);
    }

    @Test
    public void test() {
        target = new NotWhereExpression(component);
        shouldBe("NOT (U.name = ?1)");
    }

    @Test
    public void testNotNot() {
        target = new NotWhereExpression(component).not();
        shouldBe("U.name = ?1");
    }

    @Test
    public void testNotBetween() {
        target = new NotWhereExpression(new SimpleWhereExpression("name", BETWEEN));
        shouldBe("NOT (U.name < ?1 AND U.name > ?2)");
    }

}
