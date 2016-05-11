/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.ui;

import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 *
 * @author myururdurmaz
 */
@SpringUI
public class MyVaadinUI extends UI {

    @Lazy
    @Autowired
    private ReportStatusWindow reportStatusWindow;

    @Autowired
    private LoginWindow loginWindow;

    private MenuBar.Command menuCommand(final Window window) {
        return (MenuBar.MenuItem selectedItem) -> {
            if (!window.isAttached()) {
                MyVaadinUI.this.addWindow(window);
            }
        };
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        HorizontalLayout hl = new HorizontalLayout();
        MenuBar mb = new MenuBar();
        hl.addComponent(mb);
        setContent(hl);
        loginWindow.addSuccessListener(() -> {
            reportStatusWindow.setVisible(true);
            addWindow(reportStatusWindow);
        });
        addWindow(loginWindow);
    }

}
