package act.db.jpa.util;

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

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.osgl.$;
import org.osgl.Osgl;
import org.osgl.exception.NotAppliedException;
import org.osgl.util.E;

import java.lang.reflect.Field;

public class TimestampSetter<MODEL_TYPE> extends $.Visitor<MODEL_TYPE> {
    private Field timestampField;
    private $.Func0 now;
    public TimestampSetter(Class<MODEL_TYPE> modelType, String timestampFieldName) {
        timestampField = $.fieldOf(modelType, timestampFieldName);
        timestampField.setAccessible(true);
        Class timestampType = timestampField.getType();
        if (java.sql.Date.class.isAssignableFrom(timestampType)) {
            now = new $.Func0() {
                @Override
                public Object apply() throws NotAppliedException, Osgl.Break {
                    return new java.sql.Date($.ms());
                }
            };
        } else if (java.sql.Time.class.isAssignableFrom(timestampType)) {
            now = new $.Func0() {
                @Override
                public Object apply() throws NotAppliedException, Osgl.Break {
                    return new java.sql.Time($.ms());
                }
            };
        } else if (java.sql.Timestamp.class.isAssignableFrom(timestampType)) {
            now = new $.Func0() {
                @Override
                public Object apply() throws NotAppliedException, Osgl.Break {
                    return new java.sql.Timestamp($.ms());
                }
            };
        } else if (DateTime.class.isAssignableFrom(timestampType)) {
            now = new $.Func0() {
                @Override
                public Object apply() throws NotAppliedException, Osgl.Break {
                    return DateTime.now();
                }
            };
        } else if (LocalDate.class.isAssignableFrom(timestampType)) {
            now = new $.Func0() {
                @Override
                public Object apply() throws NotAppliedException, Osgl.Break {
                    return LocalDate.now();
                }
            };
        } else if (LocalTime.class.isAssignableFrom(timestampType)) {
            now = new $.Func0() {
                @Override
                public Object apply() throws NotAppliedException, Osgl.Break {
                    return LocalTime.now();
                }
            };
        } else if (LocalDateTime.class.isAssignableFrom(timestampType)) {
            now = new $.Func0() {
                @Override
                public Object apply() throws NotAppliedException, Osgl.Break {
                    return LocalDateTime.now();
                }
            };
        }
    }

    @Override
    public void visit(MODEL_TYPE o) throws Osgl.Break {
        try {
            timestampField.set(o, now.apply());
        } catch (IllegalAccessException e) {
            throw E.unexpected(e);
        }
    }
}
