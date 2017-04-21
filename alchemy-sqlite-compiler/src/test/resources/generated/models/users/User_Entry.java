// Generated code from Alchemy. Do not modify!
package alchemy.sqlite.models.users;

import alchemy.sqlite.platform.SQLiteDb;
import alchemy.sqlite.platform.SQLiteEntry;
import alchemy.sqlite.platform.SQLiteRelation;
import alchemy.sqlite.platform.SQLiteRow;
import alchemy.sqlite.platform.SQLiteSchema;
import alchemy.sqlite.platform.SQLiteStmt;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public final class User_Entry implements SQLiteEntry<User> {
    @Override
    public long getId(User object) {
        return object.mId;
    }

    @Override
    public Collection<SQLiteRelation<User, ?>> getRelations() {
        return Collections.emptyList();
    }

    @Override
    public int bind(SQLiteSchema schema, SQLiteStmt stmt, User object) {
        if (object.mId > 0) {
            stmt.bindLong(1, object.mId);
        } else {
            stmt.bindNull(1);
        }
        stmt.bindLong(2, object.mAge);
        if (object.mName == null) {
            stmt.bindNull(3);
        } else {
            stmt.bindString(3, object.mName);
        }
        stmt.bindDouble(4, object.mWeight);
        if (object.mAvatar == null) {
            stmt.bindNull(5);
        } else {
        }
        if (object.mCreated == null) {
            stmt.bindNull(6);
        } else {
            stmt.bindLong(6, object.mCreated.getTime());
        }
        return 7;
    }

    @Override
    public User map(SQLiteSchema schema, SQLiteDb db, SQLiteRow row) {
        final User object = new User();
        object.mId = row.getColumnLong(0);
        object.mAge = (int) row.getColumnLong(1);
        object.mName = row.getColumnString(2);
        object.mWeight = (double) row.getColumnDouble(3);
        object.mCreated = new Date(row.getColumnLong(5));
        return object;
    }
}