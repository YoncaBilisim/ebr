package com.yoncabt.ebr.jdbcbridge.pool;

import com.yoncabt.abys.core.util.EBRConf;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 *
 * @author myururdurmaz
 */
@Component
public class DataSourceManager {

    private Map<String, DataSource> connections = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        try {
            createDataSources();
        } catch (SQLException ex) {
            throw new Error(ex);
        }
    }

    public Set<String> getDataSourceNames() {
        Set<String> ret = new HashSet<>();
        for (Map.Entry<String, String> entrySet : EBRConf.INSTANCE.getMap().entrySet()) {
            String key = entrySet.getKey();
            if (key.startsWith("report.datasource.") && key.endsWith(".url")) {
                ret.add(StringUtils.removeEnd(StringUtils.removeStart(key, "report.datasource."), ".url"));
            }
        }
        return ret;
    }

    private void createDataSources() throws SQLException {
        for (String dataSourceName : getDataSourceNames()) {
            connections.put(dataSourceName, new DataSource(dataSourceName));
        }
    }

    public EBRConnection get(String dataSourceName, String client, String module, String action) throws SQLException {
        DataSource ds = connections.get(dataSourceName);
        if(!ds.isValid()) {
            ds = new DataSource(dataSourceName);
            connections.put(dataSourceName, ds);
        }
        EBRConnection ret = ds.getConnection(client, module, action);
        if(ret == null) {
            throw new IllegalArgumentException("'" + dataSourceName + "' datasource not found");
        }
        return ret;
    }
}
