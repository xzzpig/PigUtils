package com.xzzpig.pigutils.database;

import com.xzzpig.pigutils.annotation.NotNull;
import com.xzzpig.pigutils.annotation.Nullable;
import com.xzzpig.pigutils.data.DataUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class TableConstruct {

    protected List<DBField> fields = new ArrayList<>();

    protected Table table;

    public TableConstruct() {
    }

    public TableConstruct(Table table) {
        this.table = table;
    }

    public TableConstruct addDBField(DBField field) {
        fields.add(field);
        return this;
    }

    public Table getTable() {
        return table;
    }

    public @Nullable DBField findPrimaryKeyField() {
        return this.fields.stream().filter(DBField::isPrimaryKey).findAny().orElse(null);
    }

    public @Nullable DBField findField(@NotNull String name) {
        return this.fields.stream().filter(field->name.equals(field.getName())).findAny().orElse(null);
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

    public abstract DBField createDBField(String name);

    public DBField[] getDBFields() {
        return fields.toArray(new DBField[0]);
    }
}
