/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.abys.report.ui;


import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 *
 * @author myururdurmaz
 */
@SpringUI
public class MyVaadinUI extends UI {

    private MenuBar.Command menuCommand(final Window window) {
        return new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                if (!window.isAttached()) {
                    MyVaadinUI.this.addWindow(window);
                }
            }
        };
    }


    @Override
    protected void init(VaadinRequest vaadinRequest) {
        HorizontalLayout hl = new HorizontalLayout();
        MenuBar mb = new MenuBar();

        hl.addComponent(mb);
        setContent(hl);
        addWindow(new Window("Pencere", new Label("s.a")));
    }

}
