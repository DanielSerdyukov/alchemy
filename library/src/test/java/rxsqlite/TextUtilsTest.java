package rxsqlite;

import android.text.TextUtils;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;

/**
 * @author Daniel Serdyukov
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class TextUtilsTest {

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(TextUtils.isEmpty(Mockito.anyString()))
                .thenAnswer(Answers.isEmpty());
        PowerMockito.when(TextUtils.join(Mockito.anyString(), Mockito.anyCollection()))
                .thenAnswer(Answers.joinIterable());
    }

    @Test
    public void testIsEmpty() throws Exception {
        Assert.assertThat(TextUtils.isEmpty(""), Is.is(true));
        Assert.assertThat(TextUtils.isEmpty(null), Is.is(true));
        Assert.assertThat(TextUtils.isEmpty("foo"), Is.is(false));
    }

    @Test
    public void testJoinIterable() throws Exception {
        Assert.assertThat(TextUtils.join(", ", Arrays.asList(1, 2, 3, 4, 5)), Is.is("1, 2, 3, 4, 5"));
    }

}
