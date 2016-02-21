package rxsqlite.compiler;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Daniel Serdyukov
 */
public class UtilsTest {

    @Test
    public void testIsEmpty() throws Exception {
        Assert.assertThat(Utils.isEmpty(""), Is.is(true));
        Assert.assertThat(Utils.isEmpty(null), Is.is(true));
        Assert.assertThat(Utils.isEmpty("foo"), Is.is(false));
    }

    @Test
    public void testJoin() throws Exception {
        Assert.assertThat(Utils.join(", ", Arrays.asList("1", "2", "3")), Is.is("1, 2, 3"));
    }

    @Test
    public void testGetColumnName() throws Exception {
        Assert.assertThat(Utils.getColumnName("foo"), Is.is("foo"));
        Assert.assertThat(Utils.getColumnName("mFoo"), Is.is("foo"));
        Assert.assertThat(Utils.getColumnName("fooBar"), Is.is("foo_bar"));
        Assert.assertThat(Utils.getColumnName("mFooBar"), Is.is("foo_bar"));
    }

    @Test
    public void testGetCanonicalName() throws Exception {
        Assert.assertThat(Utils.getCanonicalName("foo"), Is.is("foo"));
        Assert.assertThat(Utils.getCanonicalName("mFoo"), Is.is("Foo"));
        Assert.assertThat(Utils.getCanonicalName("aFoo"), Is.is("aFoo"));
        Assert.assertThat(Utils.getCanonicalName("myFoo"), Is.is("myFoo"));
    }

    @Test
    public void testToUnderScope() throws Exception {
        Assert.assertThat(Utils.toUnderScope("foo"), Is.is("foo"));
        Assert.assertThat(Utils.toUnderScope("fooBar"), Is.is("foo_bar"));
        Assert.assertThat(Utils.toUnderScope("FooBar"), Is.is("foo_bar"));
    }

}
