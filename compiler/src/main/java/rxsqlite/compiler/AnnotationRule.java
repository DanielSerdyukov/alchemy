package rxsqlite.compiler;

import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;

import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner7;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import rxsqlite.annotation.SQLiteObject;

/**
 * @author Daniel Serdyukov
 */
abstract class AnnotationRule extends ElementScanner7<Void, Void> {

    private final Trees mTrees;

    private final Elements mElements;

    private Types mTypes;

    AnnotationRule(ProcessingEnvironment pEnv) {
        mTrees = Trees.instance(pEnv);
        mElements = pEnv.getElementUtils();
        mTypes = pEnv.getTypeUtils();
    }

    static String resolveTableName(Element element) {
        final SQLiteObject annotation = element.getAnnotation(SQLiteObject.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Not annotated with @SQLiteObject");
        }
        String tableName = annotation.value();
        if (Utils.isEmpty(tableName)) {
            tableName = Utils.toUnderScope(element.getSimpleName().toString());
        }
        return tableName;
    }

    @Override
    public Void visitVariable(VariableElement e, Void unused) {
        ((JCTree) mTrees.getTree(e)).accept(new TreeTranslator() {
            @Override
            public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
                super.visitVarDef(jcVariableDecl);
                jcVariableDecl.mods.flags &= ~Flags.PRIVATE;
                this.result = jcVariableDecl;
            }
        });
        return super.visitVariable(e, unused);
    }

    abstract TypeElement getOriginType(Element element);

    abstract void process(TableSpec spec, Element element, Map<TypeElement, TableSpec> specs) throws Exception;

    boolean isAssignable(Element element, Class<?> type) {
        final TypeElement typeElement = mElements.getTypeElement(type.getCanonicalName());
        return typeElement != null && mTypes.isAssignable(element.asType(), typeElement.asType());
    }

}
