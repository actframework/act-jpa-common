package test.jpa;

import act.controller.annotation.UrlContext;
import act.db.*;
import act.db.jpa.JPADao;
import act.db.sql.tx.Transactional;
import act.util.SimpleBean;
import act.util.Stateless;
import org.joda.time.DateTime;
import org.osgl.mvc.annotation.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity(name = "user")
public class User implements SimpleBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String firstName;
    public String lastName;
    public Gender gender;
    public int level;

    @CreatedAt
    public DateTime registerDate;

    @LastModifiedAt
    public DateTime updateDate;

    @CreatedBy
    public String creator;

    @LastModifiedBy
    public String updator;

    @Stateless
    @UrlContext("/users")
    public static class Dao extends JPADao<Long, User> {

        @PostAction
        @Transactional
        public User create(User user) {
            return save(user);
        }

        @GetAction("{id}")
        public User get(@NotNull Long id) {
            return findById(id);
        }

        @PutAction("level")
        public void set(int value) {
            //createUpdateQuery("level", )
        }

    }
}
