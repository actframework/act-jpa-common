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

import org.junit.Test;

public class OrderByElementTest extends SqlPartTestBase {

    @Test
    public void testNormal() {
        target = new OrderByElement("name");
        shouldBe("U.name");
    }

    @Test
    public void testNormalWithAsc() {
        target = new OrderByElement("+name");
        shouldBe("U.name");
        prepare();
        target = new OrderByElement("name asc");
        shouldBe("U.name");
        prepare();
        target = new OrderByElement("name ASC");
        shouldBe("U.name");
    }

    @Test
    public void testNormalWithDesc() {
        target = new OrderByElement("-name");
        shouldBe("U.name DESC");
        prepare();
        target = new OrderByElement("name desc");
        shouldBe("U.name DESC");
        prepare();
        target = new OrderByElement("name DESC");
        shouldBe("U.name DESC");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooManyWords() {
        new OrderByElement("name asc abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownDir() {
        new OrderByElement("name xyz");
    }
}
