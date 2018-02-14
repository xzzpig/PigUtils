package com.xzzpig.pigutils.database.impl;

import com.xzzpig.pigutils.data.DataUtils;
import com.xzzpig.pigutils.database.DBSelector;
import com.xzzpig.pigutils.database.Table;

public class DBSelectorImpl extends DBSelector {
    public DBSelectorImpl(Table table) {
        super(table);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("SELECT ");
        if (distinct)
            sb.append("DISTINCT ");
        DataUtils.forEachWithIndex(columns, (str, i)->{
            if (i == 0)
                sb.append(str);
            else
                sb.append(',').append(str);
            return null;
        });
        sb.append(" FROM ");
        DataUtils.forEachWithIndex(tables, (table, i)->{
            if (i == 0)
                sb.append(table.getName());
            else
                sb.append(',').append(table.getName());
            return null;
        });
        if (other != null)
            sb.append(' ').append(other);
        if (where != null)
            sb.append(" WHERE ").append(where);
        return sb.toString();
    }
}
