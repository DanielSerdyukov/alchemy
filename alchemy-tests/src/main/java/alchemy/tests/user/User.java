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

package alchemy.tests.user;

import alchemy.annotations.Column;
import alchemy.annotations.Entry;
import alchemy.annotations.PrimaryKey;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Entry("users")
public class User {

    private static final String[] FIRST_NAMES = new String[]{
            "Alice", "Bob", "Carol", "Chloe", "Dan", "Emily", "Emma", "Eric", "Eva",
            "Frank", "Gary", "Helen", "Jack", "James", "Jane",
            "Kevin", "Laura", "Leon", "Lilly", "Mary", "Maria",
            "Mia", "Nick", "Oliver", "Olivia", "Patrick", "Robert",
            "Stan", "Vivian", "Wesley", "Zoe"};

    private static final String[] LAST_NAMES = new String[]{
            "Hall", "Hill", "Smith", "Lee", "Jones", "Taylor", "Williams", "Jackson",
            "Stone", "Brown", "Thomas", "Clark", "Lewis", "Miller", "Walker", "Fox",
            "Robinson", "Wilson", "Cook", "Carter", "Cooper", "Martin"};


    @PrimaryKey
    long mId;

    @Column
    int mAge;

    @Column
    String mName;

    @Column
    double mWeight;

    @Column
    byte[] mAvatar;

    @Column
    Date mCreated;

    public static User generate(Random random) {
        final User user = new User();
        user.mName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)] + " "
                + LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        user.mAge = 10 + random.nextInt(40);
        user.mWeight = 100 * random.nextDouble();
        user.mAvatar = new byte[16];
        random.nextBytes(user.mAvatar);
        user.mCreated = new Date(ThreadLocalRandom.current()
                .nextLong(System
                        .currentTimeMillis()));
        return user;
    }

    public static List<User> generate(int count, boolean unique) {
        int maxUniques = FIRST_NAMES.length * LAST_NAMES.length;
        final Random random = new SecureRandom();
        final Set<String> names = new TreeSet<>();
        final List<User> users = new ArrayList<>(count);
        while (users.size() < count && (unique && --maxUniques >= 0)) {
            final User user = generate(random);
            if (!unique || names.add(user.mName)) {
                users.add(user);
            }
        }
        return users;
    }

    public long getId() {
        return mId;
    }

    public int getAge() {
        return mAge;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final User user = (User) o;
        return mAge == user.mAge && Double.compare(user.mWeight, mWeight) == 0
                && (mName != null ? mName.equals(user.mName) : user.mName == null)
                && Arrays.equals(mAvatar, user.mAvatar)
                && (mCreated != null ? mCreated.equals(user.mCreated) : user.mCreated == null);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = mAge;
        result = 31 * result + (mName != null ? mName.hashCode() : 0);
        temp = Double.doubleToLongBits(mWeight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + Arrays.hashCode(mAvatar);
        result = 31 * result + (mCreated != null ? mCreated.hashCode() : 0);
        return result;
    }
}
