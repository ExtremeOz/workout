package com.a_track_it.workout.common;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import java.util.Optional;

public class VectorDrawableUtils {

    /**
     * Gets a Bitmap from provided Vector Drawable image
     *
     * @param vd VectorDrawable
     * @return Bitmap
     */
    public static Optional<Bitmap> createBitmapFromVectorDrawable(final @NonNull Drawable vd, final int dimenRes) {
        try {
            Bitmap bitmap;
          //  bitmap = Bitmap.createBitmap(vd.getIntrinsicWidth(), vd.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            bitmap = Bitmap.createBitmap(dimenRes, dimenRes, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vd.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vd.draw(canvas);
            return Optional.of(bitmap);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Loads vector drawable and apply tint color on it.
     */
/*    public static Drawable loadVectorDrawableWithTintColor(final @DrawableRes int vdRes,
                                                           final @ColorRes int clrRes, final Context context) {
        Drawable drawable = ContextCompat.getDrawable(context, vdRes);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(context,clrRes));
        return drawable;
    }*/

    /**
     * Converts given vector drawable to Bitmap drawable
     */
    public static BitmapDrawable convertVectorDrawableToBitmapDrawable(final @NonNull Drawable vd, final @NonNull int dimenRes, final @NonNull Context context) {
        //it is safe to create empty bitmap drawable from null source
        return new BitmapDrawable(context.getResources(), createBitmapFromVectorDrawable(vd,dimenRes).get());
    }

    /**
     * Loads vector drawable , aplys tint on it and returns a wrapped bitmap drawable.
     * Bitmap drawable can be resized using setBounds method (unlike the VectorDrawable)
     * @param context Requires view context !
     */
    public static Drawable loadVectorDrawableWithTint(
            final @DrawableRes int vectorDrawableRes, final @ColorRes int colorRes,final @DimenRes int dimenRes, final @NonNull Context context) {
        Drawable vd = ContextCompat.getDrawable(context, vectorDrawableRes);
        final int dimen = context.getResources().getDimensionPixelSize(dimenRes);
        final BitmapDrawable bitmapDrawable = VectorDrawableUtils.convertVectorDrawableToBitmapDrawable(vd,dimen,context);
        ColorStateList tint = ContextCompat.getColorStateList(context,colorRes);
        final Drawable wrappedDrawable = DrawableCompat.wrap(bitmapDrawable);
        DrawableCompat.setTintList(wrappedDrawable,tint);
        return wrappedDrawable;
    }
}
