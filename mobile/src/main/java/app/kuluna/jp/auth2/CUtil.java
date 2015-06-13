package app.kuluna.jp.auth2;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.security.auth.x500.X500Principal;

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

    /**
     * 新しいマスターキーを作成します。このメソッドを実行するたびにキーが変更されます
     *
     * @param context {@link Context}
     * @return 新しいマスターキー
     * @throws GeneralSecurityException キーの生成に失敗
     */
    private static String createKey(Context context) throws GeneralSecurityException {
        String alias = context.getString(R.string.app_name);
        // キーの有効期限を25年に設定
        Calendar start = new GregorianCalendar();
        Calendar end = new GregorianCalendar();
        end.add(Calendar.YEAR, 25);

        // キーの生成
        KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                .setAlias(alias)
                .setSubject(new X500Principal("CN=" + alias))
                .setSerialNumber(BigInteger.valueOf(1337))
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();
        KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
        kpGenerator.initialize(spec);
        KeyPair kp = kpGenerator.generateKeyPair();

        return kp.getPrivate().toString();
    }


    /**
     * マスターキーを取得します。
     *
     * @param context {@link Context}
     * @return マスターキー
     * @throws GeneralSecurityException キーの生成に失敗
     */
    public static String getKey(Context context) throws GeneralSecurityException {
        String alias = context.getString(R.string.app_name);
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        try {
            ks.load(null);
        } catch (IOException ignored) {
            // 普通はありえないのでもみ消す
        }
        KeyStore.Entry entry = ks.getEntry(alias, null);


        if (entry == null || !(entry instanceof KeyStore.PrivateKeyEntry)) {
            // まだ秘密鍵が作られてないなら作る
            return createKey(context);
        } else {
            // すでにあるならそれを返す
            return ((KeyStore.PrivateKeyEntry) entry).getPrivateKey().toString();
        }
    }
}
