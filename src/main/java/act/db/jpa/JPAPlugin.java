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

import act.app.ActionContext;
import act.app.ActionContext.ActionContextDestroyed;
import act.app.App;
import act.db.DbPlugin;
import act.db.jpa.sql.Operator;
import act.event.ActEventListenerBase;
import act.handler.builtin.controller.impl.ReflectedHandlerInvoker;
import act.handler.event.ReflectedHandlerInvokerInit;
import act.handler.event.ReflectedHandlerInvokerInvoke;
import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;
import osgl.version.Version;
import osgl.version.Versioned;

import java.util.Map;

// TODO - support JTA Transactional
@Versioned
public abstract class JPAPlugin extends DbPlugin {

    public static final Logger LOGGER = LogManager.get(JPAPlugin.class);

    public static final String ATTR_NO_TRANSACTION = "no-trans";

    public static final Version VERSION = Version.get();
    public static final String CONF_MAPPING_FILES = "mapping-file";

    static {
        // this ensures the SQL.operatorLookup is initialized
        Operator.values();
    }

    @Override
    protected void applyTo(App app) {
        app.eventBus().bind(ReflectedHandlerInvokerInit.class, new ActEventListenerBase<ReflectedHandlerInvokerInit>() {
            @Override
            public void on(ReflectedHandlerInvokerInit event) {
                ReflectedHandlerInvoker<?> invoker = event.source();
                NoTransaction noTrans = invoker.getAnnotation(NoTransaction.class);
                if (null != noTrans) {
                    invoker.attribute(ATTR_NO_TRANSACTION, noTrans);
                }
            }
        }).bind(ReflectedHandlerInvokerInvoke.class, new ActEventListenerBase<ReflectedHandlerInvokerInvoke>() {
            @Override
            public void on(ReflectedHandlerInvokerInvoke event) {
                ReflectedHandlerInvoker invoker = event.source();
                ActionContext context = event.context();
                Object noTrans = invoker.attribute(ATTR_NO_TRANSACTION);
                if (null != noTrans) {
                    context.attribute(ATTR_NO_TRANSACTION, noTrans);
                }
            }
        }).bind(ActionContextDestroyed.class, new ActEventListenerBase<ActionContextDestroyed>() {
            @Override
            public void on(ActionContextDestroyed event) {
                JPAContext.close();
            }
        });
    }

    // The `getDefaultDialect` code come from PlayFramework v1.x JPAPlugin
    public static String getDefaultDialect(Map<String, String> dbConfig, String driver) {
        String dialect = dbConfig.get("jpa.dialect");
        if (dialect != null) {
            return dialect;
        } else if ("org.h2.Driver".equals(driver)) {
            return "org.hibernate.dialect.H2Dialect";
        } else if ("org.hsqldb.jdbcDriver".equals(driver)) {
            return "org.hibernate.dialect.HSQLDialect";
        } else if ("com.mysql.jdbc.Driver".equals(driver)) {
            return "play.db.jpa.MySQLDialect";
        } else if ("org.postgresql.Driver".equals(driver)) {
            return "org.hibernate.dialect.PostgreSQLDialect";
        } else if ("com.ibm.db2.jdbc.app.DB2Driver".equals(driver)) {
            return "org.hibernate.dialect.DB2Dialect";
        } else if ("com.ibm.as400.access.AS400JDBCDriver".equals(driver)) {
            return "org.hibernate.dialect.DB2400Dialect";
        } else if ("com.ibm.as400.access.AS390JDBCDriver".equals(driver)) {
            return "org.hibernate.dialect.DB2390Dialect";
        } else if ("oracle.jdbc.OracleDriver".equals(driver)) {
            return "org.hibernate.dialect.Oracle10gDialect";
        } else if ("com.sybase.jdbc2.jdbc.SybDriver".equals(driver)) {
            return "org.hibernate.dialect.SybaseAnywhereDialect";
        } else if ("com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(driver)) {
            return "org.hibernate.dialect.SQLServerDialect";
        } else if ("com.sap.dbtech.jdbc.DriverSapDB".equals(driver)) {
            return "org.hibernate.dialect.SAPDBDialect";
        } else if ("com.informix.jdbc.IfxDriver".equals(driver)) {
            return "org.hibernate.dialect.InformixDialect";
        } else if ("com.ingres.jdbc.IngresDriver".equals(driver)) {
            return "org.hibernate.dialect.IngresDialect";
        } else if ("progress.sql.jdbc.JdbcProgressDriver".equals(driver)) {
            return "org.hibernate.dialect.ProgressDialect";
        } else if ("com.mckoi.JDBCDriver".equals(driver)) {
            return "org.hibernate.dialect.MckoiDialect";
        } else if ("InterBase.interclient.Driver".equals(driver)) {
            return "org.hibernate.dialect.InterbaseDialect";
        } else if ("com.pointbase.jdbc.jdbcUniversalDriver".equals(driver)) {
            return "org.hibernate.dialect.PointbaseDialect";
        } else if ("com.frontbase.jdbc.FBJDriver".equals(driver)) {
            return "org.hibernate.dialect.FrontbaseDialect";
        } else if ("org.firebirdsql.jdbc.FBDriver".equals(driver)) {
            return "org.hibernate.dialect.FirebirdDialect";
        } else {
            throw new UnsupportedOperationException("I do not know which hibernate dialect to use with "
                    + driver + " and I cannot guess it, use the property jpa.dialect in config file");
        }
    }

}
