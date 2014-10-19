package app.kuluna.jp.auth2;

import android.net.Uri;

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
     * コンストラクタ
     */
    public TotpModel() {
    }

    /**
     * コンストラクタ
     *
     * @param uriString otpauth:// から始まるUri
     */
    public TotpModel(String uriString) {
        Uri uri = Uri.parse(uriString);
        accountId = uri.getLastPathSegment();
        issuer = uri.getQueryParameter("issuer");
        secret = uri.getQueryParameter("secret");
    }

    /**
     * コンストラクタ
     *
     * @param accountId サービス名
     * @param secret    アカウント名
     * @param issuer    secret キー
     */
    public TotpModel(String accountId, String secret, String issuer) {
        this.accountId = accountId;
        this.secret = secret;
        this.issuer = issuer;
    }

    /**
     * アカウントが正しく登録されているか確認します
     *
     * @return 正常なら true
     */
    public boolean isOtpAuth() {
        return !CUtil.isNullOrEmpty(accountId, issuer, secret);
    }

    /**
     * 6桁の認証キーを取得します
     * @return 認証キー
     */
    public String getAuthKey() {
        return new Totp(secret).now();
    }
}
