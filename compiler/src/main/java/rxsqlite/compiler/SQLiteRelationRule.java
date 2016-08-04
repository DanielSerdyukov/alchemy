package rxsqlite.compiler;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor7;

import rxsqlite.annotation.SQLiteRelation;

/**
 * @author Daniel Serdyukov
 */
class SQLiteRelationRule extends AnnotationRule {

    SQLiteRelationRule(ProcessingEnvironment pEnv) {
        super(pEnv);
    }

    static List<Element> resolveTypes(TypeMirror typeMirror) {
        final List<Element> resolution = new ArrayList<>();
        typeMirror.accept(new SimpleTypeVisitor7<Void, Void>() {
            @Override
            public Void visitDeclared(DeclaredType t, Void o) {
                resolution.add(t.asElement());
                for (final TypeMirror type : t.getTypeArguments()) {
                    resolution.add(type.accept(new SimpleTypeVisitor7<Element, Void>() {
                        @Override
                        public Element visitDeclared(DeclaredType t, Void aVoid) {
                            return t.asElement();
                        }
                    }, null));
                }
                return null;
            }
        }, null);
        return resolution;
    }

    @Override
    TypeElement getOriginType(Element element) {
        return (TypeElement) element.getEnclosingElement();
    }

    @Override
    void process(TableSpec spec, Element element, Map<TypeElement, TableSpec> specs) throws Exception {
        element.accept(this, null); // mark accessible
        final List<Element> types = resolveTypes(element.asType());
        if (types.size() == 1) {
            processRelation(spec, element, types.get(0), false);
        } else if (types.size() == 2) {
            if (isAssignable(types.get(0), List.class)) {
                processRelation(spec, element, types.get(1), true);
            } else {
                throw new IllegalArgumentException("Only List<T> supported as relation type");
            }
        } else {
            throw new IllegalArgumentException("Unsupported relation type");
        }
        spec.hasRelations = true;
    }

    void processStringListRelation(TableSpec spec, Element originElement, boolean onDeleteCascade)
            throws Exception {
        final String originTable = resolveTableName(getOriginType(originElement));
        final String fieldName = originElement.getSimpleName().toString();
        final String usFieldName = Utils.toUnderScope(Utils.normalize(fieldName));
        spec.create.addStatement("db.exec($S)", String.format(Locale.US,
                "CREATE TABLE IF NOT EXISTS %1$s_%2$s(" +
                        "fk INTEGER, " +
                        "value TEXT, " +
                        "FOREIGN KEY(fk) REFERENCES %1$s(_id) ON DELETE %3$s);",
                originTable, usFieldName, onDeleteCascade ? "CASCADE" : "NO ACTION"));
        makeStringListInsert(spec, originTable, fieldName, usFieldName);
        makeStringListSelect(spec, originTable, fieldName, usFieldName);
    }

    private void processRelation(TableSpec spec, Element originElement, Element relationElement,
            boolean oneToMany) throws Exception {
        final SQLiteRelation annotation = originElement.getAnnotation(SQLiteRelation.class);
        if (isAssignable(relationElement, String.class)) {
            processStringListRelation(spec, originElement, annotation.onDeleteCascade());
            return;
        }
        final String originTable = resolveTableName(getOriginType(originElement));
        final String fieldName = originElement.getSimpleName().toString();
        final String usFieldName = Utils.toUnderScope(Utils.normalize(fieldName));
        final String relationTable = resolveTableName(relationElement);
        createRelationSchema(spec, originTable, usFieldName, relationTable, annotation.onDeleteCascade());
        final TypeName relationType = TypeName.get(relationElement.asType());

        if (oneToMany) {
            makeOneToManyInsert(spec, originTable, fieldName, usFieldName, relationType);
            makeOneToManySelect(spec, originTable, fieldName, usFieldName, relationTable, relationType);
        } else {
            makeOneToOneInsert(spec, originTable, fieldName, usFieldName, relationType);
            makeOneToOneSelect(spec, originTable, fieldName, usFieldName, relationTable, relationType);
        }
    }

    private void createRelationSchema(TableSpec spec, String originTable, String usFieldName, String relationTable,
            boolean onDeleteCascade) {
        spec.create.addStatement("db.exec($S)", String.format(Locale.US,
                "CREATE TABLE IF NOT EXISTS %1$s_%2$s(" +
                        "fk1 INTEGER, " +
                        "fk2 INTEGER, " +
                        "UNIQUE(fk1, fk2) ON CONFLICT NONE, " +
                        "FOREIGN KEY(fk1) REFERENCES %1$s(_id) ON DELETE CASCADE);", originTable, usFieldName));
        if (onDeleteCascade) {
            spec.create.addStatement("db.exec($S)", String.format(Locale.US,
                    "CREATE TRIGGER IF NOT EXISTS delete_%1$s AFTER DELETE ON %2$s_%1$s FOR EACH ROW" +
                            " BEGIN" +
                            " DELETE FROM %3$s WHERE %3$s._id = OLD.fk2;" +
                            " END;", usFieldName, originTable, relationTable));
        }
    }

    private void makeOneToOneInsert(TableSpec spec, String originTable, String fieldName,
            String usFieldName, TypeName relationType) {
        final MethodSpec insertMethod = MethodSpec.methodBuilder("insert_" + fieldName)
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Consts.R_DB, "db")
                .addParameter(relationType, "rel")
                .addParameter(TypeName.LONG, "fk")
                .addStatement("final long[] rowIds = $1T.<$2T>findTable($2T.class).insert(db, $3T.singletonList(rel))",
                        Consts.SQLITE_SCHEMA, relationType, Consts.J_COLLECTIONS)
                .addStatement("final $T stmt = db.prepare($S)", Consts.R_STMT, String.format(Locale.US,
                        "INSERT INTO %s_%s VALUES(?, ?);", originTable, usFieldName))
                .beginControlFlow("try")
                .addStatement("stmt.bindLong(1, fk)")
                .addStatement("stmt.bindLong(2, rowIds[0])")
                .addStatement("stmt.executeInsert()")
                .nextControlFlow("finally")
                .addStatement("stmt.close()")
                .endControlFlow()
                .build();
        spec.privateMethods.add(insertMethod);
        spec.doAfterInsert.addStatement("$N(db, object.$L, id)", insertMethod, fieldName);
    }

    private void makeOneToOneSelect(TableSpec spec, String originTable, String fieldName, String usFieldName,
            String relationTable, TypeName relationType) {
        final MethodSpec selectMethod = MethodSpec.methodBuilder("select_" + fieldName)
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Consts.R_DB, "db")
                .addParameter(TypeName.LONG, "fk")
                .returns(relationType)
                .addStatement("final $T stmt = db.prepare($S)", Consts.R_STMT, String.format(Locale.US,
                        "SELECT m.* FROM %1$s AS m, %2$s_%3$s AS r WHERE m._id = r.fk2 AND r.fk1 = ?;",
                        relationTable, originTable, usFieldName))
                .beginControlFlow("try")
                .addStatement("stmt.bindLong(1, fk)")
                .addStatement("final $T cursor = stmt.executeSelect()", Consts.R_CURSOR)
                .beginControlFlow("if (cursor.step())")
                .addStatement("return $1T.<$2T>findTable($2T.class).instantiate(db, cursor)",
                        Consts.SQLITE_SCHEMA, relationType)
                .endControlFlow()
                .addStatement("return null")
                .nextControlFlow("finally")
                .addStatement("stmt.close()")
                .endControlFlow()
                .build();

        spec.privateMethods.add(selectMethod);
        spec.instantiate.addStatement("object.$L = $N(db, object.$L)", fieldName, selectMethod, spec.pkFieldName);
    }

    private void makeOneToManyInsert(TableSpec spec, String originTable, String fieldName,
            String usFieldName, TypeName relationType) {
        final MethodSpec insertMethod = MethodSpec.methodBuilder("insert_" + fieldName)
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Consts.R_DB, "db")
                .addParameter(Consts.generic(Consts.J_LIST, relationType), "rel")
                .addParameter(TypeName.LONG, "fk")
                .addStatement("final long[] rowIds = $1T.<$2T>findTable($2T.class).insert(db, rel)",
                        Consts.SQLITE_SCHEMA, relationType)
                .addStatement("final $T stmt = db.prepare($S)", Consts.R_STMT, String.format(Locale.US,
                        "INSERT INTO %s_%s VALUES(?, ?);", originTable, usFieldName))
                .beginControlFlow("try")
                .beginControlFlow("for (final long rowId : rowIds)")
                .addStatement("stmt.bindLong(1, fk)")
                .addStatement("stmt.bindLong(2, rowId)")
                .addStatement("stmt.executeInsert()")
                .addStatement("stmt.clearBindings()")
                .endControlFlow()
                .nextControlFlow("finally")
                .addStatement("stmt.close()")
                .endControlFlow()
                .build();
        spec.privateMethods.add(insertMethod);
        spec.doAfterInsert.addStatement("$N(db, object.$L, id)", insertMethod, fieldName);
    }

    private void makeOneToManySelect(TableSpec spec, String originTable, String fieldName, String usFieldName,
            String relationTable, TypeName relationType) {
        final MethodSpec selectMethod = MethodSpec.methodBuilder("select_" + fieldName)
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Consts.R_DB, "db")
                .addParameter(TypeName.LONG, "fk")
                .returns(Consts.generic(Consts.J_LIST, relationType))
                .addStatement("final $T stmt = db.prepare($S)", Consts.R_STMT, String.format(Locale.US,
                        "SELECT m.* FROM %1$s AS m, %2$s_%3$s AS r WHERE m._id = r.fk2 AND r.fk1 = ?;",
                        relationTable, originTable, usFieldName))
                .beginControlFlow("try")
                .addStatement("stmt.bindLong(1, fk)")
                .addStatement("final $T cursor = stmt.executeSelect()", Consts.R_CURSOR)
                .addStatement("final $1T table = $2T.findTable($3T.class)",
                        Consts.generic(Consts.ABSTRACT_TABLE, relationType), Consts.SQLITE_SCHEMA, relationType)
                .addStatement("final $T objects = new $T<>()",
                        Consts.generic(Consts.J_LIST, relationType), Consts.J_ARRAY_LIST)
                .beginControlFlow("while (cursor.step())")
                .addStatement("objects.add(table.instantiate(db, cursor))",
                        Consts.SQLITE_SCHEMA, relationType)
                .endControlFlow()
                .addStatement("return objects")
                .nextControlFlow("finally")
                .addStatement("stmt.close()")
                .endControlFlow()
                .build();

        spec.privateMethods.add(selectMethod);
        spec.instantiate.addStatement("object.$L = $N(db, object.$L)", fieldName, selectMethod, spec.pkFieldName);
    }

    private void makeStringListInsert(TableSpec spec, String originTable, String fieldName, String usFieldName) {
        final MethodSpec insertMethod = MethodSpec.methodBuilder("insert_" + fieldName)
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Consts.R_DB, "db")
                .addParameter(Consts.generic(Consts.J_LIST, Consts.J_STRING), "values")
                .addParameter(TypeName.LONG, "fk")
                .addStatement("final $T stmt = db.prepare($S)", Consts.R_STMT, String.format(Locale.US,
                        "INSERT INTO %s_%s VALUES(?, ?);", originTable, usFieldName))
                .beginControlFlow("try")
                .beginControlFlow("for (final $T value : values)", Consts.J_STRING)
                .addStatement("stmt.bindLong(1, fk)")
                .addStatement("stmt.bindString(2, value)")
                .addStatement("stmt.executeInsert()")
                .addStatement("stmt.clearBindings()")
                .endControlFlow()
                .nextControlFlow("finally")
                .addStatement("stmt.close()")
                .endControlFlow()
                .build();
        spec.privateMethods.add(insertMethod);
        spec.doAfterInsert.addStatement("$N(db, object.$L, id)", insertMethod, fieldName);
    }

    private void makeStringListSelect(TableSpec spec, String originTable, String fieldName, String usFieldName) {
        final MethodSpec selectMethod = MethodSpec.methodBuilder("select_" + fieldName)
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Consts.R_DB, "db")
                .addParameter(TypeName.LONG, "fk")
                .returns(Consts.generic(Consts.J_LIST, Consts.J_STRING))
                .addStatement("final $T stmt = db.prepare($S)", Consts.R_STMT, String.format(Locale.US,
                        "SELECT value FROM %s_%s WHERE fk = ?;", originTable, usFieldName))
                .beginControlFlow("try")
                .addStatement("stmt.bindLong(1, fk)")
                .addStatement("final $T cursor = stmt.executeSelect()", Consts.R_CURSOR)
                .addStatement("final $T values = new $T<>()",
                        Consts.generic(Consts.J_LIST, Consts.J_STRING), Consts.J_ARRAY_LIST)
                .beginControlFlow("while (cursor.step())")
                .addStatement("values.add(cursor.getColumnString(0))")
                .endControlFlow()
                .addStatement("return values")
                .nextControlFlow("finally")
                .addStatement("stmt.close()")
                .endControlFlow()
                .build();
        spec.privateMethods.add(selectMethod);
        spec.instantiate.addStatement("object.$L = $N(db, object.$L)", fieldName, selectMethod, spec.pkFieldName);
    }

}
