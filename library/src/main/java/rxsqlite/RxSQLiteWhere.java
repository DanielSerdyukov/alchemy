package rxsqlite;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Serdyukov
 */
@SuppressWarnings("checkstyle:methodcount")
public class RxSQLiteWhere {

    private final StringBuilder mWhereBuilder = new StringBuilder();

    private final List<Object> mBindValues = new ArrayList<>();

    private final List<String> mGroupBy = new ArrayList<>();

    private final List<String> mOrderBy = new ArrayList<>();

    private String mHaving;

    private String mLimit;

    private static void appendClause(@NonNull StringBuilder query, @NonNull String name, @Nullable String clause) {
        if (!TextUtils.isEmpty(clause)) {
            query.append(name).append(clause);
        }
    }

    @NonNull
    public RxSQLiteWhere where(@NonNull String where, Object... values) {
        mWhereBuilder.append(where);
        Collections.addAll(mBindValues, values);
        return this;
    }

    @NonNull
    public RxSQLiteWhere equalTo(@NonNull String column, @NonNull Object value) {
        return where(column, " = ?", value);
    }

    @NonNull
    public RxSQLiteWhere notEqualTo(@NonNull String column, @NonNull Object value) {
        return where(column, " <> ?", value);
    }

    @NonNull
    public RxSQLiteWhere lessThan(@NonNull String column, @NonNull Object value) {
        return where(column, " < ?", value);
    }

    @NonNull
    public RxSQLiteWhere lessThanOrEqualTo(@NonNull String column, @NonNull Object value) {
        return where(column, " <= ?", value);
    }

    @NonNull
    public RxSQLiteWhere greaterThan(@NonNull String column, @NonNull Object value) {
        return where(column, " > ?", value);
    }

    @NonNull
    public RxSQLiteWhere greaterThanOrEqualTo(@NonNull String column, @NonNull Object value) {
        return where(column, " >= ?", value);
    }

    @NonNull
    public RxSQLiteWhere like(@NonNull String column, @NonNull Object value) {
        return where(column, " LIKE ?", value);
    }

    @NonNull
    public RxSQLiteWhere between(@NonNull String column, @NonNull Object lv, @NonNull Object rv) {
        return where(column, " BETWEEN ? AND ?", lv, rv);
    }

    @NonNull
    public RxSQLiteWhere isNull(@NonNull String column) {
        return where(column, " IS NULL");
    }

    @NonNull
    public RxSQLiteWhere notNull(@NonNull String column) {
        return where(column, " NOT NULL");
    }

    @NonNull
    public RxSQLiteWhere in(@NonNull String column, @NonNull Object... values) {
        mWhereBuilder.append(column)
                .append(" IN(")
                .append(TextUtils.join(", ", Collections.nCopies(values.length, "?")))
                .append(")");
        Collections.addAll(mBindValues, values);
        return this;
    }

    @NonNull
    public RxSQLiteWhere and() {
        mWhereBuilder.append(" AND ");
        return this;
    }

    @NonNull
    public RxSQLiteWhere or() {
        mWhereBuilder.append(" OR ");
        return this;
    }

    @NonNull
    public RxSQLiteWhere beginGroup() {
        mWhereBuilder.append("(");
        return this;
    }

    @NonNull
    public RxSQLiteWhere endGroup() {
        mWhereBuilder.append(")");
        return this;
    }

    @NonNull
    public RxSQLiteWhere groupBy(@NonNull String column) {
        mGroupBy.add(column);
        return this;
    }

    @NonNull
    public RxSQLiteWhere having(@NonNull String having, @NonNull Object... values) {
        mHaving = having;
        Collections.addAll(mBindValues, values);
        return this;
    }

    @NonNull
    public RxSQLiteWhere orderByAsc(@NonNull String column) {
        mOrderBy.add(column + " ASC");
        return this;
    }

    @NonNull
    public RxSQLiteWhere orderByDesc(@NonNull String column) {
        mOrderBy.add(column + " DESC");
        return this;
    }

    @NonNull
    public RxSQLiteWhere limit(int limit) {
        mLimit = String.valueOf(limit);
        return this;
    }

    @NonNull
    public RxSQLiteWhere limit(int offset, int limit) {
        mLimit = limit + " OFFSET " + offset;
        return this;
    }

    @NonNull
    @VisibleForTesting
    String toSelectSql() {
        final StringBuilder sql = new StringBuilder(120);
        appendClause(sql, " WHERE ", mWhereBuilder.toString());
        appendClause(sql, " GROUP BY ", TextUtils.join(", ", mGroupBy));
        appendClause(sql, " HAVING ", mHaving);
        appendClause(sql, " ORDER BY ", TextUtils.join(", ", mOrderBy));
        appendClause(sql, " LIMIT ", mLimit);
        return sql.append(";").toString();
    }

    @NonNull
    @VisibleForTesting
    String toDeleteSql() {
        final StringBuilder sql = new StringBuilder(60);
        appendClause(sql, " WHERE ", mWhereBuilder.toString());
        return sql.append(";").toString();
    }

    @NonNull
    @VisibleForTesting
    Iterable<Object> getBindValues() {
        return Collections.unmodifiableList(mBindValues);
    }

    @NonNull
    private RxSQLiteWhere where(@NonNull String column, @NonNull String operand, @NonNull Object... values) {
        mWhereBuilder.append(column).append(operand);
        Collections.addAll(mBindValues, values);
        return this;
    }

}
