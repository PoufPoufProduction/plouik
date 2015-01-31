package com.ppp.plouik;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 
 * @author Pouf-Pouf Production
 *
 */
public class ColorPickerView extends View {
	/** The log TAG */
	static final String TAG = "ColorPickerView";
	
	static public interface ColorPickerListener {
		public void onChange(float[] hsv, int eventId);
	}
	
	private interface ColorWidget {
		public void onDraw(Canvas canvas);
	}
	
	/**
	 * The Hue color circle
	 * @author Pouf-Pouf Production
	 *
	 */
	private class HueCircle implements ColorWidget{
		
		/** The radius circle */
		private int					mRadius;
		
		/** The paint for the circle, mainly filled by a sweep gradient shader */
		private Paint				mCirclePaint;
		
		/** The paint for the pointer when user has clicked on the circle */
		private Paint				mPointerPaint;
		
		/** The pointer coordinates */
		private float				mAngle = 0;
		private float				mX = 0;
		private float				mY = 0;
		
		/**
		 * Hue circle constructor. Build the gradient and prepare the paint object
		 * @param radius is the hue circle radius
		 * @param stroke is the hue circle stroke, obviously
		 */
		public HueCircle(int radius, int stroke) {
			final int			NBCOLORS = 10;
			final int[]			mColors;
			
			// Create the colors of the circle by using a sweep gradient
			mColors = new int[NBCOLORS+1];
			for (int i=0; i<NBCOLORS; i++) {
				float[] hsv = new float[] { (360.0f*i/NBCOLORS), 1.0f, 1.0f };
				mColors[i] = Color.HSVToColor(hsv);
			}
			mColors[NBCOLORS]=Color.RED;
			Shader s = new SweepGradient(0, 0, mColors, null);
			
			mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mCirclePaint.setShader(s);
			mCirclePaint.setStyle(Paint.Style.STROKE);
			setRadiusNStroke(radius, stroke);

	        mPointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	        mPointerPaint.setColor(Color.WHITE);
	        mPointerPaint.setStyle(Paint.Style.STROKE);
	        mPointerPaint.setStrokeWidth(STROKE_POINTER);
		}
		
		/**
		 * Set the initial parameters in the case of the 
		 * default settings are not correct
		 * @param radius
		 * @param stroke
		 */
		public void setRadiusNStroke(int radius, int stroke) {
			// Set the circle radius
			mRadius = radius;
			
			// Set the stroke
			mCirclePaint.setStrokeWidth(stroke);
		}
		
		/**
		 * Set the angle and the absolute position too
		 * @param angle
		 */
		public void setAngle(float angle) {
			mAngle = angle;
			mX = (float) (java.lang.Math.cos(mAngle) * (mRadius - mCirclePaint.getStrokeWidth()*0.5f));
        	mY = (float) (java.lang.Math.sin(mAngle) * (mRadius - mCirclePaint.getStrokeWidth()*0.5f));
		}
		
		/**
		 * Return the angle in radian [-PI, PI]
		 * @return the angle of the hue circle in radian
		 */
		public float getAngle() { return mAngle; }
		
		/**
		 * Clear the circle value
		 */
		public void clear() { mAngle = mX = mY = 0; }
		
		/**
		 * Return the angle in degrees [0, 360]
		 * @return the angle of the hue circle in degrees
		 */
		public float getDegAngle() {
			float angle = (mAngle/PI)*180;
    		if (angle<0) { angle+=360.0f; }
    		return angle;
		}
		
		/**
		 * Has a Hue be already chosen ? 
		 * @return true if a hue is clicked
		 */
		public boolean isOK() { return mX!=0 || mY!=0; }

		/**
		 * Draw the circle and eventually the pointer
		 * @param canvas is the canvas
		 */
		public void onDraw(Canvas canvas) {
			float r = RADIUS - mCirclePaint.getStrokeWidth()*0.5f;
	        canvas.drawOval(new RectF(-r, -r, r, r), mCirclePaint);
	        
	        if (mX!=0 || mY!=0) {
	        	canvas.drawOval(new RectF( -RADIUS_POINTER+mX, -RADIUS_POINTER+mY, RADIUS_POINTER+mX, RADIUS_POINTER+mY), mPointerPaint);
	        }
		}
		
		/**
		 * Check if the pointer is inside the circle
		 * @param x is the relative X-axis position
		 * @param y is the relative Y-axis position
		 * @return true if the pointer coordinates are inside the circle
		 */
		public boolean isInside(float x, float y) {
			float rExt = RADIUS;
			float rInt = RADIUS - mCirclePaint.getStrokeWidth() + A_LITTLE_BIT;
			return (x*x + y*y <= rExt*rExt) && (x*x + y*y >= rInt*rInt);
		}
	}
	
	private class SVTriangle implements ColorWidget{

		/** The triangle paint */
		private Paint						mTrianglePaint = null;
		
		/** The pointer paint */
		private Paint						mPointerPaint = null;
		
		/** The triangle matrix for rotating the stuff */
	    private Matrix						mTriangleMatrix = null;
	    
	    /** The color matrix which is used for updating the hue regarding the heu circle */
	    private ColorMatrix					mTriangleColorMatrix = null;
	    
	    /** The triangle bitmap */
	    private Bitmap						mTriangleBitmap = null;
	    
	    /** The triangle coordinates */
	    private boolean						mIsOK = false;
	    private float						mRelX = 0;
	    private float						mRelY = 0;
	    private float						mAbsX = 0;
	    private float						mAbsY = 0;
	    
	    /** The radius circle */
		private int							mRadius;
		
		/** Sqrt3 for avoiding calculation */
		private final float					SQRT3 = (float) java.lang.Math.sqrt(3);
		
		/** Some useful stuffs */
		private final Context				mContext;
		private final int					mResId;
		
		/**
		 * Clear the SV triangle
		 */
		public void clear() {
			mIsOK = false;
			mRelX = mRelY = mAbsX = mAbsY = 0;
		}
	    
	    public SVTriangle(Context context, int resId, int radius) {
	    	mContext = context;
	    	mResId = resId;
	    	
	    	mTriangleMatrix = new Matrix();
	        mTriangleColorMatrix = new ColorMatrix();
	        mTrianglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	        mPointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	        mPointerPaint.setColor(Color.WHITE);
	        mPointerPaint.setStyle(Paint.Style.STROKE);
	        mPointerPaint.setStrokeWidth(STROKE_POINTER);
	        
	        setRadius(radius);
	    }
	    
	    /**
	     * Initialize the radius and the triangle Bitmap
	     * @param radius
	     */
	    public void setRadius(int radius) {
	    	mRadius = radius;
	 
	    	Bitmap bitmapTmp = BitmapFactory.decodeResource(mContext.getResources(), mResId);
	        if (bitmapTmp!=null) {
	        	mTriangleBitmap = Bitmap.createScaledBitmap(bitmapTmp, 2*mRadius, 2*mRadius, false);
	        	bitmapTmp.recycle();
	        }
	        
	        update(0);
	    }
	    
	    /**
	     * Update the color Triangle regarding the angle provided by the Hue circle
	     * @param rad the rotation angle
	     */
	    private void update(float rad) {
	    	
	    	// Position
	    	mTriangleMatrix.setTranslate(-mRadius, -mRadius);
	    	mTriangleMatrix.postRotate(180*rad/PI);
	    	
	    	// Hue of the triangle
	        ColorMatrix tmp = new ColorMatrix();

	        mTriangleColorMatrix.setRGB2YUV();
	        tmp.setRotate(0, -180*rad/PI);
	        mTriangleColorMatrix.postConcat(tmp);
	        tmp.setYUV2RGB();
	        mTriangleColorMatrix.postConcat(tmp);

	        mTrianglePaint.setColorFilter(new ColorMatrixColorFilter(mTriangleColorMatrix));
	        
	        // pointer position
	        Rel2Abs(rad);
	    }
	    
	    /**
	     * Return the value from the triangle (quite the same as getRelRed)
	     * @return the value as float [0..1]
	     */
	    public float getValue() {
	    	return Math.min(1, Math.max(0, (2*mRadius + SQRT3*mRelY + mRelX)/(3*mRadius)));
	    }
	
	    /**
	     * Return the saturation from the triangle
	     * @return the saturation as float [0..1]
	     */
	    public float getSaturation() {
	    	return (mRelX + SQRT3*mRelY + 2*mRadius==0)?1.0f:Math.min(1, Math.max(0, (2*mRelX+mRadius)/(mRelX + SQRT3*mRelY + 2*mRadius)));
	    }
	    
	    /**
	     * Set the coordinates regarding the new saturation and value
	     * @param saturation is the new saturation value
	     * @param value is the new value value (?!?)
	     */
	    public void setSV(float saturation, float value, float rad) {
	    	mIsOK = true;
	    	mRelX = (3*saturation*value - 1) * (mRadius/2);
	    	mRelY = (2*value - value*saturation - 1) * ( SQRT3*mRadius/2);
	    	Rel2Abs(rad);
	    }
	    
	    /**
	     * Return true if we already clicked inside the triangle
	     * @return true if OK
	     */
	    public boolean isOK() {
	    	return mIsOK;
	    }
	    
	    /**
	     * Compute the absolute values from the relative position in the SV Triangle
	     * And evaluate the corresponding color
	     * @param rad
	     */
	    private void Rel2Abs(float rad) {
	    	mAbsX = (float) (java.lang.Math.cos(rad) * mRelX - java.lang.Math.sin(rad) * mRelY);
	        mAbsY = (float) (java.lang.Math.sin(rad) * mRelX + java.lang.Math.cos(rad) * mRelY);
	    }
	    
	    /**
	     * Compute the X/Y values to find if the current point is inside the triangle or not and what its
	     * relative coordinates if in.
	     * @param x is the x
	     * @param y is the y
	     * @param rad is the already computed angle in radiant
	     * @param inTriangle say the point is in the Triangle, even if not (because we
	     * begin a movement)
	     * @return true if the point is inside the triangle
	     */
	    public boolean computeCoor(float x, float y, float rad, boolean inTriangle) {
	    	boolean			value = true;
	    	
	    	if (inTriangle) {
	    		// If we re suppose to be in the triangle but we're not, we take care
	    		// to be in the including circle at least (because it will create some
	    		// issue during the orthogonal projection step)
	    		if (x*x+y*y>mRadius*mRadius) {
	    			float alpha = (float) java.lang.Math.sqrt(mRadius*mRadius/(x*x + y*y));
	    			x = x * alpha;
	    			y = y * alpha;
	    		}
	    	}
	    	else {
	    		// We are definitely not in the triangle
	    		if (x*x+y*y>mRadius*mRadius) {
	    			value = false;
	    		}
	    	}
	    	
	    	if (value) {
	    		// the point is inside the including circle and even if it is not really
	    		// in the SV Triangle, it is good
	    		mIsOK = true;
	    		
	    		mRelX = (float) (java.lang.Math.cos(rad) * x + java.lang.Math.sin(rad) * y);
	        	mRelY = (float) (-java.lang.Math.sin(rad) * x + java.lang.Math.cos(rad) * y);
	        	
	        	// If the point is not inside the triangle, find the closest point
	        	// by orthogonal projection
	        	if (mRelX < -mRadius/2) {
	        		mRelX = -mRadius/2;
	        	}
	        	if (mRelX + SQRT3*mRelY - mRadius > 0) {
	        		float alpha = ((mRadius/SQRT3) - (mRelX/SQRT3) - mRelY)/(mRadius+1);
	        		mRelX = mRelX + alpha * SQRT3;
	        		mRelY = mRelY + alpha * mRadius;
	        	}
	        	if (mRelX - SQRT3*mRelY - mRadius > 0) {
	        		float alpha = ((mRadius/SQRT3) - (mRelX/SQRT3) + mRelY)/(mRadius+1);
	        		mRelX = mRelX + alpha * SQRT3;
	        		mRelY = mRelY - alpha * mRadius;
	        	}
	        	
	        	// update the absolute coordinates to be able to display the pointer
	        	Rel2Abs(rad);
	    	}
	    	
	    	return value;
	    }
		
	    /**
	     * Draw the Value/Saturation triangle
	     * @param canvas is the canvas
	     */
		public void onDraw(Canvas canvas) {
			// Draw the bitmap. By the way, the test is mandatory for the eclipse layout editor
			if (mTriangleBitmap!=null && mTriangleMatrix!=null && mTrianglePaint!=null) {
				canvas.drawBitmap(mTriangleBitmap, mTriangleMatrix, mTrianglePaint);
			}
			
			if (mIsOK) {
	        	canvas.drawOval(new RectF(-RADIUS_POINTER+mAbsX,-RADIUS_POINTER+mAbsY,
	        			RADIUS_POINTER+mAbsX, RADIUS_POINTER+mAbsY),
	        					mPointerPaint);
	        }
		}
	}
	
	/** The hue circle */
	HueCircle							mHueCircle = null;
	
    /** The value/saturation triangle */
    SVTriangle							mSVTriangle = null;
    
    /** The listener */
    ColorPickerListener					mListener = null;
    
    /** Useful values */
    private final float			PI = 3.1415926f;
    
    private int					RADIUS = getContext().getResources().getDimensionPixelSize(R.dimen.colorPickerRadius);
    private int					STROKE = getContext().getResources().getDimensionPixelSize(R.dimen.colorPickerStroke);
    
    private final int			RADIUS_POINTER = 10;
    private final int			STROKE_POINTER = 2;
    
    private final int			A_LITTLE_BIT = 4;
    
    /**
     * The constructor of the color picker view
     * @param context
     * @param attrs
     */
    public ColorPickerView(Context context, AttributeSet attrs) {
    	super(context);
        mHueCircle = new HueCircle(RADIUS, STROKE);
        mSVTriangle = new SVTriangle(context, R.drawable.colorpicker, RADIUS-STROKE-A_LITTLE_BIT);
    }
    
    /**
     * Set the color change listener
     * @param listener the listener
     */
    public void setOnColorChange(ColorPickerListener listener) {
    	mListener = listener;
    }
    
    /**
     * Check if the color picker is OK
     * @return
     */
    public boolean isOk() { return (mHueCircle.isOK() && mSVTriangle.isOK()); }
    
    /**
     * Get the HSV Color
     * @return the HSV values as float[3]
     */
    public float[] getHSVColor() {  	
    	return (isOk()?new float[] {
    			mHueCircle.getDegAngle(),
    			mSVTriangle.getSaturation(),
    			mSVTriangle.getValue() }:new float[] {0f, 0f, 0f });
    }
    
    /**
     * Set the Hue circle and the S/V triangle regarding the provided color
     * @param color is a provided color
     */
    public void setColor(int color) {
    	if (color!=-1) {
	    	float[]		hsv = new float[3];
	    	
	    	Color.colorToHSV(color, hsv);
	    	setHSVColor(hsv[0], hsv[1], hsv[2]);
    	}
    }
    
    /**
     * Set the picker color from a HSV value
     * @param hue is the hue
     * @param saturation is the saturation
     * @param value is the value
     */
    public void setHSVColor(float hue, float saturation, float value) {
    	float		angle;
    	angle = hue/180*PI;
    	if (angle>PI) { angle-=2*PI; }
    	mHueCircle.setAngle(angle);
    	
    	mSVTriangle.update(mHueCircle.getAngle());
    	mSVTriangle.setSV(saturation, value, angle);
    	
    	if (mListener!=null) { mListener.onChange(new float[] { hue, saturation, value}, MotionEvent.ACTION_DOWN); }
    	//invalidate();
    }
    
    /**
     * Clear the picker view
     */
    public void clear() {
    	mHueCircle.clear();
    	mSVTriangle.clear();
    	mSVTriangle.update(mHueCircle.getAngle());
    	
    	if (mListener!=null) { mListener.onChange(getHSVColor(), MotionEvent.ACTION_CANCEL); }
    	invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        
        canvas.translate(RADIUS, RADIUS);
        
        // Draw the color circle
        // The no-null test is necessary for the eclipse layout editor
        if (mHueCircle!=null)		{ mHueCircle.onDraw(canvas); }
        
        // Draw the color triangle
        if (mSVTriangle!=null)		{ mSVTriangle.onDraw(canvas); }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(RADIUS*2, RADIUS*2);
    }

    static boolean eventInCircle = false;
    static boolean eventInTriangle = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float		x = event.getX() - RADIUS;
        float		y = event.getY() - RADIUS;
        boolean		inCircle = eventInCircle || !eventInTriangle && mHueCircle.isInside(x, y);
        
        if (inCircle) {
        	mHueCircle.setAngle((float)java.lang.Math.atan2(y, x));
        	mSVTriangle.update(mHueCircle.getAngle());
        }
        eventInCircle = inCircle;
        
        boolean		inTriangle = !eventInCircle &&
        						mSVTriangle.computeCoor(x, y, mHueCircle.getAngle(), eventInTriangle);
        eventInTriangle = inTriangle;
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            	if (mListener!=null) {
            		mListener.onChange(getHSVColor(), MotionEvent.ACTION_DOWN);
            	}
            	invalidate();
            	break;
            case MotionEvent.ACTION_MOVE:
            	if (mListener!=null) {
            		mListener.onChange(getHSVColor(), MotionEvent.ACTION_MOVE);
            	}
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                eventInCircle = false;
                eventInTriangle = false;
                invalidate();
                break;
        }
        return true;
    }
}
