package jpa_common;

import act.controller.annotation.UrlContext;
import act.db.DbBind;
import org.osgl.mvc.annotation.*;

import javax.inject.Inject;

@UrlContext("15")
public class GH15 {

    @Inject
    private Foo.Dao dao;

    @PostAction
    public Foo create(Foo foo) {
        return dao.save(foo);
    }

    @GetAction("{foo}")
    public Foo get(@DbBind Foo foo) {
        return foo;
    }

    @DeleteAction("{id}")
    public void del(long id) {
        dao.deleteById(id);
    }


}
