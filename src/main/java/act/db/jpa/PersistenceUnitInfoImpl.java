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

import org.osgl.$;
import org.osgl.util.C;
import org.osgl.util.E;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

/**
 * **Note** the code is copied from PlayFramework v1.5
 *
 * @author Vlad Mihalcea
 *
 * Taken from https://vladmihalcea.com/2015/11/26/how-to-bootstrap-hibernate-without-the-persistence-xml-file/
 */
public class PersistenceUnitInfoImpl implements PersistenceUnitInfo {

    private final String persistenceUnitName;
    private final String persistenceProviderClass;
    private final List<Class> managedClasses;
    private final List<String> mappingFileNames;
    private final Properties properties;
    private final ClassLoader classLoader;

    private PersistenceUnitTransactionType transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;


    private DataSource jtaDataSource;

    private DataSource nonJtaDataSource;

    public PersistenceUnitInfoImpl(
            String persistenceProviderClass,
            String persistenceUnitName,
            List<Class> managedClasses,
            List<String> mappingFileNames,
            Properties properties,
            ClassLoader classLoader
    ) {
        this.persistenceProviderClass = persistenceProviderClass;
        this.persistenceUnitName = persistenceUnitName;
        this.managedClasses = managedClasses;
        this.mappingFileNames = mappingFileNames;
        this.properties = properties;
        this.classLoader = $.requireNotNull(classLoader);
    }

    @Override
    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    @Override
    public String getPersistenceProviderClassName() {
        return persistenceProviderClass;
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return transactionType;
    }

    @Override
    public DataSource getJtaDataSource() {
        return jtaDataSource;
    }

    public PersistenceUnitInfoImpl setJtaDataSource(DataSource jtaDataSource) {
        this.jtaDataSource = jtaDataSource;
        this.nonJtaDataSource = null;
        transactionType = PersistenceUnitTransactionType.JTA;
        return this;
    }

    @Override
    public DataSource getNonJtaDataSource() {
        return nonJtaDataSource;
    }

    public PersistenceUnitInfoImpl setNonJtaDataSource(DataSource nonJtaDataSource) {
        this.nonJtaDataSource = nonJtaDataSource;
        this.jtaDataSource = null;
        transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;
        return this;
    }

    @Override
    public List<String> getMappingFileNames() {
        return mappingFileNames;
    }

    @Override
    public List<URL> getJarFileUrls() {
        return Collections.emptyList();
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        try {
            return new File(".").toURI().toURL();
        } catch (IOException e) {
            throw E.ioException(e);
        }
    }

    @Override
    public List<String> getManagedClassNames() {
        return C.list(managedClasses).map(new $.Transformer<Class, String>(){
            @Override
            public String transform(Class aClass) {
                return aClass.getName();
            }
        });
    }

    @Override
    public boolean excludeUnlistedClasses() {
        return false;
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return SharedCacheMode.UNSPECIFIED;
    }

    @Override
    public ValidationMode getValidationMode() {
        return ValidationMode.AUTO;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
        return "2.2";
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public void addTransformer(ClassTransformer transformer) {

    }

    @Override
    public ClassLoader getNewTempClassLoader() {
        return null;
    }

    public static void main(String[] args) throws Exception {
        new URL("file:/jpa-common");
    }
}
