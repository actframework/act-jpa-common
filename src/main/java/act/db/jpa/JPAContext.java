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

    private static class Info {
        EntityManager em;
        EntityTransaction tx;
        SqlDialect dialect;
        Info(JPAService svc, boolean noTx) {
            em = svc.createEntityManager();
            dialect = svc.dialect();
            if (!noTx) {
                tx = em.getTransaction();
                tx.begin();
            }
        }
    }

    private boolean noTx;
    private Map<String, Info> data = new HashMap<>();

    private JPAContext(boolean noTx) {
        super(true);
        this.noTx = noTx;
    }

    @Override
    protected void releaseResources() {
        for (Info info : data.values()) {
            EntityTransaction tx = info.tx;
            if (null != tx && tx.isActive()) {
                if (tx.getRollbackOnly()) {
                    tx.rollback();
                } else {
                    tx.commit();
                }
            }
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
            info = new Info(jpa, noTx);
            data.put(jpa.id(), info);
        }
        return info;
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

    public static void init(boolean noTx) {
        E.illegalStateIf(null != cur_.get(), "JPAContext already set");
        cur_.set(new JPAContext(noTx));
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
