package com.example.missevan.mydubbing.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.missevan.mydubbing.R;
import com.example.missevan.mydubbing.entity.SRTEntity;
import com.example.missevan.mydubbing.entity.SRTSubtitleEntity;
import com.example.missevan.mydubbing.utils.Config;
import com.example.missevan.mydubbing.utils.DimenUtil;
import com.example.missevan.mydubbing.utils.SRTUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by missevan on 2017/4/24.
 */

public class DubbingSubtitleView extends TextView {
    private static String TAG = DubbingSubtitleView.class.getSimpleName();

    public static final int LONG_BREAK_TIME = 1500;
    private Paint mColorBackupLightPaint;
    private int mColorBackupLightPaintColor = 0x80e71277;
    private Paint mColorCooperaDarkPaint;
    private int mColorCooperaDarkPaintColor = 0x809d998e;
    private Paint mColorCooperaLightPaint;
    private int mColorCooperaLightPaintColor = 0x80ff1601;
    private Paint mColorSingleDarkPaint;
    private int mColorSingleDarkPaintColor = 0x809d998f;
    private Paint mColorSingleLightPaint;
    private int mColorSingleLightPaintColor = 0xffa33c3c;
    private Paint mCurrentPaint;
    private int mCurrentPaintColor = 0xffbdbdbd;
    private Paint mNotCurrentPaint;
    private final int SLOP = DimenUtil.dip2px(getContext(), 22.0F);

    private static final int STATE_NO_REFRESH = 3;
    private static final int STATE_REFRESH_COLUMN = 2;
    private static final int STATE_REFRESH_TEXT = 1;
    private static final int STATE_SCROLL_COLUMN = 4;
    private int STATE = STATE_NO_REFRESH;

    private int mShadowColorPaintColor = 0x00000000;
    private Paint mShadowCurrentPaint;
    private Paint mShadowNotCurrentPaint;
    private Typeface mTextTypeface = Typeface.SERIF;
    private int mAnimationTime = 200;
    private Bitmap mBreakBitmap;
    private boolean mCanScroll = false;
    private String mCurrentContent;
    private boolean disabled = false;
    private float downY;
    private int mDuration;
    private Bitmap mEditBitmap;
    private String first_role = "";
    private String last_role = "";
    private float mHeight;
    private float mHighlightTextSize;
    private float mHighlightTextHeight = 0;
    private float mHighlightTextHeight_dp = 30;
    private float mHighlightTextSize_sp = 15;
    private float horizontal_highlighttextsize_sp = 24;
    private int mIndex = 0;
    private boolean isEdited = true;
    private long mLimitTime;
    private float mLineHeight;
    private Context mContext;
    private int mOrientation = 1;
    private DisplayMetrics mMetric;
    private int mode;
    private boolean mNeedWaitingProgress = true;
    private int mNotCurrentPaintColor = 0xff757575;
    private String notSrt = "o(╯□╰)o暂无歌词";//
    private OnEventListener mOnEventListener;
    private int mProcessTime = 1500;
    private float rate;
    private HashMap<String, Paint> mRolePaintMap = new HashMap();
    private float speed1;
    private float speed2;
    private List<SRTSubtitleEntity> srtSubtitleEntityList;
    private float mTextHeight = 0;
    private float mTextHeight_dp = 30;
    private float mTextSize;
    private float text_margin_offset;
    private final float text_margin_offset_dp = 5;
    private final float textsize_sp = 15;
    private int mTime;
    private float mTopMargin;
    private float mWidth;


    public DubbingSubtitleView(Context context) {
        super(context);
        create(context);
    }

    public DubbingSubtitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        create(context);
    }

    public DubbingSubtitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        create(context);
    }

    private void create(Context context) {
        mContext = context;
        mBreakBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.line_720);
        mEditBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.dubbing_icon_revise);
        initSizeAndPaint();
    }

    private int checkColor(String role) {
        return checkPaint(role).getColor();
    }

    private Paint checkPaint(String role) {
        if (mRolePaintMap.size() == 0) {
            return mColorSingleLightPaint;
        }
        if (mRolePaintMap.containsKey(role)) {
            return mRolePaintMap.get(role);
        }
        return mColorBackupLightPaint;
    }

    private void getEndTime() {
    }

    private void initSizeAndPaint() {
        mMetric = getResources().getDisplayMetrics();
        mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textsize_sp, mMetric);
        mHighlightTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mHighlightTextSize_sp, mMetric);
        mHighlightTextHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mHighlightTextHeight_dp, mMetric);
        mTextHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mTextHeight_dp, mMetric);
        text_margin_offset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, text_margin_offset_dp, mMetric);

        mLineHeight = DimenUtil.dip2px(getContext(), 2.0F);
        mTopMargin = ((mHighlightTextHeight + mHighlightTextSize) / 2.0F + mLineHeight);

        mNotCurrentPaint = new Paint();
        mNotCurrentPaint.setAntiAlias(true);
        mNotCurrentPaint.setTextSize(mTextSize);
        mNotCurrentPaint.setColor(mNotCurrentPaintColor);
        mNotCurrentPaint.setTypeface(mTextTypeface);
        mNotCurrentPaint.setTextAlign(Paint.Align.LEFT);
        mNotCurrentPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mCurrentPaint = new Paint();
        mCurrentPaint.setAntiAlias(true);
        mCurrentPaint.setColor(mCurrentPaintColor);
        mCurrentPaint.setTextSize(mHighlightTextSize);
        mCurrentPaint.setTypeface(mTextTypeface);
        mCurrentPaint.setTextAlign(Paint.Align.LEFT);
        mCurrentPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mShadowNotCurrentPaint = new Paint();
        mShadowNotCurrentPaint.setAntiAlias(true);
        mShadowNotCurrentPaint.setStrokeWidth(2.0F);
        mShadowNotCurrentPaint.setTextSize(mTextSize);
        mShadowNotCurrentPaint.setColor(mShadowColorPaintColor);
        mShadowNotCurrentPaint.setTypeface(mTextTypeface);
        mShadowNotCurrentPaint.setTextAlign(Paint.Align.LEFT);

        mShadowCurrentPaint = new Paint();
        mShadowCurrentPaint.setAntiAlias(true);
        mShadowCurrentPaint.setStrokeWidth(4.0F);
        mShadowCurrentPaint.setColor(mShadowColorPaintColor);
        mShadowCurrentPaint.setTextSize(mHighlightTextSize);
        mShadowCurrentPaint.setTypeface(mTextTypeface);
        mShadowCurrentPaint.setTextAlign(Paint.Align.LEFT);

        mColorSingleLightPaint = new Paint();
        mColorSingleLightPaint.setAntiAlias(true);
        mColorSingleLightPaint.setColor(mColorSingleLightPaintColor);
        mColorSingleLightPaint.setTextSize(mHighlightTextSize);
        mColorSingleLightPaint.setTypeface(mTextTypeface);
        mColorSingleLightPaint.setTextAlign(Paint.Align.LEFT);

        mColorBackupLightPaint = new Paint();
        mColorBackupLightPaint.setAntiAlias(true);
        mColorBackupLightPaint.setColor(mColorBackupLightPaintColor);
        mColorBackupLightPaint.setTextSize(mHighlightTextSize);
        mColorBackupLightPaint.setTypeface(mTextTypeface);
        mColorBackupLightPaint.setTextAlign(Paint.Align.LEFT);

        mColorSingleDarkPaint = new Paint();
        mColorSingleDarkPaint.setAntiAlias(true);
        mColorSingleDarkPaint.setColor(mColorSingleDarkPaintColor);
        mColorSingleDarkPaint.setTextSize(mTextSize);
        mColorSingleDarkPaint.setTypeface(mTextTypeface);
        mColorSingleDarkPaint.setTextAlign(Paint.Align.LEFT);

        mColorCooperaLightPaint = new Paint();
        mColorCooperaLightPaint.setAntiAlias(true);
        mColorCooperaLightPaint.setColor(mColorCooperaLightPaintColor);
        mColorCooperaLightPaint.setTextSize(mHighlightTextSize);
        mColorCooperaLightPaint.setTypeface(mTextTypeface);
        mColorCooperaLightPaint.setTextAlign(Paint.Align.LEFT);

        mColorCooperaDarkPaint = new Paint();
        mColorCooperaDarkPaint.setAntiAlias(true);
        mColorCooperaDarkPaint.setColor(mColorCooperaDarkPaintColor);
        mColorCooperaDarkPaint.setTextSize(mTextSize);
        mColorCooperaDarkPaint.setTypeface(mTextTypeface);
        mColorCooperaDarkPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void movetonearIndex(int time) {
        int mIndex = 0;
        if (mIndex < srtSubtitleEntityList.size() &&
                srtSubtitleEntityList.get(mIndex).getStarttime() >= time) {
            mIndex = 0;
        } else {
            for (; mIndex < srtSubtitleEntityList.size(); mIndex++) {
                if (srtSubtitleEntityList.get(srtSubtitleEntityList.size() - 1).getEndtime() < time) {
                    mIndex = srtSubtitleEntityList.size() - 1;
                } else if (srtSubtitleEntityList.get(mIndex).getStarttime() <= time &&
                        srtSubtitleEntityList.get(mIndex).getEndtime() >= time) {
                    mIndex = mIndex;
                } else if (srtSubtitleEntityList.get(mIndex).getEndtime() < time &&
                        srtSubtitleEntityList.get(mIndex + 1).getStarttime() > time) {
                    mIndex = mIndex;
                }
            }
        }
    }

    private float processRate(float rate, boolean isend) {
        float f = rate;
        if (rate < 0) {
            f = 0;
        }
        if (f > 1 && isend) {
            return 0.99F;
        }
        return 0;
    }

    private void processSrtList() {
        preTime(300, srtSubtitleEntityList);
        if (mOrientation != LinearLayout.HORIZONTAL) {
            setMultiLineAndEndLine(srtSubtitleEntityList);
        }
        setRetracementFlag();
        if (srtSubtitleEntityList != null &&
                srtSubtitleEntityList.size() == 1 &&
                "此素材没有提供字幕哦".equals(srtSubtitleEntityList.get(0).getContent())) {
            mNeedWaitingProgress = false;
        }
        setVisibility(VISIBLE);
        invalidate();
    }

    private void refreshColumn(int column) {
        mIndex += 1;
        this.rate = 0.0F;
        if (mIndex < srtSubtitleEntityList.size()) {
            postInvalidate();
        }
    }

    private void refreshText(SRTEntity srtEntity, int time) {
        if (STATE == STATE_REFRESH_TEXT) {
            rate = ((time - srtEntity.getStarttime()) / mDuration);
            postInvalidate();
        }
    }

    private void setMultiLineAndEndLine(List<SRTSubtitleEntity> entities) {
        LinkedList<SRTSubtitleEntity> linkedList = new LinkedList<>();

        for (int i = 0; i < entities.size(); i++) {
            SRTSubtitleEntity entity = entities.get(i);
            if (mCurrentPaint.measureText(entity.getRole() + ":" + entity.getContent()) +
                    2.0f * (2.0f * mHighlightTextSize) > Config.screen_width) {
                String content = entity.getContent();
                String firstHalf = content.substring(0, content.length() / 2);
                String secondHalf = content.substring(content.length() / 2, content.length());
                int halfmDuration = (entity.getEndtime() - entity.getStarttime()) / 2;
                SRTSubtitleEntity firstEntity = new SRTSubtitleEntity(entity.getType(),
                        entity.getRole(), entity.getStarttime(), entity.getStarttime() + halfmDuration,
                        firstHalf, entity.isShowAnim());
                SRTSubtitleEntity secondEntity = new SRTSubtitleEntity(SRTSubtitleEntity.SHOWNORMAL_TYPE,
                        "", entity.getStarttime() + halfmDuration, entity.getEndtime(),
                        secondHalf, false);
                linkedList.add(firstEntity);
                linkedList.add(secondEntity);
            }
        }

        if (linkedList.size() > 0) {
            this.srtSubtitleEntityList = linkedList;
        }
    }

    private void setRetracementFlag() {
    }

    private void preTime(int mDuration, List<SRTSubtitleEntity> entities) {
        for (int i = 0; i < entities.size(); i++) {
            SRTSubtitleEntity entity = entities.get(i);
            if (entity.getStarttime() - mDuration > 0) {
                int pre = entity.getStarttime() - mDuration;
                entity.setStarttime(pre);
            } else if (entity.getEndtime() - mDuration <= 0) {
                int pro = entity.getEndtime() - mDuration;
                entity.setEndtime(pro);
            }
        }
    }

    public boolean checkHasEntity() {
        return srtSubtitleEntityList != null && srtSubtitleEntityList.size() != 0;
    }

    public boolean checkTime(int time) {
        return (time <= mLimitTime) || (mode <= 0);
    }

    public void clearRolesPaint() {
        mRolePaintMap.clear();
    }

    public int getCanRetracementCount() {
        return -1;
    }

    public int getTime() {
        return mTime;
    }

    public void init(List<SRTEntity> srtEntities) {
        srtSubtitleEntityList = SRTUtil.processToSubtitleList(srtEntities);
        processSrtList();
    }

    public void initnew(List<SRTSubtitleEntity> srtSubtitleEntities) {
        srtSubtitleEntityList = srtSubtitleEntities;
        processSrtList();
    }

    public boolean ismCanScroll() {
        return mCanScroll;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean isEditted() {
        return isEdited;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (disabled) {
            canvas.drawText(notSrt, (mWidth - mCurrentPaint.measureText(notSrt)) / 2.0F, mTopMargin, mCurrentPaint);
            return;
        }

        if (srtSubtitleEntityList == null || srtSubtitleEntityList.size() == 0) return;
        if (mIndex == -1 || mIndex >= srtSubtitleEntityList.size()) return;

        // draw edit icon
        if (isEdited && srtSubtitleEntityList != null && srtSubtitleEntityList.size() != 0) {
            canvas.drawBitmap(mEditBitmap,
                    mWidth - mEditBitmap.getWidth() - DimenUtil.dip2px(mContext, 10.0f),
                    DimenUtil.dip2px(mContext, 10.0f),
                    mCurrentPaint);
        }

        SRTSubtitleEntity entity = srtSubtitleEntityList.get(mIndex);
        String content = entity.getContent();
        String role = entity.getRole() + ": ";

        if (mIndex == 0) {
            mProcessTime = entity.getStarttime();
//            int duratin = mProcessTime - mTime;
            last_role = entity.getRole();
            content = role + content;
        } else if (mIndex > 0 && mIndex < srtSubtitleEntityList.size()) {
            SRTSubtitleEntity curSRT = srtSubtitleEntityList.get(mIndex);
            SRTSubtitleEntity lastSRT = srtSubtitleEntityList.get(mIndex - 1);
            content = curSRT.getRole().equals(lastSRT.getRole()) ? content : role + content;
        }

        speed1 = rate;
        speed2 = rate + 0.01f;
        if (speed2 > 1.01d) {
            speed1 = 1f;
            speed2 = 1.01f;
        }

        float contentWidth = mCurrentPaint.measureText(content);

        // draw line
        if (entity.isShowAnim() && mOrientation != LinearLayout.HORIZONTAL) {
            canvas.drawBitmap(mBreakBitmap, (mWidth - mBreakBitmap.getWidth()) / 2f, 0f, mCurrentPaint);
        }

        float contentX = (mWidth - contentWidth) / 2;
        canvas.drawText(content, contentX, mTopMargin - 2, mCurrentPaint);


        // draw had played subtitle
        float roleOffset = content.contains(":") ? mCurrentPaint.measureText(role) : 0;
        float timeOffset = 0;
        if (mTime >= entity.getStarttime()) {
            timeOffset = contentWidth * (mTime - entity.getStarttime()) / (entity.getEndtime() - entity.getStarttime());
        }
        canvas.save();
        canvas.clipRect(contentX, 0, contentX + timeOffset/*offset*/ + roleOffset, getHeight());
        canvas.drawText(content, contentX, mTopMargin - 2, checkPaint(entity.getRole()));
        canvas.restore();

        // draw circle bar for landing indicator
        int pre = entity.getStarttime() - mTime;

        float sweepAngle = 0;
        if (mNeedWaitingProgress && entity.isShowAnim() && pre <= mProcessTime && pre >= 0) {
            sweepAngle = pre * 360 / mProcessTime;
            canvas.save();
            canvas.rotate(-90f, contentX - 1.0F * mHighlightTextSize, (mTopMargin - 2) - 1 * mHighlightTextSize + text_margin_offset / 2.0F);
            float arcL = contentX - 2 * mHighlightTextSize;
            float arcR = contentX - 1 * mHighlightTextSize;
            float arcT = (mTopMargin - 2) - 2 * mHighlightTextSize + text_margin_offset;
            float arcB = (mTopMargin - 2) - mHighlightTextSize + text_margin_offset;
            RectF rectF = new RectF(arcL, arcT, arcR, arcB);
            canvas.drawArc(rectF, 0, -sweepAngle, true, checkPaint(entity.getRole()));
            canvas.restore();
        }


        int nextRow = mIndex + 1;
        float y = mHighlightTextHeight - nextRow;
        while (nextRow < srtSubtitleEntityList.size()) {
            SRTSubtitleEntity subtitleEntity = srtSubtitleEntityList.get(nextRow);
            contentX = y + (mTextHeight - mTextSize) / 2;

            if (subtitleEntity.isShowAnim() && mOrientation != LinearLayout.HORIZONTAL) {
                canvas.drawBitmap(mBreakBitmap, (mWidth - mBreakBitmap.getWidth()) / 2.0F, contentX, mCurrentPaint);
            }
            y = contentX + (mTextHeight + mTextSize) / 2.0F;
            if (y > mHeight) {
            }

            if (subtitleEntity.getType() != SRTSubtitleEntity.SHOWNORMAL_TYPE) {

            }

            content = subtitleEntity.getContent();
            if (!last_role.equals(subtitleEntity.getRole())) {
                last_role = subtitleEntity.getRole();
                content = last_role + ": " + content;
            }
            contentWidth = mNotCurrentPaint.measureText(content);
            canvas.drawText(content, (mWidth - contentWidth) / 2, y, mNotCurrentPaint);
            nextRow++;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    public synchronized void processTime(int time) {
        if (srtSubtitleEntityList == null || mIndex >= srtSubtitleEntityList.size()) {
            return;
        }
        mTime = time;
        SRTEntity entity = srtSubtitleEntityList.get(mIndex);

        mDuration = entity.getEndtime() - entity.getStarttime();
        if (mIndex == srtSubtitleEntityList.size() - 1) {//current highlight is the last row
            STATE = STATE_REFRESH_TEXT;
            refreshText(entity, time);
            postInvalidate();
        }

        if (time > entity.getEndtime() + mAnimationTime) {
            STATE = STATE_REFRESH_COLUMN;
            refreshColumn(time - entity.getEndtime());
        }

        STATE = STATE_REFRESH_TEXT;
        refreshText(entity, time);
    }


    public synchronized void refresh(int time) {
        mIndex = SRTUtil.getIndexByTime(srtSubtitleEntityList, time);
        processTime(time);

    }

    public void refreshEnd() {
        mIndex = (srtSubtitleEntityList.size() - 1);
        rate = 0.99F;
        STATE = STATE_REFRESH_TEXT;
        postInvalidate();
    }

    public void reset() {
        mIndex = 0;
        mTime = 0;
        STATE = STATE_NO_REFRESH;
        invalidate();
    }


    public void setmCanScroll(boolean mCanScroll) {
        mCanScroll = false;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void setEditted(boolean editted) {
        isEdited = editted;
        postInvalidate();
    }

    public void setModeAndLimitTime(int mode, long limittime) {
        this.mode = mode;
        mLimitTime = limittime;
    }

    public void setNeedWaitingProgress(boolean needWaitingProgress) {
        mNeedWaitingProgress = needWaitingProgress;
    }

    public void setOnEventListener(OnEventListener eventListener) {
        mOnEventListener = eventListener;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
        if (orientation == LinearLayout.HORIZONTAL) {
            mTextSize = TypedValue.applyDimension(2, mHighlightTextSize_sp, mMetric);
            mHighlightTextSize = TypedValue.applyDimension(2, horizontal_highlighttextsize_sp, mMetric);
            mNotCurrentPaint.setColor(mCurrentPaintColor);
            mColorSingleDarkPaint.setColor(mColorSingleLightPaintColor);
            mColorCooperaDarkPaint.setColor(mColorCooperaLightPaintColor);
            mNotCurrentPaint.setShadowLayer(3.0F, 0.0F, 0.0F, 0x81000000);
            mCurrentPaint.setShadowLayer(5.0F, 0.0F, 0.0F, 0x81000000);
            mColorSingleLightPaint.setShadowLayer(5.0F, 0.0F, 0.0F, 0x81000000);
            mColorSingleDarkPaint.setShadowLayer(5.0F, 0.0F, 0.0F, 0x81000000);
            mColorCooperaLightPaint.setShadowLayer(5.0F, 0.0F, 0.0F, 0x81000000);
            mColorCooperaDarkPaint.setShadowLayer(5.0F, 0.0F, 0.0F, 0x81000000);
            mColorBackupLightPaint.setShadowLayer(5.0F, 0.0F, 0.0F, 0x81000000);
        } else {
            mNotCurrentPaint.setTextSize(mTextSize);
            mCurrentPaint.setTextSize(mHighlightTextSize);
            mColorSingleLightPaint.setTextSize(mHighlightTextSize);
            mColorSingleDarkPaint.setTextSize(mTextSize);
            mColorCooperaLightPaint.setTextSize(mHighlightTextSize);
            mColorBackupLightPaint.setTextSize(mHighlightTextSize);
            mColorCooperaDarkPaint.setTextSize(mTextSize);
            mTextSize = TypedValue.applyDimension(2, textsize_sp, mMetric);
            mHighlightTextSize = TypedValue.applyDimension(2, mHighlightTextSize_sp, mMetric);
            mNotCurrentPaint.setColor(mNotCurrentPaintColor);
            mColorSingleDarkPaint.setColor(mColorSingleDarkPaintColor);
            mColorCooperaDarkPaint.setColor(mColorCooperaDarkPaintColor);
            mNotCurrentPaint.clearShadowLayer();
            mCurrentPaint.clearShadowLayer();
            mColorSingleLightPaint.clearShadowLayer();
            mColorSingleDarkPaint.clearShadowLayer();
            mColorCooperaLightPaint.clearShadowLayer();
            mColorCooperaDarkPaint.clearShadowLayer();
            mColorBackupLightPaint.clearShadowLayer();
            setBackgroundColor(0);
        }
    }

    public void setRolesPaint(String singleKey, String CooperaKey) {
        mRolePaintMap.put(singleKey, mColorSingleLightPaint);
        mRolePaintMap.put(CooperaKey, mColorCooperaLightPaint);
    }

    public interface OnEventListener {
        void onSyncTimeEvent(int time);
    }

}
