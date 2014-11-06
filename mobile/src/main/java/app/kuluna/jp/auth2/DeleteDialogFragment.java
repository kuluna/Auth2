package app.kuluna.jp.auth2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

/**
 * 削除確認ダイアログを表示し、OKなら削除します
 */
public class DeleteDialogFragment extends DialogFragment {

    public static DeleteDialogFragment newInstance(int position, long totpId) {
        DeleteDialogFragment f = new DeleteDialogFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putLong("totpid", totpId);
        f.setArguments(args);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.confirm_delete);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int position = getArguments().getInt("position");
                long totpId = getArguments().getLong("totpid");
                // 該当データを消す
                new Delete().from(TotpModel.class).where("Id = ?", totpId).execute();


                Intent intent = new Intent();
                intent.putExtra("position", position);
                if (getTargetFragment() != null) {
                    // 呼び出し元がFragmentの場合
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                } else {
                    // 呼び出し元がActivityの場合
                    PendingIntent pi = getActivity().createPendingResult(getTargetRequestCode(), intent, PendingIntent.FLAG_ONE_SHOT);
                    try {
                        pi.send(Activity.RESULT_OK);
                    } catch (PendingIntent.CanceledException ex) {
                        // send failed
                    }
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);

        return builder.create();
    }
}
