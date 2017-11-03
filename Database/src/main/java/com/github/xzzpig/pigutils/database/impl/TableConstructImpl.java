package com.github.xzzpig.pigutils.database.impl;

import com.github.xzzpig.pigutils.data.DataUtils;
import com.github.xzzpig.pigutils.database.DBField;
import com.github.xzzpig.pigutils.database.Table;
import com.github.xzzpig.pigutils.database.TableConstruct;

class TableConstructImpl extends TableConstruct {

    public TableConstructImpl() {
    }

    public TableConstructImpl(TableImpl table) {
        this.table = table;
    }

    public TableConstructImpl addDBField(DBField field) {
        fields.add(field);
        return this;
    }

    public Table getTable() {
        return table;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('(');
        DataUtils.forEachWithIndex(fields, (field, i)->{
            if (i == 0)
                sb.append(field.toString());
            else
                sb.append(',').append(field.toString());
            return null;
        });
        sb.append(')');
        return sb.toString();
    }

    @Override public DBField createDBField(String name) {
        return new DBFieldImpl().setName(name);
    }
}
