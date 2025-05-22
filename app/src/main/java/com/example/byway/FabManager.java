package com.example.byway;

import android.view.View;

public class FabManager {
    // FAB 나타나는 애니메이션
    public static void showSubFabs(View fabSubContainer) {
        fabSubContainer.setVisibility(View.VISIBLE);
        fabSubContainer.setAlpha(0f);
        fabSubContainer.setScaleX(0.8f);
        fabSubContainer.setScaleY(0.8f);

        fabSubContainer.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .start();
    }

    public static void hideSubFabs(View fabSubContainer) {
        fabSubContainer.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(200)
                .withEndAction(() -> fabSubContainer.setVisibility(View.GONE))
                .start();
    }

}
