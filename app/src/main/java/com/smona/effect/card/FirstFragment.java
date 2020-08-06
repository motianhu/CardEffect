package com.smona.effect.card;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.smona.aoplib.DoubleClick;
import com.smona.effect.card.effect1.AnimationTransformer;
import com.smona.effect.card.effect1.CardItem;
import com.smona.effect.card.effect1.CardSpringView;
import com.smona.effect.card.effect1.ZIndexTransformer;
import com.smona.effect.card.effect1.transformer.DefaultCommonTransformer;

public class FirstFragment extends Fragment {

    private CardSpringView mCardView;
    private int[] resId = {R.mipmap.pic1, R.mipmap.pic2, R.mipmap.pic3, R.mipmap
            .pic4, R.mipmap.pic5};

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        initViews(view);
    }

    private void initViews(View view) {
        mCardView = view.findViewById(R.id.springView);
        mCardView.setNewData(resId);
        mCardView.setCardAnimationListener(new CardSpringView.CardAnimationListener() {
            @Override
            public void onAnimationStart() {
                Toast.makeText(getContext(), "Animation Start", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAnimationEnd() {
                Toast.makeText(getContext(), "Animation End", Toast.LENGTH_SHORT).show();
            }
        });
        initButton(view);
    }

    private void initButton(View view) {
        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @DoubleClick
            @Override
            public void onClick(View v) {
                setStyle3();
                mCardView.bringCardToFront(1);
            }
        });
    }

    private void setStyle3() {
        mCardView.setClickable(true);
        mCardView.setAnimType(CardSpringView.ANIM_TYPE_FRONT_TO_LAST);
        mCardView.setAnimInterpolator(new OvershootInterpolator(-15));
        mCardView.setTransformerToFront(new DefaultCommonTransformer());
        mCardView.setTransformerToBack(new AnimationTransformer() {
           @Override
           public void transformAnimation(View view, float fraction, int cardWidth, int cardHeight, int fromPosition, int toPosition) {
               int positionCount = fromPosition - toPosition;
               float scale = (0.8f - 0.1f * fromPosition) + (0.1f * fraction * positionCount);
               view.setScaleX(scale);
               view.setScaleY(scale);
               if (fraction < 0.7) {
                   view.setRotationX(180 * fraction);
               } else {
                   view.setRotationX(180 * (1 - fraction));
               }
           }

            @Override
            public void transformInterpolatedAnimation(View view, float fraction, int cardWidth, int cardHeight, int fromPosition, int toPosition) {
                int positionCount = fromPosition - toPosition;
                float scale = (0.8f - 0.1f * fromPosition) + (0.1f * fraction * positionCount);
                view.setTranslationY(-cardHeight * (0.8f - scale) * 0.5f - cardWidth * (0.02f *
                        fromPosition - 0.02f * fraction * positionCount));
            }
        });
        mCardView.setZIndexTransformerToBack(new ZIndexTransformer() {
            @Override
            public void transformAnimation(CardItem card, float fraction, int cardWidth, int cardHeight, int fromPosition, int toPosition) {
                if (fraction < 0.5f) {
                    card.zIndex = 1f + 0.01f * fromPosition;
                } else {
                    card.zIndex = 1f + 0.01f * toPosition;
                }
            }

            @Override
            public void transformInterpolatedAnimation(CardItem card, float fraction, int cardWidth, int cardHeight, int fromPosition, int toPosition) {

            }
        });
    }
}