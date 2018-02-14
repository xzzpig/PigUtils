package com.xzzpig.pigutils.database.impl;

import com.xzzpig.pigutils.annotation.NotNull;
import com.xzzpig.pigutils.annotation.Nullable;
import com.xzzpig.pigutils.database.TableConstruct;
import com.xzzpig.pigutils.reflect.ClassUtils;
import com.xzzpig.pigutils.reflect.MethodUtils;

import java.io.Closeable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 对应数据库实例
 *
 * @author xzzpig
 */
public class DatabaseImpl extends com.xzzpig.pigutils.database.Database implements Closeable {

    private Connection connection;
    @Nullable
    private List<String> tableNames;

    public DatabaseImpl(@NotNull Connection connection) {
        ClassUtils.checkThisConstructorArgs(connection);
        try {
            if (connection.isClosed()) {
                throw new IllegalArgumentException("connection is closed");
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
        this.connection = connection;
    }

    @NotNull
    public TableImpl createTable(@NotNull String name, @NotNull TableConstruct construct) throws SQLException {
        MethodUtils.checkArgs(DatabaseImpl.class, "createTable", name, construct);
        TableImpl table = new TableImpl(name, this);
        table.setConstruct(construct);
        String sql = "CREATE TABLE \"" + name + "\" " + construct.toString() + ";";
        connection.prepareStatement(sql).execute();
        return table;
    }

    public void execSql(String sql, List<Object> prepareList) throws SQLException {
        if (prepareList == null)
            prepareList = new ArrayList<>();
        SQLException[] exceptions = new SQLException[1];
        if (prepareList.size() == 0)
            this.withStatement(statement->{
                try {
                    statement.execute(sql);
                } catch (SQLException e) {
                    exceptions[0] = e;
                }
            });
        else {
            PreparedStatement ps = this.getConnection().prepareStatement(sql);
            initPrepareList(ps, prepareList, exceptions);
            ps.execute();
            ps.close();
        }
        if (exceptions[0] != null)
            throw exceptions[0];
    }

    @NotNull
    public Connection getConnection() {
        return connection;
    }

    @NotNull
    public TableImpl getTable(@NotNull String name) {
        return new TableImpl(name, this);
    }

    public void withStatement(@NotNull Consumer<Statement> consumer) throws SQLException {
        Statement statement = connection.createStatement();
        consumer.accept(statement);
        statement.close();
    }

    @NotNull
    public String[] getAllTableNames() {
        if (tableNames == null)
            try {
                tableNames = new ArrayList<>();
                ResultSet result = connection.getMetaData().getTables(connection.getCatalog(), null, null,
                        new String[]{"TABLE"});
                while (result.next()) {
                    tableNames.add(result.getString("TABLE_NAME"));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        return tableNames.toArray(new String[0]);
    }

    public boolean isTableExists(@NotNull String tableName) {
        if (tableNames == null)
            getAllTableNames();
        return tableNames.contains(tableName);
    }

    @Override protected com.xzzpig.pigutils.database.Table newTable(String name) {
        return new TableImpl(name, this);
    }

    @Override public TableConstruct createConstruct() {
        return new TableConstructImpl();
    }
}
