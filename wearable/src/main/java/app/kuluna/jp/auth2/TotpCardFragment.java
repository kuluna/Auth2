package app.kuluna.jp.auth2;

import android.graphics.drawable.Drawable;
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
    private Drawable defaultCardDrawable;
    private View rootView;

    /**
     * インスタンスを生成します
     *
     * @param accountId アカウント名
     * @param authKey   6ケタの認証キー
     * @param ambient   AmbientMode
     * @return {@link TotpCardFragment}
     */
    public static TotpCardFragment newInstance(String accountId, String authKey, int listOrder, boolean ambient) {
        TotpCardFragment f = new TotpCardFragment();
        Bundle args = new Bundle();
        args.putString("accountid", accountId);
        args.putString("authkey", authKey);
        args.putInt("listorder", listOrder);
        args.putBoolean("ambient", ambient);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = container;
        defaultCardDrawable = container.getBackground();

        return inflater.inflate(R.layout.frame_key, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        View v = getView();

        // AmbientModeかどうかでカードの背景色を変える
        if (args.getBoolean("ambient", false)) {
            rootView.setBackgroundColor(getResources().getColor(android.R.color.black));
        } else {
            rootView.setBackground(defaultCardDrawable);
        }

        ((TextView) v.findViewById(R.id.card_account)).setText(args.getString("accountid"));
        ((TextView) v.findViewById(R.id.card_authkey)).setText(args.getString("authkey"));
        if (args.getInt("listorder") > 0) {
            v.findViewById(R.id.card_star).setVisibility(View.VISIBLE);
        } else {
            v.findViewById(R.id.card_star).setVisibility(View.GONE);
        }
    }
}
