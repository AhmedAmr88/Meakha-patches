package app.revanced.extension.samsungkeyboard;

import android.content.Context;
import android.content.res.Resources;

public final class KeyboardSizeCompat {
    private static volatile boolean resolved;
    private static int phoneHeightId;
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
            if (resourceId != phoneHeightId) return size;
            float density = resources.getDisplayMetrics().density;
            if (density <= 0f || basis / density < 600f) return size;
            return Math.min(size, basis * largeFraction);
        } catch (RuntimeException ignored) {
            return size;
        }
    }

    private static synchronized void resolve(Context context, Resources resources) {
        if (resolved) return;
        String pkg = context.getPackageName();
        phoneHeightId = resources.getIdentifier("phone_height_fraction", "string", pkg);
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
