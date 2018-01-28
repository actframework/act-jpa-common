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
import act.db.DbPlugin;
import act.db.jpa.sql.Operator;
import act.event.ActEventListenerBase;
import act.handler.builtin.controller.ActionHandlerInvoker;
import act.handler.builtin.controller.ExceptionInterceptor;
import act.handler.builtin.controller.RequestHandlerProxy;
import act.handler.builtin.controller.impl.ReflectedHandlerInvoker;
import act.handler.event.*;
import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;
import org.osgl.mvc.result.Result;
import osgl.version.Version;
import osgl.version.Versioned;

import java.util.EventObject;

// TODO - support JTA Transactional
@Versioned
public abstract class JPAPlugin extends DbPlugin {

    public static final Logger LOGGER = LogManager.get(JPAPlugin.class);

    public static final String ATTR_NO_TRANSACTION = "no-trans";
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
                    JPAContext.setNoTx();
                }
            }
        }).bind(PostHandle.class, new ActEventListenerBase<PostHandle>() {
            @Override
            public void on(PostHandle event) {
                // try close JPAContext anyway
                JPAContext.close();
            }
        }).bind(PreHandle.class, new ActEventListenerBase() {
            @Override
            public void on(EventObject event) {
                JPAContext.init();
            }
        }).bind(BeforeResultCommit.class, new ActEventListenerBase<BeforeResultCommit>() {
            @Override
            public void on(BeforeResultCommit event) throws Exception {
                JPAContext.close();
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
