package app.kuluna.jp.auth2;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Auth2(Wear) メインアクティビティ
 */
public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks {
    private GoogleApiClient googleApiClient;
    private CardPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final GridViewPager gridViewPager = (GridViewPager) findViewById(R.id.gridviewpager);
        adapter = new CardPagerAdapter(getFragmentManager());
        gridViewPager.setAdapter(adapter);

        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addApi(Wearable.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        adapter.clear();
        // Google Play Serviceに接続
        if (googleApiClient != null && !googleApiClient.isConnected()) {
            googleApiClient.connect();
        }

        h.sendEmptyMessageDelayed(0, CUtil.justZeroSecond());
    }

    /**
     * Google Play Serviceに接続できたときに呼ばれるメソッド
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.getDataItems(googleApiClient).setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                for (DataItem item : dataItems) {
                    try {
                        // 先頭に変なゴミがついてるので除去
                        String[] splits = new String(item.getData()).split("\\[");
                        String jsonStr = "[" + splits[splits.length - 1];

                        JSONArray array = new JSONArray(jsonStr);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject json = array.getJSONObject(i);
                            if (adapter != null) {
                                adapter.add(new TotpModel(json.getString("accountid"), json.getString("secret"), json.getString("issuer")));
                            }
                        }
                    } catch (Exception e) {
                        Log.e("Auth2", e.getMessage(), e);
                        Toast.makeText(MainActivity.this, getString(R.string.error_uri), Toast.LENGTH_LONG).show();
                    }
                }
                dataItems.release();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Google Play Serviceから切断
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }

        h.removeMessages(0);
    }

    // 使わない
    @Override
    public void onConnectionSuspended(int i) {
    }

    private Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 1分後に再更新
            h.sendEmptyMessageDelayed(0, CUtil.justZeroSecond());
            adapter.notifyDataSetChanged();
            Log.i("Auth2", "Key Updated.");
        }
    };


    /**
     * GridViewPagerにTOTPキーを表示するためのAdapter
     */
    private class CardPagerAdapter extends FragmentGridPagerAdapter {
        private List<TotpModel> models = new ArrayList<>();

        public CardPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * キーを追加します
         * @param model {@link app.kuluna.jp.auth2.TotpModel}
         */
        public void add(TotpModel model) {
            models.add(model);
            notifyDataSetChanged();
        }

        /**
         * リストをすべて消します
         */
        public void clear() {
            models.clear();
            notifyDataSetChanged();
        }

        @Override
        public Fragment getFragment(int i, int i2) {
            TotpModel model = models.get(i);
            return TotpCardFragment.create(model.accountId, model.getAuthKey());
        }

        @Override
        public int getRowCount() {
            return models.size();
        }

        @Override
        public int getColumnCount(int i) {
            return 1;
        }
    }
}
