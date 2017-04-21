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

package alchemy.tests.matchers;

import org.hamcrest.Matcher;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.hamcrest.core.IsEqual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IterableContains {

    public static <T> Matcher<Iterable<? extends T>> inAnyOrder(Iterable<T> items) {
        final Collection<Matcher<? super T>> matchers = new ArrayList<>();
        for (final Object item : items) {
            matchers.add(IsEqual.equalTo(item));
        }
        return IsIterableContainingInAnyOrder.containsInAnyOrder(matchers);
    }

    public static <T> Matcher<Iterable<? extends T>> inOrder(Iterable<T> items) {
        final List<Matcher<? super T>> matchers = new ArrayList<>();
        for (final Object item : items) {
            matchers.add(IsEqual.equalTo(item));
        }
        return IsIterableContainingInOrder.contains(matchers);
    }

}
