/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.jdbcbridge.pool;

import com.yoncabt.abys.core.util.EBRConf;
import com.yoncabt.abys.core.util.log.FLogManager;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author myururdurmaz
 */
public class DataSource {

    private String name;
    private String url;
    private String user;
    private String pass;
    private String driver;
    private String connectionCheckQuery;
    private int minPool;
    private int maxPool;
    private boolean invalidated;
    private Deque<EBRConnection> used;
    private Deque<EBRConnection> standBy;

    private FLogManager logManager = FLogManager.getLogger(getClass());

    public DataSource(String name) throws SQLException {
        this.standBy = new ArrayDeque<>();
        this.used = new ArrayDeque<>();
        this.name = name;

        driver = EBRConf.INSTANCE.getValue("report.datasource." + name + ".driver", "");
        if (StringUtils.isEmpty(driver)) {
            throw new IllegalArgumentException(name + " driver not found");
        }

        url = EBRConf.INSTANCE.getValue("report.datasource." + name + ".url", "");
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException(name + " url not found");
        }

        user = EBRConf.INSTANCE.getValue("report.datasource." + name + ".user", "");
        if (StringUtils.isEmpty(user)) {
            throw new IllegalArgumentException(name + " user not found");
        }

        pass = EBRConf.INSTANCE.getValue("report.datasource." + name + ".pass", "");
        if (StringUtils.isEmpty(pass)) {
            throw new IllegalArgumentException(name + " pass not found");
        }

        minPool = EBRConf.INSTANCE.getValue("report.datasource." + name + ".minPool", 5);
        maxPool = EBRConf.INSTANCE.getValue("report.datasource." + name + ".maxPool", 50);
        connectionCheckQuery = EBRConf.INSTANCE.getValue("report.datasource." + name + ".connectionCheckQuery", "");

        try {
            initPool();
        } catch (SQLException ex) {
            logManager.error("ignoring connection error for startup", ex);
        }
    }

    private void initPool() throws SQLException {
        logManager.debug(name + " init min:" + minPool + ", max:" + maxPool);
        for (int i = 0; i < minPool; i++) {
            standBy.add(connect(driver, url, user, pass));
        }
    }

    public EBRConnection connect(String driver, String url, String user, String pass) throws SQLException {
        try {
            DriverManager.registerDriver((Driver) Class.forName(driver).newInstance());
            Connection connection = DriverManager.getConnection(url, user, pass);
            return new EBRConnection(this, connection);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new SQLException(ex);
        }
    }

    // bu metod biraz kötü
    private void fillHint(Connection connection, String client, String module, String action) {
        if (connection.getClass().getName().startsWith("oracle")) { //diğer veritabanları için bakmak lazım
            try {
                CallableStatement call;
                call = connection.prepareCall("call dbms_application_info.set_client_info(?)");
                call.setString(1, client);
                call.execute();

                call = connection.prepareCall("call dbms_application_info.set_module(module_name => ?, action_name => ?)");
                call.setString(1, module);
                call.setString(2, action);
                call.execute();

            } catch (SQLException ex) {
                logManager.error(ex);
            }
        }
    }

    public boolean isValid() {
        if (invalidated) {
            return false;
        }
        if (isChanged()) {
            invalidated = true;
            return false;
        }
        return true;
    }

    @SuppressWarnings("LocalVariableHidesMemberVariable")
    private boolean isChanged() {
        final String driver = EBRConf.INSTANCE.getValue("report.datasource." + name + ".driver", "");
        if (!Objects.equals(this.driver, driver)) {
            return true;
        }

        final String url = EBRConf.INSTANCE.getValue("report.datasource." + name + ".url", "");
        if (!Objects.equals(this.url, url)) {
            return true;
        }

        final String user = EBRConf.INSTANCE.getValue("report.datasource." + name + ".user", "");
        if (!Objects.equals(this.user, user)) {
            return true;
        }

        final String pass = EBRConf.INSTANCE.getValue("report.datasource." + name + ".pass", "");
        if (!Objects.equals(this.pass, pass)) {
            return true;
        }

        final String connectionCheckQuery = EBRConf.INSTANCE.getValue("report.datasource." + name + ".connectionCheckQuery", "");
        if (!Objects.equals(this.connectionCheckQuery, connectionCheckQuery)) {
            return true;
        }
        return false;
    }

    synchronized void putConnection(EBRConnection con) {
        logManager.debug(name + " PUT - " + con.getId());
        try {
            if(!con.getAutoCommit())
                con.rollback();
        } catch (SQLException ex) {
            logManager.error(ex);
        }
        used.remove(con);
        if (invalidated) {
            try {
                logManager.info(name + " INVALIDATE - " + con.getId());
                con.connection.close();
            } catch (SQLException ex) {
                logManager.error(ex);
            }
        } else {
            standBy.push(con);
        }
    }

    synchronized EBRConnection getConnection(String client, String module, String action) throws SQLException {
        EBRConnection con;
        if (standBy.isEmpty()) {
            if (used.size() < maxPool) {
                con = connect(driver, url, user, pass);
            } else {
                throw new IllegalStateException(name + " pool is full " + maxPool + "/" + maxPool);
            }
        } else {
            con = standBy.pop();
        }
        con = checkConnection(con);
        used.add(con);
        fillHint(con, client, module, action);
        logManager.debug(name + " GET - " + con.getId());
        return con;
    }

    private EBRConnection checkConnection(EBRConnection ret) throws SQLException {
        if (StringUtils.isNotBlank(connectionCheckQuery)) {
            try (Statement st = ret.createStatement(); ResultSet res = st.executeQuery(connectionCheckQuery)) {
                res.next();
                return ret;
            } catch (SQLException ex) {
                logManager.error(ex);
                return connect(driver, url, user, pass);
            }
        }
        return ret;
    }

}
