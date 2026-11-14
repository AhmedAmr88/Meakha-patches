package app.revanced.extension.samsungkeyboard;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public final class KeyboardSizeCompat {
    // Height on large screens = width * Galaxy Fold inner-screen fraction (0.427) * this scale.
    private static final float HEIGHT_SCALE = 0.85f;
    // Default phone landscape width ratio (base width * 1.6 = keyboard width in landscape).
    private static final float PHONE_LANDSCAPE_WIDTH_RATIO = 1.6f;
    private static final float LARGE_SCREEN_MIN_WIDTH_DP = 600f;

    private static volatile boolean resolved;
    private static int phoneHeightId;
    private static int phoneWidthId;
    private static float largeFraction = 0.42735f;

    private KeyboardSizeCompat() {
    }

    public static float adjustSize(int resourceId, float basis, float size) {
        try {
            if (resourceId == 0 || basis <= 0f) return size;
            Context context = SettingsStore.getContext();
            if (context == null) return size;
            Resources resources = context.getResources();
            resolve(context, resources);
            if (resourceId != phoneHeightId && resourceId != phoneWidthId) return size;

            DisplayMetrics metrics = resources.getDisplayMetrics();
            float density = metrics.density;
            if (density <= 0f || basis / density < LARGE_SCREEN_MIN_WIDTH_DP) return size;

            if (resourceId == phoneHeightId) {
                return Math.min(size, basis * largeFraction * HEIGHT_SCALE);
            }

            // Landscape: the phone layout makes the keyboard 1.6x the base width, which is wider
            // than a near-square inner screen and cuts off keys on both sides.
            if (metrics.widthPixels > metrics.heightPixels) {
                return Math.min(size, metrics.widthPixels / PHONE_LANDSCAPE_WIDTH_RATIO);
            }
            return size;
        } catch (RuntimeException ignored) {
            return size;
        }
    }

    private static synchronized void resolve(Context context, Resources resources) {
        if (resolved) return;
        String pkg = context.getPackageName();
        phoneHeightId = resources.getIdentifier("phone_height_fraction", "string", pkg);
        phoneWidthId = resources.getIdentifier("phone_width_fraction", "string", pkg);
        int largeId = resources.getIdentifier("winner_height_fraction", "string", pkg);
        if (largeId != 0) {
            try {
                float value = Float.parseFloat(resources.getString(largeId));
                if (value > 0f && value < 1f) largeFraction = value;
            } catch (RuntimeException ignored) {
            }
        }
        resolved = true;
    }
}
