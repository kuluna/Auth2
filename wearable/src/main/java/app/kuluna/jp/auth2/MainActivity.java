package app.kuluna.jp.auth2;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.net.Uri;
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
 *
 */
public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks {
    private GoogleApiClient googleApiClient;
    private AuthListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final WearableListView listView = (WearableListView) findViewById(R.id.wearablelistview);
        adapter = new AuthListAdapter();
        listView.setAdapter(adapter);

        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addApi(Wearable.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                        String itemData = "[" + new String(item.getData()).split("\\[")[1];
                        Log.d("Auth2", item.getUri().toString());
                        JSONArray array = new JSONArray(itemData);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject json = array.getJSONObject(i);
                            adapter.add(new TotpModel(json.getString("accountid"), json.getString("secret"), json.getString("issuer")));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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
    public void onConnectionSuspended(int i) {}

    private Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 1分後に再更新
            h.sendEmptyMessageDelayed(0, CUtil.justZeroSecond());
            adapter.notifyDataSetChanged();
            Log.i("Auth2", "Key Updated.");
        }
    };

    private class AuthListAdapter extends WearableListView.Adapter {
        private List<TotpModel> models = new ArrayList<>();

        public void add(TotpModel model) {
            models.add(model);
            notifyItemInserted(getItemCount() - 1);
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(MainActivity.this).inflate(android.R.layout.simple_list_item_1, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder viewHolder, int i) {
            TotpModel model = models.get(viewHolder.getPosition());
            ((ViewHolder) viewHolder).textView.setText(model.accountId + " " + model.getAuthKey());
        }

        @Override
        public int getItemCount() {
            return models.size();
        }

        private class ViewHolder extends WearableListView.ViewHolder {
            public TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
