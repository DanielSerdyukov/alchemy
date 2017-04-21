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

package alchemy.tests.group;

import alchemy.annotations.Column;
import alchemy.annotations.Entry;
import alchemy.annotations.PrimaryKey;
import alchemy.annotations.Relation;
import alchemy.tests.user.User;

import java.security.SecureRandom;
import java.util.List;

@Entry("groups")
public class Group {

    @PrimaryKey
    long mId;

    @Column
    String mName;

    @Relation("group_admins")
    User mAdmin;

    @Relation
    List<User> mUsers;

    public static Group generate(String name, int users) {
        final Group group = new Group();
        group.mName = name;
        group.mAdmin = User.generate(new SecureRandom());
        group.mUsers = User.generate(users, true);
        return group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Group group = (Group) o;
        return (mName != null ? mName.equals(group.mName) : group.mName == null)
                && (mAdmin != null ? mAdmin.equals(group.mAdmin) : group.mAdmin == null)
                && (mUsers != null ? mUsers.equals(group.mUsers) : group.mUsers == null);
    }

    @Override
    public int hashCode() {
        int result = mName != null ? mName.hashCode() : 0;
        result = 31 * result + (mAdmin != null ? mAdmin.hashCode() : 0);
        result = 31 * result + (mUsers != null ? mUsers.hashCode() : 0);
        return result;
    }

}
