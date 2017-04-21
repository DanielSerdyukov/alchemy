// Generated code from Alchemy. Do not modify!
package alchemy.sqlite.models.groups;

import alchemy.sqlite.models.users.User;
import alchemy.sqlite.platform.SQLiteDb;
import alchemy.sqlite.platform.SQLiteIterator;
import alchemy.sqlite.platform.SQLiteRelation;
import alchemy.sqlite.platform.SQLiteSchema;
import alchemy.sqlite.platform.SQLiteStmt;
import alchemy.sqlite.platform.SQLiteTable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class Group_Admin implements SQLiteRelation<Group, User> {
    @Override
    public void create(SQLiteDb db) {
        db.exec("CREATE TABLE IF NOT EXISTS group_admin(lfk INTEGER REFERENCES groups(_id) ON DELETE CASCADE ON UPDATE CASCADE, rfk INTEGER, UNIQUE(lfk, rfk) ON CONFLICT IGNORE);");
    }

    @Override
    public void insert(SQLiteSchema schema, SQLiteDb db, Group object, long id) {
        final SQLiteTable<User> table = schema.getTable(User.class);
        final long[] relIds = table.insert(schema, db, Collections.singletonList(object.mAdmin));
        final SQLiteStmt stmt = db.prepare("INSERT INTO group_admin VALUES(" + id + ", ?);");
        try {
            for (final long relId : relIds) {
                stmt.bindLong(1, relId);
                stmt.execute();
                stmt.clearBindings();
            }
        } finally {
            stmt.close();
        }
    }

    @Override
    public User fetchOne(SQLiteSchema schema, SQLiteDb db, long id) {
        final SQLiteTable<User> table = schema.getTable(User.class);
        final SQLiteStmt stmt = db.prepare("SELECT * FROM " + table.getName() + " WHERE _id IN(SELECT rfk FROM group_admin WHERE lfk = " + id + ") LIMIT 1;");
        try {
            final SQLiteIterator iterator = stmt.select();
            if (iterator.hasNext()) {
                return table.getEntry().map(schema, db, iterator.next());
            }
            return null;
        } finally {
            stmt.close();
        }
    }

    @Override
    public List<User> fetchList(SQLiteSchema schema, SQLiteDb db, long id) {
        final SQLiteTable<User> table = schema.getTable(User.class);
        final SQLiteStmt stmt = db.prepare("SELECT * FROM " + table.getName() + " WHERE _id IN(SELECT rfk FROM group_admin WHERE lfk = " + id + ");");;
        try {
            final SQLiteIterator iterator = stmt.select();
            final List<User> list = new ArrayList<>();
            while (iterator.hasNext()) {
                list.add(table.getEntry().map(schema, db, iterator.next()));
            }
            return list;
        } finally {
            stmt.close();
        }
    }
}