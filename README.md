## RxSQLite [![Build Status](https://travis-ci.org/DanielSerdyukov/rxsqlite.svg?branch=master)](https://travis-ci.org/DanielSerdyukov/rxsqlite) [![Coverage Status](https://coveralls.io/repos/DanielSerdyukov/rxsqlite/badge.svg?branch=master&service=github)](https://coveralls.io/github/DanielSerdyukov/rxsqlite?branch=master) [![Apache License](https://img.shields.io/badge/license-Apache%20v2-blue.svg)](https://github.com/DanielSerdyukov/rxsqlite/blob/master/LICENSE)

Reactive SQLite extension for Android

----

### Gradle
```groovy
compile 'rxsqlite:library:1.0.0'
provided 'rxsqlite:compiler:1.0.0'
```

### Configuration
Add some code blocks in your ```Application``` class 
```java
static {
    SQLiteDb.loadLibrary(); // load native sqlite library
}
```
```java
@Override
public void onCreate() {
    super.onCreate();
    RxSQLite.register(RxSQLiteClient.builder(this, 1)
        .doOnOpen(db -> db.exec("PRAGMA case_sensitive_like = true;", null))
        .doOnOpen(db -> db.profile((sql, execTimeMs)
                -> Log.e("SQLite", sql + " in " + execTimeMs)))
        .doOnCreate(db -> db.exec("CREATE TRIGGER...", null))
        .build());
}
```

### Some examples

#### Query objects
```java
RxSQLite.query(User.class, new RxSQLiteWhere()
    .between('age', 18, 25))
    .toList()
    .subscribe(users -> {
        adapter.changeDataSet(users)
    });
```

#### Save objects
```java
User user = ...
RxSQLite.save(user)
    .subscribe(savedUser -> {
        showGreeting(savedUser);
    });
```
##### or save objects in one transaction
```java
List<User> users = ...
RxSQLite.saveAll(users)
    .subscribe(savedUsers -> {
        adapter.changeDataSet(savedUsesr)
    });
```

#### Delete objects
```java
User user = ...
RxSQLite.remove(user)
    .subscribe(affectedRows -> {
        
    });
```
##### or delete objects in one transaction
```java
List<User> users = ...
RxSQLite.removeAll(users)
    .subscribe(affectedRows -> {
        
    });
```

#### Clear table
```java
RxSQLite.clear(User.class, new RxSQLiteWhere()
    .equalTo('name', 'Joe'))
    .subscribe(affectedRows -> {

    });
```

----

RxSQLite uses **Annotation Processor** to **save you from coding boilerplate classes**.
Just annotate you ```model``` class as in the example below and it will work!
```java
@SQLiteObject("users")
public class User {

    @SQLitePk
    private long mId;

    @SQLiteColumn
    private String mName;

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

}
```
Supported of field types:
* ```long```, ```int```, ```short``` as **INTEGER**
* ```double```, ```float``` as **REAL**
* ```byte[]``` as **BLOB**
* ```String``` as **TEXT**
* ```Enum<T>``` as **TEXT**

----

License
-------

    Copyright 2015-2016 Daniel Serdyukov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
