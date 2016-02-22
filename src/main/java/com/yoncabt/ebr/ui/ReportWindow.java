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
import com.vaadin.shared.ui.grid.HeightMode;
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
import com.yoncabt.abys.core.util.EBRConf;
import com.yoncabt.abys.core.util.EBRParams;
import com.yoncabt.abys.core.util.YoncaGridXLSExporter;
import com.yoncabt.ebr.executor.definition.ReportDefinition;
import com.yoncabt.ebr.executor.definition.ReportParam;
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
    private Grid grid;
    private String sql;
    private SQLReport sqlreport;
    private HorizontalLayout gridLayout;
    private Button btnExport;

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
        btnReload.setDisableOnClick(true);
        btnReload.addClickListener((Button.ClickEvent event) -> {
            try {
                fillTheGrid();
            } catch (Exception ex) {
                Notification.show("Hata", Notification.Type.ERROR_MESSAGE);
                Logger.getLogger(ReportWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
            event.getButton().setEnabled(true);
        });
        gridLayout = new HorizontalLayout();
        createGrid();
        btnExport = YoncaGridXLSExporter.createDownloadButton(grid, "raporlar.xls");

        gridLayout.setSizeFull();

        window.setSizeUndefined();
        window.setContent(new VerticalLayout(
                formLayout,
                new HorizontalLayout(btnExport, btnReload),
                gridLayout
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
            String reportPath = EBRConf.INSTANCE.getValue(EBRParams.REPORTS_JRXML_PATH, "/home/myururdurmaz/reports");
            File reportDir = new File(reportPath);
            File reportFile = new File(reportDir, frag);
            if (reportFile.exists()) {
                sql = FileUtils.readFileToString(reportFile, "utf-8").trim();
                sqlreport = new SQLReport(JasperReport.getReportFile(frag));
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
        fl.removeAllComponents();
        w.setCaption(definition.getCaption());
        for (ReportParam param : definition.getReportParams()) {
            Class type = param.getType();
            AbstractField comp = null;
            if (type == Date.class) {
                DateField f = new DateField(param.getLabel());
                f.setDateFormat(param.getFormat());
                comp = f;
            } else if (type == String.class) {
                TextField f = new TextField(param.getLabel());
                comp = f;
            } else if (type == Integer.class) {
                TextField f = new TextField(param.getLabel());
                f.addValidator(new IntegerRangeValidator("Sayı kontrolü", (Integer) param.getMin(), (Integer) param.getMax()));
                comp = f;
            } else if (type == Long.class) {
                TextField f = new TextField(param.getLabel());
                f.addValidator(new LongRangeValidator("Sayı kontrolü", (Long) param.getMin(), (Long) param.getMax()));
                comp = f;
            } else {
                throw new AssertionError(param.getName() + " in tipi tanınmıyor :" + param.getType());
            }

            comp.setImmediate(true);
            comp.setValidationVisible(false);
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
        gridLayout.removeComponent(grid);
        createGrid();
        JDBCNamedParameters p = new JDBCNamedParameters(sql);
        for (ReportParam reportParam : reportDefinition.getReportParams()) {
            Class type = reportParam.getType();
            if (type == Integer.class) {
                String value = (String) findFormField(reportParam.getName()).getValue();
                if (!StringUtils.isEmpty(value)) {
                    p.set(reportParam.getName(), Integer.parseInt(value));
                }
            }
            else if (type == Long.class) {
                String value = (String) findFormField(reportParam.getName()).getValue();
                if (!StringUtils.isEmpty(value)) {
                    p.set(reportParam.getName(), Long.parseLong(value));
                }
            }
            else if (type == Long.class) {
                String value = (String) findFormField(reportParam.getName()).getValue();
                p.set(reportParam.getName(), Long.parseLong(value));
            }
            else if (type == Date.class) {
                Date value = (Date) findFormField(reportParam.getName()).getValue();
                p.set(reportParam.getName(), value);
            }
            else {
                throw new AssertionError(reportParam.getName() + " in tipi tanınmıyor :" + reportParam.getType());
            }
        }

        try (YoncaConnection con = jdbcutil.connect(reportDefinition.getDataSource());
                PreparedStatement st = p.prepare(con);
                ResultSet res = st.executeQuery()) {
            ResultSetMetaData md = res.getMetaData();
            for (int i = 0; i < md.getColumnCount(); i++) {
                Class type;
                if (md.getColumnType(i + 1) == Types.VARCHAR || md.getColumnType(i + 1) == Types.CHAR) {
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
                    if (md.getColumnType(i + 1) == Types.VARCHAR || md.getColumnType(i + 1) == Types.CHAR) {
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
        grid.recalculateColumnWidths();
    }

    private void createGrid() {
        grid = new Grid();
        grid.setWidth("800px");
        grid.setHeight("600px");
        grid.setHeightMode(HeightMode.CSS);
        gridLayout.addComponent(grid);
        if(btnExport != null)
            btnExport.setData(grid);
    }

    private MenuBar createMenuBar() throws IOException, JRException {
        MenuBar mb = new MenuBar();
        File dir = new File(EBRConf.INSTANCE.getValue(EBRParams.REPORTS_JRXML_PATH, "/home/myururdurmaz/reports"));
        createMenuBar(mb.addItem("Raporlar", null), dir);
        return mb;
    }

    private void createMenuBar(MenuBar.MenuItem mainItem, File dir) throws IOException, JRException {
        String menuText = dir.getName();
        File folderConfig = new File(dir, EBRParams.FOLDER_EBR_JSON);
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
                    String frag = StringUtils.removeStart(r.getFile().getAbsolutePath(), EBRConf.INSTANCE.getValue(EBRParams.REPORTS_JRXML_PATH, ""));
                    frag = StringUtils.removeStart(frag, "/");
                    getPage().setUriFragment(frag);
                });
            } else if (file.getName().endsWith(".jrxml")) {//FIXME support for compiled jasper files
                final JasperReport r = new JasperReport(file);
                String text = r.loadDefinition().getCaption();
                menuItem.addItem(text, (MenuBar.MenuItem selectedItem) -> {
                    System.out.println(r.getFile() + " çalıştırılacak");
                    String frag = StringUtils.removeStart(r.getFile().getAbsolutePath(), EBRConf.INSTANCE.getValue(EBRParams.REPORTS_JRXML_PATH, ""));
                    frag = StringUtils.removeStart(frag, "/");
                    getPage().setUriFragment(frag);
                });
            }
        }
    }
}
