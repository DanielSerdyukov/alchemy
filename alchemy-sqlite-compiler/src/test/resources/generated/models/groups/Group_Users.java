// Generated code from Alchemy. Do not modify!
package alchemy.sqlite.models.groups;

import alchemy.sqlite.platform.*;
import alchemy.test.sqlite.models.b.User;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("TryFinallyCanBeTryWithResources")
final class Group_Users implements SQLiteRelation<Group, User> {
    @Override
    public void create(SQLiteDb db) {
        db.exec("CREATE TABLE IF NOT EXISTS group_musers(lfk INTEGER REFERENCES groups(_id) ON DELETE CASCADE ON UPDATE CASCADE, rfk INTEGER, UNIQUE(lfk, rfk) ON CONFLICT IGNORE);");
    }

    @Override
    public void insert(SQLiteSchema schema, SQLiteDb db, Group object, long id) {
        final SQLiteTable<User> table = schema.getTable(User.class);
        final long[] relIds = table.insert(schema, db, object.mUsers);
        final SQLiteStmt stmt = db.prepare("INSERT INTO group_musers VALUES(" + id + ", ?);");
        ;
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
        final SQLiteStmt stmt = db.prepare("SELECT * FROM " + table.getName() + " WHERE _id IN(SELECT rfk FROM group_musers WHERE lfk = " + id + ") LIMIT 1;");
        ;
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
        final SQLiteStmt stmt = db.prepare("SELECT * FROM " + table.getName() + " WHERE _id IN(SELECT rfk FROM group_musers WHERE lfk = " + id + ");");
        ;
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
