package app.pielm.extension.twitter.gif;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import java.lang.reflect.Constructor;

/**
 * Displays the Piko GIF Patch add-on in Piko's "Patch information" screen:
 * 1. Under "Informations sur la version" (category 0) right beneath "Patchs Piko":
 *    - "Piko GIF Patch" -> "v1.2.0 • Inclus" (clickable, opens GitHub repo)
 * 2. Under "Patchs" (category 1) alongside all other patches:
 *    - "Téléchargement natif des GIF" -> "Inclus" (clickable, opens GitHub repo)
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
            if (screen.findPreference(KEY_PREFIX + "version") != null) return;
            Context context = screen.getContext();

            // 1. Add directly to "Informations sur la version" (verPref, index 0)
            if (screen.getPreferenceCount() > 0) {
                Preference p0 = screen.getPreference(0);
                if (p0 instanceof PreferenceGroup) {
                    PreferenceGroup verPref = (PreferenceGroup) p0;
                    Preference versionPref = createButtonPreference(context);
                    versionPref.setKey(KEY_PREFIX + "version");
                    versionPref.setPersistent(false);
                    versionPref.setTitle("Piko GIF Patch");
                    versionPref.setSummary(coloredIncluded("v" + GifPatchHooks.getPatchVersion() + " • " + Strings.included()));
                    versionPref.setOnPreferenceClickListener(p -> {
                        openRepo(context);
                        return true;
                    });
                    verPref.addPreference(versionPref);
                }
            }

            // 2. Add to "Patchs" category (patPref, index 1)
            if (screen.getPreferenceCount() > 1) {
                Preference p1 = screen.getPreference(1);
                if (p1 instanceof PreferenceGroup) {
                    PreferenceGroup patPref = (PreferenceGroup) p1;
                    Preference featurePref = new Preference(context);
                    featurePref.setKey(KEY_PREFIX + "feature");
                    featurePref.setPersistent(false);
                    featurePref.setTitle(Strings.addonTitle());
                    featurePref.setSummary(coloredIncluded(Strings.included()));
                    featurePref.setOnPreferenceClickListener(p -> {
                        openRepo(context);
                        return true;
                    });
                    patPref.addPreference(featurePref);
                }
            }

            // Fallback if screen had no categories
            if (screen.getPreferenceCount() == 0) {
                Preference fallback = new Preference(context);
                fallback.setKey(KEY_PREFIX + "version");
                fallback.setPersistent(false);
                fallback.setTitle("Piko GIF Patch");
                fallback.setSummary(coloredIncluded("v" + GifPatchHooks.getPatchVersion() + " • " + Strings.included()));
                fallback.setOnPreferenceClickListener(p -> {
                    openRepo(context);
                    return true;
                });
                screen.addPreference(fallback);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Unable to add GIF add-on to patch information screen", t);
        }
    }

    private static void openRepo(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(REPO_URL))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Throwable t) {
            Log.w(TAG, "Unable to open repository page", t);
        }
    }

    private static Preference createButtonPreference(Context context) {
        try {
            Class<?> btnClass = Class.forName("app.morphe.extension.twitter.settings.widgets.ButtonPref");
            Constructor<?> ctor = btnClass.getConstructor(Context.class);
            return (Preference) ctor.newInstance(context);
        } catch (Throwable ignored) {
        }
        return new Preference(context);
    }

    private static CharSequence coloredIncluded(String text) {
        Spannable span = new SpannableString(text);
        String inc = Strings.included();
        int idx = text.indexOf(inc);
        if (idx >= 0) {
            span.setSpan(new ForegroundColorSpan(Color.parseColor(INCLUDED_COLOR)), idx, idx + inc.length(), 0);
        }
        return span;
    }
}
