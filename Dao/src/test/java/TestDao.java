import com.xzzpig.pigutils.dao.annotation.DBArray;
import com.xzzpig.pigutils.dao.annotation.DBField;
import com.xzzpig.pigutils.dao.annotation.DBForeign;
import com.xzzpig.pigutils.dao.annotation.DBTable;

import java.sql.JDBCType;

@DBTable
public class TestDao {
    @DBField(primaryKey = true, size = 10)
    public String hahaha;

    @DBField(type = JDBCType.INTEGER, check = "hehehe > 100")
    public int hehehe;

    @DBField
    @DBForeign
    public TestDao2 heiheihei;

    @DBArray
    public TestDao2[] testDao2s;

    @Override public String toString() {
        return getClass().getName() + "{" + hahaha + "," + hehehe + "," + heiheihei + "}";
    }
}
