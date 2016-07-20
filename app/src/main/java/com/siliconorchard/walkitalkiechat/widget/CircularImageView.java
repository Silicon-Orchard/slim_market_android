package com.siliconorchard.walkitalkiechat.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by adminsiriconorchard on 9/28/15.
 */

public class CircularImageView extends ImageView {
    Context mContext;

    public CircularImageView(Context context) {
        super(context);
        mContext = context;
    }

    public CircularImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

    public CircularImageView(Context context, AttributeSet attrs,
                             int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if(bm==null) return;
        setImageDrawable(new BitmapDrawable(mContext.getResources(),
                getCircularBitmap(bm)));
    }

    /**
     * Creates a circular bitmap and uses whichever dimension is smaller to determine the width
     * Also constrains the circle to the leftmost part of the image
     *
     * @param bitmap
     * @return bitmap
     */
    public Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output;
        Bitmap src;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width > height) {
            output = Bitmap.createBitmap(height, height, Config.ARGB_8888);
            src = Bitmap.createBitmap(bitmap, (width - height) / 2, 0, height, height);
        } else {
            output = Bitmap.createBitmap(width, width, Config.ARGB_8888);
            src = Bitmap.createBitmap(bitmap, 0, (height - width) / 2, width, width);
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();


        float r = 0;

        if (width > height) {
            r = height / 2;
        } else {
            r = width / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

        final Rect rect = new Rect(0, 0, (int) r*2, (int) r*2);
        canvas.drawBitmap(src, rect, rect, paint);
        return output;
    }

}