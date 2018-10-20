package jpa_common;

import act.cli.Command;
import act.cli.Required;
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
    public static class Dao extends JPADao<Long, Order> {

        @Command("order.create")
        @PostAction
        @EnableCircularReferenceDetect
        public Order create(
                @Required String product,
                @Required int quantity,
                @Required @DbBind @NotNull User agent
        ) {
            Order order = new Order();
            order.product = product;
            order.quantity = quantity;
            order.agent = agent;
            return save(order);
        }

        @GetAction("{order}")
        @EnableCircularReferenceDetect
        public Order show(@DbBind Order order) {
            return order;
        }

        @GetAction
        @EnableCircularReferenceDetect
        public Iterable<Order> list() {
            return findAll();

        }

    }
}