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

import alchemy.query.AlchemyUpdate;
import alchemy.result.Fetchable;
import alchemy.source.SourceUpdate;

class OpUpdate<T> extends ExecutableResult implements AlchemyUpdate<T> {

    private final SourceUpdate<T> mUpdate;

    OpUpdate(SourceUpdate<T> update) {
        super(update);
        mUpdate = update;
    }

    @Override
    public Fetchable<T> fetch() {
        return new FetchableResult<>(mUpdate);
    }

}
