package jpa_common;

import act.db.jpa.JPADao;
import act.util.SimpleBean;

import javax.persistence.*;

@Entity(name = "foo")
public class Foo implements SimpleBean {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String name;

    public static class Dao extends JPADao<Long, Foo> {}
}
