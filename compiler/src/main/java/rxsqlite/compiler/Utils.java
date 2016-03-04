package rxsqlite.compiler;

import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner7;
import javax.lang.model.util.Elements;

/**
 * @author Daniel Serdyukov
 */
class Utils {

    private static final List<Pattern> NAMING = Arrays.asList(
            Pattern.compile("^m([A-Z][a-zA-Z0-9]*)$"),
            Pattern.compile("^([a-z][a-zA-Z0-9]*)$")
    );

    private static Elements sElements;

    private static Trees sTrees;

    static void init(ProcessingEnvironment processingEnv) {
        sElements = processingEnv.getElementUtils();
        sTrees = Trees.instance(processingEnv);
    }

    static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    static String join(String glue, Iterable<String> tokens) {
        final StringBuilder sb = new StringBuilder(128);
        final Iterator<String> iterator = tokens.iterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next());
            if (iterator.hasNext()) {
                sb.append(glue);
            }
        }
        return sb.toString();
    }

    static String join(String glue, String[] tokens) {
        return join(glue, Arrays.asList(tokens));
    }

    static String getColumnName(String fieldName) {
        return toUnderScope(getCanonicalName(fieldName));
    }

    static String getCanonicalName(String fieldName) {
        for (final Pattern pattern : NAMING) {
            final Matcher matcher = pattern.matcher(fieldName);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }
        return fieldName;
    }

    static String toUnderScope(String fieldName) {
        return fieldName.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }

    static boolean isLongType(TypeMirror type) {
        return TypeKind.LONG == type.getKind()
                || TypeKind.INT == type.getKind()
                || TypeKind.SHORT == type.getKind();
    }

    static boolean isDoubleType(TypeMirror type) {
        return TypeKind.DOUBLE == type.getKind()
                || TypeKind.FLOAT == type.getKind();
    }

    static boolean isBooleanType(TypeMirror type) {
        return TypeKind.BOOLEAN == type.getKind();
    }

    static boolean isStringType(TypeMirror type) {
        return TypeKind.DECLARED == type.getKind() && "java.lang.String".equals(type.toString());
    }

    static boolean isBlobType(TypeMirror type) {
        if (TypeKind.ARRAY == type.getKind()) {
            final ArrayType arrayType = (ArrayType) type;
            return TypeKind.BYTE == arrayType.getComponentType().getKind();
        }
        return false;
    }

    static boolean isEnumType(TypeMirror type) {
        return TypeKind.DECLARED == type.getKind() &&
                ElementKind.ENUM == sElements.getTypeElement(type.toString()).getKind();
    }

    static void setAccessible(Element element) {
        element.accept(new ElementScanner7<Void, Void>() {
            @Override
            public Void visitVariable(VariableElement e, Void unused) {
                ((JCTree) sTrees.getTree(e)).accept(new TreeTranslator() {
                    @Override
                    public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
                        super.visitVarDef(jcVariableDecl);
                        jcVariableDecl.mods.flags &= ~Flags.PRIVATE;
                        this.result = jcVariableDecl;
                    }
                });
                return super.visitVariable(e, unused);
            }
        }, null);
    }

}
