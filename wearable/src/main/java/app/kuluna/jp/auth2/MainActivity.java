package app.kuluna.jp.auth2;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Auth2(Wear) メインアクティビティ
 */
public class MainActivity extends WearableActivity implements GoogleApiClient.ConnectionCallbacks {
    private GoogleApiClient googleApiClient;
    private CardPagerAdapter adapter;
    private UpdateHandler updateHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // AmbientModeを有効にする
        setAmbientEnabled();
        // キーを表示するカードを設置する
        final GridViewPager gridViewPager = (GridViewPager) findViewById(R.id.gridviewpager);
        adapter = new CardPagerAdapter(getFragmentManager());
        gridViewPager.setAdapter(adapter);
        // 1分毎に実行するHandlerのセットアップ
        updateHandler = new UpdateHandler(this);

        // Google Play Serviceクライアント
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

        // 1分毎にキーを更新
        updateHandler.sendEmptyMessageDelayed(0, CUtil.justZeroSecond());
    }

    /**
     * Google Play Serviceに接続できたときに呼ばれるメソッド
     */
    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.getDataItems(googleApiClient).setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                for (DataItem item : dataItems) {
                    try {
                        // 送られてきたTOTPデータを受け取る
                        DataMap data = DataMap.fromByteArray(item.getData());
                        // JsoNからデシリアライズ
                        List<TotpModel> models = new Gson().fromJson(data.getString("Totp"), new TypeToken<List<TotpModel>>() {
                        }.getType());
                        // リストに追加
                        adapter.addAll(models);

                    } catch (Exception e) {
                        Log.e("Auth2", e.getMessage(), e);
                        Toast.makeText(MainActivity.this, getString(R.string.error_uri), Toast.LENGTH_LONG).show();
                    }
                }

                // リソース解放(ただしデータはまだ生きている模様)
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

        // キーの更新を止める
        updateHandler.removeMessages(0);
    }

    // 使わない
    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        // AmbientMode時はFanViewを停止する
        FanView fanView = (FanView) findViewById(R.id.fanview);
        fanView.setVisibility(View.INVISIBLE);
        fanView.stopDraw();
    }

    @Override
    public void onExitAmbient() {
        // 通常モードに戻った時FanViewを再開する
        FanView fanView = (FanView) findViewById(R.id.fanview);
        fanView.setVisibility(View.VISIBLE);
        fanView.startDraw();
        super.onExitAmbient();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        // AmbientMode時はHandlerが停止しているのでこちらで更新する
        adapter.notifyDataSetChanged();
        Log.i("Auth2", "Key Updated.");
    }

    /**
     * 1分毎にキーを更新するHandler
     */
    private static class UpdateHandler extends Handler {
        private final WeakReference<MainActivity> activity;

        public UpdateHandler(MainActivity activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            sendEmptyMessageDelayed(msg.what, CUtil.justZeroSecond());
            activity.get().adapter.notifyDataSetChanged();
            Log.i("Auth2", "Key Updated.");
        }
    }

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
         *
         * @param models モデルのリスト
         */
        public void addAll(List<TotpModel> models) {
            this.models.addAll(models);
            notifyDataSetChanged();
        }

        /**
         * リストをすべて消します
         */
        public void clear() {
            this.models.clear();
            notifyDataSetChanged();
        }

        @Override
        public Fragment getFragment(int row, int column) {
            TotpModel model = this.models.get(row);
            return TotpCardFragment.newInstance(model.accountId, model.getAuthKey(), model.listOrder);
        }

        @Override
        public int getRowCount() {
            return this.models.size();
        }

        @Override
        public int getColumnCount(int row) {
            return 1;
        }
    }
}
