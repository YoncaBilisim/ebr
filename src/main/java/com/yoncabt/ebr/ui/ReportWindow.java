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
import com.vaadin.ui.ComboBox;
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
import com.yoncabt.ebr.ReportIDGenerator;
import com.yoncabt.ebr.ReportOutputFormat;
import com.yoncabt.ebr.ReportRequest;
import com.yoncabt.ebr.ReportService;
import com.yoncabt.ebr.executor.BaseReport;
import com.yoncabt.ebr.executor.ReportTask;
import com.yoncabt.ebr.executor.YoncaMailSender;
import com.yoncabt.ebr.executor.definition.ReportDefinition;
import com.yoncabt.ebr.executor.definition.ReportParam;
import com.yoncabt.ebr.executor.jasper.JasperReport;
import com.yoncabt.ebr.executor.sql.SQLReport;
import com.yoncabt.ebr.jdbcbridge.pool.DataSourceManager;
import com.yoncabt.ebr.jdbcbridge.pool.EBRConnection;
import com.yoncabt.ebr.logger.ReportLogger;
import com.yoncabt.ebr.util.ResultSetDeserializer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
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
import org.springframework.context.ApplicationContext;

/**
 *
 * @author myururdurmaz
 */
@SpringUI(path = "/report/ui")
public class ReportWindow extends UI {

    private Window window = new Window("BAŞLIKSIZ");
    private FormLayout formLayout = new FormLayout();
    private Grid grid;
    private String sql;
    private String reportName;
    private BaseReport report;
    private HorizontalLayout gridLayout;
    private Button btnExport;
    private ComboBox reportType = new ComboBox("Rapor Tipi");
    private ComboBox reportLocale = new ComboBox("Dil");
    private TextField email = new TextField("email");
    private ReportDefinition reportDefinition;

    @Autowired
    private DataSourceManager dataSourceManager;
    @Autowired
    private ReportIDGenerator reportIDGenerator;
    @Autowired
    private ReportLogger reportLogger;
    @Autowired
    private SQLReport sqlReport;
    @Autowired
    private JasperReport jasperReport;
    @Autowired
    private YoncaMailSender mailSender;
    @Autowired
    private ReportService reportService;
    @Autowired
    private ApplicationContext context;

    @Override
    protected void init(VaadinRequest request) {
        reportType.setNullSelectionAllowed(false);

        reportLocale.setNullSelectionAllowed(false);

        reportLocale.addItem("tr_TR");
        reportLocale.setItemCaption("tr_TR", "Türkçe");

        reportLocale.addItem("en_US");
        reportLocale.setItemCaption("en_US", "English");

        email.setEnabled(mailSender.isConfigured());

        grid = new Grid();

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
        if (report instanceof SQLReport) {
            createGrid();
        }
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
                    report = sqlReport;
                    reportDefinition = ((SQLReport) report).loadDefinition(JasperReport.getReportFile(frag));
                } else if (FilenameUtils.getExtension(reportFile.getName()).equalsIgnoreCase("jrxml")) {
                    sql = "";
                    report = jasperReport;
                    reportDefinition = ((JasperReport) report).loadDefinition(JasperReport.getReportFile(frag));
                } else {
                    Notification.show(frag + " bilinmeyen rapor türü", Notification.Type.ERROR_MESSAGE);
                }
                this.reportName = frag;
                showFields(reportDefinition, window, formLayout);
            } else {
                Notification.show(frag + " raporu sisteminizde yok", Notification.Type.ERROR_MESSAGE);
            }
        } catch (IOException | JRException ex) {
            Notification.show("Hata", Notification.Type.ERROR_MESSAGE);
            Logger.getLogger(ReportWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showFields(ReportDefinition definition, final Window w, final FormLayout fl) throws AssertionError, JSONException {
        fl.removeAllComponents();
        w.setCaption(definition.getCaption());
        for (ReportParam param : definition.getReportParams()) {
            AbstractField comp = null;
            if (param.getInputType() == InputType.COMBO) {
                ComboBox f = new ComboBox(param.getLabel());
                param.getLovData().forEach((k, v) -> {
                    f.addItem(k);
                    f.setItemCaption(k, (String) v);
                });
                comp = f;
            } else {
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
            }
            if (param.getDefaultValue() != null) {
                comp.setValue(param.getDefaultValue());
            }
            comp.setImmediate(true);
            comp.setValidationVisible(false);
            comp.setId(param.getName());
            fl.addComponent(comp);

        }
        if (report instanceof SQLReport) {
            reportType.addItem(ReportOutputFormat.xls);
            reportType.setItemCaption(ReportOutputFormat.xls, ReportOutputFormat.xls.getTypeName());
        } else {
            for (ReportOutputFormat value : ReportOutputFormat.values()) {
                reportType.addItem(value);
                reportType.setItemCaption(value, value.getTypeName());
            }
        }
        reportType.setValue(ReportOutputFormat.xls);
        fl.addComponent(reportType);
        fl.addComponent(reportLocale);
        fl.addComponent(email);
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
        ReportRequest request = new ReportRequest();
        request.setUuid(reportIDGenerator.generate());
        request.setLocale((String) reportLocale.getValue());
        request.setDatasourceName(reportDefinition.getDataSource());
        request.setExtension(((ReportOutputFormat) reportType.getValue()).name());
        request.setAsync(false);
        request.setEmail(email.getValue());
        String dataSourceName = StringUtils.defaultIfEmpty(reportDefinition.getDataSource(), "default");
        for (ReportParam reportParam : reportDefinition.getReportParams()) {
            if (reportParam.isRaw()) {
                String value = (String) findFormField(reportParam.getName()).getValue();
                value = StringEscapeUtils.escapeSql(value);
                Pattern pattern = Pattern.compile(":\\b" + reportParam.getName() + "\\b");
                sql = pattern.matcher(sql).replaceAll(value);
            } else {
                Object value = findFormField(reportParam.getName()).getValue();
                request.getReportParams().put(reportParam.getName(), value);
            }
        }
        request.setReportQuery(sql);
        request.setReport(reportName);
        try (EBRConnection con = dataSourceManager.get(dataSourceName, "EBR", "SQL", reportDefinition.getFile().getAbsolutePath());) {
            if (report instanceof JasperReport) {
                fillTheGridJRXML(request, con);
            } else if (report instanceof SQLReport) {
                fillTheGridSQL(request, con);
            } else {
                throw new IllegalArgumentException(String.valueOf(report));
            }
        }
    }

    private void fillTheGridJRXML(ReportRequest request, EBRConnection con) throws SQLException, IOException {
        ReportTask task = reportService.request(request);
        if (task.getException() != null) {
            Notification.show("Hata", Notification.Type.ERROR_MESSAGE);
        } else {
            Page.getCurrent().open("/ebr/ws/1.0/output/" + request.getUuid(), "_blank");
        }
    }

    private void fillTheGridSQL(ReportRequest request, EBRConnection con) throws SQLException, IOException {
        createGrid();
        report.exportTo(request, null, con, reportDefinition);
        byte[] buf = reportLogger.getReportData(request.getUuid());
        ResultSetDeserializer des = new ResultSetDeserializer(new ByteArrayInputStream(buf));
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
                context.getAutowireCapableBeanFactory().autowireBean(r);
                String text = r.loadDefinition(file).getCaption();
                menuItem.addItem(text, (MenuBar.MenuItem selectedItem) -> {
                    System.out.println(r.getFile() + " çalıştırılacak");
                    String frag = StringUtils.removeStart(r.getFile().getAbsolutePath(), EBRConf.INSTANCE.getValue(EBRParams.REPORTS_JRXML_PATH, ""));
                    frag = StringUtils.removeStart(frag, "/");
                    getPage().setUriFragment(frag);
                });
            } else if (file.getName().endsWith(".jrxml")) {//FIXME support for compiled jasper files
                final JasperReport r = new JasperReport();
                context.getAutowireCapableBeanFactory().autowireBean(r);
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
