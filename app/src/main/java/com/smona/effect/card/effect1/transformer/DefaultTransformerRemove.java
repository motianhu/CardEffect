package com.smona.effect.card.effect1.transformer;

import android.view.View;

import com.smona.effect.card.effect1.AnimationTransformer;

public class DefaultTransformerRemove implements AnimationTransformer {
    @Override
    public void transformAnimation(View view, float fraction, int cardWidth, int cardHeight, int fromPosition, int toPosition) {
        float scale = (0.8f - 0.1f * fromPosition);
        view.setScaleX(scale);
        view.setScaleY(scale);
        view.setTranslationY(-cardHeight * (0.8f - scale) * 0.5f - cardWidth * 0.02f
                * fromPosition + cardHeight * fraction);
        view.setAlpha(1 - fraction);
    }

    @Override
    public void transformInterpolatedAnimation(View view, float fraction, int cardWidth, int cardHeight, int fromPosition, int toPosition) {

    }
}
