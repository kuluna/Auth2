package app.kuluna.jp.auth2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Fan Draw View
 */
public class FanView extends View {
    private int arcColor = Color.WHITE;

    public FanView(Context context) {
        super(context);
    }

    public FanView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
        Paint paint = new Paint();
        paint.setColor(arcColor);
        paint.setAntiAlias(true);

        RectF rectF = new RectF(overWidth, overHeight, (float) diagonal + overWidth, (float) diagonal + overHeight);
        canvas.drawArc(rectF, 270, rad, true, paint);

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
