package com.yoncabt.ebr.ui;

import com.vaadin.server.Page;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.yoncabt.abys.core.util.log.FLogManager;
import com.yoncabt.ebr.security.Authenticator;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author myururdurmaz
 */
@UIScope
@SpringUI
public class LoginWindow extends Window {

    private FLogManager logManager = FLogManager.getLogger(getClass());

    @Autowired
    private Authenticator authenticator;

    @PostConstruct
    private void init() {
    }

    private void loggedIn(String kullanici, String sifre) {
        getUI().setData(kullanici);
        LoginWindow.this.close();
        for (LoginSuccessListener loginSuccessListener : loginSuccessListeners) {
            loginSuccessListener.success();
        }
    }

    public LoginWindow() {
        super("EBR / Giriş");
        setModal(true);
        setClosable(false);
        FormLayout fl = new FormLayout();
        fl.setMargin(true);

        final TextField user = new TextField("Kullanıcı");
        user.setId("login_user");
        final PasswordField password = new PasswordField("Şifre");
        password.setId("login_password");
        final Button btnLogin = new Button("Giriş");
        btnLogin.setId("login_btnlogin");
        btnLogin.setDisableOnClick(true);
        btnLogin.addClickListener((Button.ClickEvent event) -> {
            try {
                // abys.conftan login
                // uuide kontrolü boş olması duurmunda şifre tutmasın diye
                boolean login = authenticator.check(user.getValue(), password.getValue());

                // abys login başarılı ise alternatife login ol
                if (login) {
                    loggedIn(user.getValue(), password.getValue());
                    logManager.info("login :" + user.getValue());
                    return;
                }

                logManager.error("LOGIN ERROR " + user.getValue());
                new Notification("Kullanıcı veya şifre hatası", Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
            } finally {
                btnLogin.setEnabled(true);
            }
        });

        fl.addComponent(user);
        fl.addComponent(password);
        fl.addComponent(btnLogin);
        setContent(fl);
    }

    private List<LoginSuccessListener> loginSuccessListeners = new ArrayList<>();

    public void addSuccessListener(LoginSuccessListener lstnr) {
        loginSuccessListeners.add(lstnr);
    }

    public static interface LoginSuccessListener {
        void success();
    }
}
