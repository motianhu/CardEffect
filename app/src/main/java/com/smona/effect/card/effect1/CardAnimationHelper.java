package com.smona.effect.card.effect1;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smona.effect.card.R;
import com.smona.effect.card.effect1.transformer.DefaultCommonTransformer;
import com.smona.effect.card.effect1.transformer.DefaultTransformerAdd;
import com.smona.effect.card.effect1.transformer.DefaultTransformerToBack;
import com.smona.effect.card.effect1.transformer.DefaultTransformerToFront;
import com.smona.effect.card.effect1.transformer.DefaultZIndexTransformerCommon;
import com.smona.effect.card.effect1.transformer.DefaultZIndexTransformerToFront;

import java.util.LinkedList;

class CardAnimationHelper implements Animator.AnimatorListener,
        ValueAnimator.AnimatorUpdateListener {
    //animation duration
    static final int ANIM_DURATION = 1000, ANIM_ADD_REMOVE_DELAY = 200,
            ANIM_ADD_REMOVE_DURATION = 500;
    //animation type
    private int mAnimType = CardSpringView.ANIM_TYPE_FRONT;
    //animation duration
    private int mAnimDuration = ANIM_DURATION, mAnimAddRemoveDelay = ANIM_ADD_REMOVE_DELAY,
            mAnimAddRemoveDuration = ANIM_ADD_REMOVE_DURATION;
    //card container view
    private CardSpringView mCardView;
    //card item list
    private LinkedList<CardItem> mCards;
    //total card count
    private int mCardCount;
    //card width, card height
    //for judge Z index
    //    private ArrayList<CardItem> mCards4JudgeZIndex;
    //current card moving to back, current card moving to front
    private CardItem mCardToBack, mCardToFront;
    //current card position moving to front, current card position moving to front
    private int mPositionToBack = 0, mPositionToFront = 0;
    private int mCardWidth, mCardHeight;
    //is doing animation now
    private boolean mIsAnim = false, mIsAddRemoveAnim = false;
    //animator
    private ValueAnimator mValueAnimator;
    //custom animation transformer for card moving to front, card moving to back, and common card
    private AnimationTransformer mTransformerToFront, mTransformerToBack, mTransformerCommon;
    //custom animation transformer for card add and remove
    private AnimationTransformer mTransformerAnimAdd;
    //custom Z index transformer for card moving to front, card moving to back, and common card
    private ZIndexTransformer mZIndexTransformerToFront, mZIndexTransformerToBack, mZIndexTransformerCommon;
    //animation interpolator
    private Interpolator mAnimInterpolator, mAnimAddRemoveInterpolator;
    //current animation fraction
    private float mCurrentFraction = 1;
    //animation listener
    private CardSpringView.CardAnimationListener mCardAnimationListener;

    CardAnimationHelper(int mAnimType, int mAnimDuration, CardSpringView CardSpringView) {
        this.mAnimType = mAnimType;
        this.mAnimDuration = mAnimDuration;
        this.mCardView = CardSpringView;
        initTransformer();
        initAnimator();
    }

    private void initTransformer() {
        mAnimInterpolator = new LinearInterpolator();
        mAnimAddRemoveInterpolator = new LinearInterpolator();
        mTransformerToFront = new DefaultTransformerToFront();
        mTransformerToBack = new DefaultTransformerToBack();
        mTransformerCommon = new DefaultCommonTransformer();
        mTransformerAnimAdd = new DefaultTransformerAdd();
        mZIndexTransformerToFront = new DefaultZIndexTransformerToFront();
        mZIndexTransformerToBack = new DefaultZIndexTransformerCommon();
        mZIndexTransformerCommon = new DefaultZIndexTransformerCommon();
    }

    /**
     * setup animator
     */
    private void initAnimator() {
        mValueAnimator = ValueAnimator.ofFloat(0, 1).setDuration(mAnimDuration);
        mValueAnimator.addUpdateListener(this);
        mValueAnimator.addListener(this);
    }

    /**
     * do animation while update
     *
     * @param animation animation
     */
    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        mCurrentFraction = (float) animation.getAnimatedValue();
        float fractionInterpolated = mCurrentFraction;
        if (mAnimInterpolator != null) {
            fractionInterpolated = mAnimInterpolator.getInterpolation(mCurrentFraction);
        }
        doAnimationBackToFront(mCurrentFraction, fractionInterpolated);
        doAnimationFrontToBack(mCurrentFraction, fractionInterpolated);
        doAnimationCommon(mCurrentFraction, fractionInterpolated);
        bringToFrontByZIndex();
    }

    /**
     * do animation for card moving from back to front
     *
     * @param fraction             animation progress from 0.0f to 1.0f
     * @param fractionInterpolated interpolated animation progress
     */
    private void doAnimationBackToFront(float fraction, float fractionInterpolated) {
        mTransformerToFront.transformAnimation(mCardToFront.view,
                fraction, mCardWidth, mCardHeight, mPositionToFront, 0);
        if (mAnimInterpolator != null) {
            mTransformerToFront.transformInterpolatedAnimation(mCardToFront.view,
                    fractionInterpolated, mCardWidth, mCardHeight, mPositionToFront, 0);
        }
        doAnimationZIndex(mZIndexTransformerToFront, mCardToFront, fraction, fractionInterpolated,
                mPositionToFront, 0);
    }

    /**
     * do animation for card moving from from to back
     *
     * @param fraction             animation progress from 0.0f to 1.0f
     * @param fractionInterpolated interpolated animation progress
     */
    private void doAnimationFrontToBack(float fraction, float fractionInterpolated) {
        if (mAnimType == CardSpringView.ANIM_TYPE_FRONT) {
            return;
        }
        mTransformerToBack.transformAnimation(mCardToBack.view, fraction, mCardWidth,
                mCardHeight, 0, mPositionToBack);
        if (mAnimInterpolator != null) {
            mTransformerToBack.transformInterpolatedAnimation(mCardToBack.view,
                    fractionInterpolated, mCardWidth, mCardHeight, 0, mPositionToBack);
        }
        doAnimationZIndex(mZIndexTransformerToBack, mCardToBack, fraction, fractionInterpolated,
                0, mPositionToBack);
    }

    /**
     * do animation for common card items
     *
     * @param fraction             animation progress from 0.0f to 1.0f
     * @param fractionInterpolated interpolated animation progress
     */
    private void doAnimationCommon(float fraction, float fractionInterpolated) {
        if (mAnimType == CardSpringView.ANIM_TYPE_FRONT) {
            for (int i = 0; i < mPositionToFront; i++) {
                CardItem card = mCards.get(i);
                doAnimationCommonView(card.view, fraction, fractionInterpolated, i, i + 1);
                doAnimationZIndex(mZIndexTransformerCommon, card, fraction, fractionInterpolated,
                        i, i + 1);
            }
        } else if (mAnimType == CardSpringView.ANIM_TYPE_FRONT_TO_LAST) {
            for (int i = mPositionToFront + 1; i < mCardCount; i++) {
                CardItem card = mCards.get(i);
                doAnimationCommonView(card.view, fraction, fractionInterpolated, i, i - 1);
                doAnimationZIndex(mZIndexTransformerCommon, card, fraction, fractionInterpolated,
                        i, i - 1);
            }
        }
    }

    /**
     * do animation for common card views
     *
     * @param view                 card view
     * @param fraction             animation progress from 0.0f to 1.0f
     * @param fractionInterpolated interpolated animation progress
     * @param fromPosition         card moving from
     * @param toPosition           card moving to
     */
    private void doAnimationCommonView(View view, float fraction, float fractionInterpolated, int
            fromPosition, int toPosition) {
        mTransformerCommon.transformAnimation(view, fraction, mCardWidth,
                mCardHeight, fromPosition, toPosition);
        if (mAnimInterpolator != null) {
            mTransformerCommon.transformInterpolatedAnimation(view, fractionInterpolated, mCardWidth,
                    mCardHeight, fromPosition, toPosition);
        }
    }

    /**
     * do calculation for card Z index
     *
     * @param transformer          Z index transformer
     * @param card                 card item
     * @param fraction             animation progress from 0.0f to 1.0f
     * @param fractionInterpolated interpolated animation progress
     * @param fromPosition         card moving from
     * @param toPosition           card moving to
     */
    private void doAnimationZIndex(ZIndexTransformer transformer, CardItem card, float fraction,
                                   float fractionInterpolated, int fromPosition, int toPosition) {
        transformer.transformAnimation(card, fraction, mCardWidth,
                mCardHeight, fromPosition, toPosition);
        if (mAnimInterpolator != null) {
            transformer.transformInterpolatedAnimation(card, fractionInterpolated, mCardWidth,
                    mCardHeight, fromPosition, toPosition);
        }
    }

    /**
     * bring card to front by Z index, the card with smaller Z index is in front of the card with
     * bigger Z index
     */
    private void bringToFrontByZIndex() {
        if (mAnimType == CardSpringView.ANIM_TYPE_FRONT) {
            //if the animation type is ANIM_TYPE_FRONT, which means other cards are under common
            // animation, so start cycling the card items from the position before the moving
            // card, and while the moving card's Z index is smaller than an other card, we
            // call bringToFront for it, otherwise we call bringToFront for other cards
            for (int i = mPositionToFront - 1; i >= 0; i--) {
                CardItem card = mCards.get(i);
                if (card.zIndex > mCardToFront.zIndex) {
                    mCardToFront.view.bringToFront();
                    mCardView.updateViewLayout(mCardToFront.view, mCardToFront.view.getLayoutParams());
                } else {
                    card.view.bringToFront();
                    mCardView.updateViewLayout(card.view, card.view.getLayoutParams());
                }
            }
        } else {
            //sort the card items by Z index and call bringToFront foreach view
//            Collections.sort(mCards4JudgeZIndex, this);
//            for (int i = mCardCount - 1; i >= 0; i--) {
//                mCards4JudgeZIndex.get(i).view.bringToFront();
//            }
            //##########################for better performance#########################
            boolean cardToFrontBrought = false;//is card moving to front called bringToFront
            //cycling the card items
            for (int i = mCardCount - 1; i > 0; i--) {
                CardItem card = mCards.get(i);
                //get the card before current card
                CardItem cardPre = i > 1 ? mCards.get(i - 1) : null;
                //is card moving to back behind the card before current card
                boolean cardToBackBehindCardPre = cardPre == null ||
                        mCardToBack.zIndex > cardPre.zIndex;
                //if the card moving to back Z index is smaller than current card, and is behind
                // the card before current card, we should call bringToFront for it
                boolean bringCardToBackViewToFront = mCardToBack.zIndex < card.zIndex && cardToBackBehindCardPre;
                //is card moving to front behind the current card
                boolean cardToFrontBehindCardPre = cardPre == null ||
                        mCardToFront.zIndex > cardPre.zIndex;
                //if the card moving to front Z index is smaller than current card, and is behind
                // the card before current card, we should call bringToFront for it
                boolean bringCardToFrontViewToFront = mCardToFront.zIndex < card.zIndex && cardToFrontBehindCardPre;
                //if current card is not the card moving to front
                if (i != mPositionToFront) {
                    //call bringToFront for it
                    card.view.bringToFront();
                    mCardView.updateViewLayout(card.view, card.view.getLayoutParams());
                    //if we should bring the card moving to back to front, just do it
                    if (bringCardToBackViewToFront) {
                        mCardToBack.view.bringToFront();
                        mCardView.updateViewLayout(mCardToBack.view, mCardToBack.view.getLayoutParams());
                    }
                    //if we should bring the card moving to front to front, just do it
                    if (bringCardToFrontViewToFront) {
                        mCardToFront.view.bringToFront();
                        mCardView.updateViewLayout(mCardToFront.view, mCardToFront.view.getLayoutParams());
                        cardToFrontBrought = true;
                    }
                    //if we has both bring the card moving to front and back to front, and the
                    // card moving to back Z index is smaller than the card moving to front Z
                    // index, we call bringToFront for the card moving to back
                    if (bringCardToBackViewToFront && bringCardToFrontViewToFront &&
                            mCardToBack.zIndex < mCardToFront.zIndex) {
                        mCardToBack.view.bringToFront();
                        mCardView.updateViewLayout(mCardToBack.view, mCardToBack.view.getLayoutParams());
                    }
                } else {
                    //if current card is the card moving to front, and behind the card before
                    if (cardToFrontBehindCardPre) {
                        mCardToFront.view.bringToFront();
                        mCardView.updateViewLayout(mCardToFront.view, mCardToFront.view.getLayoutParams());
                        cardToFrontBrought = true;
                        //if the card moving to back Z index is smaller than the card moving to
                        // front, call bringToFront for it
                        if (cardToBackBehindCardPre && mCardToBack.zIndex < mCardToFront.zIndex) {
                            mCardToBack.view.bringToFront();
                            mCardView.updateViewLayout(mCardToBack.view, mCardToBack.view.getLayoutParams());
                        }
                    }
                }
            }
            // it the card moving to front has not call bringToFront yet, which means it is
            // already in the first position, call bringToFront for it
            if (!cardToFrontBrought) {
                mCardToFront.view.bringToFront();
                mCardView.updateViewLayout(mCardToFront.view, mCardToFront.view.getLayoutParams());
            }
        }
    }

    @Override
    public void onAnimationStart(Animator animation) {
        mCurrentFraction = 0;
        if (mCardAnimationListener != null) {
            mCardAnimationListener.onAnimationStart();
        }
    }

    /**
     * animation end
     *
     * @param animation animation
     */
    @Override
    public void onAnimationEnd(Animator animation) {
        if (mAnimType == CardSpringView.ANIM_TYPE_FRONT) {
            //move the card moving to front to the first position
            mCards.remove(mPositionToFront);
            mCards.addFirst(mCardToFront);
        } else if (mAnimType == CardSpringView.ANIM_TYPE_SWITCH) {
            //switch the position of the card moving to front and back
            mCards.remove(mPositionToFront);
            mCards.removeFirst();
            mCards.addFirst(mCardToFront);
            mCards.add(mPositionToFront, mCardToBack);
        } else {
            //moving the first position card to last
            mCards.remove(mPositionToFront);
            mCards.removeFirst();
            mCards.addFirst(mCardToFront);
            mCards.addLast(mCardToBack);
        }
        mPositionToFront = 0;
        mPositionToBack = 0;
        mCurrentFraction = 1;
        mIsAnim = false;
        if (mCardAnimationListener != null) {
            mCardAnimationListener.onAnimationEnd();
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    /**
     * init adapter view
     *
     * @param resIds
     */
    void initAdapterView(int[] resIds) {
        if (mCardWidth > 0 && mCardHeight > 0) {
            if (mCards == null) {
                mCardView.removeAllViews();
                BaseAdapter adapter = new MyAdapter(resIds);
                firstSetAdapter(adapter);
            }
        }
    }

    /**
     * first time set an adapter
     *
     * @param adapter adapter
     */
    private void firstSetAdapter(BaseAdapter adapter) {
        if (mTransformerAnimAdd != null) {
            mIsAddRemoveAnim = true;
        }
        mCards = new LinkedList<>();
//            mCards4JudgeZIndex = new ArrayList<>();
        mCardCount = adapter.getCount();
        for (int i = mCardCount - 1; i >= 0; i--) {
            View child = adapter.getView(i, null, mCardView);
            CardItem cardItem = new CardItem(child, 0, i);
            mCardView.addCardView(cardItem);
            mZIndexTransformerCommon.transformAnimation(cardItem, mCurrentFraction, mCardWidth, mCardHeight, i, i);
            mTransformerCommon.transformAnimation(child, mCurrentFraction, mCardWidth, mCardHeight, i, i);
            mCards.addFirst(cardItem);
//                mCards4JudgeZIndex.add(cardItem);
            child.setVisibility(View.INVISIBLE);
            showAnimAdd(child, i * mAnimAddRemoveDelay, i, i == mCardCount - 1);
        }
    }

    private void showAnimAdd(final View view, int delay, final int position, final boolean isLast) {
        if (mTransformerAnimAdd == null) {
            return;
        }
        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1).setDuration(mAnimAddRemoveDuration);
        valueAnimator.setStartDelay(delay);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = (float) animation.getAnimatedValue();
                mTransformerAnimAdd.transformAnimation(view, fraction, mCardWidth, mCardHeight,
                        position, position);
                if (mAnimAddRemoveInterpolator != null) {
                    mTransformerAnimAdd.transformInterpolatedAnimation(view,
                            mAnimAddRemoveInterpolator.getInterpolation(fraction),
                            mCardWidth, mCardHeight, position, position);
                }
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isLast) {
                    mIsAddRemoveAnim = false;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mCardView.post(new Runnable() {
            @Override
            public void run() {
                valueAnimator.start();
            }
        });
    }

    /**
     * bring the specific position card to front
     *
     * @param position position
     */
    void bringCardToFront(int position) {
        if (position >= 0 && position != mPositionToFront && !mIsAnim && !mIsAddRemoveAnim) {
            mPositionToFront = position;
            //if the animation type is not ANIM_TYPE_SWITCH, the card to back post is the last
            // position
            mPositionToBack = mAnimType == CardSpringView.ANIM_TYPE_SWITCH ? mPositionToFront :
                    (mCardCount - 1);
            mCardToBack = mCards.getFirst();
            mCardToFront = mCards.get(mPositionToFront);
            if (mValueAnimator.isRunning()) {
                mValueAnimator.end();
            }
            mIsAnim = true;
            mValueAnimator.start();
        }
    }

    void setCardSize(int cardWidth, int cardHeight) {
        this.mCardWidth = cardWidth;
        this.mCardHeight = cardHeight;
    }

    void setTransformerToFront(AnimationTransformer toFrontTransformer) {
        if (mIsAnim || mIsAddRemoveAnim) {
            return;
        }
        this.mTransformerToFront = toFrontTransformer;
    }

    void setTransformerToBack(AnimationTransformer toBackTransformer) {
        if (mIsAnim || mIsAddRemoveAnim) {
            return;
        }
        this.mTransformerToBack = toBackTransformer;
    }

    void setZIndexTransformerToBack(ZIndexTransformer zIndexTransformerToBack) {
        if (mIsAnim || mIsAddRemoveAnim) {
            return;
        }
        this.mZIndexTransformerToBack = zIndexTransformerToBack;
    }

    void setAnimInterpolator(Interpolator animInterpolator) {
        if (mIsAnim || mIsAddRemoveAnim) {
            return;
        }
        this.mAnimInterpolator = animInterpolator;
    }

    void setAnimType(int animType) {
        if (mIsAnim || mIsAddRemoveAnim) {
            return;
        }
        this.mAnimType = animType;
    }

    void setAnimAddRemoveDelay(int animAddRemoveDelay) {
        if (mIsAnim || mIsAddRemoveAnim) {
            return;
        }
        this.mAnimAddRemoveDelay = animAddRemoveDelay;
    }

    void setAnimAddRemoveDuration(int animAddRemoveDuration) {
        if (mIsAnim || mIsAddRemoveAnim) {
            return;
        }
        this.mAnimAddRemoveDuration = animAddRemoveDuration;
    }

    boolean isAnimating() {
        return mIsAnim || mIsAddRemoveAnim;
    }

    void setCardAnimationListener(CardSpringView.CardAnimationListener cardAnimationListener) {
        this.mCardAnimationListener = cardAnimationListener;
    }


    private class MyAdapter extends BaseAdapter {
        private int[] resIds = {};

        MyAdapter(int[] resIds) {
            this.resIds = resIds;
        }

        @Override
        public int getCount() {
            return resIds.length;
        }

        @Override
        public Integer getItem(int position) {
            return resIds[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .item_card, parent, false);
            }
            convertView.setBackgroundResource(resIds[position]);
            TextView textView = convertView.findViewById(R.id.title);
            textView.setText("Index: " + position);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("motianhu", "postion : " + position);
                    mCardView.bringCardToFront(1);
                }
            });
            return convertView;
        }
    }
}
