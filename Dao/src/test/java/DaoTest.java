import com.xzzpig.pigutils.dao.DaoManager;
import com.xzzpig.pigutils.database.Database;
import com.xzzpig.pigutils.database.impl.DatabaseImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

public class DaoTest {

    Database database;
    DaoManager manager;

    @Before
    public void setUp() throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection connection = DriverManager.getConnection("jdbc:sqlite:F:/Temp/test.db");
        database = new DatabaseImpl(connection);
        manager = new DaoManager(database);
        manager.addResultSolver((result, dbField, resultSet)->{
            if (dbField.classType == String.class) {
                result.set(resultSet.getString(dbField.getName()));
                return true;
            }
            return false;
        });
        manager.addResultSolver((result, dbField, resultSet)->{
            if (dbField.classType == int.class) {
                result.set(resultSet.getInt(dbField.getName()));
                return true;
            }
            return false;
        });
    }

    @After
    public void tearDown() throws Exception {
        database.close();
    }

    @Test @Ignore
    public void testGetOrCreateTable() throws SQLException {
        System.out.println(manager.getOrCreateTable(TestDao2.class).getConstruct());
        System.out.println(manager.getOrCreateTable(TestDao.class).getConstruct());
    }

    @Test @Ignore
    public void testInsertOrUpdate() throws SQLException {
        TestDao testClass = new TestDao();
        testClass.hahaha = "AAA";
        testClass.hehehe = 6666;
        testClass.heiheihei = new TestDao2();
        testClass.heiheihei.heiheihei = "hhhh";
        testClass.testDao2s = new TestDao2[]{new TestDao2(), new TestDao2()};
        testClass.testDao2s[0].heiheihei = "aaa";
        testClass.testDao2s[1].heiheihei = "bbbb";
        manager.insertOrUpdate(testClass, null, null);
    }

    @Test @Ignore
    public void testSelect() throws IllegalAccessException, SQLException, InstantiationException {
        System.out.println(manager.select(TestDao.class, "AAA"));
        System.out.println(Arrays.toString(manager.select(TestDao.class, "AAA").testDao2s));
    }

    @Test @Ignore
    public void testCache() throws IllegalAccessException, SQLException, InstantiationException {
        TestDao dao = manager.select(TestDao.class, "AAA");
        System.out.println(dao == manager.select(TestDao.class, "AAA"));
        TestDao2 dao2 = manager.select(TestDao2.class, "hhhh");
        TestDao2 dao3 = manager.select(TestDao2.class, "aaa");
        System.out.println(dao.heiheihei == dao2);
        System.out.println(dao.testDao2s[0] == dao3);
    }

    @Test @Ignore
    public void testFill() throws IllegalAccessException, SQLException, InstantiationException {
        TestDao testDao = new TestDao();
        testDao.hahaha = "AAA";
        manager.fill(testDao);
        System.out.println(testDao);
    }

    @Test @Ignore
    public void tesUpdateCons() throws SQLException {
        manager.getTable(TestDao.class).updateConstruct(manager.solveTableClass(TestDao.class));
    }

    @Test @Ignore
    public void testDelete() throws SQLException, IllegalAccessException, InstantiationException {
        TestDao testClass = new TestDao();
        testClass.hahaha = "AAA";
        testClass.hehehe = 66666;
        manager.fill(testClass);
        manager.delete(testClass);
        manager.delete(testClass.heiheihei);
    }
}