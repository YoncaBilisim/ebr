/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.ui;

import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.data.validator.LongRangeValidator;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.yoncabt.abys.core.util.ABYSConf;
import com.yoncabt.abys.core.util.YoncaGridXLSExporter;
import com.yoncabt.ebr.executor.definition.ReportDefinition;
import com.yoncabt.ebr.executor.definition.ReportParam;
import com.yoncabt.ebr.executor.definition.ReportType;
import com.yoncabt.ebr.executor.jasper.JasperReport;
import com.yoncabt.ebr.executor.sql.SQLReport;
import com.yoncabt.ebr.jdbcbridge.JDBCNamedParameters;
import com.yoncabt.ebr.jdbcbridge.JDBCUtil;
import com.yoncabt.ebr.jdbcbridge.YoncaConnection;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author myururdurmaz
 */
@SpringUI(path = "/report/ui")
public class ReportWindow extends UI {

    @Autowired
    private JDBCUtil jdbcutil;

    private Window window = new Window("BAŞLIKSIZ");
    private FormLayout formLayout = new FormLayout();
    private Grid grid = new Grid();
    private String sql;
    private SQLReport sqlreport;

    @Override
    protected void init(VaadinRequest request) {

        try {
            MenuBar mb = createMenuBar();
            HorizontalLayout hl = new HorizontalLayout(mb);
            setContent(hl);
        } catch (IOException | JRException ex) {
            Notification.show("Hata", Notification.Type.ERROR_MESSAGE);
            Logger.getLogger(ReportWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        Button btnReload = new Button(FontAwesome.LIST_ALT);
        btnReload.addClickListener((Button.ClickEvent event) -> {
            try {
                fillTheGrid();
            } catch (SQLException ex) {
                Notification.show("Hata", Notification.Type.ERROR_MESSAGE);
                Logger.getLogger(ReportWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        Button btnExport = YoncaGridXLSExporter.createDownloadButton(grid, "raporlar.xls");

        window.setContent(new VerticalLayout(
                formLayout,
                new HorizontalLayout(btnExport, btnReload),
                grid
        ));
        window.setClosable(false);
        addWindow(window);
        window.center();

        getPage().addUriFragmentChangedListener((Page.UriFragmentChangedEvent event) -> {
            String frag = event.getUriFragment();
            ReportWindow.this.uriFragmentChanged(frag);
        });
        if (StringUtils.isNotEmpty(getPage().getUriFragment())) {
            ReportWindow.this.uriFragmentChanged(getPage().getUriFragment());
        }
    }

    private void uriFragmentChanged(String frag) {
        try {
            ReportType type = frag.endsWith(".sql") ? ReportType.SQL : ReportType.JASPER;
            String reportPath = type == ReportType.SQL
                    ? ABYSConf.INSTANCE.getValue("report.sql.path", "/home/myururdurmaz/reports/sql")
                    : ABYSConf.INSTANCE.getValue("report.jrxml.path", "/home/myururdurmaz/reports");
            File reportDir = new File(reportPath);
            File reportFile = new File(reportDir, frag);
            if (reportFile.exists()) {
                sql = FileUtils.readFileToString(reportFile, "utf-8").trim();
                sqlreport = new SQLReport(JasperReport.getSqlFile(frag));
                reportDefinition = sqlreport.loadDefinition();
                showFields(reportDefinition, window, formLayout);
            } else {
                Notification.show(frag + " raporu sisteminizde yok", Notification.Type.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            Notification.show("Hata", Notification.Type.ERROR_MESSAGE);
            Logger.getLogger(ReportWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private ReportDefinition reportDefinition;

    private void showFields(ReportDefinition definition, final Window w, final FormLayout fl) throws AssertionError, JSONException {
        w.setCaption(definition.getCaption());
        for (ReportParam param : definition.getReportParams()) {
            Class type = param.getType();
            AbstractField comp = null;
            if (type == Date.class) {
                DateField f = new DateField(param.getLabel());
                f.setDateFormat(param.getFormat());
                f.setImmediate(true);
                comp = f;
            } else if (type == String.class) {
                TextField f = new TextField(param.getLabel());
                f.setImmediate(true);
                comp = f;
            } else if (type == Integer.class) {
                TextField f = new TextField(param.getLabel());
                f.addValidator(new IntegerRangeValidator("Sayı kontrolü", (Integer) param.getMin(), (Integer) param.getMax()));
                f.setImmediate(true);
                comp = f;
            } else if (type == Long.class) {
                TextField f = new TextField(param.getLabel());
                f.addValidator(new LongRangeValidator("Sayı kontrolü", (Long) param.getMin(), (Long) param.getMax()));
                f.setImmediate(true);
                comp = f;
            } else {
                throw new AssertionError(type);
            }

            comp.setId(param.getName());
            fl.addComponent(comp);

        }
    }

    public AbstractField findFormField(String id) {
        for (int i = 0; i < formLayout.getComponentCount(); i++) {
            if (id.equals(formLayout.getComponent(i).getId())) {
                return (AbstractField) formLayout.getComponent(i);
            }
        }
        return null;
    }

    private void fillTheGrid() throws SQLException {
        grid.removeAllColumns();
        JDBCNamedParameters p = new JDBCNamedParameters(sql);
        for (ReportParam reportParam : reportDefinition.getReportParams()) {
            Class type = reportParam.getType();
            if (type == Integer.class) {
                String value = (String) findFormField(reportParam.getName()).getValue();
                if (!StringUtils.isEmpty(value)) {
                    p.set(reportParam.getName(), Integer.parseInt(value));
                }
            }
            if (type == Long.class) {
                String value = (String) findFormField(reportParam.getName()).getValue();
                if (!StringUtils.isEmpty(value)) {
                    p.set(reportParam.getName(), Long.parseLong(value));
                }
                break;
            }
            if (type == Long.class) {
                String value = (String) findFormField(reportParam.getName()).getValue();
                p.set(reportParam.getName(), Long.parseLong(value));
                break;
            }
            if (type == Date.class) {
                Date value = (Date) findFormField(reportParam.getName()).getValue();
                p.set(reportParam.getName(), value);
                break;
            }
        }

        try (YoncaConnection con = jdbcutil.connect(reportDefinition.getDataSource());
                PreparedStatement st = p.prepare(con);
                ResultSet res = st.executeQuery()) {
            ResultSetMetaData md = res.getMetaData();
            for (int i = 0; i < md.getColumnCount(); i++) {
                Class type;
                if (md.getColumnType(i + 1) == Types.VARCHAR) {
                    type = String.class;
                } else if (md.getColumnType(i + 1) == Types.DATE) {
                    type = Date.class;
                } else if (md.getColumnType(i + 1) == Types.NUMERIC) {
                    if (md.getScale(i + 1) > 0) {
                        type = Double.class;
                    } else if (md.getPrecision(i + 1) > 9) {
                        type = Long.class;
                    } else {
                        type = Integer.class;
                    }
                } else {
                    throw new AssertionError(md.getColumnTypeName(i + 1));
                }
                grid.addColumn(md.getColumnName(i + 1), type);
            }
            while (res.next()) {
                Object values[] = new Object[md.getColumnCount()];
                for (int i = 0; i < md.getColumnCount(); i++) {
                    if (md.getColumnType(i + 1) == Types.VARCHAR) {
                        values[i] = res.getString(i + 1);
                    } else if (md.getColumnType(i + 1) == Types.DATE) {
                        values[i] = res.getDate(i + 1);
                    } else if (md.getColumnType(i + 1) == Types.NUMERIC) {
                        if (md.getScale(i + 1) > 0) {
                            values[i] = res.getDouble(i + 1);
                        } else if (md.getPrecision(i + 1) > 9) {
                            values[i] = res.getLong(i + 1);
                        } else {
                            values[i] = res.getInt(i + 1);
                        }
                    } else {
                        throw new AssertionError(md.getColumnTypeName(i + 1));
                    }
                }
                grid.addRow(values);
            }
        }
    }

    private MenuBar createMenuBar() throws IOException, JRException {
        MenuBar mb = new MenuBar();
        File dir = new File(ABYSConf.INSTANCE.getValue("report.jrxml.path", "/home/myururdurmaz/reports"));
        createMenuBar(mb.addItem("Raporlar", null), dir);
        return mb;
    }

    private void createMenuBar(MenuBar.MenuItem mainItem, File dir) throws IOException, JRException {
        String menuText = dir.getName();
        File folderConfig = new File(dir, "folder.ebr.json");
        if (folderConfig.exists()) {
            JSONObject jo = new JSONObject(FileUtils.readFileToString(folderConfig, "utf-8"));
            menuText = jo.optString("label", menuText);
        }
        MenuBar.MenuItem menuItem = mainItem.addItem(menuText, null);

        for (File file : dir.listFiles()) {
            if (file.getName().charAt(0) == '.') {
                continue;//gereksiz ama olduğu belli olsun
            } else if (file.isDirectory()) {
                createMenuBar(menuItem, file);
            } else if (file.getName().endsWith(".sql")) {
                final SQLReport r = new SQLReport(file);
                String text = r.loadDefinition().getCaption();
                menuItem.addItem(text, (MenuBar.MenuItem selectedItem) -> {
                    System.out.println(r.getFile() + " çalıştırılacak");
                });
            } else if (file.getName().endsWith(".jrxml")) {//FIXME support for compiled jasper files
                final JasperReport r = new JasperReport(file.getAbsolutePath());
                String text = r.loadDefinition().getCaption();
                menuItem.addItem(text, (MenuBar.MenuItem selectedItem) -> {
                    System.out.println(r.getName() + " çalıştırılacak");
                });
            }
        }
    }
}
