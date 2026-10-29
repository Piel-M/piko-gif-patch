package app.pielm.extension.twitter.gif;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

/**
 * Adds an "Add-ons" section to Piko's "Patch information" screen showing that the
 * Piko GIF Patch add-on is applied, its version and which GIF integrations were hooked.
 */
@SuppressWarnings({"deprecation", "unused"})
public final class PatchInfoHook {
    private static final String TAG = "PikoGifPatch";
    private static final String REPO_URL = "https://github.com/Piel-M/piko-gif-patch";
    /** Same blue Piko uses for "Included" entries. */
    private static final String INCLUDED_COLOR = "#008FC4";
    private static final String KEY_PREFIX = "piko_gif_patch_";

    private PatchInfoHook() {
    }

    /**
     * Injected in {@code SettingsAboutFragment.onCreate()} right before {@code setPreferenceScreen(screen)}.
     */
    public static void onPatchInfoScreenCreated(Object fragment, PreferenceScreen screen) {
        if (screen == null) return;
        try {
            if (screen.findPreference(KEY_PREFIX + "addon") != null) return;
            Context context = screen.getContext();

            PreferenceCategory category = newCategory(context);
            category.setKey(KEY_PREFIX + "category");
            category.setTitle(Strings.addonsCategory());
            placeAfterFirstCategory(screen, category);

            Preference addon = new Preference(context);
            addon.setKey(KEY_PREFIX + "addon");
            addon.setTitle(Strings.addonTitle());
            addon.setSummary(coloredSummary(
                    Strings.included() + " • " + Strings.addonDetails(GifPatchHooks.getPatchVersion())));
            addon.setOnPreferenceClickListener(p -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(REPO_URL))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Throwable t) {
                    Log.w(TAG, "Unable to open repository page", t);
                }
                return true;
            });
            category.addPreference(addon);

            String integrations = GifPatchHooks.getEnabledIntegrations();
            if (!TextUtils.isEmpty(integrations)) {
                Preference details = new Preference(context);
                details.setKey(KEY_PREFIX + "integrations");
                details.setTitle(Strings.integrationTitle());
                details.setSummary(integrations);
                details.setSelectable(false);
                category.addPreference(details);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Unable to add GIF add-on to patch information screen", t);
        }
    }

    /** Uses X's own category widget (as Piko does) so the section matches the rest of the screen. */
    private static PreferenceCategory newCategory(Context context) {
        try {
            Class<?> legacy = Class.forName("com.twitter.ui.widget.LegacyTwitterPreferenceCategory");
            Object instance = legacy.getConstructor(Context.class).newInstance(context);
            if (instance instanceof PreferenceCategory) {
                return (PreferenceCategory) instance;
            }
        } catch (Throwable ignored) {
        }
        return new PreferenceCategory(context);
    }

    /**
     * Inserts the category right below Piko's "Version info" block instead of after the long
     * list of patches. PreferenceGroup re-sorts its children by order when it is displayed.
     */
    private static void placeAfterFirstCategory(PreferenceGroup screen, PreferenceCategory category) {
        int count = screen.getPreferenceCount();
        if (count >= 1) {
            for (int i = 0; i < count; i++) {
                Preference p = screen.getPreference(i);
                p.setOrder(i == 0 ? 0 : (i + 1) * 10);
            }
            category.setOrder(5);
        }
        screen.addPreference(category);
    }

    private static CharSequence coloredSummary(String text) {
        Spannable span = new SpannableString(text);
        int end = Math.min(text.length(), Strings.included().length());
        span.setSpan(new ForegroundColorSpan(Color.parseColor(INCLUDED_COLOR)), 0, end, 0);
        return span;
    }
}
