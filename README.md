# RxSQLite [![Apache License](https://img.shields.io/badge/license-Apache%20v2-blue.svg)](https://github.com/DanielSerdyukov/rxsqlite/blob/master/LICENSE) [![Build Status](https://gitlab.exzogeni.com/android/rxsqlite/badges/master/build.svg)](https://github.com/DanielSerdyukov/rxsqlite)

Reactive SQLite extension for Android

----

### Gradle
```groovy
compile 'rxsqlite:library:4.0.0'
provided 'rxsqlite:compiler:4.0.0'
```

### Configuration
Add some code blocks in your ```Application``` class 
```java
@Override
public void onCreate() {
    super.onCreate();
    SQLite4a.loadLibrary(this); // load native sqlite library
    RxSQLite.init(this, SQLiteConfig.memory()
        .doOnOpen(rawDb -> rawDb.exec("PRAGMA case_sensitive_like = true;"))
        .enableTracing() // debug logs in logcat
        .build(this));
}
```

### Some examples

#### Query objects
```java
RxSQLite.select(User.class, new Where()
    .between('age', 18, 25))
    .toList()
    .subscribe(users -> {
        adapter.changeDataSet(users)
    });
```

#### Save objects
```java
User user = ...
RxSQLite.insert(user)
    .subscribe(savedUser -> {
        showGreeting(savedUser);
    });
```
##### or save objects in one transaction
```java
List<User> users = ...
RxSQLite.insert(users)
    .subscribe(savedUsers -> {
        adapter.changeDataSet(savedUsesr)
    });
```

#### Delete objects
```java
User user = ...
RxSQLite.delete(user)
    .subscribe(affectedRows -> {
        
    });
```
##### or delete objects in one transaction
```java
List<User> users = ...
RxSQLite.delete(users)
    .subscribe(affectedRows -> {
        
    });
```

#### Clear table
```java
RxSQLite.delete(User.class, new Where()
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
* ```boolean``` as **INTEGER**
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
