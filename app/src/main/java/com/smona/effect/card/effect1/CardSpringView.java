package com.smona.effect.card.effect1;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.BaseAdapter;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CardSpringView extends ViewGroup {
    public static final int ANIM_TYPE_FRONT = 0, ANIM_TYPE_SWITCH = 1, ANIM_TYPE_FRONT_TO_LAST = 2;
    //cardHeight / cardWidth = CARD_SIZE_RATIO
    private static final float CARD_SIZE_RATIO = 0.5f;
    //cardHeight / cardWidth = mCardRatio
    private float mCardRatio = CARD_SIZE_RATIO;
    //animation helper
    private CardAnimationHelper mAnimationHelper;
    //view adapter
    private BaseAdapter mAdapter;
    private int mCardWidth, mCardHeight;

    public CardSpringView(@NonNull Context context) {
        this(context, null);
    }

    public CardSpringView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardSpringView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
        setClickable(true);
    }

    private void init(Context context, AttributeSet attrs) {
        int animType = ANIM_TYPE_FRONT;
        int animDuration = CardAnimationHelper.ANIM_DURATION;
        int animAddRemoveDuration = CardAnimationHelper.ANIM_ADD_REMOVE_DURATION;
        int animAddRemoveDelay = CardAnimationHelper.ANIM_ADD_REMOVE_DELAY;
        mAnimationHelper = new CardAnimationHelper(animType, animDuration, this);
        mAnimationHelper.setAnimAddRemoveDuration(animAddRemoveDuration);
        mAnimationHelper.setAnimAddRemoveDelay(animAddRemoveDelay);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            int childCount = getChildCount();
            int childWidth = 0, childHeight = 0;
            for (int i = 0; i < childCount; i++) {
                View childView = getChildAt(i);
                childWidth = Math.max(childView.getMeasuredWidth(), childWidth);
                childHeight = Math.max(childView.getMeasuredHeight(), childHeight);
            }
            setMeasuredDimension((widthMode == MeasureSpec.EXACTLY) ? sizeWidth : childWidth,
                    (heightMode == MeasureSpec.EXACTLY) ? sizeHeight : childHeight);
        } else {
            setMeasuredDimension(sizeWidth, sizeHeight);
        }
        if (mCardWidth == 0 || mCardHeight == 0) {
            setCardSize(true);
        }
    }

    private void setCardSize(boolean resetAdapter) {
        mCardWidth = getMeasuredWidth();
        mCardHeight = (int) (mCardWidth * mCardRatio);
        mAnimationHelper.setCardSize(mCardWidth, mCardHeight);
        mAnimationHelper.initAdapterView(mAdapter, resetAdapter);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        int childWidth, childHeight;
        int childLeft, childTop, childRight, childBottom;
        int width = getWidth(), height = getHeight();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            childWidth = childView.getMeasuredWidth();
            childHeight = childView.getMeasuredHeight();
            childLeft = (width - childWidth) / 2;
            childTop = (height - childHeight) / 2;
            childRight = childLeft + childWidth;
            childBottom = childTop + childHeight;
            childView.layout(childLeft, childTop, childRight, childBottom);
        }
    }

    void addCardView(CardItem card) {
        addView(getCardView(card));
    }

    void addCardView(CardItem card, int position) {
        addView(getCardView(card), position);
    }

    private View getCardView(final CardItem card) {
        View view = card.view;
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(mCardWidth,
                mCardHeight);
        view.setLayoutParams(layoutParams);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                bringCardToFront(card);
            }
        });
        return view;
    }

    private void bringCardToFront(CardItem card) {
        if (!isClickable()) {
            return;
        }
        mAnimationHelper.bringCardToFront(card);
    }

    /**
     * bring the specific position card to front
     *
     * @param position position
     */
    public void bringCardToFront(int position) {
        mAnimationHelper.bringCardToFront(position);
    }

    /**
     * set view adapter
     *
     * @param adapter adapter
     */
    public void setAdapter(BaseAdapter adapter) {
        this.mAdapter = adapter;
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                mAnimationHelper.notifyDataSetChanged(mAdapter);
            }
        });
        mAnimationHelper.initAdapterView(adapter, true);
    }

    public void setTransformerToFront(AnimationTransformer toFrontTransformer) {
        mAnimationHelper.setTransformerToFront(toFrontTransformer);
    }

    public void setTransformerToBack(AnimationTransformer toBackTransformer) {
        mAnimationHelper.setTransformerToBack(toBackTransformer);
    }

    public void setCommonSwitchTransformer(AnimationTransformer commonTransformer) {
        mAnimationHelper.setCommonSwitchTransformer(commonTransformer);
    }

    public void setTransformerCommon(AnimationTransformer transformerCommon) {
        mAnimationHelper.setTransformerCommon(transformerCommon);
    }

    public void setZIndexTransformerToFront(ZIndexTransformer zIndexTransformerToFront) {
        mAnimationHelper.setZIndexTransformerToFront(zIndexTransformerToFront);
    }

    public void setZIndexTransformerToBack(ZIndexTransformer zIndexTransformerToBack) {
        mAnimationHelper.setZIndexTransformerToBack(zIndexTransformerToBack);
    }

    public void setZIndexTransformerCommon(ZIndexTransformer zIndexTransformerCommon) {
        mAnimationHelper.setZIndexTransformerCommon(zIndexTransformerCommon);
    }

    public void setAnimInterpolator(Interpolator animInterpolator) {
        mAnimationHelper.setAnimInterpolator(animInterpolator);
    }

    public void setAnimType(int animType) {
        mAnimationHelper.setAnimType(animType);
    }

    public void setTransformerAnimAdd(AnimationTransformer transformerAnimAdd) {
        mAnimationHelper.setTransformerAnimAdd(transformerAnimAdd);
    }

    public void setTransformerAnimRemove(AnimationTransformer transformerAnimRemove) {
        mAnimationHelper.setTransformerAnimRemove(transformerAnimRemove);
    }

    public void setAnimAddRemoveInterpolator(Interpolator animAddRemoveInterpolator) {
        mAnimationHelper.setAnimAddRemoveInterpolator(animAddRemoveInterpolator);
    }

    public void setCardSizeRatio(float cardSizeRatio) {
        this.mCardRatio = cardSizeRatio;
        setCardSize(false);
    }

    public boolean isAnimating() {
        return mAnimationHelper.isAnimating();
    }

    public void setCardAnimationListener(CardAnimationListener cardAnimationListener){
        mAnimationHelper.setCardAnimationListener(cardAnimationListener);
    }

    public static interface CardAnimationListener{
        void onAnimationStart();
        void onAnimationEnd();
    }
}
