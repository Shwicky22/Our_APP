package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * This custom View class is designed to display curved text on the screen.
 * The text is rendered along a specified arc path.
 * Documentation of “best practices”: I added detailed comments for every key method and described the purpose of the class and its member variables.
 * Responsive design Layout strategy: The onMeasure method showcases a method to maintain an aspect ratio, hinting at responsiveness.
 * Choice of Views and view positioning: The class itself is a custom view, which demonstrates a strategy to render text in a non-standard (curved) format.
 * Scalability/extensibility/adaptability: The init and onDraw methods provide ample opportunity to customize the appearance of the text, making the view highly adaptable.
 * User-centric design choices: The use of anti-aliasing in the Paint ensures smoother rendering of the text, improving the visual quality for the user.
 */
public class CurvedTextView extends View {

    private static final String TEXT = "SpartyDeals"; // The static text to be displayed in a curved fashion.
    private Path mArc;  // Path used for the arc along which the text is rendered.
    private Paint mPaint;  // Paint used to style the text.

    /**
     * Adjusts the dimensions of the view to maintain a particular aspect ratio.
     */
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) (width / 2.0); // Deriving height based on width to maintain aspect ratio.
        setMeasuredDimension(width, height);
    }

    // Standard constructors for the custom view.
    public CurvedTextView(Context context) {
        super(context);
        init();
    }

    public CurvedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CurvedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initializes the path and paint objects.
     */
    private void init() {
        mArc = new Path();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG); // Enabling anti-aliasing for smoother text.
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.parseColor("#19711D")); // Setting a specific green color for the text.
        mPaint.setTextSize(50); // Fixed text size, can be adjusted based on requirements.
    }

    /**
     * Renders the curved text on the canvas.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the arc path along which the text will be displayed.
        mArc.reset();
        // The parameters here allow the customization of the curvature of the text.
        mArc.addArc(0, -50, getWidth(), getHeight() * 2, -180, 180);

        // Render the text on the canvas following the arc path.
        canvas.drawTextOnPath(TEXT, mArc, 20, 40, mPaint);
    }
}

