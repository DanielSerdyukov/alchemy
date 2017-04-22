/*
 * Copyright (C) 2017 exzogeni.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package alchemy.tests;

import alchemy.Alchemy;
import alchemy.tests.group.Group;
import alchemy.tests.matchers.IterableContains;
import alchemy.tests.user.User;
import alchemy.tests.user.UserContract;
import org.hamcrest.core.IsCollectionContaining;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public abstract class AlchemyTest {

    private Alchemy mAlchemy;

    @Before
    public void setUp() throws Exception {
        mAlchemy = getAlchemy();
    }

    @Test
    public void insert() throws Exception {
        final Group wheel = Group.generate("wheel", 50);
        mAlchemy.insert(wheel).run();
        final List<Group> groups = mAlchemy.where(Group.class).fetch().list();
        Assert.assertThat(groups, IsCollectionContaining.hasItem(wheel));
    }

    @Test
    public void update_run() throws Exception {
        final List<User> users = User.generate(50, true);
        final List<User> fetched = mAlchemy.insert(users).fetch().list();
        final List<User> forUpdate = new ArrayList<>();
        for (final User user : fetched) {
            if (user.getName().startsWith("Alice")) {
                user.setName(user.getName().replace("Alice", "Jim"));
                forUpdate.add(user);
            }
        }
        mAlchemy.update(forUpdate).run();
        final List<User> updated = mAlchemy.where(User.class)
                .startsWith(UserContract.NAME, "Jim")
                .fetch().list();
        Assert.assertThat(updated, IterableContains.inAnyOrder(forUpdate));
    }

    @Test
    public void update_fetch() throws Exception {
        final List<User> users = User.generate(50, true);
        final List<User> fetched = mAlchemy.insert(users).fetch().list();
        final List<User> forUpdate = new ArrayList<>();
        for (final User user : fetched) {
            if (user.getName().endsWith("Smith")) {
                user.setName(user.getName().replace("Smith", "Doe"));
                forUpdate.add(user);
            }
        }
        final List<User> updated = mAlchemy.update(forUpdate)
                .fetch().list();
        Assert.assertThat(updated, IterableContains.inAnyOrder(forUpdate));
    }

    @Test
    public void delete() throws Exception {
        final List<User> users = User.generate(50, true);
        final List<User> fetched = mAlchemy.insert(users).fetch().list();
        final List<User> deleted = new ArrayList<>();
        for (final User user : fetched) {
            if (user.getId() % 2 == 0) {
                deleted.add(user);
            }
        }
        mAlchemy.delete(deleted).run();
        final List<User> allUsers = mAlchemy.where(User.class).fetch().list();
        Assert.assertThat(allUsers, IsNot.not(IterableContains.inAnyOrder(deleted)));
    }

    @Test
    public void delete_by_where() throws Exception {
        final List<User> users = User.generate(50, true);
        mAlchemy.insert(users).run();
        final List<User> deleted = new ArrayList<>();
        for (final User user : deleted) {
            if (user.getAge() < 25) {
                deleted.add(user);
            }
        }
        mAlchemy.where(User.class).lessThan(UserContract.AGE, 25).delete().run();
        final List<User> allUsers = mAlchemy.where(User.class).fetch().list();
        Assert.assertThat(allUsers, IsNot.not(IterableContains.inAnyOrder(deleted)));
    }

    protected abstract Alchemy getAlchemy();

}
