package act.db.jpa;

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

import act.db.meta.EntityMetaInfoRepo;
import act.util.AnnotatedClassFinder;
import org.osgl.$;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Converter;

@Singleton
public class ConverterExplorer {

    private EntityMetaInfoRepo repo;

    @Inject
    public ConverterExplorer(EntityMetaInfoRepo repo) {
        this.repo = $.requireNotNull(repo);
    }

    @AnnotatedClassFinder(Converter.class)
    public void foundConverter(Class converterClass) {
        repo.registerConverter(converterClass);
    }

}
