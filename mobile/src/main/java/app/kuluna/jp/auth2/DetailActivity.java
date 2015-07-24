package app.kuluna.jp.auth2;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;

/**
 * TOTPキーの詳細を表示するActivity
 */
public class DetailActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher, SeekBar.OnSeekBarChangeListener {
    /**
     * ダイアログのリターンコード
     */
    private static final int BACK_DIALOG = 20;

    private TextView textAccountId, textSecret;
    private ImageView imageStar;
    private EditText editAccountId;
    private SeekBar seekbarPriority;
    private TotpModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 表示対象のIDを取得
        long id = getIntent().getLongExtra("id", -1);
        if (id == -1) {
            // 取得できなかった場合はエラーで前の画面に戻る
            Toast.makeText(this, getString(R.string.error_auth2id), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // XMLの関連付け
        textAccountId = (TextView) findViewById(R.id.card_accountid);
        textSecret = (TextView) findViewById(R.id.card_authkey);
        imageStar = (ImageView) findViewById(R.id.card_star);
        EditText auth2id = (EditText) findViewById(R.id.edittext_auth2id);
        editAccountId = (EditText) findViewById(R.id.edittext_accountId);
        editAccountId.addTextChangedListener(this);
        EditText editIssue = (EditText) findViewById(R.id.edittext_issue);
        editIssue.addTextChangedListener(this);
        seekbarPriority = (SeekBar) findViewById(R.id.seekbar_priority);
        seekbarPriority.setOnSeekBarChangeListener(this);
        findViewById(R.id.include_keycard).setOnClickListener(this);
        findViewById(R.id.button_apply).setOnClickListener(this);

        // データ取得
        model = new Select().from(TotpModel.class).where("id=?", id).executeSingle();
        // データマッピング
        setTitle(model.accountId);
        imageStar.setVisibility(View.INVISIBLE);
        auth2id.setText(String.valueOf(model.getId()));
        editAccountId.setText(model.accountId);
        editIssue.setText(model.issuer);
        seekbarPriority.setProgress(model.listOrder);

        // ActionBarにUpボタンの追加
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateHandler.sendEmptyMessage(100);
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateHandler.removeMessages(100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Delボタンを追加
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Delボタン
            case R.id.action_del:
                // 削除確認ダイアログを表示する
                DeleteDialogFragment dialog = DeleteDialogFragment.newInstance(model.getId());
                dialog.setTargetFragment(null, BACK_DIALOG);
                dialog.show(getSupportFragmentManager(), "dialog");
                break;

            // Action Upボタン
            case android.R.id.home:
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 削除確認ダイアログから戻ってきたときに呼ばれるメソッド
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BACK_DIALOG && resultCode == RESULT_OK) {
            // 削除に成功した場合はActivityを終了する
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     *
     */
    @Override
    public void onClick(@NonNull View v) {
        if (v.getId() == R.id.button_apply) {
            // Applyボタンが押された時
            // データの更新を行い、キー一覧画面へ戻る
            model.accountId = editAccountId.getText().toString();
            model.listOrder = seekbarPriority.getProgress();
            model.save();

            finish();
        } else if (v.getId() == R.id.include_keycard) {
            // キーを表示するカードが押された時
            // キーをクリップボードにコピーする
            copyClipboard();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(@NonNull CharSequence s, int start, int before, int count) {
    }

    /**
     * EditText入力後に呼ばれるメソッド
     * 文字列をチェックし、禁止文字を取り除いて画面に表示する
     */
    @Override
    public void afterTextChanged(Editable s) {
        // 禁止文字を使っていたら除去する
        String match = "(\\[|\\])";
        if (s.toString().matches(".*" + match + ".*")) {
            editAccountId.setText(editAccountId.getText().toString().replaceAll(match, ""));
            Toast.makeText(this, getString(R.string.error_textfilter), Toast.LENGTH_LONG).show();
        }

        // 画面情報を更新する
        textAccountId.setText(editAccountId.getText().toString());
    }

    /**
     * 優先度のシークバーを操作した時に呼ばれるメソッド
     * 優先度1以上なら星を表示させる
     */
    @Override
    public void onProgressChanged(@NonNull SeekBar seekBar, int progress, boolean user) {
        if (progress > 0) {
            imageStar.setVisibility(View.VISIBLE);
        } else {
            imageStar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(@NonNull SeekBar seekBar) {
    }

    /**
     * キーをクリップボードにコピーします
     */
    private void copyClipboard() {
        // 認証キーをクリップボードに保存する
        ClipData.Item item = new ClipData.Item(model.getAuthKey());
        String[] mimeType = {ClipDescription.MIMETYPE_TEXT_PLAIN};
        ClipData cd = new ClipData(new ClipDescription("text_data", mimeType), item);
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        cm.setPrimaryClip(cd);

        // コピー成功のToastを表示する
        Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
    }

    /**
     * 1分置きにキーを更新するHandler
     */
    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            // 1分後に再更新
            updateHandler.sendEmptyMessageDelayed(0, CUtil.justZeroSecond());

            textSecret.setText(String.valueOf(model.getAuthKey()));
            copyClipboard();
            Log.i("Auth2", "Key Updated.");
        }
    };
}
