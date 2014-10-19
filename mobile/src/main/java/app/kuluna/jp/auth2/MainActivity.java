package app.kuluna.jp.auth2;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.jdeferred.DoneCallback;
import org.jdeferred.DonePipe;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.android.DeferredAsyncTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Auth2 メインアクティビティ
 */
public class MainActivity extends ActionBarActivity {
    private CardAdapter cardAdapter;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // 全件取得
        List<TotpModel> datas = new Select().from(TotpModel.class).execute();
        cardAdapter = new CardAdapter(this);
        cardAdapter.addAll(datas);
        recyclerView.setAdapter(cardAdapter);

        // Android Wear用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            googleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        }
    }

    private Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 1分後に再更新
            h.sendEmptyMessageDelayed(0, CUtil.justZeroSecond());
            cardAdapter.notifyDataSetChanged();
            Log.i("Auth2", "Key Updated.");
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        h.sendEmptyMessageDelayed(0, CUtil.justZeroSecond());
    }

    @Override
    protected void onPause() {
        super.onPause();
        h.removeMessages(0);

        // Android Wear用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (googleApiClient != null && googleApiClient.isConnected()) {
                googleApiClient.disconnect();
            }
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        if (data != null) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            final TotpModel totpModel = new TotpModel(result.getContents());
            if (totpModel.isOtpAuth()) {
                // 問題なければ保存
                totpModel.save();
                // リストに追加
                cardAdapter.add(totpModel);


                // Android Wear用
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && googleApiClient != null) {
                    final ExecutorService executorService = Executors.newSingleThreadExecutor();
                    new AndroidDeferredManager(executorService).when(new DeferredAsyncTask<Void, Object, Boolean>() {
                        @Override
                        protected Boolean doInBackgroundSafe(Void... voids) throws Exception {
                            // この時点で接続切れてるのでもう一度つなぎにいく
                            ConnectionResult conRes = googleApiClient.blockingConnect(30, TimeUnit.SECONDS);
                            return conRes.isSuccess();
                        }
                    }).then(new DonePipe<Boolean, Boolean, Throwable, Object>() {
                        @Override
                        public Promise<Boolean, Throwable, Object> pipeDone(final Boolean result) {
                            DeferredAsyncTask<Void, Object, Boolean> task = new DeferredAsyncTask<Void, Object, Boolean>() {
                                @Override
                                protected Boolean doInBackgroundSafe(Void... voids) throws Exception {
                                    if (result) {
                                        // Data Layerに書き込む
                                        PutDataMapRequest dataMap = PutDataMapRequest.create("/auth2data");
                                        dataMap.getDataMap().putString("Totp", cardAdapter.getModels().toString());
                                        PutDataRequest request = dataMap.asPutDataRequest();
                                        DataApi.DataItemResult res = Wearable.DataApi.putDataItem(googleApiClient, request).await();
                                        return res.getStatus().isSuccess();
                                    }
                                    return false;
                                }
                            };
                            task.executeOnExecutor(executorService);
                            return task.promise();
                        }
                    }).done(new DoneCallback<Boolean>() {
                        @Override
                        public void onDone(Boolean result) {
                            Log.i("Auth2", "Data send: " + result);
                        }
                    }).fail(new FailCallback<Throwable>() {
                        @Override
                        public void onFail(Throwable result) {
                            result.printStackTrace();
                        }
                    });
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // メニューにADDボタンを追加
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            // ADDメニューが押されたらQRコードアプリを起動してスキャンする
            IntentIntegrator intent = new IntentIntegrator(this);
            intent.initiateScan();
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    private void sendWear() {

    }

    /**
     * 二段階認証カード情報を保持する Adapter
     */
    private class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
        private Context context;
        private List<TotpModel> models = new ArrayList<>();

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView account, secret;

            public ViewHolder(View itemView) {
                super(itemView);
                account = (TextView) itemView.findViewById(R.id.card_accountid);
                secret = (TextView) itemView.findViewById(R.id.card_authkey);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                // 認証キーをクリップボードに保存する
                ClipData.Item item = new ClipData.Item(models.get(getPosition()).getAuthKey());
                String[] mimeType = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                ClipData cd = new ClipData(new ClipDescription("text_data", mimeType), item);
                ClipboardManager cm = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                cm.setPrimaryClip(cd);

                // Toastを表示してアプリを終了する
                Toast.makeText(context, getString(R.string.copied), Toast.LENGTH_SHORT).show();
                MainActivity.this.finish();
            }
        }

        public CardAdapter(Context context) {
            this.context = context;
        }

        /**
         * データを追加します
         *
         * @param model 二段階認証データ
         */
        public void add(TotpModel model) {
            models.add(model);
            notifyItemInserted(getItemCount() - 1);
        }

        /**
         * データを追加します
         *
         * @param models 二段階認証データ
         */
        public void addAll(Collection<TotpModel> models) {
            this.models.addAll(models);
            notifyItemRangeInserted(this.models.size() - models.size() - 1, models.size());
        }

        /**
         * データ一覧を取得します
         * @return 二段階認証データ一覧
         */
        public List<TotpModel> getModels() {
            return models;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(context).inflate(R.layout.list_card, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            viewHolder.account.setText(models.get(i).accountId);
            viewHolder.secret.setText(models.get(i).getAuthKey());
            viewHolder.secret.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fadeout_fadein));
        }

        @Override
        public int getItemCount() {
            return models.size();
        }
    }
}
