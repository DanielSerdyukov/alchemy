package rxsqlite.compiler;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;

/**
 * @author Daniel Serdyukov
 */
class OneToMany extends OneToOne {

    OneToMany(String tableName, Element field, Element relType) {
        super(tableName, field, relType);
    }

    @Override
    public void appendToSave(MethodSpec.Builder methodSpec, String pkField) {
    }

    @Override
    public void appendToInstantiate(MethodSpec.Builder methodSpec, String pkField) {
    }

    @Override
    public void brewSaveRelationMethod(TypeSpec.Builder typeSpec) {
    }

    @Override
    public void brewQueryRelationMethod(TypeSpec.Builder typeSpec) {
    }

}
