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

package alchemy;

import alchemy.result.CloseableIterator;
import alchemy.result.Fetchable;
import alchemy.result.RxJava;
import alchemy.result.RxJava2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings({"TryFinallyCanBeTryWithResources"})
class FetchableResult<T> implements Fetchable<T> {

    private final Callable<CloseableIterator<T>> mCallable;

    FetchableResult(Callable<CloseableIterator<T>> callable) {
        mCallable = callable;
    }

    @Override
    public CloseableIterator<T> call() throws Exception {
        return mCallable.call();
    }

    @Override
    public List<T> list() {
        try {
            final CloseableIterator<T> iterator = call();
            try {
                final List<T> list = new ArrayList<>();
                while (iterator.hasNext()) {
                    list.add(iterator.next());
                }
                return list;
            } finally {
                iterator.close();
            }
        } catch (Exception e) {
            throw new AlchemyException(e);
        }
    }

    @Override
    @SuppressWarnings("Since15")
    public java.util.stream.Stream<T> stream(int characteristics, boolean parallel) {
        try {
            final CloseableIterator<T> iterator = call();
            final java.util.Spliterator<T> spliterator = java.util.Spliterators
                    .spliteratorUnknownSize(iterator, characteristics);
            return java.util.stream.StreamSupport
                    .stream(spliterator, parallel)
                    .onClose(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("SQLiteStream.close");
                            iterator.close();
                        }
                    });
        } catch (Exception e) {
            throw new AlchemyException(e);
        }
    }

    @Override
    public RxJava.Fetchable<T> rx() {
        return new RxJavaFetchable<>(mCallable);
    }

    @Override
    public RxJava2.Fetchable<T> rx2() {
        return new RxJava2Fetchable<>(mCallable);
    }

}
