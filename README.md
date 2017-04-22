# Alchemy [![Apache License](https://img.shields.io/badge/license-Apache%20v2-blue.svg)](https://github.com/DanielSerdyukov/alchemy/blob/master/LICENSE)
Powerful object mapping for Android with RxJava and Java 8 support.

Examples
--------

Define entities from an abstract class:

```java
@Entry
public class Group {

    @PrimaryKey
    long mId;               // Primary Key must be 'long'

    @Column
    String mName;           // Basic column

    @Relation
    List<User> mUsers;      // Many to many relation

    @Relation
    User mAdmin;            // One to one relation
}
```

Create Alchemy instance:

```java
public class MyApplication extends Appliaction {

    public static volatile Alchemy sRepo;

    @Override
    public void onCreate() {
        super.onCreate();
        sRepo = new Alchemy(new AndroidSource(new DefaultSchema(1),
                getDatabasePath("main.db").getAbsolutePath())); // for android sqlite
    }

}
```

**Queries:** dsl based query

```java
List<User> users = repo
    .where(User.class)
    .starsWith("name", "Alice")
    .and().greaterThan("age", 18)
    .fetch()
    .list();
```

**Java 8 [streams](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html):**

```java
repo.where(User.class)
    .fetch()
    .stream()
    .forEach(user -> Log.i("TAG", user.toString()));
```

**[RxJava](https://github.com/ReactiveX/RxJava) [Observables](http://reactivex.io/documentation/observable.html):**

```java
Observable<User> observable = repo
    .where(User.class)
    .fetch()
    .rx()
    .observable();
```

**[RxJava2](https://github.com/ReactiveX/RxJava) [Flowables](https://github.com/ReactiveX/RxJava/wiki/What%27s-different-in-2.0#observable-and-flowable):**

```java
Flowable<User> observable = repo
    .where(User.class)
    .fetch()
    .rx2()
    .flowable(BackpressureStrategy.BUFFER);
```

Using it
--------

Soon in bintray jcenter.


License
-------

    Copyright (C) 2017 exzogeni.com

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
