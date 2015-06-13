package app.kuluna.jp.auth2;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Currency;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * CUtilのテストクラス
 */
@RunWith(AndroidJUnit4.class)
public class UtilTest {

    @Test
    public void isNullOrEmptテスト() {
        String nullString = null;
        String emptyString = "";
        String s1 = "ABC";
        String s2 = "あいう";

        assertThat(CUtil.isNullOrEmpty(nullString), is(true));
        assertThat(CUtil.isNullOrEmpty(emptyString), is(true));
        assertThat(CUtil.isNullOrEmpty(s1, nullString), is(true));
        assertThat(CUtil.isNullOrEmpty(s2, emptyString), is(true));
        assertThat(CUtil.isNullOrEmpty(s1, s2), is(false));
    }

    @Test
    public void justZeroSecondテスト() throws Exception {
        CUtil.justZeroSecond();
    }
}
