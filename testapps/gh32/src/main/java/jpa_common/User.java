package jpa_common;

import act.cli.Command;
import act.controller.annotation.UrlContext;
import act.db.DbBind;
import act.db.jpa.JPADao;
import act.util.EnableCircularReferenceDetect;
import act.util.Stateless;
import org.osgl.mvc.annotation.GetAction;
import org.osgl.mvc.annotation.PostAction;

import java.util.List;
import javax.persistence.*;

@Entity(name = "user")
public class User  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String firstName;
    public String lastName;

    @OneToMany(mappedBy = "agent")
    public List<Order> orders;

    @Stateless
    @UrlContext("users")
    @EnableCircularReferenceDetect
    public static class Dao extends JPADao<Long, User> {

        @Command("user.create")
        @PostAction
        public User create(User user) {
            return save(user);
        }

        @Command("user.show")
        @GetAction("{user}")
        public User find(@DbBind User user) {
            return user;
        }

        @GetAction
        public Iterable<User> list() {
            return findAll();
        }
    }
}
