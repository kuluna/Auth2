package app.kuluna.jp.auth2;

/**
 * Auth2 ユーティリティクラス
 */
public class CUtil {
    /**
     * 文字列が NULL または空文字か判定します
     *
     * @param strings 文字列(複数可)
     * @return 1つでも NULL か空文字なら true
     */
    public static boolean isNullOrEmpty(String... strings) {
        for (String s : strings) {
            if (s == null || s.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 現在時刻から次の0秒ちょうどになるまでの残り時間を返します
     *
     * @return 次の0秒になるまでの残りミリ秒
     */
    public static long justZeroSecond() {
        return 60000 - System.currentTimeMillis() % 60000;
    }
}
