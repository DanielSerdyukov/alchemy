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

import alchemy.result.RxJava;
import rx.Completable;
import rx.functions.Action0;

class RxJavaExecutable implements RxJava.Executable {

    private final Runnable mRunnable;

    RxJavaExecutable(Runnable runnable) {
        mRunnable = runnable;
    }

    @Override
    public Completable completable() {
        return Completable.fromAction(new Action0() {
            @Override
            public void call() {
                System.out.println(Thread.currentThread());
                mRunnable.run();
            }
        });
    }

}
