package app.kuluna.jp.auth2;

import android.net.Uri;
import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.scottyab.aescrypt.AESCrypt;

import org.jboss.aerogear.security.otp.Totp;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.GeneralSecurityException;

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
     * リストの表示順
     */
    @Column(name = "list_order")
    public int listOrder;

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
            listOrder = 0;

            try {
                // TOTPキーかどうか確認
                new Totp(secret).now();

            } catch (Exception e) {
                throw new IllegalArgumentException("this is not totp uri " + uriString + "\n" + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("this is not totp uri " + uriString);
        }
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

    @Override
    public String toString() {
        try {
            JSONObject json = new JSONObject();
            json.put("accountId", accountId);
            json.put("issuer", issuer);
            json.put("secret", secret);
            json.put("listOrder", listOrder);
            return json.toString();
        } catch (JSONException e) {
            return super.toString();
        }
    }
}
