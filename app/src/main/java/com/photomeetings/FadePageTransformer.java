package com.photomeetings;

import android.support.v4.view.ViewPager;
import android.view.View;

public class FadePageTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View page, float position) {
        page.setTranslationX(page.getWidth() * -position);

        if (position <= -1F || position >= 1F) {
            page.setAlpha(0F);
        } else if (position == 0F) {
            page.setAlpha(1F);
        } else {
            // position is between -1F & 0F OR 0F & 1F
            page.setAlpha(1F - Math.abs(position));
        }
    }

}
