package rxsqlite;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Serdyukov
 */
public class Where {

    private final StringBuilder mWhereBuilder = new StringBuilder();

    private final List<Object> mBindValues = new ArrayList<>();

    private final List<String> mGroupBy = new ArrayList<>();

    private final List<String> mOrderBy = new ArrayList<>();

    private String mHaving;

    private String mLimit;

    @NonNull
    public Where where(@NonNull String where, Object... values) {
        mWhereBuilder.append(where);
        Collections.addAll(mBindValues, values);
        return this;
    }

    @NonNull
    public Where equalTo(@NonNull String column, @NonNull Object value) {
        return where(column, " = ?", value);
    }

    @NonNull
    public Where notEqualTo(@NonNull String column, @NonNull Object value) {
        return where(column, " <> ?", value);
    }

    @NonNull
    public Where lessThan(@NonNull String column, @NonNull Object value) {
        return where(column, " < ?", value);
    }

    @NonNull
    public Where lessThanOrEqualTo(@NonNull String column, @NonNull Object value) {
        return where(column, " <= ?", value);
    }

    @NonNull
    public Where greaterThan(@NonNull String column, @NonNull Object value) {
        return where(column, " > ?", value);
    }

    @NonNull
    public Where greaterThanOrEqualTo(@NonNull String column, @NonNull Object value) {
        return where(column, " >= ?", value);
    }

    @NonNull
    public Where like(@NonNull String column, @NonNull Object value) {
        return where(column, " LIKE ?", value);
    }

    @NonNull
    public Where between(@NonNull String column, @NonNull Object lv, @NonNull Object rv) {
        return where(column, " BETWEEN ? AND ?", lv, rv);
    }

    @NonNull
    public Where isNull(@NonNull String column) {
        return where(column, " IS NULL");
    }

    @NonNull
    public Where notNull(@NonNull String column) {
        return where(column, " NOT NULL");
    }

    @NonNull
    public Where in(@NonNull String column, @NonNull Object... values) {
        final int last = values.length - 1;
        mWhereBuilder.append(column).append(" IN(");
        for (int i = 0; i < values.length; ++i) {
            mWhereBuilder.append("?");
            if (i < last) {
                mWhereBuilder.append(", ");
            }
        }
        mWhereBuilder.append(")");
        Collections.addAll(mBindValues, values);
        return this;
    }

    @NonNull
    public Where and() {
        mWhereBuilder.append(" AND ");
        return this;
    }

    @NonNull
    public Where or() {
        mWhereBuilder.append(" OR ");
        return this;
    }

    @NonNull
    public Where beginGroup() {
        mWhereBuilder.append("(");
        return this;
    }

    @NonNull
    public Where endGroup() {
        mWhereBuilder.append(")");
        return this;
    }

    @NonNull
    public Where groupBy(@NonNull String column) {
        mGroupBy.add(column);
        return this;
    }

    @NonNull
    public Where having(@NonNull String having, @NonNull Object... values) {
        mHaving = having;
        Collections.addAll(mBindValues, values);
        return this;
    }

    @NonNull
    public Where orderByAsc(@NonNull String column) {
        mOrderBy.add(column + " ASC");
        return this;
    }

    @NonNull
    public Where orderByDesc(@NonNull String column) {
        mOrderBy.add(column + " DESC");
        return this;
    }

    @NonNull
    public Where limit(int limit) {
        mLimit = String.valueOf(limit);
        return this;
    }

    @NonNull
    public Where limit(int offset, int limit) {
        mLimit = limit + " OFFSET " + offset;
        return this;
    }

    @VisibleForTesting
    Where idIn(long[] ids) {
        mWhereBuilder.append("_id IN(");
        for (int i = 0, last = ids.length - 1; i <= last; ++i) {
            mWhereBuilder.append(ids[i]);
            if (i < last) {
                mWhereBuilder.append(", ");
            }
        }
        mWhereBuilder.append(")");
        return this;
    }

    @VisibleForTesting
    String buildSelectSql() {
        final StringBuilder sql = new StringBuilder(120);
        Utils.append(sql, " WHERE ", mWhereBuilder.toString());
        Utils.append(sql, " GROUP BY ", mGroupBy, ", ");
        Utils.append(sql, " HAVING ", mHaving);
        Utils.append(sql, " ORDER BY ", mOrderBy, ", ");
        Utils.append(sql, " LIMIT ", mLimit);
        return sql.append(";").toString();
    }

    @VisibleForTesting
    String buildDeleteSql() {
        final StringBuilder sql = new StringBuilder(60);
        Utils.append(sql, " WHERE ", mWhereBuilder.toString());
        return sql.append(";").toString();
    }

    @VisibleForTesting
    Iterable<Object> getArgs() {
        return Collections.unmodifiableList(mBindValues);
    }

    @NonNull
    private Where where(@NonNull String column, @NonNull String operand, @NonNull Object... values) {
        mWhereBuilder.append(column).append(operand);
        Collections.addAll(mBindValues, values);
        return this;
    }

}
