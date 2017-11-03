package com.github.xzzpig.pigutils.database;


import com.github.xzzpig.pigutils.annotation.Nullable;

import java.lang.reflect.Field;
import java.sql.JDBCType;

public abstract class DBField {

    public @Nullable Field DaoFiled;
    public @Nullable Class<?> classType;
    private Object defaultValue;
    private String name, check;
    private boolean primaryKey, unique, notNull, autoIncrement;
    private JDBCType type = JDBCType.LONGVARCHAR;
    private int size = -1;
    private String foreign_table, foreign_key;

    public DBField() {
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public DBField setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String getName() {
        return name;
    }

    public DBField setName(String name) {
        this.name = name;
        return this;
    }

    public JDBCType getType() {
        return type;
    }

    public DBField setType(JDBCType type) {
        this.type = type;
        return this;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public DBField setNotNull(boolean notNull) {
        this.notNull = notNull;
        return this;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public DBField setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    public boolean isUnique() {
        return unique;
    }

    public DBField setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }

    @Override
    public abstract String toString();

    public int getSize() {
        return size;
    }

    public DBField setSize(int size) {
        this.size = size;
        return this;
    }

    public @Nullable String getCheck() {
        return check;
    }

    public DBField setCheck(@Nullable String check) {
        this.check = check;
        return this;
    }

    public String getForeignTable() {
        return foreign_table;
    }

    public String getForeignKey() {
        return foreign_key;
    }

    public DBField setForeign(String foreignTable, String foreignKey) {
        this.foreign_key = foreignKey;
        this.foreign_table = foreignTable;
        return this;
    }
}
