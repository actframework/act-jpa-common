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
import act.db.jpa.sql.SqlDialect;
import act.db.sql.tx.TxContext;
import act.util.DestroyableBase;
import org.osgl.logging.Logger;
import org.osgl.util.E;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class JPAContext extends DestroyableBase {

    private static final Logger LOGGER = JPAPlugin.LOGGER;

    private static class Info {
        EntityManager em;
        EntityTransaction tx;
        boolean readOnly;
        JPAService svc;
        Info(JPAService svc, boolean readOnly) {
            this.em = svc.createEntityManager(readOnly);
            this.readOnly = readOnly;
            this.svc = svc;
        }
        void updateEm(boolean readOnly) {
            if (readOnly == this.readOnly) {
                return;
            }
            if (null != this.em) {
                this.em.close();
            }
            this.em = svc.createEntityManager(readOnly);
            this.readOnly = readOnly;
        }
    }

    private boolean noTx;
    private boolean rollback;
    private boolean initForJob;
    private Map<String, Info> data = new HashMap<>();

    private JPAContext() {
    }

    private void _setNoTx() {
        this.noTx = true;
    }

    private void _setRollback() {
        this.rollback = true;
    }

    private void _clear(JPAService jpa) {
        data.remove(jpa.id());
    }

    @Override
    protected void releaseResources() {
        if (_withinTxScope()) {
            _exitTxScope(rollback);
        }
        for (Info info : data.values()) {
            EntityManager em = info.em;
            if (null != em && em.isOpen()) {
                em.close();
            }
        }
    }

    private EntityManager _em(JPAService jpa, boolean preferredReadOnly) {
        return ensureInfo(jpa, preferredReadOnly).em;
    }

    private boolean isReadOnly() {
        E.illegalStateIfNot(_withinTxScope(), "No transaction context");
        return TxContext.readOnly();
    }

    private boolean isReadOnly(boolean preferredReadOnly) {
        if (!_withinTxScope()) {
            return preferredReadOnly;
        }
        return TxContext.readOnly();
    }

    private Info ensureInfo(JPAService jpa, boolean preferredReadOnly) {
        if (_withinTxScope()) {
            boolean txReadOnly = TxContext.readOnly();
            E.illegalStateIf (txReadOnly && !preferredReadOnly, "Cannot do write operation within readonly transaction");
            preferredReadOnly = txReadOnly;
        }
        Info info = data.get(jpa.id());
        if (null == info) {
            info = new Info(jpa, preferredReadOnly);
            data.put(jpa.id(), info);
        } else {
            info.updateEm(preferredReadOnly);
        }
        ensureTx(info);
        return info;
    }

    private void ensureTx(Info info) {
        if (null != info.tx) {
            return;
        }
        if (info.svc.beginTxIfRequired(info.em)) {
            info.tx = info.em.getTransaction();
        }
    }

    private boolean _withinTxScope() {
        return TxContext.withinTxScope();
    }

    private void _exitTxScope(boolean rollback) {
        if (!noTx) {
            E.illegalStateIfNot(_withinTxScope(), "No transaction found");
            boolean readOnly = TxContext.readOnly();
            for (Info info : data.values()) {
                EntityTransaction tx = info.tx;
                if (null != tx && tx.isActive()) {
                    if (readOnly || rollback || this.rollback || tx.getRollbackOnly()) {
                        tx.rollback();
                    } else {
                        tx.commit();
                    }
                }
                info.tx = null;
            }
        }
    }

    private static final ThreadLocal<JPAContext> cur_ = new ThreadLocal<>();

    public static boolean ready() {
        return null != cur_.get();
    }

    public static EntityManager emWithTx(JPAService jpa) {
        JPAContext ctx = ensureContext();
        Info info = ctx.ensureInfo(jpa, false);
        EntityManager em = info.em;
        if (!ctx._withinTxScope()) {
            jpa.forceBeginTx(em);
            info.tx = em.getTransaction();
        }
        return em;
    }

    public static EntityManager em(JPAService jpa) {
        JPAContext ctx = ensureContext();
        if (ctx._withinTxScope()) {
            return ctx._em(jpa, TxContext.readOnly());
        } else {
            // By default use master datasource in case
            // developer need this em for write operations
            return ctx._em(jpa, false);
        }
    }

    public static EntityManager em(JPAService jpa, boolean preferredReadonly) {
        return ensureContext()._em(jpa, preferredReadonly);
    }

    public static void clear(JPAService jpa) {
        JPAContext ctx = cur_.get();
        if (null != ctx) {
            ctx._clear(jpa);
        }
    }

    public static void reset() {
        cur_.remove();
    }

    public static SqlDialect dialect(JPAService jpa) {
        return jpa.dialect();
    }

    public static boolean readOnly() {
        return TxContext.readOnly();
    }

    public static boolean readOnly(boolean preferredReadOnly) {
        return TxContext.readOnly(preferredReadOnly);
    }

    public static void initForJob() {
        if (null != ActionContext.current()) {
            return;
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("JPA context init for job");
        }
        E.illegalStateIf(null != cur_.get(), "JPAContext already set");
        JPAContext ctx = new JPAContext();
        ctx.initForJob = true;
        cur_.set(ctx);
    }

    public static void init() {
        if (LOGGER.isTraceEnabled()) {
            ActionContext ctx = ActionContext.current();
            if (null == ctx) {
                LOGGER.trace("JPA context init");
            } else {
                LOGGER.trace("JPA context init - " + ctx.req());
            }
        }
        E.illegalStateIf(null != cur_.get(), "JPAContext already set");
        cur_.set(new JPAContext());
    }

    public static void setNoTx() {
        JPAContext ctx = ensureContext();
        ctx._setNoTx();
    }

    public static void setRollback() {
        JPAContext ctx = cur_.get();
        if (null != ctx) {
            ctx._setRollback();
        }
    }

    public static void closeForJob() {
        if (null != ActionContext.current()) {
            return;
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("JPA context close for job");
        }
        JPAContext cur = cur_.get();
        if (null != cur && cur.initForJob) {
            cur.destroy();
            cur_.remove();
        }
    }

    public static void close() {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("JPA context close");
        }
        JPAContext cur = cur_.get();
        if (null != cur) {
            cur.destroy();
            cur_.remove();
        }
    }

    private static JPAContext ensureContext() {
        return ensureContext(false);
    }

    private static JPAContext ensureContext(boolean createIfNotAvailable) {
        JPAContext ctx = cur_.get();
        if (null == ctx) {
            E.illegalStateIf( !createIfNotAvailable, "JPAContext is not ready");
            ctx = new JPAContext();
            cur_.set(ctx);
        }
        return ctx;
    }

}
