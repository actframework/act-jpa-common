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

import static act.db.jpa.sql.OrderByList.parse;

import org.junit.Test;
import osgl.ut.TestBase;

public class OrderByListTest extends TestBase {
    OrderByList list;

    @Test
    public void testParseEmptyString() {
        list = parse("");
        eq("", list.toString());
    }

    @Test
    public void testParseSingleElement() {
        list = parse("-name");
        eq("ORDER BY name DESC", list.toString());

        list = parse("name asc");
        eq("ORDER BY name", list.toString());
    }

    @Test
    public void testParseMultipleElements() {
        list = parse("-name,+age");
        eq("ORDER BY name DESC,age", list.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalidOrderStr() {
        parse("name,-age abc");
    }
}
