package alchemy.sqlite.compiler;

import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubjectFactory;
import org.junit.Test;

import java.util.Arrays;

public class SQLiteCompilerTest {

    @Test
    public void generatedFiles() throws Exception {
        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("models/groups/Group.java"),
                        JavaFileObjects.forResource("models/users/User.java")
                ))
                .processedWith(new SQLiteCompiler())
                .compilesWithoutError()
                .and()
                .generatesSources(
                        JavaFileObjects.forResource("generated/alchemy/sqlite/DefaultSchema.java"),
                        JavaFileObjects.forResource("generated/alchemy/sqlite/Group_Table.java"),
                        JavaFileObjects.forResource("generated/alchemy/sqlite/User_Table.java"),
                        JavaFileObjects.forResource("generated/models/groups/Group_Entry.java"),
                        JavaFileObjects.forResource("generated/models/groups/Group_Admin.java"),
//                        JavaFileObjects.forResource("generated/models/groups/Group_Users.java"),
                        JavaFileObjects.forResource("generated/models/users/User_Entry.java")
                );
    }

}
