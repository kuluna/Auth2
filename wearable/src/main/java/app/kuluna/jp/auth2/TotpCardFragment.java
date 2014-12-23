package app.kuluna.jp.auth2;

import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * キーを表示するCardFragment
 */
public class TotpCardFragment extends CardFragment {

    /**
     * インスタンスを生成します
     * @param accountId アカウント名
     * @param authKey 6ケタの認証キー
     * @return {@link app.kuluna.jp.auth2.TotpCardFragment}
     */
    public static TotpCardFragment create(String accountId, String authKey) {
        TotpCardFragment f = new TotpCardFragment();
        Bundle args = new Bundle();
        args.putString("accountid", accountId);
        args.putString("authkey", authKey);
        f.setArguments(args);
        return f;
    }

    @Override
    protected View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frame_key, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        View v = getView();
        ((TextView) v.findViewById(R.id.card_account)).setText(args.getString("accountid"));
        ((TextView) v.findViewById(R.id.card_authkey)).setText(args.getString("authkey"));
    }
}
