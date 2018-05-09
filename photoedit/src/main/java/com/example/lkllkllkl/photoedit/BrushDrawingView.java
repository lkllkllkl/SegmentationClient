package com.example.lkllkllkl.photoedit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Burhanuddin Rashid on 12/1/18.
 */

public class BrushDrawingView extends AppCompatImageView {

    private float mBrushSize = 25;
    private float mBrushEraserSize = 50;
    private int mOpacity = 255;

    private List<LinePath> mLinePaths = new ArrayList<>();
    private List<LinePath> mRedoLinePaths = new ArrayList<>();
    private Paint mDrawPaint;

    private Canvas mDrawCanvas;
    private boolean mBrushDrawMode;

    private Path mPath;
    private Matrix mPathMatrix;
    private float mTouchX, mTouchY;
    private static final float TOUCH_TOLERANCE = 4;

    private BrushViewChangeListener mBrushViewChangeListener;

    public BrushDrawingView(Context context) {
        this(context, null);
    }

    public BrushDrawingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BrushDrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setupBrushDrawing();
        setScaleType(ScaleType.MATRIX);
    }

    void setupBrushDrawing() {
        //Caution: This line is to disable hardware acceleration to make eraser feature work properly
        setLayerType(LAYER_TYPE_HARDWARE, null);
        mDrawPaint = new Paint();
        mPath = new Path();
        mPathMatrix = new Matrix();
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setDither(true);
        mDrawPaint.setColor(Color.BLACK);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);
        mDrawPaint.setStrokeWidth(mBrushSize);
        mDrawPaint.setAlpha(mOpacity);
        mDrawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
//        this.setVisibility(View.GONE);
    }


    private void refreshBrushDrawing() {
        mBrushDrawMode = true;
        mPath = new Path();
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setDither(true);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);
        mDrawPaint.setStrokeWidth(mBrushSize);
        mDrawPaint.setAlpha(mOpacity);
        mDrawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
    }

    void brushEraser() {
        mBrushDrawMode = true;
        mDrawPaint.setStrokeWidth(mBrushEraserSize);
        mDrawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    void setBrushDrawingMode(boolean brushDrawMode) {
        this.mBrushDrawMode = brushDrawMode;
        if (brushDrawMode) {
            this.setVisibility(View.VISIBLE);
            refreshBrushDrawing();
        }
    }

    void setOpacity(@IntRange(from = 0, to = 255) int opacity) {
        this.mOpacity = opacity;
        setBrushDrawingMode(true);
    }

    boolean getBrushDrawingMode() {
        return mBrushDrawMode;
    }

    void setBrushSize(float size) {
        mBrushSize = size;
        setBrushDrawingMode(true);
    }

    void setBrushColor(@ColorInt int color) {
        mDrawPaint.setColor(color);
        setBrushDrawingMode(true);
    }

    void setBrushEraserSize(float brushEraserSize) {
        this.mBrushEraserSize = brushEraserSize;
        setBrushDrawingMode(true);
    }

    void setBrushEraserColor(@ColorInt int color) {
        mDrawPaint.setColor(color);
        setBrushDrawingMode(true);
    }

    float getEraserSize() {
        return mBrushEraserSize;
    }

    float getBrushSize() {
        return mBrushSize;
    }

    int getBrushColor() {
        return mDrawPaint.getColor();
    }

    void clearAll() {
        mLinePaths.clear();
        mRedoLinePaths.clear();
        if (mDrawCanvas != null) {
            mDrawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        }
        invalidate();
    }

    public void setBrushViewChangeListener(BrushViewChangeListener brushViewChangeListener) {
        mBrushViewChangeListener = brushViewChangeListener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Bitmap canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mDrawCanvas = new Canvas(canvasBitmap);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (LinePath linePath : mLinePaths) {
            linePath.getDrawPaint().setStrokeWidth(linePath.getInitStrokeWidth() * mScaleFactor);
            canvas.drawPath(linePath.getDrawPath(), linePath.getDrawPaint());
        }
        canvas.drawPath(mPath, mDrawPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (mBrushDrawMode) {
            float touchX = event.getX();
            float touchY = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchStart(touchX, touchY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    touchMove(touchX, touchY);
                    break;
                case MotionEvent.ACTION_UP:
                    touchUp();
                    break;
            }
            concatMatrix();
            invalidate();
        } else {
            transform(event);
        }
        return true;
    }

    private void transform(MotionEvent event) {
        PointF midPoint = getMidPointOfFinger(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                // 每次触摸事件开始都初始化mLastMidPonit
                mLastMidPoint.set(midPoint);
                isTransforming = false;
                // 新手指落下则需要重新判断是否可以对图片进行变换
                mCanRotate = false;
                mCanScale = false;
                mCanDrag = false;
                if (event.getPointerCount() == 2) {
                    // 旋转、平移、缩放分别使用三个判断变量，避免后期某个操作执行条件改变
                    mCanScale = true;
                    mLastPoint1.set(event.getX(0), event.getY(0));
                    mLastPoint2.set(event.getX(1), event.getY(1));
                    mCanRotate = true;
                    mLastVector.set(event.getX(1) - event.getX(0),
                            event.getY(1) - event.getY(0));
                } else if (event.getPointerCount() == 1) {
                    mCanDrag = true;
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if (mCanDrag) translate(midPoint);
                if (mCanScale) scale(event);
                if (mCanRotate) rotate(event);
                // 判断图片是否发生了变换
                if (!getImageMatrix().equals(mMatrix)) isTransforming = true;
                if (mCanDrag || mCanScale || mCanRotate) applyMatrix();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_UP:
                mCanScale = false;
                mCanDrag = false;
                mCanRotate = false;
                break;
        }
    }

    private class LinePath {
        private Paint mDrawPaint;
        private Path mDrawPath;
        private float initStrokeWidth = mBrushEraserSize;

        LinePath(Path drawPath, Paint drawPaints) {
            mDrawPaint = new Paint(drawPaints);
            mDrawPath = new Path(drawPath);
        }

        Paint getDrawPaint() {
            return mDrawPaint;
        }

        Path getDrawPath() {
            return mDrawPath;
        }

        void setInitStrokeWidth(float initStrokeWidth) {
            this.initStrokeWidth = initStrokeWidth;
        }

        float getInitStrokeWidth() {
            return initStrokeWidth;
        }
    }

    boolean undo() {
        if (mLinePaths.size() > 0) {
            mRedoLinePaths.add(mLinePaths.remove(mLinePaths.size() - 1));
            invalidate();
        }
        if (mBrushViewChangeListener != null) {
            mBrushViewChangeListener.onViewRemoved(this);
        }
        return mLinePaths.size() != 0;
    }

    boolean redo() {
        if (mRedoLinePaths.size() > 0) {
            mLinePaths.add(mRedoLinePaths.remove(mRedoLinePaths.size() - 1));
            invalidate();
        }
        if (mBrushViewChangeListener != null) {
            mBrushViewChangeListener.onViewAdd(this);
        }
        return mRedoLinePaths.size() != 0;
    }


    private void touchStart(float x, float y) {
        mRedoLinePaths.clear();
        mPath.reset();
        mPath.moveTo(x, y);
        mTouchX = x;
        mTouchY = y;
        if (mBrushViewChangeListener != null) {
            mBrushViewChangeListener.onStartDrawing();
        }
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mTouchX);
        float dy = Math.abs(y - mTouchY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mTouchX, mTouchY, (x + mTouchX) / 2, (y + mTouchY) / 2);
            mTouchX = x;
            mTouchY = y;
        }
    }

    private void touchUp() {
        mPath.lineTo(mTouchX, mTouchY);
        // Commit the path to our offscreen
        mDrawCanvas.drawPath(mPath, mDrawPaint);
        // kill this so we don't double draw
        LinePath linePath = new LinePath(mPath, mDrawPaint);
        linePath.setInitStrokeWidth((mBrushEraserSize / mScaleFactor));
        mLinePaths.add(linePath);
        mPath.reset();
        if (mBrushViewChangeListener != null) {
            mBrushViewChangeListener.onStopDrawing();
            mBrushViewChangeListener.onViewAdd(this);
        }
    }


    /*--------------------------transform begin-----------------------*/

    private static final String TAG = "BrushDrawingView";
    private static final float MAX_SCALE_FACTOR = 2.0f; // 默认最大缩放比例为2
    private static final float UNSPECIFIED_SCALE_FACTOR = -1f; // 未指定缩放比例
    private static final float MIN_SCALE_FACTOR = 1.0f; // 默认最小缩放比例为0.3
    private static final float INIT_SCALE_FACTOR = 0.3f; // 默认适应控件大小后的初始化缩放比例

    private float mMaxScaleFactor = MAX_SCALE_FACTOR; // 最大缩放比例
    private float mMinScaleFactor = UNSPECIFIED_SCALE_FACTOR; // 此最小缩放比例优先级高于下面两个
    private float mVerticalMinScaleFactor = MIN_SCALE_FACTOR; // 图片最初的最小缩放比例
    private float mHorizontalMinScaleFactor = MIN_SCALE_FACTOR; // 图片旋转90（或-90）度后的的最小缩放比例
    protected Matrix mMatrix = new Matrix(); // 总变换矩阵，用于图片旋转、平移、缩放的矩阵
    protected Matrix mCurMatrix = new Matrix(); // 当前的变换矩阵，最终需要叠加到总变换矩阵中
    protected RectF mImageRect = new RectF(); // 保存图片所在区域矩形，坐标为相对于本View的坐标


    /**
     * 初始化图片位置和大小
     */
    private void initImgPositionAndSize() {
        mMatrix.reset();
        mCurMatrix.reset();
        // 初始化ImageRect
        refreshImageRect();

        // 计算缩放比例，使图片适应控件大小
        mHorizontalMinScaleFactor = Math.min(getWidth() / mImageRect.width(),
                getHeight() / mImageRect.height());
        mVerticalMinScaleFactor = Math.min(getHeight() / mImageRect.width(),
                getWidth() / mImageRect.height());

        float scaleFactor = mHorizontalMinScaleFactor;

        // 初始图片缩放比例比最小缩放比例稍大
        scaleFactor *= INIT_SCALE_FACTOR;
        mScaleFactor = scaleFactor;
        mCurMatrix.postScale(scaleFactor, scaleFactor, mImageRect.centerX(), mImageRect.centerY());
        refreshImageRect();
        // 移动图片到中心
        mCurMatrix.postTranslate((getRight() - getLeft()) / 2 - mImageRect.centerX(),
                (getBottom() - getTop()) / 2 - mImageRect.centerY());
        applyMatrix();

        // 如果用户有指定最小缩放比例则使用用户指定的
        if (mMinScaleFactor != UNSPECIFIED_SCALE_FACTOR) {
            mHorizontalMinScaleFactor = mMinScaleFactor;
            mVerticalMinScaleFactor = mMinScaleFactor;
        }
    }


    private PointF mLastPoint1 = new PointF(); // 上次事件的第一个触点
    private PointF mLastPoint2 = new PointF(); // 上次事件的第二个触点
    private PointF mCurrentPoint1 = new PointF(); // 本次事件的第一个触点
    private PointF mCurrentPoint2 = new PointF(); // 本次事件的第二个触点
    private float mScaleFactor = 1.0f; // 当前的缩放倍数
    private boolean mCanScale = false; // 是否可以缩放

    protected PointF mLastMidPoint = new PointF(); // 图片平移时记录上一次ACTION_MOVE的点
    private PointF mCurrentMidPoint = new PointF(); // 当前各触点的中点
    protected boolean mCanDrag = false; // 是否可以平移

    private PointF mLastVector = new PointF(); // 记录上一次触摸事件两指所表示的向量
    private PointF mCurrentVector = new PointF(); // 记录当前触摸事件两指所表示的向量
    private boolean mCanRotate = false; // 判断是否可以旋转


    protected boolean isTransforming = false; // 图片是否正在变化

    private void rotate(MotionEvent event) {
        // 计算当前两指触点所表示的向量
        mCurrentVector.set(event.getX(1) - event.getX(0),
                event.getY(1) - event.getY(0));
        // 获取旋转角度
        float degree = getRotateDegree(mLastVector, mCurrentVector);
        mCurMatrix.postRotate(degree, mImageRect.centerX(), mImageRect.centerY());
        mLastVector.set(mCurrentVector);
    }

    /**
     * 使用Math#atan2(double y, double x)方法求上次触摸事件两指所示向量与x轴的夹角，
     * 再求出本次触摸事件两指所示向量与x轴夹角，最后求出两角之差即为图片需要转过的角度
     *
     * @param lastVector    上次触摸事件两指间连线所表示的向量
     * @param currentVector 本次触摸事件两指间连线所表示的向量
     * @return 两向量夹角，单位“度”，顺时针旋转时为正数，逆时针旋转时返回负数
     */
    private float getRotateDegree(PointF lastVector, PointF currentVector) {
        //上次触摸事件向量与x轴夹角
        double lastRad = Math.atan2(lastVector.y, lastVector.x);
        //当前触摸事件向量与x轴夹角
        double currentRad = Math.atan2(currentVector.y, currentVector.x);
        // 两向量与x轴夹角之差即为需要旋转的角度
        double rad = currentRad - lastRad;
        //“弧度”转“度”
        return (float) Math.toDegrees(rad);
    }

    protected void translate(PointF midPoint) {
        float dx = midPoint.x - mLastMidPoint.x;
        float dy = midPoint.y - mLastMidPoint.y;
        mCurMatrix.postTranslate(dx, dy);
        mLastMidPoint.set(midPoint);
    }

    /**
     * 计算所有触点的中点
     *
     * @param event 当前触摸事件
     * @return 本次触摸事件所有触点的中点
     */
    private PointF getMidPointOfFinger(MotionEvent event) {
        // 初始化mCurrentMidPoint
        mCurrentMidPoint.set(0f, 0f);
        int pointerCount = event.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            mCurrentMidPoint.x += event.getX(i);
            mCurrentMidPoint.y += event.getY(i);
        }
        mCurrentMidPoint.x /= pointerCount;
        mCurrentMidPoint.y /= pointerCount;
        return mCurrentMidPoint;
    }

    private static final int SCALE_BY_IMAGE_CENTER = 0; // 以图片中心为缩放中心
    private static final int SCALE_BY_FINGER_MID_POINT = 1; // 以所有手指的中点为缩放中心
    private int mScaleBy = SCALE_BY_IMAGE_CENTER;
    private PointF scaleCenter = new PointF();

    /**
     * 获取图片的缩放中心，该属性可在外部设置，或通过xml文件设置
     * 默认中心点为图片中心
     *
     * @return 图片的缩放中心点
     */
    private PointF getScaleCenter() {
        // 使用全局变量避免频繁创建变量
        switch (mScaleBy) {
            case SCALE_BY_IMAGE_CENTER:
                scaleCenter.set(mImageRect.centerX(), mImageRect.centerY());
                break;
            case SCALE_BY_FINGER_MID_POINT:
                scaleCenter.set(mLastMidPoint.x, mLastMidPoint.y);
                break;
        }
        return scaleCenter;
    }

    private void scale(MotionEvent event) {
        PointF scaleCenter = getScaleCenter();

        // 初始化当前两指触点
        mCurrentPoint1.set(event.getX(0), event.getY(0));
        mCurrentPoint2.set(event.getX(1), event.getY(1));
        // 计算缩放比例
        float scaleFactor = distance(mCurrentPoint1, mCurrentPoint2)
                / distance(mLastPoint1, mLastPoint2);

        // 更新当前图片的缩放比例
        mScaleFactor *= scaleFactor;

        mCurMatrix.postScale(scaleFactor, scaleFactor,
                scaleCenter.x, scaleCenter.y);
        mLastPoint1.set(mCurrentPoint1);
        mLastPoint2.set(mCurrentPoint2);
    }

    /**
     * 获取两点间距离
     */
    private float distance(PointF point1, PointF point2) {
        float dx = point2.x - point1.x;
        float dy = point2.y - point1.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }


    /**
     * 更新图片所在区域，并将矩阵应用到图片
     */
    protected void applyMatrix() {
        refreshImageRect(); /*将矩阵映射到ImageRect*/
        concatMatrix();
        setImageMatrix(mMatrix);
        invalidate();
    }

    /**
     * 图片使用矩阵变换后，刷新图片所对应的mImageRect所指示的区域
     */
    private void refreshImageRect() {
        if (getDrawable() != null) {
            mImageRect.set(getDrawable().getBounds());
            concatMatrix();
            mMatrix.mapRect(mImageRect, mImageRect);
        }
    }

    private void concatMatrix() {
        mMatrix.postConcat(mCurMatrix);

        for (LinePath linePath : mLinePaths) {
            linePath.getDrawPath().transform(mCurMatrix);
        }
        mCurMatrix.reset();
    }


    /*--------------------------transform end-----------------------*/

}