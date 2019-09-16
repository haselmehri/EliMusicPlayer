package haselmehri.app.com.elimusicplayer;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;

import androidx.annotation.NonNull;

public class CustomTypefaceSpan extends TypefaceSpan {
    private final Typeface newType;
    private Context context;

    CustomTypefaceSpan(Context context, String family, Typeface type) {
        super(family);
        newType = type;
        this.context=context;
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        applyCustomTypeFace(context, ds, newType);
    }

    @Override
    public void updateMeasureState(@NonNull TextPaint paint) {
        applyCustomTypeFace(context, paint, newType);
    }

    private static void applyCustomTypeFace(Context context, Paint paint, Typeface tf) {
        int oldStyle;
        Typeface old = paint.getTypeface();
        if (old == null) {
            oldStyle = 0;
        } else {
            oldStyle = old.getStyle();
        }

        int fake = oldStyle & ~tf.getStyle();
        if ((fake & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(true);
        }

        if ((fake & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(-0.25f);
        }

        paint.setTextSize(context.getResources().getDimension(R.dimen.normal_font_size));
        paint.setTypeface(tf);
    }
}

