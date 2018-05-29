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
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.S;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SQL {

    private static final Logger LOGGER = LogManager.get(SQL.class);

    public enum Type {
        FIND() {
            @Override
            protected Builder startParsing(String entity, String... columns) {
                return new SQL.Builder().select(entity, columns);
            }

            @Override
            public boolean readOnly() {
                return true;
            }
        },
        COUNT() {
            @Override
            protected Builder startParsing(String entity, String... columns) {
                return new SQL.Builder().count(entity);
            }

            @Override
            public boolean readOnly() {
                return true;
            }
        },
        UPDATE() {
            @Override
            protected Builder startParsing(String entity, String... columns) {
                return new SQL.Builder().update(entity, columns);
            }

            @Override
            public boolean readOnly() {
                return false;
            }
        },
        DELETE() {
            @Override
            protected Builder startParsing(String entity, String... columns) {
                return new SQL.Builder().delete(entity);
            }

            @Override
            public boolean readOnly() {
                return false;
            }
        };
        protected abstract Builder startParsing(String entity, String... columns);
        public abstract boolean readOnly();
    }

    private String entityName;
    private String entityAliasPrefix;
    private Action action;
    private WhereComponent where;
    private OrderByList orderBy;
    private List<JoinExpression> joins = C.list();
    private String rawSql;

    private SQL(String entityName, Action action, List<JoinExpression> joins, WhereComponent where, OrderByList orderBy) {
        this.entityName = entityName;
        this.entityAliasPrefix = SqlPart.Util.entityAlias(entityName) + ".";
        this.action = action;
        this.joins = joins;
        this.where = where;
        this.orderBy = orderBy;
    }

    private SQL(SQL copy) {
        this(copy.entityName, copy.action, copy.joins, copy.where, copy.orderBy);
    }

    private SQL(String rawSql) {
        this.rawSql = $.requireNotNull(rawSql);
    }

    /**
     * Returns an new SQL created from this SQL with additional order by list.
     * If the order list is empty then return this SQL instance.
     * @param orderByList
     *      the order by list
     * @return an new SQL instance created from this SQL with order by list specified
     */
    public SQL withOrderBy(String... orderByList) {
        if (null == orderByList) {
            return this;
        }
        OrderByList list = Parser.parseOrderBy(orderByList);
        if (list.isEmpty()) {
            return this;
        }
        SQL sql = new SQL(this);
        if (null == sql.orderBy) {
            sql.orderBy = list;
        } else {
            sql.orderBy = sql.orderBy.merge(list);
        }
        return sql;
    }

    public String rawSql(SqlDialect dialect) {
        if (null == rawSql) {
            StringBuilder buf = new StringBuilder();
            AtomicInteger paramCounter = new AtomicInteger();
            action.print(dialect, buf, paramCounter, entityAliasPrefix);
            for (JoinExpression join : joins) {
                join.print(dialect, buf, paramCounter, entityAliasPrefix);
            }
            where.printWithLead(dialect, buf, paramCounter, entityAliasPrefix);
            orderBy.printWithLead(dialect, buf, paramCounter, entityAliasPrefix);
            rawSql = buf.toString();
        }
        return rawSql;
    }

    public static class Builder {
        private String entityName;
        private Action action;
        private List<JoinExpression> joins = new ArrayList<>();
        private WhereComponent where = WhereComponent.EMPTY;
        private OrderByList orderBy = OrderByList.EMPTY_LIST;
        public SQL toSQL() {
            return new SQL(entityName, action, joins, where, orderBy);
        }
        public Builder select(String entityName, String... columns) {
            ensureNoAction();
            action = new Action.Select(entityName, columns);
            this.entityName = entityName;
            return this;
        }
        public Builder count(String entityName) {
            ensureNoAction();
            action = new Action.Count(entityName);
            this.entityName = entityName;
            return this;
        }
        public Builder delete(String entityName) {
            ensureNoAction();
            action = new Action.Delete(entityName);
            this.entityName = entityName;
            return this;
        }
        public Builder update(String entityName, String... columns) {
            ensureNoAction();
            action = new Action.Update(entityName, columns);
            this.entityName = entityName;
            return this;
        }

        private void ensureNoAction() {
            E.illegalArgumentIf(null != action, "action already exists");
        }
    }

    public static class Parser {
        public static SQL parse(Type type, String entityName, String expression, String... columns) {
            if (S.isBlank(expression)) {
                return type.startParsing(entityName).toSQL();
            }
            String lowerCase = expression.trim().toLowerCase();
            if (lowerCase.startsWith("select ") || lowerCase.startsWith("update ") || lowerCase.startsWith("delete ") || lowerCase.startsWith("from ")) {
                return new SQL(expression);
            }
            if (lowerCase.startsWith("order by ")) {
                Builder builder = new Builder().select(entityName);
                builder.orderBy = OrderByList.parse(expression.trim());
                return builder.toSQL();
            }
            Builder builder = type.startParsing(entityName, columns);
            return doParse(builder, expression).toSQL();
        }

        private static Builder doParse(Builder builder, String expression) {
            if (S.blank(expression)) {
                return builder;
            }
            C.List<String> list;
            if (expression.contains(" order by ")) {
                list = S.fastSplit(expression, " order by ");
            } else if (expression.contains(" ORDER BY ")) {
                list = S.fastSplit(expression, " ORDER BY ");
            } else if (expression.contains("OrderBy")) {
                list = S.fastSplit(expression, "OrderBy");
            } else if (expression.contains(" ORDER_BY ")) {
                list = S.fastSplit(expression, " ORDER_BY ");
            } else if (expression.contains(" Order By ")) {
                list = S.fastSplit(expression, " Order By ");
            } else {
                list = C.list(expression);
            }
            String columns = list.get(0);
            parseWhere(columns, builder);
            OrderByList orderBy = OrderByList.EMPTY_LIST;
            int sz = list.size();
            if (sz > 1) {
                orderBy = parseOrderBy(list.drop(0).toArray(new String[sz - 1]));
            }
            builder.orderBy = orderBy;
            return builder;
        }

        private static OrderByList parseOrderBy(String... orderList) {
            int sz = orderList.length;
            if (0 == sz) {
                return OrderByList.EMPTY_LIST;
            }
            OrderByList list = OrderByList.parse(orderList[0]);
            for (int i = 1; i < sz; ++i) {
                list = list.merge(OrderByList.parse(orderList[i]));
            }
            return list;
        }

        // TODO support OR conditions
        private static void parseWhere(String whereClause, Builder builder) {
            whereClause = whereClause.trim();
            if (S.isBlank(whereClause)) {
                builder.where = WhereComponent.EMPTY;
            }
            if (whereClause.matches("^by[A-Z].*$")) {
                parsePlay1Where(whereClause, builder);
            } else {
                parseActWhere(whereClause, builder);
            }
        }

        private static void parsePlay1Where(String whereClause, Builder builder) {
            whereClause = whereClause.substring(2);
            List<String> parts = S.fastSplit(whereClause, "And");
            parseWhere(parts, builder);
        }

        private static void parseActWhere(String whereClause, Builder builder) {
            List<String> parts = C.listOf(whereClause.split("[,;:]+"));
            parseWhere(parts, builder);
        }

        private static void parseWhere(List<String> parts, Builder builder) {
            List<WhereComponent> list = new ArrayList<>();
            for (String part: parts) {
                WhereComponent comp = parseWhereComponent(part, builder);
                if (WhereComponent.EMPTY != comp) {
                    list.add(comp);
                }
            }
            builder.where = new GroupWhereComponent(LogicOperator.AND, list);
        }

        private static WhereComponent parseWhereComponent(String part, Builder builder) {
            if (S.blank(part)) {
                return WhereComponent.EMPTY;
            }
            C.List<String> tokens = S.fastSplit(part, " ");
            String column = tokens.get(0);
            int sz = tokens.size();
            if (1 == sz) {
                if (column.contains(".")) {
                    List<String> l0 = S.fastSplit(column, ".");
                    E.unsupportedIf(l0.size() != 2, "Multiple dot in column spec not supported.");
                    String joinModel = l0.get(0);
                    String field = l0.get(1);
                    builder.joins.add(new JoinExpression(joinModel));
                    return new SimpleWhereExpression(joinModel, field, Operator.EQ);
                }
                return new SimpleWhereExpression(column, Operator.EQ);
            } else {
                String s = tokens.get(1);
                Operator op = operatorLookup.get(s.toLowerCase());
                E.illegalArgumentIf(null == op, "Unknown operator: " + s);
                if (sz > 2) {
                    LOGGER.warn("unused where clause tokens ignored: %s", S.string(tokens.drop(2)));
                }
                if (column.contains(".")) {
                    List<String> l0 = S.fastSplit(column, ".");
                    E.unsupportedIf(l0.size() != 2, "Multiple dot in column spec not supported.");
                    String joinModel = l0.get(0);
                    String field = l0.get(1);
                    builder.joins.add(new JoinExpression(joinModel));
                    return new SimpleWhereExpression(joinModel, field, op);
                }
                return new SimpleWhereExpression(column, op);
            }
        }
    }

    private static Map<String, Operator> operatorLookup = new HashMap<>();

    static void registerOperator(String key, Operator operator) {
        operatorLookup.put(key.toLowerCase(), operator);
    }
}
