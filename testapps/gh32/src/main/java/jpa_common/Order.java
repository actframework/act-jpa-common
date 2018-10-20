package jpa_common;

import act.controller.annotation.UrlContext;
import act.db.DbBind;
import act.db.jpa.JPADao;
import act.util.EnableCircularReferenceDetect;
import act.util.Stateless;
import org.osgl.mvc.annotation.GetAction;
import org.osgl.mvc.annotation.PostAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "agent_id")
    public User agent;

    public String product;

    public int quantity;

    @Stateless
    @UrlContext("orders")
    @EnableCircularReferenceDetect
    public static class Dao extends JPADao<Long, Order> {

        @PostAction
        public Order create(Order order, @DbBind @NotNull User agent) {
            order.agent = agent;
            return save(order);
        }

        @GetAction("{order}")
        public Order show(@DbBind Order order) {
            return order;
        }

        @GetAction
        public Iterable<Order> list() {
            return findAll();
        }

    }
}