package jpa_common;

import act.controller.annotation.UrlContext;
import act.db.DbBind;
import act.db.jpa.JPADao;
import act.util.EnableCircularReferenceDetect;
import act.util.Stateless;
import org.hibernate.Hibernate;
import org.osgl.mvc.annotation.GetAction;
import org.osgl.mvc.annotation.PostAction;

import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity(name = "user")
public class User  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String firstName;
    public String lastName;

    @OneToMany(mappedBy = "agent", fetch = FetchType.LAZY)
    private List<Order> orders;

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    @Stateless
    @UrlContext("users")
    @EnableCircularReferenceDetect
    public static class Dao extends JPADao<Long, User> {

        @PostAction
        public User create(User user) {
            return save(user);
        }

        @GetAction("{user}")
        public User find(@DbBind User user) {
            Hibernate.initialize(user.getOrders());
            return user;
        }

        @GetAction
        public Iterable<User> list() {
            return findAll();
        }

        @GetAction("{agent}/orders")
        public Iterable<Order> ordersByUser(@DbBind @NotNull User agent, Order.Dao orderDao) {
            return orderDao.findBy("agent_id", agent.id);
        }

    }
}
