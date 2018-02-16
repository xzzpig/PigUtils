package com.xzzpig.pigutils.database.impl;

import com.xzzpig.pigutils.annotation.BaseOnClass;
import com.xzzpig.pigutils.annotation.NotNull;
import com.xzzpig.pigutils.data.DataUtils;
import com.xzzpig.pigutils.database.*;

import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

@BaseOnClass(DataUtils.class)
public class TableImpl implements com.xzzpig.pigutils.database.Table {
    /**
     * 更新表结构时的临时表
     */
    public static String TABLE_UPDATECONSTRUCT_TEMP = "temp_table_pigapi_" + (int) (100 * Math.random());
    private TableConstruct construct;
    private DatabaseImpl db;
    private String name;
    private int updateNum;

    public TableImpl(String name, DatabaseImpl db) {
        this.name = name;
        this.db = db;
    }

    public TableImpl delete() throws SQLException {
        db.execSql("DELETE FROM \"" + name + "\"", null);
        return this;
    }

    public TableImpl delete(@NotNull Map<String, Object> where) throws SQLException {
        StringBuilder wheresb = new StringBuilder();
        int j = 0;
        List<Object> objs = new ArrayList<>();
        addObject(where, wheresb, j, objs);
        db.execSql("DELETE FROM \"" + name + "\" WHERE " + wheresb, objs);
        return this;
    }

    private void addObject(@NotNull Map<String, Object> where, StringBuilder wheresb, int j, List<Object> objs) {
        for (Entry<String, Object> entry : where.entrySet()) {
            Object obj = entry.getValue();
            String str;
            objs.add(obj);
            str = "?";
            if (j == 0)
                wheresb.append(entry.getKey()).append(" = ").append(str);
            else
                wheresb.append(',').append(entry.getKey()).append(" = ").append(str);
            j++;
        }
    }

    public TableImpl delete(@NotNull String where) throws SQLException {
        db.execSql("DELETE FROM \"" + name + "\" WHERE " + where, null);
        return this;
    }

    public TableImpl drop() throws SQLException {
        db.getConnection().prepareStatement("DROP TABLE \"" + name + "\"").execute();
        return this;
    }

    public TableConstruct getConstruct() {
        return construct;
    }

    @Override public com.xzzpig.pigutils.database.Table setConstruct(TableConstruct construct) {
        this.construct = construct;
        return this;
    }

    public DatabaseImpl getDatabase() {
        return db;
    }

    public int getLastUpdateNum() {
        return updateNum;
    }

    public String getName() {
        return name;
    }

    public TableImpl insert(Map<String, Object> map) throws SQLException {
        StringBuilder sb = new StringBuilder("INSERT INTO \"").append(name).append('\"');
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        int j = 0;
        List<Object> objs = new ArrayList<>();
        for (Entry<String, Object> entry : map.entrySet()) {
            if (j == 0)
                key.append(entry.getKey());
            else
                key.append(',').append(entry.getKey());
            Object obj = entry.getValue();
            String str;
            // if (obj.getClass() == DBFieldType.Blob.targetClazz) {
            objs.add(obj);
            str = "?";
            // } else if (obj.getClass() == DBFieldType.Int.targetClazz) {
            // str = "" + obj;
            // } else if (obj.getClass() == DBFieldType.Double.targetClazz) {
            // str = "" + obj;
            // } else {
            // str = "\"" + obj + "\"";
            // }
            if (j == 0)
                value.append(str);
            else
                value.append(",").append(str);
            j++;
        }
        sb.append('(').append(key).append(')').append(" VALUES (").append(value).append(");");
        db.execSql(sb.toString(), objs);
        return this;
    }

    public TableImpl insert(Object... values) throws SQLException {
        StringBuffer sb = new StringBuffer("INSERT INTO \"");
        sb.append(name).append('\"').append(" VALUES (");
        List<Object> objs = new ArrayList<>();
        DataUtils.forEachWithIndex(values, (obj, i)->{
            String str;
            // if (obj.getClass() == DBFieldType.Blob.targetClazz) {
            objs.add(obj);
            str = "?";
            // } else if (obj.getClass() == DBFieldType.Int.targetClazz) {
            // str = "" + obj;
            // } else if (obj.getClass() == DBFieldType.Double.targetClazz) {
            // str = "" + obj;
            // } else {
            // str = "\"" + obj + "\"";
            // }
            if (i == 0)
                sb.append(str);
            else
                sb.append(",").append(str);
            return null;
        });
        sb.append(')').append(';');
        db.execSql(sb.toString(), objs);
        return this;
    }

    public DBSelector select() {
        return new DBSelectorImpl(this);
    }

    @Override
    public TableImpl update(Map<String, Object> map, String where, Object... wherePrepare) throws SQLException {
        StringBuffer sb = new StringBuffer("UPDATE \"" + name + "\" SET ");
        StringBuilder sets = new StringBuilder();

        int j = 0;
        List<Object> objs = new ArrayList<>();
        addObject(map, sets, j, objs);
        if (where != null) {
            sb.append(sets).append(" WHERE ").append(where);
            if (wherePrepare != null)
                Collections.addAll(objs, wherePrepare);
        }
        SQLException[] exceptions = new SQLException[1];
        if (objs.size() == 0)
            db.withStatement(statement->{
                try {
                    updateNum = statement.executeUpdate(sb.toString());
                } catch (SQLException e) {
                    exceptions[0] = e;
                }
            });
        else {
            PreparedStatement ps = db.getConnection().prepareStatement(sb.toString());
            Database.initPrepareList(ps, objs, exceptions);
            updateNum = ps.executeUpdate();
            ps.close();
        }
        if (exceptions[0] != null)
            throw exceptions[0];
        return this;
    }

    public TableImpl queryConstruct() {
        PreparedStatement pst = null;
        try {
            pst = getDatabase().getConnection().prepareStatement("select * from \"" + getName() + "\" where 1=2");
            ResultSetMetaData rsd = pst.executeQuery().getMetaData();
            TableConstruct construct = getDatabase().createConstruct();
            DatabaseMetaData dbMeta = getDatabase().getConnection().getMetaData();
            ResultSet pkRSet = dbMeta.getPrimaryKeys(getDatabase().getConnection().getCatalog(), null, getName());
            List<String> pk = new ArrayList<>();
            while (pkRSet.next()) {
                pk.add(pkRSet.getString(6));
            }
            pkRSet.close();
            DBField dbField;
            for (int i = 1; i <= rsd.getColumnCount(); i++) {
                dbField = construct.createDBField(rsd.getColumnName(i));
                dbField.setName(rsd.getColumnName(i));
                dbField.setType(JDBCType.valueOf(rsd.getColumnType(i)));
                dbField.setAutoIncrement(rsd.isAutoIncrement(i));
                if (rsd.isNullable(i) == ResultSetMetaData.columnNoNulls)
                    dbField.setNotNull(true);
                else if (rsd.isNullable(i) == ResultSetMetaData.columnNullable)
                    dbField.setNotNull(false);
                if (pk.contains(dbField.getName()))
                    dbField.setPrimaryKey(true);
                construct.addDBField(dbField);
            }
            setConstruct(construct);
            return this;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (pst != null)
                    pst.close();
                pst = null;
            } catch (Exception ignored) {
            }
        }
    }

    @Override public Table updateConstruct(TableConstruct tableConstruct) throws SQLException {
        queryConstruct();
        try {
            getDatabase().execSql("PRAGMA foreign_keys = 0", null);
            try {
                getDatabase().execSql("CREATE TABLE " + TABLE_UPDATECONSTRUCT_TEMP + " AS SELECT * FROM " + getName(), null);
                getDatabase().execSql("DROP TABLE " + getName(), null);
                getDatabase().createTable(getName(), tableConstruct);
                Set<String> nameSet = new HashSet<>(), nameSet2 = new HashSet<>();
                Stream.of(construct.getDBFields()).map(DBField::getName).forEach(nameSet::add);
                Stream.of(tableConstruct.getDBFields()).map(DBField::getName).forEach(nameSet2::add);
                nameSet.retainAll(nameSet2);
                List<String> nameList = new ArrayList<>(nameSet.size());
                nameList.addAll(nameSet);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < nameList.size(); i++) {
                    if (i == 0)
                        sb.append(nameList.get(i));
                    else
                        sb.append(',').append(nameList.get(i));
                }
                getDatabase().execSql("INSERT INTO " + getName() + " (" + sb.toString() + ") SELECT " + sb.toString() + " FROM " + TABLE_UPDATECONSTRUCT_TEMP, null);
            } finally {
                getDatabase().execSql("DROP TABLE " + TABLE_UPDATECONSTRUCT_TEMP, null);
            }
        } finally {
            getDatabase().execSql("PRAGMA foreign_keys = 1", null);
        }
        setConstruct(tableConstruct);
        return this;
    }
}
