package com.test.wuziqi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * 绘制棋盘View的子类，10*10的线条绘制
 * 问题：当Activity重建后棋子会消失，需要对残局进行存储
 */
public class WuziqiPanel extends View {

    private int mPanelWidth;//画布宽度
    private float mLineHeight;//线高--画布高度
    private final int MAX_LINE = 10;//最大行数
    private final int MAX_IN_LINE = 5;//最大相邻棋子数
    /**
     * 绘制棋盘的画笔
     */
    private Paint mPaint = new Paint();
    /**
     * 白棋子
     */
    private Bitmap mWhitePiece;
    /**
     * 黑棋子m
     */
    private Bitmap mBlackPiece;
    /**
     * 两个棋子之间需要留有距离  大约为行高的3/4
     */
    private float ratioPieceOfHeight = 3 * 1.0f / 4;
    /**
     * 标记哪个棋子先落
     * true  -- 白子先落，或者轮到白棋了
     * false -- 黑子先落，或者轮到黑棋了
     */
    private boolean isWhiteDown = true;
    /**
     * 白子的坐标集合 Point已经实现了Parcelable接口
     */
    private ArrayList<Point> mWhitePointArray = new ArrayList<Point>();
    /**
     * 黑子的坐标集合
     */
    private ArrayList<Point> mBlackPointArray = new ArrayList<Point>();
    /**
     * 标记游戏是否结束
     * false:未结束
     * true:结束
     */
    private boolean mIsGameOver = false;
    /**
     * 标记赢家m
     * false:黑棋赢
     * true:白棋赢
     */
    private boolean mIsWhiteWinner = true;

    /**
     * @param context
     * @param attrs
     */
    public WuziqiPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        //setBackgroundColor(0x44ff0000);
        initPaint();
    }

    private void initPaint() {
        mPaint.setColor(0x88000000);
        mPaint.setAntiAlias(true);//设置抗锯齿
        mPaint.setDither(true);////防抖动，设定是否使用图像抖动处理，会使绘制出来的图片颜色更加平滑和饱满，图像更加清晰
        mPaint.setStyle(Paint.Style.STROKE);//设置填充风格 描边
        //获得黑白棋子的Bitmap对象
        mWhitePiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2);
        mBlackPiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1);
    }

    /**
     * 视图测量方法
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //分别获取宽度和高度的Size和Mode
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = Math.min(widthSize, heightSize);
        //对获取的width做处理
        /**
         * 如果高度未定义则取值为heightSize，如果宽度未定义则取值为widthSize
         */
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            width = heightSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            width = widthSize;
        }
        //存储测量得到的宽度和高度值，如果没有这么去做会触发异常IllegalStateException
        setMeasuredDimension(width, width);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPanelWidth = w;
        Log.d("WuziqiPanel_width:", w + "");
        Log.d("WuziqiPanel_height:", h + "");
        //直线的初始高度
        mLineHeight = mPanelWidth * 1.0f / MAX_LINE;
        Log.d("WuziqiPanelmLineHeight:", mLineHeight + "");
        //修改棋子的大小
        //按照比例获取棋子的宽度和高度
        int pieceWidth = (int) (mLineHeight * ratioPieceOfHeight);
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, pieceWidth, pieceWidth, false);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece, pieceWidth, pieceWidth, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBoard(canvas);
        //根据坐标绘制棋子
        drawPiece(canvas);
        checkGameOver();
    }

    /**
     * 检查游戏是否结束
     */
    private void checkGameOver() {
        //横向判断是否游戏结束

        boolean whiteWin;
        boolean blackWin;

        whiteWin = checkFiveInLine(mWhitePointArray);
        blackWin = checkFiveInLine(mBlackPointArray);
        //纵向判断是否游戏结束
        //左斜判断游戏是否结束
        //右斜判断游戏是否结束

        //只要有一方胜利则结束游戏
        if (whiteWin || blackWin) {
            mIsGameOver = true;
            mIsWhiteWinner = whiteWin;
            String text = mIsWhiteWinner ? "白棋胜利" : "黑棋胜利";
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setTitle("游戏结束");
            dialog.setMessage(text);
            dialog.setCancelable(false);
            dialog.setIcon(R.drawable.stone_w2);
            dialog.setPositiveButton("再来一局", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    restart();
                }
            });
            dialog.setNegativeButton("退出游戏", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            dialog.show();
        }
    }


    /**
     * 检查是否有一方胜利，对白子和黑子的坐标集合进行依次判断
     *
     * @param points 一方的落子坐标
     * @return
     */
    private boolean checkFiveInLine(List<Point> points) {
        boolean win = false;
        for (Point p : points) {
            int x = p.x;
            int y = p.y;
            win = checkHorizontal(x, y, points);
            if (!win) {
                win = checkVertical(x, y, points);
            }
            if (!win) {
                win = checkDiagonalOfLeft(x, y, points);
            }
            if (!win) {
                win = checkDiagonalOfRight(x, y, points);
            }

        }
        return win;
    }

    /**
     * 检查右斜方向是否构成5子一致 \ 型
     *
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkDiagonalOfRight(int x, int y, List<Point> points) {
        int count = 1;
        //右斜向右上检查
        for (int i = 1; i < MAX_IN_LINE; i++) {
            if (points.contains(new Point(x - i, y - i))) {
                ++count;
            } else {
                break;
            }
        }
        if (MAX_IN_LINE == count) return true;
        //右斜向左下检查
        for (int i = 1; i < MAX_IN_LINE; i++) {
            if (points.contains(new Point(x + i, y + i))) {
                ++count;
            } else {
                break;
            }
        }
        if (MAX_IN_LINE == count) return true;
        return false;
    }

    /**
     * 检查左斜方向是否构成5子一致  / 型
     *
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkDiagonalOfLeft(int x, int y, List<Point> points) {
        int count = 1;
        //左斜向右上检查
        for (int i = 1; i < MAX_IN_LINE; i++) {
            if (points.contains(new Point(x + i, y - i))) {
                ++count;
            } else {
                break;
            }
        }
        if (MAX_IN_LINE == count) return true;
        //左斜向左下检查
        for (int i = 1; i < MAX_IN_LINE; i++) {
            if (points.contains(new Point(x - i, y + i))) {
                ++count;
            } else {
                break;
            }
        }
        if (MAX_IN_LINE == count) return true;
        return false;
    }

    /**
     * 纵向检查游戏是否结束
     *
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkVertical(int x, int y, List<Point> points) {
        int count = 1;
        //向上检查
        for (int i = 1; i < MAX_IN_LINE; i++) {
            if (points.contains(new Point(x, y - i))) {
                ++count;
            } else {
                break;
            }
        }
        if (MAX_IN_LINE == count) return true;
        //向上检查
        for (int i = 1; i < MAX_IN_LINE; i++) {
            if (points.contains(new Point(x, y + i))) {
                ++count;
            } else {
                break;
            }
        }
        if (MAX_IN_LINE == count) return true;
        return false;
    }

    /**
     * 检查横向的棋子是否共线5颗
     *
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkHorizontal(int x, int y, List<Point> points) {
        //从中间向两边计数共线的棋子数
        int count = 1;
        //判断当前点的左边是否有共线的
        for (int i = 1; i < MAX_IN_LINE; i++) {
            if (points.contains(new Point(x - i, y))) {
                ++count;
            } else {
                break;
            }
        }
        if (MAX_IN_LINE == count) return true;
        for (int j = 1; j < MAX_IN_LINE; j++) {
            if (points.contains(new Point(x + j, y))) {
                ++count;
            } else {
                break;
            }
        }
        if (MAX_IN_LINE == count) return true;
        return false;
    }

    /**
     * 根据获得的onTouch事件获得的棋子坐标绘制棋子
     *
     * @param canvas
     */
    private void drawPiece(Canvas canvas) {
        //编写以下代码的前提是在集合中记录坐标时，已经对重复落子的事件进行处理，不会重复记录同一位置的棋子
        for (int i = 0, n = mWhitePointArray.size(); i < n; i++) {
            Point whitePoint = mWhitePointArray.get(i);
            //public void drawBitmap (Bitmap bitmap, float left, float top, Paint paint)
            canvas.drawBitmap(mWhitePiece,
                    (whitePoint.x + (1 - ratioPieceOfHeight) / 2) * mLineHeight,
                    (whitePoint.y + (1 - ratioPieceOfHeight) / 2) * mLineHeight, null);
        }

        for (int j = 0, n = mBlackPointArray.size(); j < n; j++) {
            Point blackPoint = mBlackPointArray.get(j);
            canvas.drawBitmap(mBlackPiece,
                    (blackPoint.x + (1 - ratioPieceOfHeight) / 2) * mLineHeight,
                    (blackPoint.y + (1 - ratioPieceOfHeight) / 2) * mLineHeight, null);
        }
    }

    /**
     * 绘制棋盘的具体方法
     *
     * @param canvas
     */
    private void drawBoard(Canvas canvas) {
        int w = mPanelWidth;
        float lineHeight = mLineHeight;
        //绘制棋盘的横线和竖线
        for (int i = 0; i < MAX_LINE; i++) {
            int startX = (int) (lineHeight * 0.5);
            int endX = w - startX;
            int y = (int) (lineHeight * (0.5 + i));
            //先绘制横线
            canvas.drawLine(startX, y, endX, y, mPaint);
            //后绘制竖线
            canvas.drawLine(y, startX, y, endX, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //如果游戏已经结束，则不允许再落子
        if (mIsGameOver) return false;
        int action = event.getAction();
        //在哪个事件处理以下代码，需要仔细考虑
        if (action == event.ACTION_UP) {
            //如果要处理action事件，就需要返回true
            //记录棋子的坐标
            int x = (int) event.getX();
            int y = (int) event.getY();
            Point point = getValidPoint(x, y);
            Log.d("WuziqiPanel_width:", point.x + "," + point.y);

            //问题，这样存储的point无法避免重复
            //如果重复则返回false,保证第二次onTouch事件发生在同一位置时不会再记录坐标
            if (mWhitePointArray.contains(point) || mBlackPointArray.contains(point)) {
                return false;
            }
            //判断是哪个棋子的坐标，并记录到相应的集合中
            if (isWhiteDown) {
                mWhitePointArray.add(point);
            } else {
                mBlackPointArray.add(point);
            }
            //UI线程的View刷新操作
            invalidate();
            //需要黑子和白子轮流落子
            isWhiteDown = !isWhiteDown;
            //return true;//声明要响应点击事件的
        }
        return true;
    }

    private Point getValidPoint(int x, int y) {
        return new Point((int) (x / mLineHeight), (int) (y / mLineHeight));
    }

    //View的存储与恢复
    private final static String INSTANCE = "instance";
    private final static String INSTANCE_GAME_OVER = "instance";
    private final static String INSTANCE_WHITE_ARRAY = "instance_white_array";
    private final static String INSTANCE_BLACK_ARRAY = "instance_black_array";

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE, super.onSaveInstanceState());
        bundle.putBoolean(INSTANCE_GAME_OVER, mIsGameOver);
        bundle.putParcelableArrayList(INSTANCE_WHITE_ARRAY, mWhitePointArray);
        bundle.putParcelableArrayList(INSTANCE_BLACK_ARRAY, mBlackPointArray);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mIsGameOver = bundle.getBoolean(INSTANCE_GAME_OVER);
            mWhitePointArray = bundle.getParcelableArrayList(INSTANCE_WHITE_ARRAY);
            mBlackPointArray = bundle.getParcelableArrayList(INSTANCE_BLACK_ARRAY);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    /**
     * 再来一局
     */
    public void restart(){
        mIsGameOver = false;
        mWhitePointArray.clear();
        mBlackPointArray.clear();
        mIsWhiteWinner = false;
        invalidate();
    }
}
