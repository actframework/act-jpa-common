package jpa_common;

import act.Act;
import act.db.DbBind;
import act.db.jpa.JPADao;
import act.db.sql.tx.Transactional;
import act.util.SimpleBean;
import org.osgl.mvc.annotation.DeleteAction;
import org.osgl.mvc.annotation.GetAction;
import org.osgl.mvc.annotation.PostAction;

import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "foo")
public class Foo implements SimpleBean {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String name;

    public static class Dao extends JPADao<Long, Foo> {
        public Dao() {

        }
    }

    public static class Service {

        @Inject
        private Dao dao;

        @PostAction
        @Transactional
        public Foo create(Foo foo) {
            return dao.save(foo);
        }

        @GetAction("{foo}")
        public Foo get(@DbBind Foo foo) {
            return foo;
        }

        @Transactional
        @DeleteAction("{id}")
        public void del(long id) {
            dao.deleteById(id);
        }
    }

    public static void main(String[] args) throws Exception {
        Act.start();
    }
}
