package com.ppp.plouik;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;

/**
 * A simple painted path in Sketchbook
 * @author Pouf-Pouf Production
 *
 */
public class SketchbookPath extends Path {
	
	private class Point implements java.lang.Comparable<Point> {
		final public float 	x;
		final public float 	y;
		public float 		size;
		public float 		opacity;
		Point(float _x, float _y, float _size, float _opacity) { x=_x; y=_y; size=_size; opacity=_opacity; }
		public int compareTo(Point another) {
			return (int) ((opacity-another.opacity)*100);
		}
	}
	
	final private Paint				mPaint;					// The real paint for the main picture
	final private Paint				mPaintMode;				// the mode paint
	final private float				mHardness;				// The hardness stroke (useful in a zoom change)
	final private float				mSize;					// The real stroke size
	final private float				mSmudge;				// The smudge value
	final private float				mLength;				// The smudge length
	final private int				mColor;					// The paint color
	final private Paint				mPaintSrcRT;			// Draw the Src in realtime
	final private SketchbookStamp	mStamp;					// The stamp drawer
	
	final private int				LENGTH = 20;
	
	private Paint					mPaintZoom;				// The real-time paint for zoomed picture (change with zoom)
	
	private Path					mPathClip;				// The clip path for optimizing the drawing
	private float					mX;						// The current X-position of the path (why can't we get it from this?)
	private float					mY;						// The current Y-position of the path
	private float					mZSize;					// The current size correction of the path
	private float					mZOpacity;				// The current opacity correction of the path
	private long					mMilliSeconds;			// The milliseconds timestamp
	private float					mFirstX;				// The first X-position of the path
	private float					mFirstY;				// The first Y-position of the path
	private float					mDist;					// The whole path distance
	private float					mSpeed;					// The stroke speed
	
	private boolean					mStop;
	private	ArrayList<Integer>		mColors;				// The colors
	private ArrayList<Float>		mPositions;				// The positions
	private ArrayList<Point>		mPoints;				// The points
	
	public Paint					getPaintMode()			{ return mPaintMode; }
	
	private int						mSmudgeColor;			// The smudge color
	
	SketchbookPath(SketchbookNavigator _nav)
	{
		super();
		
		// Some saves
		mHardness 	= BrushDialog.tool?BrushDialog.hardness:1f;
		mSize 		= Math.max(0.5f,BrushDialog.size/2);
		mSmudge		= BrushDialog.smudge;
		mLength		= 0.99f*BrushDialog.length/Plouik.mStrokeSizeMax;
		mColor		= SketchbookData.getForeGround();
		mStamp		= new SketchbookStamp();
		mStamp.setStamp(BrushDialog.stamp);
		
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setARGB(Color.alpha(mColor),Color.red(mColor), Color.green(mColor), Color.blue(mColor));
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(Math.max(0.5f, mHardness*mSize*(BrushDialog.tool?BrushDialog.pencil.getSize():1f))*2);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setXfermode(new PorterDuffXfermode((BrushDialog.mode==BrushDialog.PaintMode.ERASE)?
				PorterDuff.Mode.MULTIPLY:PorterDuff.Mode.DARKEN));
			
		// The paint used in real time (depending on zoom value)
		mPaintZoom = new Paint(mPaint);
		mPaintZoom.setStrokeWidth(mPaint.getStrokeWidth()*_nav.getZoom());
		if ((BrushDialog.tool) && (mHardness<1.0f)) {
			if ((1.0f-mHardness)*mSize>1.0f) {
				mPaint.setMaskFilter(new BlurMaskFilter((1.0f-mHardness)*mSize, BlurMaskFilter.Blur.NORMAL));
				mPaintZoom.setMaskFilter(new BlurMaskFilter((1.0f-mHardness)*mSize*_nav.getZoom(), BlurMaskFilter.Blur.NORMAL));
			}
		}
		
		// A mode paint
		mPaintMode = new Paint();
		mPaintMode.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.valueOf(BrushDialog.mode.getName())));
		mPaintMode.setColorFilter(null);
		mPaintMode.setFilterBitmap(true);
		// In erase mode, alpha is in the mPaint color
		mPaintMode.setAlpha((BrushDialog.mode==BrushDialog.PaintMode.ERASE)?255:BrushDialog.opacity);
		
		// The clip path
		mPathClip = new Path();
		
		// The src paint for real time
		mPaintSrcRT = new Paint();
		mPaintSrcRT.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
		
		// Initialize the list of points
		mPoints = new ArrayList<Point>();
	}
	
	private void setGradient(int color, float position) {
		if (position==0f) { mColors = new ArrayList<Integer>(); mPositions = new ArrayList<Float>(); }
		mColors.add(color); mPositions.add(position);
	}
	
	private float getPressure(float _z) {
		float ret = _z;
		float range = ToolsDialog.pressure_max - ToolsDialog.pressure_min;
		if (range>0) { ret = (_z-ToolsDialog.pressure_min)/range; }	
		if (ret<0) { ret=0; }
		if (ret>1f) { ret=1f; }
		return ret;
	}
	
	public void moveTo(float x, float y, float z, int color) {
		reset(); super.moveTo(x,y); super.lineTo(x+0.01f, y+0.01f);
		mPathClip.addRect(x-mSize, y-mSize,	x+mSize, y+mSize, Path.Direction.CCW);
		z = getPressure(z);
		mX = x; mY = y;
		mZSize = (BrushDialog.size_fadein>0f)?0f:((BrushDialog.size_pressure>0f)?1f+(BrushDialog.size_pressure/100)*(z-1f):1f);
		mZOpacity = (BrushDialog.opacity_fadein>0f)?0f:((BrushDialog.opacity_pressure>0f)?1f+(BrushDialog.opacity_pressure/100)*(z-1f):1f);
		mFirstX = x; mFirstY = y;
		mDist = 0; mSpeed = 0;
		mMilliSeconds = System.currentTimeMillis();
		mStop = false;
		
		// SMUDGE
		if (mSmudge>0) {
			mSmudgeColor = Color.argb((int)(255*(1.0f-mSmudge)), 
						(int)(Color.red(mColor)*(1.0f-mSmudge) + Color.red(color)*mSmudge),
						(int)(Color.green(mColor)*(1.0f-mSmudge) + Color.green(color)*mSmudge),
						(int)(Color.blue(mColor)*(1.0f-mSmudge) + Color.blue(color)*mSmudge));
			setGradient(mSmudgeColor, 0f);
			setGradient(mSmudgeColor, 0.1f);
		}
		
		mPoints.add(new Point(mX, mY, mZSize, mZOpacity));
		
	}
	
	public void lineTo(float x, float y, float z, int color) {
		// Compute dynamics
		z = getPressure(z);
		
		float n2 = (float) Math.sqrt( (x-mX)*(x-mX) + (y-mY)*(y-mY));
		long delta = System.currentTimeMillis() - mMilliSeconds;
		mDist+=n2;
		
		float correction=.5f;
		float speed = Math.min(1f,n2/(10*delta));
		mSpeed = mSpeed*correction + speed*(1f-correction);
		
		float size=1f;
		if (!BrushDialog.tool) {
			size*= Math.min(1.0f, (mDist/(BrushDialog.size_fadein*mSize*LENGTH)));		// FADEIN CORRECTION
			size*= 1f - mSpeed * (BrushDialog.size_speed/100f);
			size*= 1f+(BrushDialog.size_pressure/100)*(z-1f);
			if (size<0.5f/mSize) { size = .5f/mSize; }
		}
		float threshold = (float) Math.log10(Math.pow(mSize*size, 2));
		threshold=(threshold<1f)?1f:threshold;
		threshold=.5f - (threshold-1f)*(.45f/3f);
		
		float opacity=1f;
		opacity*= Math.min(1.0f, (mDist/(BrushDialog.opacity_fadein*mSize*LENGTH)));		// FADEIN CORRECTION
		opacity*= 1f - mSpeed * (BrushDialog.opacity_speed/100f);
		opacity*= 1f+(BrushDialog.opacity_pressure/100)*(z-1f);
		opacity*=opacity;
		if (opacity<.01f) { opacity = .01f; }
		
		if ((n2>1f) && (n2/(mSize*size)>threshold) && (!mStop)) {
			// SMUDGE
			if (mSmudge>0) {
				float [] v2 = { (x-mX)/n2, (y-mY)/n2 };
				float n1 = (float) Math.sqrt( (x-mFirstX)*(x-mFirstX) + (y-mFirstY)*(y-mFirstY));
				float [] v1 = { (x-mFirstX)/n1, (y-mFirstY)/n1 };
				float scal = v1[0]*v2[0] + v1[1]*v2[1];
				mStop=(scal<0.5);
				
				int c = Color.rgb( (int)(Color.red(mColor)*(1.0f-mSmudge) + Color.red(color)*mSmudge),
						(int)(Color.green(mColor)*(1.0f-mSmudge) + Color.green(color)*mSmudge),
						(int)(Color.blue(mColor)*(1.0f-mSmudge) + Color.blue(color)*mSmudge));
				
				double alpha = Math.pow((double)(mLength), (double) (n2/10.f));
				mSmudgeColor = Color.argb(
						(int)(255*(1.0f - alpha) + Color.alpha(mSmudgeColor)*alpha),
						(int)(Color.red(c)*(1.0f-alpha) + Color.red(mSmudgeColor)*alpha),
						(int)(Color.green(c)*(1.0f-alpha) + Color.green(mSmudgeColor)*alpha),
						(int)(Color.blue(c)*(1.0f-alpha) + Color.blue(mSmudgeColor)*alpha));

				setGradient(mSmudgeColor, n1);
			}
			
			int ratio = (int) Math.floor(1.0+(n2/(mSize*size)));
			for (int i=0; i<ratio; i++) {
				mPoints.add(new Point((mX*i+x*(ratio-i))/ratio, (mY*i+y*(ratio-i))/ratio,
								(mZSize*i+size*(ratio-i))/ratio, (mZOpacity*i+opacity*(ratio-i))/ratio));
			}
			
			super.lineTo(x, y);
			
			mPathClip.addRect((mX>x?x:mX)-mSize-1, (mY>y?y:mY)-mSize-1, (mX>x?mX:x)+mSize+1, (mY>y?mY:y)+mSize+1, Path.Direction.CCW);
			mX = x; mY = y; mZSize = size; mZOpacity=opacity;
			mMilliSeconds+=delta;
		}
	}
	
	/** The real-time (that means in screen size) draw */
	public void draw(Canvas _canvas, Bitmap _source, SketchbookNavigator _nav, Context _context) {
		if (!this.isEmpty() && !mPathClip.isEmpty()) {
			
			// Prepare the clip path and its bounds
			Path vClipPath = new Path();
			RectF vBounds = new RectF();
			vClipPath.addPath(mPathClip);
			vClipPath.transform(_nav.getMatrix());
			vClipPath.computeBounds(vBounds, true);
			
			// Compute the smudge shader
			if ((mSmudge>0) && (mColors.size()>1) && (BrushDialog.mode!=BrushDialog.PaintMode.ERASE)) {
				float[] pts = { mFirstX, mFirstY , mX, mY };
				_nav.getMatrix().mapPoints(pts);
				int[] c = new int[mColors.size()]; float[] p = new float[mPositions.size()];
				for (int i=0; i<mColors.size(); i++) { c[i]=mColors.get(i); p[i]=mPositions.get(i)/mPositions.get(mPositions.size()-1); }
				LinearGradient l = new LinearGradient(pts[0], pts[1], pts[2], pts[3], c, p, Shader.TileMode.CLAMP);
				Matrix m = new Matrix();
				m.setTranslate(-vBounds.left, -vBounds.top);
				l.setLocalMatrix(m);
				mPaintZoom.setShader(l);
			}
			
			try {
				// Build the prepare bitmap
				Bitmap vBitmap = Bitmap.createBitmap((int)(vBounds.right-vBounds.left+1), (int)(vBounds.bottom-vBounds.top+1),Bitmap.Config.ARGB_8888);
				Canvas vPrepare = new Canvas(vBitmap);
				vPrepare.drawColor(SketchbookData.getBackGround(), PorterDuff.Mode.DST_ATOP);
				
				_canvas.clipPath(vClipPath, Region.Op.REPLACE);
				_canvas.drawBitmap(_source, 0, 0, mPaintSrcRT);
				
				// Prepare the drawing
				if (BrushDialog.tool) {
					Path vPathTmp = new Path(this);
					vPathTmp.transform(_nav.getMatrix());
					vPathTmp.offset(-vBounds.left, -vBounds.top);
					for (int i=0; i<BrushDialog.pencil.getDots().length; i++) {
						Path vPath = new Path(vPathTmp);
						vPath.offset(BrushDialog.pencil.getDots()[i][0]*mSize*_nav.getZoom(),BrushDialog.pencil.getDots()[i][1]*mSize*_nav.getZoom());
						vPrepare.drawPath(vPath, mPaintZoom);
					}
	
				}
				else {
					// Draw the path with black and white bitmap
					for (int i=0; i<mPoints.size(); i++) {
						mStamp.draw(vPrepare, (mPoints.get(i).x-_nav.getPictureOffset()[0])*_nav.getZoom()-vBounds.left,
											  (mPoints.get(i).y-_nav.getPictureOffset()[1])*_nav.getZoom()-vBounds.top,
											  mPoints.get(i).size,
											  mPoints.get(i).opacity, mPaintZoom, i);
					}
				}

				_canvas.drawBitmap(vBitmap, vBounds.left, vBounds.top, getPaintMode());
			}
			catch (java.lang.OutOfMemoryError e) { }
			mPathClip.reset();
		}
	}
	
	/** The 'after release' draw (on real size) */
	public void draw(Canvas _canvas, int _left, int _top, Context _context){
		
		// Compute the smudge shader
		if ((mSmudge>0) && (mColors.size()>1) && (BrushDialog.mode!=BrushDialog.PaintMode.ERASE)) {
			int[] c = new int[mColors.size()]; float[] p = new float[mPositions.size()];
			for (int i=0; i<mColors.size(); i++) { c[i]=mColors.get(i); p[i]=mPositions.get(i)/mPositions.get(mPositions.size()-1); }
			LinearGradient l = new LinearGradient(mFirstX-_left, mFirstY-_top, mX-_left, mY-_top, c, p, Shader.TileMode.CLAMP);
			mPaint.setShader(l);
		}
		
		if (BrushDialog.tool) {
			// Android native path API
			for (int i=0; i<BrushDialog.pencil.getDots().length; i++) {
				Path vPath = new Path(this);
				vPath.offset(-_left + BrushDialog.pencil.getDots()[i][0]*mSize,-_top+ BrushDialog.pencil.getDots()[i][1]*mSize);
				_canvas.drawPath(vPath, mPaint);
			}
		}
		else {
			
			// Post-compute the fade out stuff
			if (BrushDialog.size_fadeout>0f || BrushDialog.opacity_fadeout>0f) {
				float distance = 0;
				if (BrushDialog.size_fadeout>0f) 	{ mPoints.get(mPoints.size()-1).size = 0f; }
				if (BrushDialog.opacity_fadeout>0f) { mPoints.get(mPoints.size()-1).opacity = 0f; }
				for (int i=mPoints.size()-2; i>=0; i--) {
					distance += Math.sqrt( ((mPoints.get(i+1).x-mPoints.get(i).x)*(mPoints.get(i+1).x-mPoints.get(i).x)) +
										   ((mPoints.get(i+1).y-mPoints.get(i).y)*(mPoints.get(i+1).y-mPoints.get(i).y)) );
					
					if (BrushDialog.size_fadeout>0f) {
						mPoints.get(i).size*= Math.min(1.0f, (distance/(BrushDialog.size_fadeout*mSize*LENGTH)));
					}
					if (BrushDialog.opacity_fadeout>0f) {
						mPoints.get(i).opacity*= Math.min(1.0f, (distance/(BrushDialog.opacity_fadeout*mSize*LENGTH)));
					}
					
				}
			}
			
			// Draw the path with stamps (sort the points by opacity)
			for (int i=0; i<mPoints.size(); i++) {
				mStamp.draw(_canvas, mPoints.get(i).x-_left,
							mPoints.get(i).y-_top, mPoints.get(i).size,
							mPoints.get(i).opacity, mPaint, i);
			}
			
			mStamp.release();

		}
	
		mPathClip.reset();
		mPoints.clear();
	}
	
	public float getStrokeWidth() { return mSize; }

	public void onChange(SketchbookNavigator _nav) {
		mPaintZoom.setStrokeWidth(mPaint.getStrokeWidth()*_nav.getZoom());
		if (mHardness<1.0f) {
			mPaintZoom.setMaskFilter(new BlurMaskFilter(
				(1.0f-mHardness)*mSize*_nav.getZoom(), BlurMaskFilter.Blur.NORMAL));
		}
	}
	
}

