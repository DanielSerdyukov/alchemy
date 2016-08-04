package rxsqlite.bindings;

import android.content.Context;

/**
 * @author Daniel Serdyukov
 */
public interface RxSQLiteBinding {

    void loadLibrary(Context context);

    RxSQLiteDb openDatabase(String path, int flags);

}
