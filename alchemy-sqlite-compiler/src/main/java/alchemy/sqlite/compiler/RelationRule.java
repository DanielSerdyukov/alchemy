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

import alchemy.annotations.Entry;
import com.squareup.javapoet.ClassName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor7;
import java.util.List;
import java.util.regex.Matcher;

class RelationRule implements ProcessingRule {

    private final TypeUtils mTypeUtils;

    private final CompileGraph mCompileGraph;

    RelationRule(ProcessingEnvironment processingEnv, CompileGraph compileGraph) {
        mTypeUtils = new TypeUtils(processingEnv);
        mCompileGraph = compileGraph;
    }

    private static ClassName makeClassName(Element field) {
        final String fieldName = field.getSimpleName().toString();
        final String suffix;
        final Matcher matcher = ColumnRule.HUNGARIAN_NOTATION.matcher(fieldName);
        if (matcher.matches()) {
            suffix = toUnderScope(matcher.group(1));
        } else {
            suffix = toUnderScope(fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
        }
        final ClassName enclosingClass = ClassName.get((TypeElement) field.getEnclosingElement());
        return ClassName.get(enclosingClass.packageName(), enclosingClass.simpleName() + "_" + suffix);
    }

    private static String toUnderScope(String value) {
        return value.replaceAll("(.)(\\p{Upper})", "$1_$2");
    }

    @Override
    public void process(Element element) throws Exception {
        final Element enclosingElement = element.getEnclosingElement();
        if (enclosingElement.getAnnotation(Entry.class) == null) {
            throw new ElementException("Class containing @Relation must be annotated with @Entry", enclosingElement);
        }
        element.asType().accept(new SimpleTypeVisitor7<Void, Void>() {
            @Override
            public Void visitDeclared(DeclaredType t, Void unused) {
                final List<? extends TypeMirror> args = t.getTypeArguments();
                if (args.isEmpty()) {
                    processOneToOne(element, t.asElement());
                } else {
                    processOneToMany(element, (TypeElement) t.asElement(), args.get(0));
                }
                return super.visitDeclared(t, unused);
            }
        }, null);
    }

    private void processOneToOne(Element field, Element relation) {
        if (relation.getAnnotation(Entry.class) == null) {
            throw new ElementException("Related type must be annotated with @Entry", relation);
        }
        final Element enclosingElement = field.getEnclosingElement();
        final TableSpec lTable = mCompileGraph.findTableSpec(enclosingElement);
        final TableSpec rTable = mCompileGraph.findTableSpec(relation);
        final ClassName className = makeClassName(field);
        final RelationSpec relationSpec = new RelationSpec(
                field, className, lTable, rTable, false);
        mCompileGraph.putRelationSpec(enclosingElement, relationSpec);
    }

    private void processOneToMany(Element field, Element collectionType, TypeMirror relation) {
        if (!mTypeUtils.isAssignable(collectionType.asType(), List.class)) {
            throw new ElementException("Relation type must be subclass of List<E>", field);
        }
        relation.accept(new SimpleTypeVisitor7<Void, Void>() {
            @Override
            public Void visitDeclared(DeclaredType t, Void unused) {
                final Element element = t.asElement();
                if (element.getAnnotation(Entry.class) == null) {
                    throw new ElementException("Related type must be annotated with @Entry", element);
                }
                final Element enclosingElement = field.getEnclosingElement();
                final TableSpec lTable = mCompileGraph.findTableSpec(enclosingElement);
                final TableSpec rTable = mCompileGraph.findTableSpec(element);
                final ClassName className = makeClassName(field);
                final RelationSpec relationSpec = new RelationSpec(
                        field, className, lTable, rTable, true);
                mCompileGraph.putRelationSpec(enclosingElement, relationSpec);
                return super.visitDeclared(t, unused);
            }
        }, null);
    }

}
