package com.xzzpig.pigutils.database;

import com.xzzpig.pigutils.data.DataUtils;
import com.xzzpig.pigutils.database.impl.DatabaseImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.JDBCType;
import java.sql.SQLException;

public class DatabaseTest {

    Database database;

    @Before
    public void setUp() throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection connection = DriverManager.getConnection("jdbc:sqlite:F:/Temp/test.db");
        database = new DatabaseImpl(connection);
    }

    @After
    public void tearDown() throws Exception {
        database.close();
    }

    @Test @Ignore
    public void testCreateTable() throws ClassNotFoundException, SQLException {
        TableConstruct construct = database.createConstruct();
        construct.addDBField(construct.createDBField("aaa").setPrimaryKey(true));
        construct.addDBField(construct.createDBField("bbb"));
        construct.addDBField(construct.createDBField("ccc").setType(JDBCType.INTEGER));
        database.createTable("test", construct);
    }

    @Test @Ignore
    public void testGetTable() throws ClassNotFoundException, SQLException {
        Table test = database.getTable("test");
        System.out.println(test.queryConstruct().getConstruct().toString());
    }

    @Test @Ignore
    public void testDrop() throws ClassNotFoundException, SQLException {
        database.getTable("test").drop();
    }

    @Test @Ignore
    public void testInsert() throws ClassNotFoundException, SQLException {
        Table test = database.getTable("test");
        test.insert("???", 233, "2333");
    }

    @Test @Ignore
    public void testDelete() throws ClassNotFoundException, SQLException {
        Table test = database.getTable("test");
        test.delete("\"aaa\" = \"???\"");
    }

    @Test @Ignore
    public void testSelect() throws ClassNotFoundException, SQLException {
        Table test = database.getTable("test");
        System.out.println(test.select().setWhere("\"aaa\" = \"???\"").select().getObject("bbb").getClass());
    }

    @Test @Ignore
    public void testUpdate() throws ClassNotFoundException, SQLException {
        Table test = database.getTable("test");
        test.update(DataUtils.array2KVMap(String.class, Object.class, "aaa", "??"), "\"aaa\" = \"???\"");
    }

    @Test @Ignore
    public void testForeign() throws SQLException {
        TableConstruct construct = database.createConstruct();
        construct.addDBField(construct.createDBField("aaa").setForeign("test", "aaa"));
        construct.addDBField(construct.createDBField("bbb"));
        construct.addDBField(construct.createDBField("ccc").setType(JDBCType.INTEGER));
        Table test2 = database.createTable("test2", construct);
        Table test = database.getTable("test");
        test.insert("111", "222", "333");
        test2.insert("111", "222", "333");
    }
}