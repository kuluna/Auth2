package app.kuluna.jp.auth2;

import android.net.Uri;
import android.util.Log;

import org.jboss.aerogear.security.otp.Totp;

/**
 * 二段階認証(TOTP) モデル
 */
public class TotpModel {
    /**
     * サービス名
     */
    public String issuer;
    /**
     * アカウント名
     */
    public String accountId;
    /**
     * secret キー
     */
    public String secret;

    /**
     * リストの表示順
     */
    public int listOrder;

    /**
     * コンストラクタ
     */
    public TotpModel() {
    }

    /**
     * 6桁の認証キーを取得します
     *
     * @return 認証キー
     */
    public String getAuthKey() {
        try {
            return new Totp(secret).now();
        } catch (Exception e) {
            Log.e("Auth2", "Invalid Key: " + secret);
            return "Invalid Key!";
        }
    }
}
