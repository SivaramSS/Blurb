package com.flair.blurb;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by sivaram-3911 on 19/01/17.
 */

public class RobotoTextView extends TextView {

    public RobotoTextView(Context context) {
        super(context);
//        Typeface face = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");// No I18N
//        this.setTypeface(face);
    }

    public RobotoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        TypedArray attrDetails = context.obtainStyledAttributes(attrs, R.styleable.RobotoTextView);
//        String val = attrDetails.getString(R.styleable.RobotoTextView_typeface);
//        if (val == null) {
//            val = "Regular"; // No I18N
//        }
//        String fontName = "fonts/Roboto-" + val + ".ttf"; // No I18N
//        attrDetails.recycle();
//        setFont(context, "fonts/Roboto-Light.ttf");
    }

    protected void setFont(Context context, String fontName) {
        Typeface face = Typeface.createFromAsset(context.getAssets(), fontName);
        setTypeface(face);
    }

}
