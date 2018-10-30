package jpa_common.gh31;

import act.controller.annotation.UrlContext;
import act.db.jpa.JPADao;
import act.db.jpa.JPAQuery;
import act.util.SimpleBean;
import org.osgl.mvc.annotation.GetAction;

import java.util.List;
import javax.persistence.*;

@Entity(name = "ua")
public class UserAddress implements SimpleBean {

    public enum State {
        NORMAL, ODD
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    public String street;

    public State state;

    @UrlContext("31/addresses")
    public static class Dao extends JPADao<Integer, UserAddress> {

        @GetAction
        public List<UserAddress> list() {
            JPAQuery<UserAddress> q = q("state", State.NORMAL);
            q.orderBy("street");
            return q.fetch();
        }

    }



}
