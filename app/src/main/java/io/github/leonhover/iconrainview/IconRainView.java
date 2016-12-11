package io.github.leonhover.iconrainview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by wangzongliang on 2016/12/1.
 */

public class IconRainView extends View {

    private final static String TAG = IconRainView.class.getSimpleName();

    private static final float PI = 3.14f;
    private static final int DEFAULT_LAUNCH_DURATION = 300;
    private static final int DEFAULT_FALL_GRAVITY = 5;
    private static final int DEFAULT_INVIDATE_INTERVAL = 16;

    private int firstTimeIconCount = 0;
    private Drawable icon = null;
    private int launchDuration = DEFAULT_LAUNCH_DURATION;
    private List<IconInfo> iconInfoList;
    private boolean isRaining = false;
    private int fallGravity = DEFAULT_FALL_GRAVITY;
    private boolean shadeToGone = false;
    private int fallDistance = -1;

    private SoundPool soundPool;
    private int soundId = -1;
    private int soundResId = -1;
    private AudioManager audioManager;
    private boolean isSoundReady = false;

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
            TypedArray typedArray = null;
            try {
                typedArray = context.obtainStyledAttributes(attrs, R.styleable.IconRainView);
                this.firstTimeIconCount = typedArray.getInt(R.styleable.IconRainView_firstTimeIconCount, 0);
                this.launchDuration = typedArray.getInt(R.styleable.IconRainView_launchDuration, DEFAULT_LAUNCH_DURATION);
                this.icon = typedArray.getDrawable(R.styleable.IconRainView_icon);
                this.fallGravity = typedArray.getInt(R.styleable.IconRainView_fallGravity, DEFAULT_FALL_GRAVITY);
                this.shadeToGone = typedArray.getBoolean(R.styleable.IconRainView_shadeToGone, false);
                this.fallDistance = typedArray.getDimensionPixelSize(R.styleable.IconRainView_fallDistance, -1);
                this.soundResId = typedArray.getResourceId(R.styleable.IconRainView_sound, -1);
            } finally {
                if (typedArray != null) {
                    typedArray.recycle();
                }
            }
        }

        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        iconInfoList = new LinkedList<>();
    }

    public void setOnIconRainFallListener(OnIconRainFallListener onIconRainFallListener) {
        this.onIconRainFallListener = onIconRainFallListener;
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

    public void setSound(int soundResId) {
        this.soundResId = soundResId;
    }

    public void startRainFall(final int iconCount) {

        if (iconCount < 1) {
            return;
        }

        this.post(new Runnable() {
            @Override
            public void run() {
                configRainfall(iconCount);
            }
        });
    }

    public boolean isRaining() {
        return isRaining;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startRainFall(this.firstTimeIconCount);
    }

    private void configRainfall(int iconCount) {

        if (icon == null || iconCount < 1) {
            Log.e(TAG, "It can't start now without icon!");
            return;
        }

        isRaining = true;
        Log.d(TAG, "icon rain fall start");

        if (this.onIconRainFallListener != null) {
            this.onIconRainFallListener.onRainStart();
        }

        int viewWidth = getWidth();
        int viewHeight = getHeight();
        Random random = new Random();
        //随机分布ICON的位置和弹射参数
        for (int i = 0; i < iconCount; i++) {
            IconInfo iconInfo = new IconInfo();
            iconInfo.setPosX(random.nextInt(viewWidth / 3) + viewWidth / 3 + icon.getIntrinsicWidth());
            iconInfo.setPosY(0);
            iconInfo.setStartTime(System.currentTimeMillis() + random.nextInt(this.launchDuration));
            iconInfo.setFallDis(this.fallDistance == -1 ? viewHeight : this.fallDistance);
            iconInfo.setG((random.nextInt(10) * 0.1f + fallGravity) * 0.001f);
            iconInfo.setEjectionAngle(random.nextInt(50) * PI / 180 + PI * 5 / 12);
            iconInfo.setShadeToGone(this.shadeToGone);
            iconInfoList.add(iconInfo);
        }

        if (isSoundReady || soundResId == -1) {
            playRainFall();
        } else {
            soundId = soundPool.load(getContext(), this.soundResId, 1);
            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    Log.d(TAG, "sampleId = " + sampleId);
                    isSoundReady = true;
                    if (isRaining) {
                        playRainFall();
                    }
                }
            });
        }

    }

    private void playRainFall() {
        int volume = this.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        soundPool.play(soundId, volume, volume, 1, 0, 1.0f);
        computeRainFall();
    }

    public void cancel() {
        this.removeCallbacks(null);
        this.iconInfoList.clear();
        this.isRaining = false;
        invalidate();
    }

    private void computeRainFall() {

        boolean finished = true;

        for (IconInfo iconInfo : iconInfoList) {
            iconInfo.computePos();
            finished = false;
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
            }, DEFAULT_INVIDATE_INTERVAL);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Iterator<IconInfo> iconInfoIterator = iconInfoList.iterator();
        while (iconInfoIterator.hasNext()) {
            IconInfo iconInfo = iconInfoIterator.next();
            int[] position = iconInfo.getPosition();
            if (isIconVisible(iconInfo)) {
                icon.setBounds(position[0] - icon.getIntrinsicWidth(), position[1] - icon.getIntrinsicHeight(), position[0], position[1]);
                icon.setAlpha(iconInfo.alpha);
                icon.draw(canvas);
            } else {
                iconInfoIterator.remove();
            }
        }
    }

    private boolean isIconVisible(IconInfo iconInfo) {
        int[] position = iconInfo.getPosition();
        int iconWidth = icon.getIntrinsicWidth();
        int iconHeight = icon.getIntrinsicHeight();
        return iconInfo.alpha != 0 && ((position[0] >= -1 && position[0] - iconWidth <= getWidth())
                && (position[1] >= -1 && position[1] - iconHeight <= getHeight()));
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
        private boolean shadeToGone = false;
        private int alpha = 255;

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

        public void setShadeToGone(boolean shadeToGone) {
            this.shadeToGone = shadeToGone;
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

                if (shadeToGone) {
                    float currentVelY = this.ejectionVelY + g * ejectionPlayTime;
                    if (currentVelY > 0) {
                        this.alpha = (int) ((1 - Math.abs(currentVelY / this.ejectionVelY)) * 255);
                        if (this.alpha < 0) {
                            this.alpha = 0;
                        }
                    }
                }

            } else {
                int deltaY = (int) (Math.pow(playTime, 2) / 2 * g + 0.5f) + this.posY;
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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancel();
        soundPool.release();
        soundPool = null;
    }

    public interface OnIconRainFallListener {
        void onRainStart();

        void onRainFinish();
    }
}
