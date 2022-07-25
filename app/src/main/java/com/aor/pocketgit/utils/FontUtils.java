package com.aor.pocketgit.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Map;

public class FontUtils {
    private static Map<String, String> fontMap = new HashMap();
    private static Typeface sSourceCode;
    private static Map<String, Typeface> typefaceCache = new HashMap();

    public interface FontTypes {
        public static final String BOLD = "BOLD";
        public static final String ITALIC = "ITALIC";
        public static final String REGULAR = "REGULAR";
    }

    static {
        fontMap.put(FontTypes.REGULAR, "fonts/RobotoCondensed-Regular.ttf");
        fontMap.put(FontTypes.BOLD, "fonts/RobotoCondensed-Bold.ttf");
        fontMap.put(FontTypes.ITALIC, "fonts/RobotoCondensed-Italic.ttf");
    }

    private static Typeface getRobotoTypeface(Context context, String fontType) {
        String fontPath = fontMap.get(fontType);
        if (!typefaceCache.containsKey(fontType)) {
            typefaceCache.put(fontType, Typeface.createFromAsset(context.getAssets(), fontPath));
        }
        return typefaceCache.get(fontType);
    }

    private static Typeface getRobotoTypeface(Context context, Typeface originalTypeface) {
        String robotoFontType = FontTypes.REGULAR;
        if (originalTypeface != null) {
            switch (originalTypeface.getStyle()) {
                case 1:
                    robotoFontType = FontTypes.BOLD;
                    break;
                case 2:
                    robotoFontType = FontTypes.ITALIC;
                    break;
            }
        }
        return getRobotoTypeface(context, robotoFontType);
    }

    public static Typeface getSourceCodeTypeface(Context context) {
        if (sSourceCode == null) {
            sSourceCode = Typeface.createFromAsset(context.getAssets(), "fonts/source-code-pro.ttf");
        }
        return sSourceCode;
    }

    public static void setRobotoFont(Context context, View view) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_roboto_font", true)) {
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    setRobotoFont(context, ((ViewGroup) view).getChildAt(i));
                }
            } else if (view instanceof TextView) {
                ((TextView) view).setTypeface(getRobotoTypeface(context, ((TextView) view).getTypeface()));
                ((TextView) view).setPaintFlags(((TextView) view).getPaintFlags() | 128);
            }
        }
    }
}
