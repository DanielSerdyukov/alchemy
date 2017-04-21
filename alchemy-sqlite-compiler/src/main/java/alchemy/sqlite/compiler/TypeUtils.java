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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Date;

class TypeUtils {

    private ProcessingEnvironment mEnv;

    TypeUtils(ProcessingEnvironment env) {
        mEnv = env;
    }

    boolean isLongFamily(TypeMirror type) {
        try {
            TypeKind typeKind = type.getKind();
            if (TypeKind.DECLARED == typeKind) {
                typeKind = unbox(type).getKind();
            }
            return TypeKind.LONG == typeKind || TypeKind.INT == typeKind || TypeKind.SHORT == typeKind;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    boolean isDoubleFamily(TypeMirror type) {
        try {
            TypeKind typeKind = type.getKind();
            if (TypeKind.DECLARED == typeKind) {
                typeKind = unbox(type).getKind();
            }
            return TypeKind.DOUBLE == typeKind || TypeKind.FLOAT == typeKind;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    boolean isByteArray(TypeMirror type) {
        return TypeKind.ARRAY == type.getKind() && TypeKind.BYTE == ((ArrayType) type).getComponentType().getKind();
    }

    boolean isString(TypeMirror type) {
        return isAssignable(type, String.class);
    }

    boolean isDate(TypeMirror type) {
        return isAssignable(type, Date.class);
    }

    boolean isAssignable(TypeMirror type, Class<?> clazz) {
        return mEnv.getTypeUtils().isAssignable(type, mEnv.getElementUtils()
                .getTypeElement(clazz.getCanonicalName()).asType());
    }

    private PrimitiveType unbox(TypeMirror typeMirror) {
        try {
            return mEnv.getTypeUtils().unboxedType(typeMirror);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Not a boxed primitive type", e);
        }
    }

}
