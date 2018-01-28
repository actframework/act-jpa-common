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

import static act.db.jpa.sql.Operator.*;

import org.junit.Test;

public class OperatorTest extends SqlPartTestBase {

    @Test
    public void testEq() {
        print(EQ);
        eq("U.name = ?1");
    }

    @Test
    public void testNE() {
        print(NE);
        eq("U.name <> ?1");
    }

    @Test
    public void testNotNull() {
        print(NOT_NULL);
        eq("U.name IS NOT NULL");
    }

    @Test
    public void testIsNull() {
        print(IS_NULL);
        eq("U.name IS NULL");
    }

    @Test
    public void testLessThan() {
        print(LT);
        eq("U.name < ?1");
    }

    @Test
    public void testLessThanOrEqualTo() {
        print(LTE);
        eq("U.name <= ?1");
    }

    @Test
    public void testGreaterThan() {
        print(GT);
        eq("U.name > ?1");
    }

    @Test
    public void testGreaterThanOrEqualTo() {
        print(GTE);
        eq("U.name >= ?1");
    }

    @Test
    public void testBetween() {
        print(BETWEEN);
        eq("U.name < ?1 AND U.name > ?2");
    }

    @Test
    public void testLike() {
        print(LIKE);
        eq("U.name LIKE ?1");
    }

    @Test
    public void testCaseInsensitiveLike() {
        print(ILIKE);
        eq("LOWER(U.name) LIKE LOWER(CONCAT('%', ?1, '%'))");
    }

    private void print(Operator op) {
        op.print(dialect, buf, "name", paramCounter, entityAliasPrefix);
    }

}
