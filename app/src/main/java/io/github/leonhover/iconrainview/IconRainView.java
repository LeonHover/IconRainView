package io.github.leonhover.iconrainview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by wangzongliang on 2016/12/1.
 */

public class IconRainView extends View {

    private final static String TAG = IconRainView.class.getSimpleName();

    private static final float PI = 3.14f;
    private static final int DEFAULT_ICON_COUNT = 5;
    private static final int DEFAULT_LAUNCH_DURATION = 300;
    private static final int DEFAULT_FALL_GRAVITY = 5;

    private int iconCounts = DEFAULT_ICON_COUNT;
    private Drawable icon = null;
    private int launchDuration = DEFAULT_LAUNCH_DURATION;
    private List<IconInfo> iconInfoList;
    private boolean isRaining = false;
    private int fallGravity = DEFAULT_FALL_GRAVITY;
    private boolean shadeToGone = false;
    private int fallDistance = -1;

    private OnIconRainFallListener onIconRainFallListener;

    public IconRainView(Context context) {
        super(context);
        init(context, null, 0);
    }


    public IconRainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public IconRainView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IconRainView);
            this.iconCounts = typedArray.getInt(R.styleable.IconRainView_iconCount, DEFAULT_ICON_COUNT);
            this.launchDuration = typedArray.getInt(R.styleable.IconRainView_launchDuration, DEFAULT_LAUNCH_DURATION);
            this.icon = typedArray.getDrawable(R.styleable.IconRainView_icon);
            this.fallGravity = typedArray.getInt(R.styleable.IconRainView_fallGravity, DEFAULT_FALL_GRAVITY);
            this.shadeToGone = typedArray.getBoolean(R.styleable.IconRainView_shadeToGone, false);
            this.fallDistance = typedArray.getDimensionPixelSize(R.styleable.IconRainView_fallDistance, -1);
        }
    }

    public void setOnIconRainFallListener(OnIconRainFallListener onIconRainFallListener) {
        this.onIconRainFallListener = onIconRainFallListener;
    }

    public void setIconCounts(int iconCounts) {
        if (!isRaining) {
            this.iconCounts = iconCounts;
        }
    }

    public void setFallDistance(int fallDistance) {
        this.fallDistance = fallDistance;
    }

    public void setShadeToGone(boolean shadeToGone) {
        this.shadeToGone = shadeToGone;
    }

    public void setFallGravity(int fallGravity) {
        this.fallGravity = fallGravity;
    }

    public void setIcon(Drawable icon) {
        if (!isRaining) {
            this.icon = icon;
        }
    }

    public void setIcon(Bitmap icon) {
        if (!isRaining) {
            this.icon = new BitmapDrawable(getResources(), icon);
        }
    }

    public void setIcon(int iconRes) {
        if (!isRaining) {
            this.icon = getResources().getDrawable(iconRes);
        }
    }

    public void setLaunchDuration(int launchDuration) {
        if (!isRaining) {
            this.launchDuration = launchDuration;
        }
    }

    public void startRainFall() {
        this.post(new Runnable() {
            @Override
            public void run() {
                rainfall();
            }
        });
    }

    public boolean isRaining() {
        return isRaining;
    }

    private void rainfall() {

        if (icon == null) {
            Log.e(TAG, "It can't start now without icon!");
            return;
        }

        if (isRaining) {
            Log.e(TAG, "It is raining,can't start now");
            return;
        }
        isRaining = true;
        Log.d(TAG, "icon rain fall start");

        if (this.onIconRainFallListener != null) {
            this.onIconRainFallListener.onRainStart();
        }
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        iconInfoList = new ArrayList<>(iconCounts);
        Random random = new Random();
        //随机分布ICON的位置和弹射参数
        for (int i = 0; i < iconCounts; i++) {
            IconInfo iconInfo = new IconInfo();
            iconInfo.setPosX(random.nextInt(viewWidth / 3) + viewWidth / 3 + icon.getIntrinsicWidth());
            iconInfo.setPosY(-icon.getIntrinsicHeight());
            iconInfo.setStartTime(System.currentTimeMillis() + random.nextInt(this.launchDuration));
            iconInfo.setFallDis(this.fallDistance == -1 ? viewHeight : this.fallDistance);
            iconInfo.setG((random.nextInt(10) + fallGravity) * 0.001f);
            iconInfo.setEjectionAngle(random.nextInt(50) * PI / 180 + PI * 5 / 12);
            iconInfoList.add(iconInfo);
        }
        computeRainFall();
    }

    private void computeRainFall() {

        boolean finished = true;

        for (IconInfo iconInfo : iconInfoList) {
            int[] position = iconInfo.computePos();
            if (isIconVisible(position)) {
                finished = false;
            }
        }

        invalidate();
        if (finished) {
            isRaining = false;
            Log.d(TAG, "icon rain finished!");
            if (this.onIconRainFallListener != null) {
                this.onIconRainFallListener.onRainFinish();
            }
        } else {
            this.postDelayed(new Runnable() {
                @Override
                public void run() {
                    computeRainFall();
                }
            }, 5);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (IconInfo iconInfo : iconInfoList) {
            int[] position = iconInfo.getPosition();
            if (isIconVisible(position)) {
                icon.setBounds(position[0] - icon.getIntrinsicWidth(), position[1] - icon.getIntrinsicHeight(), position[0], position[1]);
                icon.draw(canvas);
            }
        }
    }

    private boolean isIconVisible(int[] position) {
        int iconWidth = icon.getIntrinsicWidth();
        int iconHeight = icon.getIntrinsicHeight();
        return (position[0] >= -1 && position[0] - iconWidth <= getWidth())
                && (position[1] >= -1 && position[1] - iconHeight <= getHeight());
    }


    private static class IconInfo {

        //起始X位置
        private int posX;
        //起始Y位置
        private int posY = 0;
        //重力加速度
        private float g = 0f;
        //弹射弧度
        private float ejectionAngle;
        //弹射X轴速度
        private float ejectionVelX;
        //弹射轴速度
        private float ejectionVelY;
        //掉落距离
        private int fallDis;
        //落地次数
        private long enterEjectionTime = 0;
        //动画开始时间
        private long startTime = 0;

        //实时位置
        private int[] position = new int[]{-1, -1};

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public void setFallDis(int fallDis) {
            this.fallDis = fallDis;
        }

        public void setPosY(int posY) {
            this.posY = posY;
        }

        public void setPosX(int posX) {
            this.posX = posX;
        }

        public void setG(float g) {
            this.g = g;
        }

        public void setEjectionAngle(float ejectionAngle) {
            this.ejectionAngle = ejectionAngle;
        }

        public int[] getPosition() {
            return position;
        }

        public int[] computePos() {
            long playTime = System.currentTimeMillis() - this.startTime;
            if (playTime < 0) {
                return this.position;
            }

            if (enterEjectionTime > 0) {
                int ejectionPlayTime = (int) (playTime - enterEjectionTime);
                position[0] = (int) (posX + this.ejectionVelX * ejectionPlayTime);
                position[1] = (int) (fallDis + this.ejectionVelY * ejectionPlayTime + Math.pow(ejectionPlayTime, 2) * g / 2);
            } else {
                int deltaY = (int) (Math.pow(playTime, 2) / 2 * g + 0.5f);
                if (deltaY > this.fallDis) {
                    position[1] = this.fallDis;
                    enterEjectionTime = playTime;
                    float fallVel = g * playTime;
                    ejectionVelX = (float) (fallVel * Math.cos(this.ejectionAngle)) / 2;
                    ejectionVelY = (float) -Math.abs(fallVel * Math.sin(this.ejectionAngle)) / 2;
                } else {
                    position[0] = posX;
                    position[1] = deltaY;
                }
            }

            return this.position;
        }

    }

    public interface OnIconRainFallListener {
        void onRainStart();

        void onRainFinish();
    }
}