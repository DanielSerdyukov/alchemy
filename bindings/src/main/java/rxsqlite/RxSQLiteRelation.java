package rxsqlite;

import java.util.List;

/**
 * @author Daniel Serdyukov
 */
public interface RxSQLiteRelation<T> {

    void create(RxSQLiteDb db);

    void insert(RxSQLiteDb db, List<T> items, long fk);

    List<T> selectList(RxSQLiteDb db, long fk);

    T selectOne(RxSQLiteDb db, long fk);

}
