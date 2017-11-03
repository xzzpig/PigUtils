import com.github.xzzpig.pigutils.dao.annotation.DBField;
import com.github.xzzpig.pigutils.dao.annotation.DBTable;

@DBTable
public class TestDao2 {
    @DBField(primaryKey = true)
    public String heiheihei;

    @Override public String toString() {
        return getClass().getName() + "{" + heiheihei + "}";
    }
}
