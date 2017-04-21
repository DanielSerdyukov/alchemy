// Generated code from Alchemy. Do not modify!
package alchemy.sqlite;

import alchemy.sqlite.models.groups.Group;
import alchemy.sqlite.models.groups.Group_Entry;
import alchemy.sqlite.platform.SQLiteDb;
import alchemy.sqlite.platform.SQLiteEntry;
import alchemy.sqlite.platform.SQLiteRelation;
import alchemy.sqlite.platform.SQLiteSchema;

@SuppressWarnings("TryFinallyCanBeTryWithResources")
final class Group_Table extends AbstractTable<Group> {
    private final SQLiteEntry<Group> mEntry = new Group_Entry();

    public Group_Table() {
        super("groups", new String[]{"_id", "name"});
    }

    @Override
    public SQLiteEntry<Group> getEntry() {
        return mEntry;
    }

    @Override
    public void create(SQLiteDb db) {
        db.exec("CREATE TABLE IF NOT EXISTS groups(_id INTEGER PRIMARY KEY ON CONFLICT REPLACE, name TEXT);");
        for (final SQLiteRelation<?, ?> relation : getEntry().getRelations()) {
            relation.create(db);
        }
    }

    @Override
    protected void onInsert(SQLiteSchema schema, SQLiteDb db, Group object, long id) {
        for (final SQLiteRelation<Group, ?> relation : getEntry().getRelations()) {
            relation.insert(schema, db, object, id);
        }
    }
}