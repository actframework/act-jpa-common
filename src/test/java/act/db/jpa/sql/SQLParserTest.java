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

import static act.db.jpa.sql.SQL.Parser.parse;
import static act.db.jpa.sql.SQL.Type.FIND;

import org.junit.BeforeClass;
import org.junit.Test;
import osgl.ut.TestBase;

public class SQLParserTest extends TestBase {

    private SQL target;

    @BeforeClass
    public static void staticPrepare() {
        Operator.values();
    }

    @Test
    public void testEmptyExpression() {
        parseSelect("");
        eq("SELECT U FROM User U");
    }

    @Test
    public void testSingleField() {
        parseSelect("name");
        eq("SELECT U FROM User U  WHERE U.name = ?1");
    }

    @Test
    public void testMultipleFields() {
        parseSelect("name,age");
        eq("SELECT U FROM User U  WHERE U.name = ?1 AND U.age = ?2");
    }

    @Test
    public void testMultipleFieldsWithOp() {
        parseSelect("name like,age between,score <=,date gt");
        eq("SELECT U FROM User U  WHERE U.name LIKE ?1 AND U.age < ?2 AND U.age > ?3 AND U.score <= ?4 AND U.date > ?5");
    }

    @Test
    public void testRawJQL() {
        String raw = "select count(*) from User";
        parseSelect(raw);
        eq(raw);
    }

    private void parseSelect(String expression, String... columns) {
        target = parse(FIND, "User", expression, columns);
    }

    private void eq(String expected) {
        eq(expected, target.rawSql(DefaultSqlDialect.INSTANCE));
    }
}
