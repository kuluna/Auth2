package app.kuluna.jp.auth2;

import android.net.Uri;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.jboss.aerogear.security.otp.Totp;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 二段階認証(TOTP) モデル
 */
@Table(name = "totp")
public class TotpModel extends Model {
    /**
     * サービス名
     */
    @Column(name = "issuer")
    public String issuer;
    /**
     * アカウント名
     */
    @Column(name = "account_id")
    public String accountId;
    /**
     * secret キー
     */
    @Column(name = "secret")
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
    public TotpModel(String uriString) throws IllegalArgumentException {
        if (uriString.matches(".*/totp/.*")) {
            Uri uri = Uri.parse(uriString);
            accountId = uri.getLastPathSegment();
            issuer = uri.getQueryParameter("issuer");
            secret = uri.getQueryParameter("secret");
        } else {
            throw new IllegalArgumentException("this is not totp uri " + uriString);
        }
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

    @Override
    public String toString() {
        try {
            JSONObject json = new JSONObject();
            json.put("accountid", accountId);
            json.put("issuer", issuer);
            json.put("secret", secret);
            return json.toString();
        } catch (JSONException e) {
            return super.toString();
        }
    }
}
