package app.kuluna.jp.auth2;

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
     * コンストラクタ
     *
     * @param accountId サービス名
     * @param secret    アカウント名
     * @param issuer    secret キー
     * @param listOrder リスト表示順
     */
    public TotpModel(String accountId, String secret, String issuer, int listOrder) {
        this.accountId = accountId;
        this.secret = secret;
        this.issuer = issuer;
        this.listOrder = listOrder;
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
