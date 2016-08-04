package rxsqlite;

public abstract class AbstractTable<T> extends SQLiteTable<T> {

    public AbstractTable(String name, String[] columns, boolean hasRelations) {
        super(name, columns, hasRelations);
    }

}