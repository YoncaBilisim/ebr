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
import java.util.ArrayList;
import java.util.Collections;
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

        setContent(new VerticalLayout(
                new HorizontalLayout(btnExport, btnReload),
                grid
        ));

        fillTheGrid();
        setWidth(100, Unit.PERCENTAGE);
    }

    private Grid makeGrid() {
        Grid ret = new Grid();
        ret.setId("reportsGrid");
        ret.addColumn("uuid", String.class);
        ret.addColumn("data source", String.class);
        ret.addColumn("report", String.class);
        ret.addColumn("ext", String.class);
        ret.addColumn("started", String.class);
        ret.addColumn("ended", String.class);
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
        ret.addColumn("durum", String.class);
        ret.addColumn("exception", String.class);
        ret.setSizeFull();
        return ret;
    }

    @SuppressWarnings("ThrowableResultIgnored")
    private void fillTheGrid() {
        grid.getSelectionModel().reset();
        grid.getContainerDataSource().removeAllItems();
        ResponseEntity<List<String>> reports = reportWS.reports();
        List<ReportTask> reportTasks = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < reports.getBody().size(); i++) {
            String id = reports.getBody().get(i);
            ResponseEntity<ReportTask> task = reportWS.detail(id);
            reportTasks.add(task.getBody());
        }
        Collections.sort(reportTasks);
        reportTasks.stream().forEach((ReportTask task) -> {
            grid.addRow(
                    task.getRequest().getUuid(),
                    task.getRequest().getDatasourceName(),
                    task.getRequest().getReport(),
                    task.getRequest().getExtension(),
                    task.getStarted() == 0 ? "" : df.format(new Date(task.getStarted())),
                    task.getEnded() == 0 ? "" : df.format(new Date(task.getEnded())),
                    "İPT",
                    "GSTR",
                    task.getStatus().name(),
                    task.getException() + ""
            );
        });
        grid.recalculateColumnWidths();
    }
}
