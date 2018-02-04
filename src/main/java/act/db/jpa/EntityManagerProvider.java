package act.db.jpa;

import act.Act;
import act.app.DbServiceManager;
import act.util.Stateless;
import org.osgl.inject.NamedProvider;

import javax.inject.Provider;
import javax.persistence.EntityManager;

@Stateless
public class EntityManagerProvider implements
        NamedProvider<EntityManager>, Provider<EntityManager> {

    private static final String DEFAULT = DbServiceManager.DEFAULT;

    @Override
    public EntityManager get() {
        return get(DEFAULT);
    }

    @Override
    public EntityManager get(String s) {
        return JPAContext.em(svc(s));
    }

    private JPAService svc(String s) {
        return Act.app().dbServiceManager().dbService(s);
    }

}
