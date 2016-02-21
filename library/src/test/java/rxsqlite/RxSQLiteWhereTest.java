package rxsqlite;

import android.text.TextUtils;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Daniel Serdyukov
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class RxSQLiteWhereTest {

    private RxSQLiteWhere mWhere;

    @Before
    @SuppressWarnings("PrimitiveArrayArgumentToVariableArgMethod")
    public void setUp() throws Exception {
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(TextUtils.join(Mockito.anyString(), Mockito.anyCollection()))
                .thenAnswer(Answers.joinIterable());
        mWhere = new RxSQLiteWhere()
                .beginGroup().equalTo("a", 1).and().notEqualTo("b", 2).endGroup()
                .or()
                .beginGroup().lessThan("c", 1.23).and().lessThanOrEqualTo("d", 4.56).endGroup()
                .or()
                .beginGroup().greaterThan("e", 7.89).and().greaterThanOrEqualTo("f", 8.90).endGroup()
                .or()
                .beginGroup().isNull("g").and().notNull("h").endGroup()
                .or()
                .in("i", 1, 2, 3)
                .or()
                .like("j", "Joe%")
                .or()
                .between("k", 100, 200)
                .or()
                .where("l = ?", new byte[]{4, 4, 4})
                .groupBy("a")
                .having("COUNT(a) > 0")
                .orderByAsc("b")
                .orderByDesc("c");
    }


    @Test
    public void testToSelectSql() throws Exception {
        Assert.assertThat(mWhere.limit(10).toSelectSql(), Is.is(" WHERE (a = ? AND b <> ?)"
                + " OR (c < ? AND d <= ?)"
                + " OR (e > ? AND f >= ?)"
                + " OR (g IS NULL AND h NOT NULL)"
                + " OR i IN(?, ?, ?)"
                + " OR j LIKE ?"
                + " OR k BETWEEN ? AND ?"
                + " OR l = ?"
                + " GROUP BY a"
                + " HAVING COUNT(a) > 0"
                + " ORDER BY b ASC, c DESC"
                + " LIMIT 10;"));
    }

    @Test
    public void testToSelectSqlWithOffset() throws Exception {
        Assert.assertThat(mWhere.limit(5, 10).toSelectSql(), Is.is(" WHERE (a = ? AND b <> ?)"
                + " OR (c < ? AND d <= ?)"
                + " OR (e > ? AND f >= ?)"
                + " OR (g IS NULL AND h NOT NULL)"
                + " OR i IN(?, ?, ?)"
                + " OR j LIKE ?"
                + " OR k BETWEEN ? AND ?"
                + " OR l = ?"
                + " GROUP BY a"
                + " HAVING COUNT(a) > 0"
                + " ORDER BY b ASC, c DESC"
                + " LIMIT 10 OFFSET 5;"));
    }

    @Test
    public void testToDeleteSql() throws Exception {
        Assert.assertThat(mWhere.toDeleteSql(), Is.is(" WHERE (a = ? AND b <> ?)"
                + " OR (c < ? AND d <= ?)"
                + " OR (e > ? AND f >= ?)"
                + " OR (g IS NULL AND h NOT NULL)"
                + " OR i IN(?, ?, ?)"
                + " OR j LIKE ?"
                + " OR k BETWEEN ? AND ?"
                + " OR l = ?;"));
    }

    @Test
    public void testGetBindValues() throws Exception {
        final Iterable<Object> values = mWhere.getBindValues();
        Assert.assertThat(values, IsCollectionContaining.<Object>hasItems(
                1, 2, 1.23, 4.56, 7.89, 8.90, 1, 2, 3, "Joe%", 100, 200, new byte[]{4, 4, 4}
        ));
    }

}
