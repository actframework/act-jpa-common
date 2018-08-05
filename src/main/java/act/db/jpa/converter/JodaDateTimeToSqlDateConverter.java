package act.db.jpa.converter;

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
import org.osgl.$;

import java.sql.Date;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class JodaDateTimeToSqlDateConverter implements AttributeConverter<DateTime, Date> {
    @Override
    public Date convertToDatabaseColumn(DateTime attribute) {
        return $.convert(attribute).to(Date.class);
    }

    @Override
    public DateTime convertToEntityAttribute(Date dbData) {
        return $.convert(dbData).to(DateTime.class);
    }
}
