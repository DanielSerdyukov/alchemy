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

import alchemy.annotations.Column;
import alchemy.annotations.Entry;
import alchemy.annotations.PrimaryKey;
import alchemy.annotations.Relation;
import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(Processor.class)
public class SQLiteCompiler extends AbstractProcessor {

    private final Map<Class<? extends Annotation>, ProcessingRule> mRules = new LinkedHashMap<>();

    private CompileGraph mCompileGraph;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mCompileGraph = new CompileGraph();
        mRules.put(Entry.class, new EntryRule(mCompileGraph));
        mRules.put(PrimaryKey.class, new PrimaryKeyRule(processingEnv, mCompileGraph));
        mRules.put(Column.class, new ColumnRule(processingEnv, mCompileGraph));
        mRules.put(Relation.class, new RelationRule(processingEnv, mCompileGraph));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return mRules.keySet()
                .stream()
                .map(Class::getCanonicalName)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        mRules.forEach((annotation, rule) ->
                roundEnv.getElementsAnnotatedWith(annotation)
                        .forEach(element -> {
                            if (!SuperficialValidation.validateElement(element)) {
                                return;
                            }
                            try {
                                rule.process(element);
                            } catch (ElementException e) {
                                error(e);
                            } catch (Exception e) {
                                error(e);
                            }
                        }));
        try {
            mCompileGraph.brewJava(processingEnv.getFiler());
        } catch (ElementException e) {
            error(e);
        } catch (Exception e) {
            error(e);
        }
        return true;
    }

    private void error(Exception e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
    }

    private void error(ElementException e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage(), e.getElement());
    }

}
