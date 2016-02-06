/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.ui;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.yoncabt.abys.core.util.YoncaGridXLSExporter;
import com.yoncabt.ebr.executor.ReportTask;
import com.yoncabt.ebr.ws.ReportWS;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author myururdurmaz
 */
@SpringUI
public class ReportStatusWindow extends Window {

    @Autowired
    private ReportWS reportWS;

    private Grid grid;

    @PostConstruct
    private void init() {
        grid = makeGrid();
        Button btnReload = new Button(FontAwesome.REFRESH);
        btnReload.addClickListener((Button.ClickEvent event) -> {
            fillTheGrid();
        });
        Button btnExport = YoncaGridXLSExporter.createDownloadButton(grid, "raporlar.xls");
        btnReload.addClickListener((Button.ClickEvent event) -> {
            fillTheGrid();
        });

        setContent(new VerticalLayout(
                new HorizontalLayout(btnExport, btnReload),
                grid
        ));

        fillTheGrid();
    }

    private Grid makeGrid() {
        Grid ret = new Grid();
        ret.setId("reportsGrid");
        ret.addColumn("uuid", String.class).setWidth(170);
        ret.addColumn("data source", String.class).setWidth(150);
        ret.addColumn("report", String.class).setWidth(250);
        ret.addColumn("ext", String.class).setWidth(40);
        ret.addColumn("started", String.class).setWidth(140);
        ret.addColumn("ended", String.class).setWidth(140);
        ret.addColumn("iptal", String.class).setRenderer(
                new ButtonRenderer((ClickableRenderer.RendererClickEvent e) -> {
                    String uuid = (String) grid.getContainerDataSource().getItem(e.getItemId()).getItemProperty("uuid").getValue();
                    reportWS.cancel(uuid);
                    Notification.show(uuid + " durduruldu");
                }));
        ret.addColumn("göster", String.class).setRenderer(
                new ButtonRenderer((ClickableRenderer.RendererClickEvent e) -> {
                    String uuid = (String) grid.getContainerDataSource().getItem(e.getItemId()).getItemProperty("uuid").getValue();
                    if (reportWS.status(uuid).getStatusCode() == HttpStatus.OK) {
                        Page.getCurrent().open("/ebr/ws/1.0/output/" + uuid, "_new", false);
                    } else {
                        Notification.show("Bitmiş bir rapor yok");
                    }
                }));
        ret.addColumn("durum", String.class).setWidth(100);
        ret.addColumn("exception", String.class).setWidth(400);
        ret.setSizeFull();
        return ret;
    }

    private void fillTheGrid() {
        grid.getSelectionModel().reset();
        grid.getContainerDataSource().removeAllItems();
        ResponseEntity<List<String>> reports = reportWS.reports();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < reports.getBody().size(); i++) {
            String id = reports.getBody().get(i);
            ResponseEntity<ReportTask> task = reportWS.detail(id);
            grid.addRow(
                    task.getBody().getRequest().getUuid(),
                    task.getBody().getRequest().getDatasourceName(),
                    task.getBody().getRequest().getReport(),
                    task.getBody().getRequest().getExtension(),
                    task.getBody().getStarted() == 0 ? "" : df.format(new Date(task.getBody().getStarted())),
                    task.getBody().getEnded() == 0 ? "" : df.format(new Date(task.getBody().getEnded())),
                    "İPT",
                    "GSTR",
                    task.getBody().getStatus().name(),
                    task.getBody().getException() + ""
            );
        }
        grid.recalculateColumnWidths();
    }
}
