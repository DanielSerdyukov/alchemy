package rxsqlite.compiler;

import com.squareup.javapoet.ClassName;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor7;

import rxsqlite.annotation.SQLiteObject;
import rxsqlite.annotation.SQLiteRelation;

/**
 * @author Daniel Serdyukov
 */
class SQLiteRelationRule implements AnnotationRule {

    @Override
    public void process(final Map<TypeElement, TableSpec> specs, final Element element) throws Exception {
        element.asType().accept(new SimpleTypeVisitor7<Void, Void>() {
            @Override
            public Void visitDeclared(DeclaredType declaredType, Void unused) {
                final List<? extends TypeMirror> argTypes = declaredType.getTypeArguments();
                if (argTypes.isEmpty()) {
                    processOneToOne(specs, element, declaredType);
                } else {
                    processManyToMany(specs, element, declaredType, argTypes);
                }
                return super.visitDeclared(declaredType, unused);
            }
        }, null);
    }

    private void processOneToOne(Map<TypeElement, TableSpec> specs, Element element, DeclaredType declaredType) {
        final Element relationElement = declaredType.asElement();
        if (relationElement.getAnnotation(SQLiteObject.class) == null) {
            throw new IllegalArgumentException(declaredType + " not annotated with @SQLiteObject");
        }
        processRelation(specs, element, relationElement, true);
    }

    private void processManyToMany(final Map<TypeElement, TableSpec> specs, final Element element,
            DeclaredType declaredType, List<? extends TypeMirror> argTypes) {
        final Element relationElement = declaredType.asElement();
        if (!Literals.isAssignable(relationElement.asType(), List.class)) {
            throw new IllegalArgumentException("Only List<E> supported as relation");
        }
        argTypes.get(0).accept(new SimpleTypeVisitor7<Void, Void>() {
            @Override
            public Void visitDeclared(DeclaredType declaredType, Void unused) {
                if (declaredType.asElement().getAnnotation(SQLiteObject.class) == null
                        && !Literals.isAssignable(declaredType, String.class)) {
                    throw new IllegalArgumentException(declaredType + " not annotated with @SQLiteObject");
                }
                processRelation(specs, element, declaredType.asElement(), false);
                return super.visitDeclared(declaredType, unused);
            }
        }, null);
    }

    private void processRelation(Map<TypeElement, TableSpec> specs, Element element, Element relationElement,
            boolean oneToOne) {
        final TableSpec lTable = TableSpec.get(specs, (TypeElement) element.getEnclosingElement());
        final String fieldName = element.getSimpleName().toString();
        final SQLiteRelation annotation = element.getAnnotation(SQLiteRelation.class);
        if (Literals.isAssignable(relationElement.asType(), String.class)) {
            lTable.addRelation(new RelationSpec(lTable.getTableName(), fieldName, oneToOne, annotation.cascade()));
        } else {
            final TableSpec rTable = TableSpec.get(specs, (TypeElement) relationElement);
            lTable.addRelation(new RelationSpec(lTable.getTableName(), rTable.getClassName(),
                    ClassName.get((TypeElement) relationElement), fieldName, oneToOne, annotation.cascade()));
        }
        Literals.fixFieldAccess(element);
    }

}
