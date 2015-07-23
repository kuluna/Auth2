package app.kuluna.jp.auth2;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;

/**
 * TOTPキーの詳細を表示するActivity
 */
public class DetailActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    /**
     * ダイアログのリターンコード
     */
    private static final int BACK_DIALOG = 20;

    private TextView textAccountId;
    private EditText editAccountId;
    private SeekBar seekbarPriority;
    private TotpModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setResult(RESULT_CANCELED);
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
        TextView authKey = (TextView) findViewById(R.id.card_authkey);
        EditText auth2id = (EditText) findViewById(R.id.edittext_auth2id);
        editAccountId = (EditText) findViewById(R.id.edittext_accountId);
        editAccountId.addTextChangedListener(this);
        EditText editIssue = (EditText) findViewById(R.id.edittext_issue);
        editIssue.addTextChangedListener(this);
        seekbarPriority = (SeekBar) findViewById(R.id.seekbar_priority);
        findViewById(R.id.button_apply).setOnClickListener(this);

        // データ取得
        model = new Select().from(TotpModel.class).where("id=?", id).executeSingle();
        // データマッピング
        setTitle(model.accountId);
        authKey.setTextColor(getResources().getColor(R.color.brandcolor_dark));
        authKey.setText("XXXXXX");
        auth2id.setText(String.valueOf(model.getId()));
        editAccountId.setText(model.accountId);
        editIssue.setText(model.issuer);
        seekbarPriority.setProgress(model.listOrder);

        // ActionBarにUpボタンの追加
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                setResultAndFinish(RESULT_CANCELED);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BACK_DIALOG && resultCode == RESULT_OK) {
            // 削除に成功した場合はActivityを終了する
            setResultAndFinish(RESULT_OK);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Applyボタンが押された時に呼ばれるメソッド
     * データの更新を行い、キー一覧画面へ戻る
     */
    @Override
    public void onClick(@NonNull View v) {
        model.accountId = editAccountId.getText().toString();
        model.listOrder = seekbarPriority.getProgress();
        model.save();

        setResultAndFinish(RESULT_OK);
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
     * リザルトコードを指定してこのActivityを終了します。
     *
     * @param resultCode リザルトコード
     */
    private void setResultAndFinish(int resultCode) {
        setResult(resultCode);
        finish();
    }
}
