package com.github.xzzpig.pigutils.database.impl;


import com.github.xzzpig.pigutils.database.DBField;

public class DBFieldImpl extends DBField {
    public static String autoIncrementString = "AUTOINCREMENT";

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        sb.append(' ').append(getType().getName());
        if (getSize() != -1)
            sb.append(' ').append('(').append(getSize()).append(')');
        if (isPrimaryKey())
            sb.append(' ').append("PRIMARY KEY");
        if (getForeignKey() != null && getForeignTable() != null)
            sb.append(' ').append("REFERENCES ").append(getForeignTable()).append('(').append(getForeignKey()).append(')');
        if (isUnique())
            sb.append(' ').append("UNIQUE");
        if (isNotNull())
            sb.append(' ').append("NOT NULL");
        if (getDefaultValue() != null)
            sb.append(' ').append("DEFAULT").append(' ').append('(').append(getDefaultValue()).append(')');
        if (isAutoIncrement())
            sb.append(' ').append(autoIncrementString);
        if (getCheck() != null)
            sb.append(' ').append("CHECK").append(' ').append('(').append(getCheck()).append(' ').append(')');
        return sb.toString();
    }

}
