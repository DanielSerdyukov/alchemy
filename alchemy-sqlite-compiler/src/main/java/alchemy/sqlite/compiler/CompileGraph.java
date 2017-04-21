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

package alchemy.sqlite.compiler;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

class CompileGraph {

    private final Map<Element, EntrySpec> mEntries = new HashMap<>();

    private final Map<String, Map<Element, TableSpec>> mSchemas = new HashMap<>();

    TableSpec findTableSpec(Element element) {
        final TableSpec tableSpec = mSchemas.values().stream()
                .flatMap(specs -> Stream.of(specs.get(element)))
                .findFirst()
                .orElse(null);
        if (tableSpec == null) {
            throw new ElementException("WTF! @Entry is not processed yet!", element);
        }
        return tableSpec;
    }

    void putEntrySpec(Element element, EntrySpec entrySpec) {
        mEntries.put(element, entrySpec);
    }

    void putTableSpec(String schemaClassName, TableSpec tableSpec) {
        mSchemas.computeIfAbsent(schemaClassName, k -> new HashMap<>())
                .put(tableSpec.getElement(), tableSpec);
    }

    void putColumnSpec(Element element, ColumnSpec columnSpec) {
        final EntrySpec entrySpec = mEntries.get(element);
        if (entrySpec != null) {
            entrySpec.addColumnSpec(columnSpec);
        } else {
            throw new ElementException("WTF! @Entry is not processed yet!", element);
        }
    }

    void putRelationSpec(Element element, RelationSpec relationSpec) {
        final EntrySpec entrySpec = mEntries.get(element);
        if (entrySpec != null) {
            entrySpec.addRelationSpec(relationSpec);
        } else {
            throw new ElementException("WTF! @Entry is not processed yet!", element);
        }
    }

    void brewJava(Filer filer) throws Exception {
        for (final EntrySpec entrySpec : mEntries.values()) {
            for (final RelationSpec relationSpec : entrySpec.getRelationSpecs()) {
                relationSpec.brewJava(filer);
            }
            entrySpec.brewJava(filer);
        }
        for (final Map.Entry<String, Map<Element, TableSpec>> entry : mSchemas.entrySet()) {
            final SchemaSpec schemaSpec = new SchemaSpec(entry.getKey());
            for (final TableSpec tableSpec : entry.getValue().values()) {
                schemaSpec.putTable(tableSpec.getElement(), tableSpec.brewJava(filer));
            }
            schemaSpec.brewJava(filer);
        }
    }

}
