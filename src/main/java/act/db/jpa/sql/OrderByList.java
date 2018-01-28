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
import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;
import org.osgl.util.E;
import org.osgl.util.S;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderByList implements SqlPart {

    public static final OrderByList EMPTY_LIST = new OrderByList();

    private static final Logger LOGGER = LogManager.get(OrderByList.class);

    private static final String LEADING = "ORDER BY ";
    private static final int LEADING_LEN = LEADING.length();
    private List<OrderByElement> list = new ArrayList<>();
    // keep track of all column names in the order by list
    private Map<String, Boolean> columns = new HashMap<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderByList that = (OrderByList) o;
        return Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return $.hc(list);
    }

    @Override
    public String toString() {
        int sz = list.size();
        switch (sz) {
            case 0:
                return "";
            case 1:
                return LEADING + list.get(0).toString();
            default:
                S.Buffer buf = S.buffer(LEADING).append(list.get(0));
                for (int i = 1; i < sz; ++i) {
                    buf.append(",").append(list.get(i));
                }
                return buf.toString();
        }
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public void print(SqlDialect dialect, StringBuilder builder, AtomicInteger paramId, String entityAliasPrefix) {
        int sz = list.size();
        if (0 == sz) {
            return;
        }
        list.get(0).print(dialect, builder, paramId, entityAliasPrefix);
        for (int i = 1; i < sz; ++i) {
            builder.append(", ");
            list.get(i).print(dialect, builder, paramId, entityAliasPrefix);
        }
    }

    public void printWithLead(SqlDialect dialect, StringBuilder builder, AtomicInteger paramId, String entityAliasPrefix) {
        if (!isEmpty()) {
            builder.append(" ORDER BY ");
            print(dialect, builder, paramId, entityAliasPrefix);
        }
    }

    public OrderByList merge(OrderByList list) {
        OrderByList newList = new OrderByList();
        newList.columns.putAll(this.columns);
        newList.list.addAll(this.list);
        for (OrderByElement element : list.list) {
            newList.add(element);
        }
        return newList;
    }

    private void add(OrderByElement element) {
        String column = element.column();
        Boolean b = columns.get(column);
        if (null != b) {
            E.illegalArgumentIf(b != element.isDescending(), "conflict element:" + column);
            LOGGER.warn("duplicate element: " + column);
            return;
        }
        columns.put(column, element.isDescending());
        list.add(element);
    }

    public static OrderByList parse(String orderBy) {
        OrderByList list = new OrderByList();
        orderBy = orderBy.trim();
        if (S.empty(orderBy)) {
            return list;
        }
        if (orderBy.toUpperCase().startsWith(LEADING)) {
            orderBy = orderBy.substring(LEADING_LEN);
        }
        String[] sa = orderBy.split(",");
        for (String s : sa) {
            list.add(new OrderByElement(s));
        }
        return list;
    }

}
