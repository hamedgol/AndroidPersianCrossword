/**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2009-2010 Robert Cooper
 * Copyright (C) 2013 Adam Rosenfield
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.haw3d.jadvalKalemat.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;


public class VerticalProgressBar extends View {
    private static final int GRAY = Color.rgb(49, 49, 49);
    private static final int ORANGE = Color.rgb(213, 165, 24);
    private static final int GREEN = Color.rgb(49, 145, 90);
    private static final int RED = Color.rgb(255, 74, 77);
    private int height;
    private int percentComplete;
    private int width;

    private Paint paint = new Paint();

    public VerticalProgressBar(Context context) {
        super(context);
    }

    public VerticalProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setPercentComplete(int percentComplete) {
        this.percentComplete = percentComplete;
        invalidate();
    }

    public int getPercentComplete() {
        return percentComplete;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(GRAY);
        paint.setStyle(Style.FILL);

        if (this.percentComplete < 0) {
            canvas.drawRect(0, 0, this.width, this.height, paint);
        } else if (this.percentComplete == 0) {
            paint.setColor(RED);
            paint.setStyle(Style.FILL);
            canvas.drawRect(0, 0, this.width, this.height, paint);
        } else if (this.percentComplete == 100) {
            paint.setColor(GREEN);
            paint.setStyle(Style.FILL);
            canvas.drawRect(0, 0, this.width, this.height, paint);
        } else {
            float orangeHeight = ((float) this.percentComplete / 100f) * (float) this.height;

            float grayHeight = (float) this.height - orangeHeight;

            canvas.drawRect(0, 0, this.width, grayHeight, paint);
            paint.setColor(ORANGE);
            paint.setStyle(Style.FILL);
            canvas.drawRect(0, grayHeight, this.width, this.height, paint);
        }

        paint.setColor(Color.BLACK);
        paint.setStyle(Style.FILL);
        canvas.drawLine(0, this.height, this.width, this.height, paint);
        canvas.drawLine(0, 0, this.width, 0, paint);
    }

    @Override
    protected void onMeasure(int widthSpecId, int heightSpecId) {
        this.height = View.MeasureSpec.getSize(heightSpecId);
        this.width = View.MeasureSpec.getSize(widthSpecId);
        setMeasuredDimension(this.width, this.height);
    }
}
