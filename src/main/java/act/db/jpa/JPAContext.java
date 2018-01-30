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

import act.db.jpa.sql.SqlDialect;
import act.util.DestroyableBase;
import org.osgl.logging.Logger;
import org.osgl.util.E;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class JPAContext extends DestroyableBase {

    private static final Logger LOGGER = JPAPlugin.LOGGER;

    private static class TxScope {
        boolean readOnly;

        public TxScope(boolean readOnly) {
            this.readOnly = readOnly;
        }
    }

    private static class Info {
        EntityManager em;
        EntityTransaction tx;
        SqlDialect dialect;
        Info(JPAService svc) {
            this.em = svc.createEntityManager();
            this.dialect = svc.dialect();
        }
    }

    private boolean noTx;
    private boolean rollback;
    private Map<String, Info> data = new HashMap<>();
    private TxScope txScope;

    private JPAContext() {
        super(true);
    }

    private void _setNoTx() {
        this.noTx = true;
    }

    private void _setRollback() {
        this.rollback = true;
    }

    @Override
    protected void releaseResources() {
        for (Info info : data.values()) {
            E.illegalStateIf(null != info.tx, "transaction found unclosed");
            EntityManager em = info.em;
            if (null != em && em.isOpen()) {
                em.close();
            }
        }
    }

    private EntityManager _em(JPAService jpa) {
        return ensureInfo(jpa).em;
    }

    private SqlDialect _dialect(JPAService jpa) {
        return ensureInfo(jpa).dialect;
    }

    private Info ensureInfo(JPAService jpa) {
        Info info = data.get(jpa.id());
        if (null == info) {
            info = new Info(jpa);
            data.put(jpa.id(), info);
        }
        ensureTx(info);
        return info;
    }

    private void ensureTx(Info info) {
        if (_withinTxScope() && null == info.tx) {
            info.tx = info.em.getTransaction();
            info.tx.begin();
        }
    }

    private void _enterTxScope(boolean readOnly) {
        if (!noTx) {
            E.illegalStateIf(_withinTxScope(), "Nested transaction not supported");
            txScope = new TxScope(readOnly);
        }
    }

    private boolean _withinTxScope() {
        return null != txScope;
    }

    private void _exitTxScope(boolean rollback) {
        if (!noTx) {
            E.illegalStateIfNot(_withinTxScope(), "No transaction found");
            boolean readOnly = txScope.readOnly;
            for (Info info : data.values()) {
                EntityTransaction tx = info.tx;
                if (null != tx && tx.isActive()) {
                    if (readOnly || rollback || tx.getRollbackOnly()) {
                        tx.rollback();
                    } else {
                        tx.commit();
                    }
                }
                info.tx = null;
            }
            this.txScope = null;
        }
    }

    private static final ThreadLocal<JPAContext> cur_ = new ThreadLocal<>();

    public static boolean ready() {
        return null != cur_.get();
    }

    public static EntityManager em(JPAService jpa) {
        return ensureContext()._em(jpa);
    }

    public static SqlDialect dialect(JPAService jpa) {
        return ensureContext()._dialect(jpa);
    }

    public static void enterTxScope(boolean readOnly) {
        JPAContext ctx = ensureContext();
        ctx._enterTxScope(readOnly);
    }

    public static void exitTxScope(boolean rollback) {
        JPAContext ctx = ensureContext();
        ctx._exitTxScope(rollback);
    }

    public static void init() {
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

    public static void close() {
        JPAContext cur = cur_.get();
        if (null != cur) {
            cur.destroy();
            cur_.remove();
        }
    }

    private static JPAContext ensureContext() {
        JPAContext ctx = cur_.get();
        E.illegalStateIf(null == ctx, "JPAContext is not ready");
        return ctx;
    }

}
