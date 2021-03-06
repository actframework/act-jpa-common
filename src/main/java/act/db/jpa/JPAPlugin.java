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
import act.app.App;
import act.app.event.SysEvent;
import act.app.event.SysEventId;
import act.db.jpa.inject.JpaDbProviders;
import act.db.jpa.sql.Operator;
import act.db.sql.SqlDbPlugin;
import act.event.ActEventListenerBase;
import act.event.SysEventListenerBase;
import act.handler.builtin.controller.*;
import act.handler.builtin.controller.impl.ReflectedHandlerInvoker;
import act.handler.event.*;
import act.job.event.JobContextDestroyed;
import act.job.event.JobContextInitialized;
import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;
import org.osgl.mvc.result.Result;
import osgl.version.Version;
import osgl.version.Versioned;

import java.util.EventObject;
import javax.persistence.EntityManager;

// TODO - support JTA Transactional
@Versioned
public abstract class JPAPlugin extends SqlDbPlugin {

    public static final Logger LOGGER = LogManager.get(JPAPlugin.class);

    public static final String ATTR_NO_TRANSACTION = "no-trans";
    public static final String CONF_CACHE_ENABLED = "cache.enabled";
    public static final String CONF_DDL = "jpa.ddl";
    public static final String CONF_DDL_CREATE = "create";
    public static final String CONF_DDEL_CREATE_DROP = "create-drop";
    public static final String CONF_DDL_UPDATE = "update";
    public static final String CONF_DDL_NONE = "none";
    public static final Version VERSION = Version.get();
    public static final String CONF_MAPPING_FILES = "mapping-file";

    static {
        // this ensures the SQL.operatorLookup is initialized
        Operator.values();
    }

    @Override
    protected void doExtendedApplyTo(final App app) {
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
                    JPAContext.setNoTx();
                }
            }
        }).bind(BeforeResultCommit.class, new ActEventListenerBase<BeforeResultCommit>() {
            @Override
            public void on(BeforeResultCommit event) {
                // so when we render result the transaction has
                // been committed already, thus we get the
                // id for the new record
                JPAContext.close();
            }
        }).bind(PostHandle.class, new ActEventListenerBase() {
            @Override
            public void on(EventObject event) throws Exception {
                // try close JPAContext anyway
                // See https://github.com/actframework/act-jpa-common/issues/43
                JPAContext.close();
            }
        }).bind(PreHandle.class, new ActEventListenerBase() {
            @Override
            public void on(EventObject event) {
                JPAContext.init();
            }
        }).bind(JobContextInitialized.class, new ActEventListenerBase() {
            @Override
            public void on(EventObject event) {
                JPAContext.initForJob();
            }
        }).bind(JobContextDestroyed.class, new ActEventListenerBase() {
            @Override
            public void on(EventObject event) {
                JPAContext.closeForJob();
            }
        }).bind(SysEventId.DB_SVC_LOADED, new SysEventListenerBase<SysEvent>() {
            @Override
            public void on(SysEvent event) {
                new NamedQueryExplorer().explore(app);
            }
        });
        app.jobManager().on(SysEventId.PRE_START, jobId("class init"), new Runnable() {
            @Override
            public void run() {
                EntityManagerProvider emp = app.getInstance(EntityManagerProvider.class);
                app.injector().registerNamedProvider(EntityManager.class, emp);
                app.injector().registerProvider(EntityManager.class, emp);
                JpaDbProviders.classInit(app);
                //JPAContext.reset();
            }
        });
        RequestHandlerProxy.registerGlobalInterceptor(new ExceptionInterceptor() {
            @Override
            protected Result internalHandle(Exception e, ActionContext actionContext) {
                JPAContext.setRollback();
                return null;
            }

            @Override
            public void accept(ActionHandlerInvoker.Visitor visitor) {

            }

            @Override
            public boolean skipEvents() {
                return true;
            }

            @Override
            public boolean sessionFree() {
                return true;
            }

            @Override
            public boolean express() {
                return true;
            }
        });
    }


}
