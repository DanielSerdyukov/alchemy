// Generated code from Alchemy. Do not modify!
package alchemy.sqlite.models.groups;

import alchemy.sqlite.models.users.User;
import alchemy.sqlite.platform.SQLiteDb;
import alchemy.sqlite.platform.SQLiteEntry;
import alchemy.sqlite.platform.SQLiteRelation;
import alchemy.sqlite.platform.SQLiteRow;
import alchemy.sqlite.platform.SQLiteSchema;
import alchemy.sqlite.platform.SQLiteStmt;
import java.util.Arrays;
import java.util.Collection;

public final class Group_Entry implements SQLiteEntry<Group> {
    SQLiteRelation<Group, User> mAdmin = new Group_Admin();

    SQLiteRelation<Group, User> mUsers = new Group_Users();

    @Override
    public long getId(Group object) {
        return object.mId;
    }

    @Override
    public Collection<SQLiteRelation<Group, ?>> getRelations() {
        return Arrays.asList(mAdmin, mUsers);
    }

    @Override
    public int bind(SQLiteSchema schema, SQLiteStmt stmt, Group object) {
        if (object.mId > 0) {
            stmt.bindLong(1, object.mId);
        } else {
            stmt.bindNull(1);
        }
        if (object.mName == null) {
            stmt.bindNull(2);
        } else {
            stmt.bindString(2, object.mName);
        }
        return 3;
    }

    @Override
    public Group map(SQLiteSchema schema, SQLiteDb db, SQLiteRow row) {
        final Group object = new Group();
        object.mId = row.getColumnLong(0);
        object.mName = row.getColumnString(1);
        object.mAdmin = mAdmin.fetchOne(schema, db, object.mId);
        object.mUsers = mUsers.fetchList(schema, db, object.mId);
        return object;
    }
}