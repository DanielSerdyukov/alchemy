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
import alchemy.result.RxJava;
import rx.Observable;
import rx.Observer;
import rx.observables.SyncOnSubscribe;

import java.util.concurrent.Callable;

@SuppressWarnings("TryFinallyCanBeTryWithResources")
class RxJavaFetchable<T> implements RxJava.Fetchable<T> {

    private final Callable<CloseableIterator<T>> mCallable;

    RxJavaFetchable(Callable<CloseableIterator<T>> callable) {
        mCallable = callable;
    }

    @Override
    public Observable<T> observable() {
        return Observable.create(new SyncOnSubscribe<CloseableIterator<T>, T>() {
            @Override
            protected CloseableIterator<T> generateState() {
                try {
                    return mCallable.call();
                } catch (Exception e) {
                    throw new AlchemyException(e);
                }
            }

            @Override
            protected CloseableIterator<T> next(CloseableIterator<T> state, Observer<? super T> observer) {
                if (!state.hasNext()) {
                    observer.onCompleted();
                    state.close();
                    return state;
                }
                observer.onNext(state.next());
                return state;
            }
        });
    }

}
