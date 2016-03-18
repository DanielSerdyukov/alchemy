package rxsqlite.compiler;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

/**
 * @author Daniel Serdyukov
 */
interface Relation {

    void appendToCreate(MethodSpec.Builder builder);

    void appendToSave(MethodSpec.Builder builder, String pkField);

    void appendToInstantiate(MethodSpec.Builder builder, String pkField);

    void brewSaveRelationMethod(TypeSpec.Builder typeSpec);

    void brewQueryRelationMethod(TypeSpec.Builder typeSpec);

}
