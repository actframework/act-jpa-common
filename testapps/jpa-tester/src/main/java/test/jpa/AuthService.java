package test.jpa;

import act.app.ActionContext;
import org.osgl.mvc.annotation.PostAction;

public class AuthService {

    @PostAction("/login")
    public void login(String username, ActionContext context) {
        context.login(username);
    }

    @PostAction("/logout")
    public void logout(ActionContext context) {
        context.logout();
    }

}
