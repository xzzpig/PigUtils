package com.xzzpig.pigutils.database;

import com.xzzpig.pigutils.annotation.BaseOnClass;
import com.xzzpig.pigutils.annotation.NotNull;
import com.xzzpig.pigutils.annotation.Nullable;
import com.xzzpig.pigutils.data.DataUtils;

import java.sql.SQLException;
import java.util.Map;

@BaseOnClass(DataUtils.class)
public interface Table {
    Table delete() throws SQLException;

    Table delete(@NotNull Map<String, Object> where) throws SQLException;

    Table delete(@NotNull String where) throws SQLException;

    Table drop() throws SQLException;

    TableConstruct getConstruct();

    Table setConstruct(TableConstruct construct);

    Database getDatabase();

    int getLastUpdateNum();

    String getName();

    Table insert(Map<String, Object> map) throws SQLException;

    Table insert(Object... values) throws SQLException;

    DBSelector select();

    Table update(@NotNull Map<String, Object> map, @Nullable String where, @Nullable Object... wherePrepare) throws SQLException;

    Table queryConstruct();

    /**
     * 更新表的结构
     */
    Table updateConstruct(TableConstruct construct) throws SQLException;
}
