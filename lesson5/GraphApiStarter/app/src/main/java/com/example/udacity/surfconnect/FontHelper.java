package com.example.udacity.surfconnect;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class FontHelper {
    private static final String CUSTOM_FONT = "fonts/avenir-black.ttf";
    private static Typeface customTypeface;

    /*
     * Set custom typeface on a View, or all Views in a given ViewGroup
     */
    public static void setCustomTypeface(View v) {
        // load the font and store as a static member
        if (customTypeface == null) {
            customTypeface = Typeface.createFromAsset(v.getContext().getAssets(), CUSTOM_FONT);
        }

        if (v instanceof TextView) {
            ((TextView) v).setTypeface(customTypeface);
        }
        else if (v instanceof EditText) {
            ((EditText) v).setTypeface(customTypeface);
        }
        else if (v instanceof Button) {
            ((Button) v).setTypeface(customTypeface);
        }
        else if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                setCustomTypeface(child);
            }
        }
    }
}
