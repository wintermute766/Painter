package ru.dimasokol.learning.painting;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Дмитрий Соколов <DPSokolov.SBT@sberbank.ru>
 */

public class PaintingView extends View {
    private int tool;

    private Bitmap mBitmap, mBitmap1;
    private Canvas mBitmapCanvas;
    int pointerId1, pointerId2;
    int color;

    float x0, x1, y0, y1;

    private Paint[] mPredefinedPaints;
    private int mNextPaint = 0;

    private Paint mEditModePaint = new Paint();

    private SparseArray<PointF> mLastPoints = new SparseArray<>(10);


    private SparseArray<Paint> mPaints = new SparseArray<>(10);

    public PaintingView(Context context) {
        super(context);
        init();
    }

    public PaintingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PaintingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PaintingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        if (getRootView().isInEditMode()) {
            mEditModePaint.setColor(Color.MAGENTA);
        } else {
            TypedArray ta = getResources().obtainTypedArray(R.array.paint_colors);
            mPredefinedPaints = new Paint[ta.length()];

            for (int i = 0; i < ta.length(); i++) {
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setColor(ta.getColor(i, 0));
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeJoin(Paint.Join.ROUND);
                paint.setStrokeWidth(getResources().getDimension(R.dimen.default_paint_width));
                mPredefinedPaints[i] = paint;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w > 0 && h > 0) {
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            if (mBitmap != null) {
                canvas.drawBitmap(mBitmap, 0, 0, null);
                mBitmap.recycle();
            }

            mBitmap = bitmap;
            mBitmapCanvas = canvas;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (tool == 0) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    int pointerId = event.getPointerId(event.getActionIndex());
                    mLastPoints.put(pointerId, new PointF(event.getX(event.getActionIndex()), event.getY(event.getActionIndex())));
                    mPaints.put(pointerId, mPredefinedPaints[mNextPaint % mPredefinedPaints.length]);
                    mNextPaint++;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        PointF last = mLastPoints.get(event.getPointerId(i));
                        Paint paint = mPaints.get(event.getPointerId(i));

                        if (last != null) {
                            float x = event.getX(i);
                            float y = event.getY(i);

                            mBitmapCanvas.drawLine(last.x, last.y, x, y, paint);
                            last.x = x;
                            last.y = y;
                        }
                    }
                    invalidate();
                    return true;
                case MotionEvent.ACTION_POINTER_UP:
                    return true;
                case MotionEvent.ACTION_UP:
                    mLastPoints.clear();
                    return true;
            }
        } else if (tool == 1) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    color = mNextPaint % mPredefinedPaints.length;
                    mNextPaint++;

                    mBitmap1 = mBitmap.copy(Bitmap.Config.ARGB_8888, false);

                    x0 = event.getX();
                    y0 = event.getY();

                    return true;
                case MotionEvent.ACTION_MOVE:
                    mBitmapCanvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
                    mBitmapCanvas.drawBitmap(mBitmap1, 1, 1,null);

                    x1 = event.getX();
                    y1 = event.getY();
                    mBitmapCanvas.drawRect(x0, y0, x1, y1, mPredefinedPaints[color]);

                    invalidate();
                    return true;
                case MotionEvent.ACTION_UP:
                    return true;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            canvas.drawRect(getWidth() / 10, getHeight() / 10, (getWidth() / 10) * 9,
                    (getHeight() / 10) * 9, mEditModePaint);
        }

        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    /**
     * Очищает нарисованное
     */
    public void clear() {
        mBitmapCanvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    public void drawLines() {
        tool = 0;
    }
    public void drawRectangle() {
        tool = 1;
    }

}
