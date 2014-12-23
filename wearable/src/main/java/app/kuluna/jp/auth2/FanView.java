package app.kuluna.jp.auth2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Fan Draw View
 */
public class FanView extends View {
    private Paint mPaint;
    private RectF mRectF;

    private int arcColor = Color.GRAY;

    public FanView(Context context) {
        super(context);
        init();
    }

    public FanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /** 描画に必要な準備を行います */
    private void init() {
        // Paintの初期化
        mPaint = new Paint();
        mPaint.setColor(arcColor);
        mPaint.setAntiAlias(true);

        // Fanの初期化
        mRectF = new RectF(0, 0, 0, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 現在時刻に基づいた円弧の角度を求める
        float rad = (float) 0.006 * (System.currentTimeMillis() % 60000);
        // 描画枠の対角線の長さを求める
        double diagonal = Math.sqrt((getWidth() * getWidth()) + (getHeight() * getHeight()));
        int overWidth = (int) (getWidth() - diagonal) / 2;
        int overHeight = (int) (getHeight() - diagonal) / 2;

        // 描画
        mRectF.set(overWidth, overHeight, (float) diagonal + overWidth, (float) diagonal + overHeight);
        canvas.drawArc(mRectF, 270, rad, true, mPaint);

        // そして再描画のループ
        invalidate();
    }

    public void setArcColor(int color) {
        arcColor = color;
    }

    public int getArcColor() {
        return arcColor;
    }
}
