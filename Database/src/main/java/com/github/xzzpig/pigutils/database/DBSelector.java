package com.github.xzzpig.pigutils.database;

import com.github.xzzpig.pigutils.annotation.NotNull;
import com.github.xzzpig.pigutils.annotation.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class DBSelector {

    protected String[] columns = {"*"};
    protected boolean distinct;

    protected Table mainTable;
    protected List<Table> tables = new ArrayList<>();
    protected String where, other;
    List<Object> prepareList;

    public DBSelector(@NotNull Table table) {
        mainTable = table;
        this.tables.add(table);
    }

    public DBSelector addTable(Table table) {
        if (table.getDatabase() != mainTable.getDatabase()) {
            throw new IllegalArgumentException("arg0 is not from same DB of MainTable");
        }
        tables.add(table);
        return this;
    }

    public ResultSet select() throws SQLException {
        if (prepareList == null)
            prepareList = new ArrayList<>();
        SQLException[] exceptions = new SQLException[1];
        PreparedStatement ps = mainTable.getDatabase().getConnection().prepareStatement(this.toString());
        Database.initPrepareList(ps, prepareList, exceptions);
        ResultSet resultSet = ps.executeQuery();
        if (exceptions[0] != null)
            throw exceptions[0];
        return resultSet;
        //        PreparedStatement statement = mainTable.getDatabase().getConnection().prepareStatement(this.toString());
        //		statement.close();
        //        return statement.executeQuery(this.toString());
    }

    public DBSelector setColumns(@Nullable String... columns) {
        if (columns == null)
            return this;
        this.columns = columns;
        return this;
    }

    public DBSelector setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public DBSelector setOther(String other) {
        this.other = other;
        return this;
    }

    public DBSelector setWhere(String where, Object... wherePrepare) {
        this.where = where;
        this.prepareList = new ArrayList<>(Arrays.asList(wherePrepare));
        return this;
    }

    @Override
    public abstract String toString();
}
