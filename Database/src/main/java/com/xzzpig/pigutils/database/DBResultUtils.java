package com.xzzpig.pigutils.database;

import com.xzzpig.pigutils.data.DataUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBResultUtils {

    private ResultSet result;

    public DBResultUtils(ResultSet result) {
        this.result = result;
    }

    public String[] getColumnNames() throws SQLException {
        String[] names = new String[result.getMetaData().getColumnCount()];
        for (int i : DataUtils.range(result.getMetaData().getColumnCount())) {
            names[i] = result.getMetaData().getColumnName(i + 1);
        }
        return names;
    }

    public ResultSet getResultSet() {
        return result;
    }

}
