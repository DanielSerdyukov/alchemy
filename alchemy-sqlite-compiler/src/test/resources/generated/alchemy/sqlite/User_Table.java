// Generated code from Alchemy. Do not modify!
package alchemy.sqlite;

import alchemy.sqlite.models.users.User;
import alchemy.sqlite.models.users.User_Entry;
import alchemy.sqlite.platform.SQLiteDb;
import alchemy.sqlite.platform.SQLiteEntry;
import alchemy.sqlite.platform.SQLiteRelation;

@SuppressWarnings("TryFinallyCanBeTryWithResources")
final class User_Table extends AbstractTable<User> {
    private final SQLiteEntry<User> mEntry = new User_Entry();

    public User_Table() {
        super("users", new String[]{"_id", "age", "name", "weight", "avatar", "created"});
    }

    @Override
    public SQLiteEntry<User> getEntry() {
        return mEntry;
    }

    @Override
    public void create(SQLiteDb db) {
        db.exec("CREATE TABLE IF NOT EXISTS users(_id INTEGER PRIMARY KEY ON CONFLICT REPLACE, age INTEGER, name TEXT, weight REAL, avatar BLOB, created INTEGER);");
        for (final SQLiteRelation<?, ?> relation : getEntry().getRelations()) {
            relation.create(db);
        }
    }
}