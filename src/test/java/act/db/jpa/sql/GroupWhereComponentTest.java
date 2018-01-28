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
import static act.db.jpa.sql.Operator.LTE;

import org.junit.Before;
import org.junit.Test;

public class GroupWhereComponentTest extends SqlPartTestBase {
    private WhereComponent comp1;
    private WhereComponent comp2;

    @Before
    public void prepare2() {
        comp1 = new SimpleWhereExpression("name", EQ);
        comp2 = new SimpleWhereExpression("age", LTE);
    }

    @Test
    public void test() {
        target = new GroupWhereComponent(LogicOperator.AND, comp1, comp2);
        shouldBe("U.name = ?1 AND U.age <= ?2");
    }

    @Test
    public void test2() {
        target = new GroupWhereComponent(LogicOperator.OR, comp1, comp2.not());
        shouldBe("U.name = ?1 OR NOT (U.age <= ?2)");
    }


}
