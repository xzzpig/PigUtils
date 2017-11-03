package com.github.xzzpig.pigutils.database;

import com.github.xzzpig.pigutils.annotation.NotNull;
import com.github.xzzpig.pigutils.data.DataUtils;
import com.github.xzzpig.pigutils.data.DataUtils.EachResult;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * 对应数据库实例
 *
 * @author xzzpig
 */
public abstract class Database implements Closeable {

    public static void initPrepareList(PreparedStatement ps, List<Object> prepareList, Exception[] exceptions) {
        DataUtils.forEachWithIndex(prepareList, (o, i)->{
            try {
                ps.setObject(i + 1, o);
            } catch (SQLException e) {
                exceptions[0] = e;
                return EachResult.BREAK;
            }
            return null;
        });
    }

    public abstract @NotNull Connection getConnection();

    @Override
    public void close() throws IOException {
        try {
            getConnection().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isTableExists(@NotNull String tableName) {
        return Arrays.stream(getAllTableNames()).anyMatch(str->str.equalsIgnoreCase(tableName));
    }

    @NotNull
    public Table createTable(@NotNull String name, @NotNull TableConstruct construct) throws SQLException {
        Table table = newTable(name);
        table.setConstruct(construct);
        String sql = "CREATE TABLE \"" + name + "\" " + construct.toString() + ";";
        getConnection().prepareStatement(sql).execute();
        return table;
    }

    protected abstract @NotNull Table newTable(@NotNull String name);

    public abstract @NotNull Table getTable(@NotNull String name);

    public abstract @NotNull String[] getAllTableNames();

    public abstract TableConstruct createConstruct();
}
