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
import org.osgl.util.E;
import org.osgl.util.S;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderByElement implements SqlPart {

    private boolean descending;
    private String column;

    public OrderByElement(String column) {
        E.illegalArgumentIf(S.blank(column), "column required");
        column = column.trim();
        if (column.startsWith("+")) {
            column = column.substring(1).trim();
        } else if (column.startsWith("-")) {
            column = column.substring(1).trim();
            this.descending = true;
        }
        if (column.contains(" ")) {
            List<String> list = S.fastSplit(column, " ");
            E.illegalArgumentIf(list.size() > 2, "Invalid order element: " + column);
            String dir = list.get(1).toLowerCase();
            if ("desc".equals(dir)) {
                descending = true;
            } else if ("asc".equals(dir)) {
                descending = false;
            } else {
                throw new IllegalArgumentException("Unknown order dir in order element: " + column);
            }
            column = list.get(0);
        }
        this.column = column;
    }

    public String column() {
        return column;
    }

    public boolean isDescending() {
        return descending;
    }

    @Override
    public int hashCode() {
        return $.hc(column, descending);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderByElement orderBy = (OrderByElement) o;
        return descending == orderBy.descending &&
                $.eq(column, orderBy.column);
    }

    @Override
    public String toString() {
        return descending ? column + " DESC" : column;
    }

    @Override
    public void print(SqlDialect dialect, StringBuilder builder, AtomicInteger paramId) {
        builder.append(column);
        if (descending) {
            builder.append(" DESC");
        }
    }

    OrderByElement descending() {
        this.descending = true;
        return this;
    }

    OrderByElement ascending() {
        this.descending = false;
        return this;
    }
}
