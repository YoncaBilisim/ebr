/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.ui;

import com.vaadin.data.validator.DoubleRangeValidator;
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
import com.yoncabt.ebr.FieldType;
import com.yoncabt.ebr.executor.BaseReport;
import com.yoncabt.ebr.executor.definition.ReportDefinition;
import com.yoncabt.ebr.executor.definition.ReportParam;
import com.yoncabt.ebr.executor.jasper.JasperReport;
import com.yoncabt.ebr.executor.sql.SQLReport;
import com.yoncabt.ebr.jdbcbridge.JDBCNamedParameters;
import com.yoncabt.ebr.jdbcbridge.JDBCUtil;
import com.yoncabt.ebr.jdbcbridge.YoncaConnection;
import com.yoncabt.ebr.util.ResultSetDeserializer;
import com.yoncabt.ebr.util.ResultSetSerializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
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
    private BaseReport sqlreport;
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
                if (FilenameUtils.getExtension(reportFile.getName()).equalsIgnoreCase("sql")) {
                    sql = FileUtils.readFileToString(reportFile, "utf-8").trim();
                    sqlreport = new SQLReport();
                    reportDefinition = ((SQLReport) sqlreport).loadDefinition(JasperReport.getReportFile(frag));
                } else if (FilenameUtils.getExtension(reportFile.getName()).equalsIgnoreCase("jrxml")) {
                    sql = "";
                    sqlreport = new JasperReport();
                    reportDefinition = ((JasperReport) sqlreport).loadDefinition(JasperReport.getReportFile(frag));
                } else {
                    Notification.show(frag + " bilinmeyen rapor türü", Notification.Type.ERROR_MESSAGE);
                }
                showFields(reportDefinition, window, formLayout);
            } else {
                Notification.show(frag + " raporu sisteminizde yok", Notification.Type.ERROR_MESSAGE);
            }
        } catch (IOException | JRException ex) {
            Notification.show("Hata", Notification.Type.ERROR_MESSAGE);
            Logger.getLogger(ReportWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private ReportDefinition reportDefinition;

    private void showFields(ReportDefinition definition, final Window w, final FormLayout fl) throws AssertionError, JSONException {
        fl.removeAllComponents();
        w.setCaption(definition.getCaption());
        for (ReportParam param : definition.getReportParams()) {
            AbstractField comp = null;
            switch (param.getFieldType()) {
                case STRING: {
                    TextField f = new TextField(param.getLabel());
                    comp = f;
                    break;
                }
                case INTEGER: {
                    TextField f = new TextField(param.getLabel());
                    f.addValidator(new IntegerRangeValidator("Sayı kontrolü", (Integer) param.getMin(), (Integer) param.getMax()));
                    comp = f;
                    break;
                }
                case LONG: {
                    TextField f = new TextField(param.getLabel());
                    f.addValidator(new LongRangeValidator("Sayı kontrolü", (Long) param.getMin(), (Long) param.getMax()));
                    comp = f;
                    break;
                }
                case DOUBLE: {
                    TextField f = new TextField(param.getLabel());
                    f.addValidator(new DoubleRangeValidator("Sayı kontrolü", (Double) param.getMin(), (Double) param.getMax()));
                    comp = f;
                    break;
                }
                case DATE: {
                    DateField f = new DateField(param.getLabel());
                    f.setDateFormat(param.getFormat());
                    comp = f;
                    break;
                }
                default: {
                    throw new AssertionError(param.getName() + " in tipi tanınmıyor :" + param.getJavaType());
                }
            }
            if (param.getDefaultValue() != null) {
                comp.setValue(param.getDefaultValue());
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

    private void fillTheGrid() throws SQLException, IOException {
        gridLayout.removeComponent(grid);
        createGrid();
        for (ReportParam reportParam : reportDefinition.getReportParams()) {
            if (reportParam.isRaw()) {
                String value = (String) findFormField(reportParam.getName()).getValue();
                value = StringEscapeUtils.escapeSql(value);
                Pattern pattern = Pattern.compile(":\\b" + reportParam.getName() + "\\b");
                sql = pattern.matcher(sql).replaceAll(value);
            }
        }
        JDBCNamedParameters p = new JDBCNamedParameters(sql);
        for (ReportParam reportParam : reportDefinition.getReportParams()) {
            if (reportParam.isRaw()) {
                //
            } else {
                FieldType type = reportParam.getFieldType();
                switch (type) {
                    case STRING: {
                        String value = (String) findFormField(reportParam.getName()).getValue();
                        if (!StringUtils.isEmpty(value)) {
                            p.set(reportParam.getName(), value);
                        }
                        break;
                    }
                    case INTEGER: {
                        String value = (String) findFormField(reportParam.getName()).getValue();
                        if (!StringUtils.isEmpty(value)) {
                            p.set(reportParam.getName(), Integer.parseInt(value));
                        }
                        break;
                    }
                    case LONG: {
                        String value = (String) findFormField(reportParam.getName()).getValue();
                        if (!StringUtils.isEmpty(value)) {
                            p.set(reportParam.getName(), Long.parseLong(value));
                        }
                        break;
                    }
                    case DOUBLE: {
                        String value = (String) findFormField(reportParam.getName()).getValue();
                        p.set(reportParam.getName(), Double.parseDouble(value));
                        break;
                    }
                    case DATE: {
                        Date value = (Date) findFormField(reportParam.getName()).getValue();
                        p.set(reportParam.getName(), value);
                        break;
                    }
                    default:
                        throw new AssertionError(reportParam.getName() + " in tipi tanınmıyor :" + reportParam.getFieldType());
                }
            }
        }

        try (YoncaConnection con = jdbcutil.connect(StringUtils.defaultIfEmpty(reportDefinition.getDataSource(), "default"));
                PreparedStatement st = p.prepare(con);
                ResultSet res = st.executeQuery()) {
            File file = File.createTempFile("ebr_ser_", ".json");
            ResultSetSerializer ser = new ResultSetSerializer(res, file);
            ser.serialize();
            FileInputStream fis = new FileInputStream(file);
            ResultSetDeserializer des = new ResultSetDeserializer(fis);
            List<String> names = des.getNames();
            List<FieldType> types = des.getTypes();
            for (int i = 0; i < types.size(); i++) {
                Class type = types.get(i).getJavaType();
                String name = names.get(i);
                grid.addColumn(name, type);
            }
            List<Object[]> data = des.getData();
            data.stream().forEach((d) -> {
                grid.addRow(d);
            });
        }
        grid.recalculateColumnWidths();
    }

    private void createGrid() {
        grid = new Grid();
        grid.setWidth("800px");
        grid.setHeight("600px");
        grid.setHeightMode(HeightMode.CSS);
        gridLayout.addComponent(grid);
        if (btnExport != null) {
            btnExport.setData(grid);
        }
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
            try {
                JSONObject jo = new JSONObject(FileUtils.readFileToString(folderConfig, "utf-8"));
                menuText = jo.optString("label", menuText);
            } catch (IOException ex) {
                throw new IOException(folderConfig.getAbsolutePath(), ex);
            } catch (JSONException ex) {
                throw new JSONException(new RuntimeException(folderConfig.getAbsolutePath(), ex));
            }
        }
        MenuBar.MenuItem menuItem = mainItem.addItem(menuText, null);

        for (File file : dir.listFiles()) {
            if (file.getName().charAt(0) == '.') {
                continue;//gereksiz ama olduğu belli olsun
            } else if (file.isDirectory()) {
                createMenuBar(menuItem, file);
            } else if (file.getName().endsWith(".sql")) {
                final SQLReport r = new SQLReport();
                String text = r.loadDefinition(file).getCaption();
                menuItem.addItem(text, (MenuBar.MenuItem selectedItem) -> {
                    System.out.println(r.getFile() + " çalıştırılacak");
                    String frag = StringUtils.removeStart(r.getFile().getAbsolutePath(), EBRConf.INSTANCE.getValue(EBRParams.REPORTS_JRXML_PATH, ""));
                    frag = StringUtils.removeStart(frag, "/");
                    getPage().setUriFragment(frag);
                });
            } else if (file.getName().endsWith(".jrxml")) {//FIXME support for compiled jasper files
                final JasperReport r = new JasperReport();
                String text = r.loadDefinition(file).getCaption();
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
