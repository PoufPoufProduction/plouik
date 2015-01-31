package com.ppp.plouik;

import java.io.FileInputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

/**
 * 
 * @author Pouf-Pouf Production
 *
 */
public class SketchbookData {
	static final String 	TAG 		= "data";
	static final int		DEG90		= 1;
	static final int		DEG180		= 2;
	static final int		DEG270		= 3;
	static final int		HORIZ		= 1;
	static final int		VERT		= 2;
	
	/** The layer display definition */
	static final int		NOLAYER		= 0;
	static final int		LAYERUP		= 1;
	static final int		LAYERDOWN 	= 2;
	static final int		LAYERUPX	= 3;
	static final int		LAYERDOWNX	= 4;
	static final int[]		LAYERS 		= { R.drawable.desktoplayer, R.drawable.desktoplayerup, R.drawable.desktoplayerdown, R.drawable.desktoplayerupx, R.drawable.desktoplayerdownx };
	static int				LAYER		= NOLAYER;
	
	/** The picture Bitmap and its associated canvas*/
	private Canvas			mCanvas;					// Its the true canva (whose dimensions are the picture ones)
	private Bitmap			mBitmap;					// The associated bitmap
	private Canvas			mCanvasUp;					// The layer up canvas
	private Bitmap			mBitmapUp;					// The layer up bitmap
	private Bitmap			mBitmapOld;					// The old bitmap
	private int				mOffsetXOld;				// The old bitmap X-axis offset
	private int				mOffsetYOld;				// The old bitmap Y-axis offset
	private boolean			mIsBitmapOldUp;				// The old bitmap is from the up layer
	
	/** The displayed Bitmap: it is build from a cropped mBitmap regarding the zoom value and the focus point*/
	/** The RT (real-time is the DispBitmap + the current path before releasing) */
	private Bitmap			mDispBitmap;
	private Bitmap			mDispBitmapRT;
	private Canvas			mDispCanvasRT;
	
	/** canvas Border */
	final private Paint		mBorderPaint;
	
	/** the initial picture */
	static public Bitmap	picture;
	
	/** The Paint object for filling the canvas */
	private SketchbookPath		mPath;

	/** The navigator */
	private SketchbookNavigator	mNav;
	
	/** The pattern offset which is set at the first user pressure */
	private int	[]			mPatternOff = { 0, 0 };		// The pattern offset
	
	public int []			getSize() { return new int [] { mBitmap.getWidth(), mBitmap.getHeight()}; }
	
	/** Manage color */
	static int getBackGround() {
		return BrushDialog.mode==BrushDialog.PaintMode.ERASE?Color.WHITE:Color.TRANSPARENT;
	}
	static int getForeGround() {
		int color = ColorDialog.color;
		if (BrushDialog.mode==BrushDialog.PaintMode.ERASE) { 
			color = Color.argb(255-BrushDialog.opacity, 255, 255, 255);
		}
		return color;
	}
	
	/**
	 * Update the bitmap with the new bitmap
	 * @param _newBitmap is the new bitmap
	 * @param _clearFocus
	 */
	private void updateBitmap(Bitmap _newBitmap, boolean _clearFocus) {
		if ((_newBitmap!=null) && (_newBitmap.getWidth()!=0) && (_newBitmap.getHeight()!=0) && (_newBitmap.isMutable())) {
		
			// Save the last bitmap 
			mBitmapOld = null;
			
			// Build the true bitmap which contains the picture
			mBitmap = _newBitmap;
			mCanvas = new Canvas(mBitmap);
			
			if (mNav!=null) {
				mNav.setBitmap(_newBitmap);
				if (_clearFocus) { mNav.center(); }
				onChange(mNav);
			}
		}
	}
	
	/** Try to save the application (very hard stuff) */
	private void fixBitmap() { newData(SketchbookView.WIDTH, SketchbookView.HEIGHT, NewDialog.FillBut.WHITE); }
	
	/** Clean the memory */
	public void clean(boolean _all) {
		if (mBitmapOld!=null) 		{ mBitmapOld.recycle(); mBitmapOld = null; mOffsetXOld = 0; mOffsetYOld = 0; mIsBitmapOldUp = false; }
		if (_all && mBitmapUp!=null){ mCanvasUp = null; mBitmapUp.recycle(); mBitmapUp = null; LAYER=NOLAYER;}
		if (_all && mBitmap!=null)	{ mCanvas = null; mBitmap.recycle(); mBitmap = null; }
	}
	public void cleanRT() {
		if (mDispBitmap!=null) 		{ mDispBitmap.recycle(); mDispBitmap = null; }
		if (mDispBitmapRT!=null) 	{ mDispCanvasRT = null; mDispBitmapRT.recycle(); mDispBitmapRT = null; }
	}
	
	/**
	 * Initialize a new image
	 * @param _width is the width of the new picture
	 * @param _height is the height of the new picture
	 * @param _fill is the fill mode (see NewDialog)
	 */
	public void newData(int _width, int _height, NewDialog.FillBut _fill) {
		clean(true); cleanRT();
		if (SketchbookData.picture!=null) {
			// Get the configuration picture if there is one (from camera for example)
			Bitmap vBitmap = SketchbookData.picture.copy(Bitmap.Config.ARGB_8888, true);
			SketchbookData.picture.recycle(); SketchbookData.picture = null;
			updateBitmap(vBitmap, true);
		}
		else
		{
			// Create a new picture
			Bitmap tmpBitmap = Bitmap.createBitmap(_width, _height, Bitmap.Config.ARGB_8888);
			
			if (_fill==NewDialog.FillBut.BLACK) {
				tmpBitmap.eraseColor(Color.BLACK);
				updateBitmap(tmpBitmap, true);
			}
			else if (_fill==NewDialog.FillBut.ALPHA) {
				tmpBitmap.eraseColor(Color.TRANSPARENT);
				updateBitmap(tmpBitmap, true);
			}
			/*
			else if (_fill==NewDialog.FillBut.COLOR) {
				tmpBitmap.eraseColor(ColorDialog.color);
				updateBitmap(tmpBitmap, true);
			}
			*/
			else {
				tmpBitmap.eraseColor(Color.WHITE);
				updateBitmap(tmpBitmap, true);
			}
		}
	}
	
	/**
	 * Create a Bitmap from a path name or stream
	 * @param pathName the path name of the picture
	 */
	public void newData(String pathName, boolean _up) {
		if (_up) {
			clean(false); cleanRT();
			if (mBitmapUp!=null){ mCanvasUp = null; mBitmapUp.recycle(); mBitmapUp = null; LAYER=NOLAYER;}
			try {
				Bitmap vBitmap = BitmapFactory.decodeFile(pathName);
				boolean vGood = (vBitmap!=null);
				if (vGood) {
					vGood = (mBitmap!=null) && (vBitmap.getWidth()==mBitmap.getWidth()) && (vBitmap.getHeight()==mBitmap.getHeight());
				}
				if (vGood) {
					mBitmapUp = vBitmap.copy(Bitmap.Config.ARGB_8888, true);
					mCanvasUp = new Canvas(mBitmapUp);
					LAYER = LAYERUP;
				}
				
				if (mNav!=null) { onChange(mNav); }
			}
			catch (java.lang.OutOfMemoryError e) {
				if (mBitmapUp!=null){ mCanvasUp = null; mBitmapUp.recycle(); mBitmapUp = null; LAYER=NOLAYER;}
			}
		}
		else {
			clean(true); cleanRT();
			try {
				Bitmap vBitmap = BitmapFactory.decodeFile(pathName);
				updateBitmap(vBitmap!=null?vBitmap.copy(Bitmap.Config.ARGB_8888, true):null, true);
			}
			catch (java.lang.OutOfMemoryError e) { fixBitmap(); }
		}
	}
	
	/** Create a new layer from input stream */
	public void newData(FileInputStream _input, boolean _up) {
		if (_up) {
			clean(false); cleanRT();
			if (mBitmapUp!=null){ mCanvasUp = null; mBitmapUp.recycle(); mBitmapUp = null; LAYER=NOLAYER;}
			try {
				Bitmap vBitmap = BitmapFactory.decodeStream(_input);
				if (vBitmap!=null) {
					mBitmapUp = vBitmap.copy(Bitmap.Config.ARGB_8888, true);
					mCanvasUp = new Canvas(mBitmapUp);
					LAYER = LAYERUP;
					if (mNav!=null) { onChange(mNav); }
				}
			}
			catch (java.lang.OutOfMemoryError e) {
				if (mBitmapUp!=null){ mCanvasUp = null; mBitmapUp.recycle(); mBitmapUp = null; LAYER=NOLAYER;}
			}
		}
		else {
			clean(true); cleanRT();
			try {
				Bitmap vBitmap = BitmapFactory.decodeStream(_input);
				updateBitmap(vBitmap!=null?vBitmap.copy(Bitmap.Config.ARGB_8888, true):null, true);
			}
			catch (java.lang.OutOfMemoryError e) {
				fixBitmap();
			}
		}
		
		Plouik.trace(TAG, "[DATA]     Create a new data from stream ("+(_up?"UP":"DOWN")+
				") (Bitmap:"+(mBitmap!=null?"OK":"null")+") (BitmapUp:"+(mBitmapUp!=null?"OK":"null")+")");
		
	}
	
	/** Rotate the image by 90, 180 or 270 degrees */
	public void rotate(int value) {
		try 	{ 
			// create a matrix for the manipulation
			Matrix	matrix = new Matrix();
			boolean	clearFocus = true;
			
			switch(value) {
			case DEG90 :	matrix.postRotate(90);	break;
			case DEG180 :	matrix.postRotate(180);	clearFocus = false; break;
			case DEG270 :	matrix.postRotate(270);	break;	
			}
			clean(false); cleanRT();
			updateBitmap(Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true).copy(Bitmap.Config.ARGB_8888, true), clearFocus);
		}
		catch	(java.lang.OutOfMemoryError e) { fixBitmap(); }
	}
	
	/** Flip the image horizontally or vertically */
	public void flip(int value) {
		try { 
			// create a matrix for the manipulation
			Matrix	matrix = new Matrix();
			
			switch(value) {
			case HORIZ :	matrix.preScale(-1, 1); break;
			case VERT :		matrix.preScale(1, -1); break;
			}
			clean(false); cleanRT();
			updateBitmap(Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true).copy(Bitmap.Config.ARGB_8888, true), false);
		}
		catch	(java.lang.OutOfMemoryError e) { fixBitmap(); }
	}
	
	/** Create or delete the layer */
	public void layer() {
		clean(false);
		if (LAYER==NOLAYER) {
			try {
				mBitmapUp = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
				mBitmapUp.eraseColor(Color.TRANSPARENT);
				mCanvasUp = new Canvas(mBitmapUp);
				LAYER = LAYERUP;
				if (mNav!=null) { onChange(mNav); }
			}
			catch	(java.lang.OutOfMemoryError e) {
				mCanvasUp = null; if (mBitmapUp!=null) { mBitmapUp.recycle(); mBitmapUp = null; }
				LAYER = NOLAYER;
			}
		}
		else {
			mCanvasUp = null; mBitmapUp.recycle(); mBitmapUp = null;
			LAYER = NOLAYER;
			if (mNav!=null) { onChange(mNav); }
		}
	}
	
	/** Merge the layers */
	public void mergeLayer() {
		if (LAYER!=NOLAYER) {
			clean(false);
			mCanvas.drawBitmap(mBitmapUp, 0, 0, null);
			mCanvasUp = null; mBitmapUp.recycle(); mBitmapUp = null;
			LAYER=NOLAYER;
			if (mNav!=null) { onChange(mNav); }
		}
	}
	
	/** Flip the layers */
	public void flipLayer() {
		if (LAYER!=NOLAYER && mBitmap!=null && mBitmapUp!=null ) {
			clean(false);
			Bitmap bitmap 	= mBitmap;
			Canvas canvas 	= mCanvas;
			mBitmap 		= mBitmapUp;
			mCanvas 		= mCanvasUp;
			mBitmapUp 		= bitmap;
			mCanvasUp		= canvas;
			if (mNav!=null) { onChange(mNav); }
		}
	}
	
	/**
	 * Undo the last operation
	 */
	public void undo() {
		if (mBitmapOld!=null) {
			Paint paint = new Paint();
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
			(mIsBitmapOldUp?mCanvasUp:mCanvas).drawBitmap(mBitmapOld, mOffsetXOld, mOffsetYOld, paint);
			if (mNav!=null) { onChange(mNav); }
			clean(false);
		}
	}
	
	/**
	 * Draw the current data into the surface holder canvas
	 * @param canvas is the current canvas
	 */
	public void doDraw(Canvas canvas, Context _context) {
		if (mDispBitmap!=null) {
			// Update the mDispBitmapRT during the drawing
			if (canvas!=null && mDispCanvasRT!=null) {
				mPath.draw(mDispCanvasRT, mDispBitmap, mNav, _context);
				canvas.drawBitmap(mDispBitmapRT, mNav.getScreenOffset()[0], mNav.getScreenOffset()[1], null);
			}
	
			// Draw the border of the canvas: useful in case of transparent canvas
			canvas.drawRect(mNav.getScreenOffset()[0]-1, mNav.getScreenOffset()[1]-1,
							mNav.getScreenOffset()[0]+mDispBitmap.getWidth(),
							mNav.getScreenOffset()[1]+mDispBitmap.getHeight(), mBorderPaint);
		}
	}	
	
	/**
	 * Release the current drawing process and update
	 * the real picture canvas (until now, only the displayed
	 * picture has been updated)
	 */
	public void release(Context _context) {
		clean(false); cleanRT();
		if (mBitmapOld!=null) { mBitmapOld.recycle(); mBitmapOld=null; }
		if ((mBitmap!=null) && (mCanvas!=null)) {
		
			// Get the bound box
			RectF vBounds = new RectF();
			mPath.computeBounds(vBounds, true);
				
			// Expand the bounds box
			int vLeft 	= (int) (vBounds.left>mPath.getStrokeWidth()?vBounds.left-mPath.getStrokeWidth():0 );
			int vTop	= (int) (vBounds.top>mPath.getStrokeWidth()?vBounds.top-mPath.getStrokeWidth():0);
			int vRight	= (int) (vBounds.right<mCanvas.getWidth()-mPath.getStrokeWidth()-1?vBounds.right+mPath.getStrokeWidth()+1:mCanvas.getWidth());
			int vBottom = (int) (vBounds.bottom<mCanvas.getHeight()-mPath.getStrokeWidth()-1?vBounds.bottom+mPath.getStrokeWidth()+1:mCanvas.getHeight());
			
			if ((vRight>vLeft) && (vBottom>vTop)) {
				Bitmap			prepareBitmap = null;
				Canvas			prepareCanvas = null;
				try { prepareBitmap = Bitmap.createBitmap(vRight-vLeft, vBottom-vTop, Bitmap.Config.ARGB_8888); }
				catch(java.lang.OutOfMemoryError e) { prepareBitmap = null; }
				
				if (ToolsDialog.isUndoVisible()) {
					try {
						mIsBitmapOldUp = ((LAYER==LAYERUP)||(LAYER==LAYERUPX));
						mBitmapOld = Bitmap.createBitmap(mIsBitmapOldUp?mBitmapUp:mBitmap, vLeft, vTop, vRight-vLeft, vBottom-vTop);
						mOffsetXOld = vLeft;
						mOffsetYOld = vTop;
					}
					catch(java.lang.OutOfMemoryError e) { clean(false); }
				}
					
				if (prepareBitmap!=null) {
					prepareCanvas = new Canvas(prepareBitmap);
					prepareCanvas.drawColor(SketchbookData.getBackGround(), PorterDuff.Mode.SRC);
					mPath.draw(prepareCanvas, vLeft, vTop, _context);
	
					if (PatternDialog.patternBitmap!=null) {
									// The paint used for adding the pattern
						Paint vPaint = new Paint();
						vPaint.setXfermode(new PorterDuffXfermode(
								BrushDialog.mode==BrushDialog.PaintMode.ERASE?
									(PatternDialog.patternNeg?PorterDuff.Mode.SCREEN:PorterDuff.Mode.MULTIPLY)
									:
									(PatternDialog.patternNeg?PorterDuff.Mode.DST_OUT:PorterDuff.Mode.MULTIPLY)));
						
						if (BrushDialog.mode==BrushDialog.PaintMode.ERASE && !PatternDialog.patternNeg) {
							prepareCanvas.drawColor(Color.WHITE, PorterDuff.Mode.XOR);
						}
	
						for (int i=(vLeft+mPatternOff[0])/PatternDialog.patternBitmap.getWidth();
								 i<(vRight+mPatternOff[0])/PatternDialog.patternBitmap.getWidth()+1; i++) {
							for (int j=(vTop+mPatternOff[1])/PatternDialog.patternBitmap.getHeight();
									 j<(vBottom+mPatternOff[1])/PatternDialog.patternBitmap.getHeight()+1; j++) {
								prepareCanvas.drawBitmap(PatternDialog.patternBitmap,
										(float) (i * PatternDialog.patternBitmap.getWidth() - mPatternOff[0] - vLeft),
										(float) (j * PatternDialog.patternBitmap.getHeight() - mPatternOff[1] - vTop),
										vPaint);
							}
						}
						
						if (BrushDialog.mode==BrushDialog.PaintMode.ERASE && !PatternDialog.patternNeg) {
							prepareCanvas.drawColor(Color.WHITE, PorterDuff.Mode.XOR);
						}
						
					}
	
					Canvas canvas = (LAYER==LAYERUP || LAYER==LAYERUPX)?mCanvasUp:mCanvas;
					
					if (canvas!=null) {
						canvas.save();
						canvas.clipRect(vLeft, vTop, vRight, vBottom);
						canvas.drawBitmap(prepareBitmap, vLeft, vTop, mPath.getPaintMode());
						canvas.restore();
					}
						
					prepareCanvas = null;
					if (prepareBitmap!=null) { prepareBitmap.recycle(); }
					prepareBitmap = null;
				}
			}
		}
		else {
			mCanvasUp = null; 
			if (mBitmapUp!=null) { mBitmapUp.recycle(); mBitmapUp = null; }
			LAYER=NOLAYER;
		}
		
		mPath.reset();
		onChange(mNav);
	}
	
	/**
	 * Draw a line from the previous draw point
	 * @param _pos is the screen position
	 * @param _nav is the image navigator
	 */
	public void drawLine(float [] _pos, SketchbookNavigator _nav) {
			float [] vPos = _nav.s2i(_pos);
			mPath.lineTo(vPos[0], vPos[1], _pos[2], getPixel(_pos, _nav));
	}
	
	/**
	 * Draw a point
	 * @param _pos is the screen position
	 * @param _nav is the image navigator
	 */
	public void drawPoint(float [] _pos, SketchbookNavigator _nav) {
		
		// Get the point to draw
		float [] vPos = _nav.s2i(_pos);
			
		// Reset the path and add the new point
		mPath.moveTo(vPos[0], vPos[1], _pos[2], getPixel(_pos, _nav));
		//mPath.lineTo(vPos[0]+.01f, vPos[1]+.01f);
			
		// Reset the clip Path and add the surface point
		if (PatternDialog.patternBitmap!=null) {
			mPatternOff[0]=mPatternOff[1]=0;
			if (PatternDialog.pointer == PatternDialog.PointerState.RAND) {
				mPatternOff[0] = (int)( PatternDialog.patternBitmap.getWidth()*Math.random());
				mPatternOff[1] = (int)( PatternDialog.patternBitmap.getHeight()*Math.random());
			}
			else
			if (PatternDialog.pointer == PatternDialog.PointerState.PIN) {
				mPatternOff[0] = (int) (PatternDialog.patternBitmap.getWidth()/2 - (vPos[0]%PatternDialog.patternBitmap.getWidth()));
				mPatternOff[1] = (int) (PatternDialog.patternBitmap.getHeight()/2 -(vPos[1]%PatternDialog.patternBitmap.getHeight()));
					
				mPatternOff[0] += mPatternOff[0]<0?PatternDialog.patternBitmap.getWidth():0;
				mPatternOff[1] += mPatternOff[1]<0?PatternDialog.patternBitmap.getHeight():0;
			}
		}
	}
	
	/**
	 * Get the pixel color
	 * @param _x is the event X-position
	 * @param _y is the event Y-position
	 * @param _nav is the navigator
	 * @return
	 */
	public int getPixel(float [] _pos, SketchbookNavigator _nav) {
		int color = SketchbookView.BACKGROUND;
		if ((mDispBitmapRT!=null)&&(mNav!=null)) {
			int posX = (int)_pos[0]-mNav.getScreenOffset()[0];
			int posY = (int)_pos[1]-mNav.getScreenOffset()[1];
			if ((posX>=0) && (posX<mDispBitmapRT.getWidth()) && (posY>=0) && (posY<mDispBitmapRT.getHeight())) {
				color = mDispBitmapRT.getPixel(posX,posY);
			}
		}
		
		/*
		// Get the pixel position in the bitmap
		int [] vPos = _nav.s2i(_pos);
		
		// Get the color of the pixel
		if ((vPos[0]>=0)&&(vPos[0]<mBitmap.getWidth())&&(vPos[1]>=0)&&(vPos[1]<mBitmap.getHeight()))	{
			color = mBitmap.getPixel(vPos[0], vPos[1]);
		}
		*/

		return color;
	}
	
	/**
	 * Return the bitmap
	 * @return the bitmap
	 */
	public Bitmap getBitmap(boolean _up) { return _up?mBitmapUp:mBitmap; }
	
	/**
	 * SketchbookData constructor
	 * @param _width is the image width
	 * @param _height is the image height
	 */
	public SketchbookData(int _width, int _height, SketchbookNavigator _nav) {
		mNav = _nav;
		mPath = new SketchbookPath(mNav);
		mBorderPaint = new Paint();
		mBorderPaint.setColor(Color.GRAY);
		mBorderPaint.setStrokeWidth(1.0f);
		mBorderPaint.setStyle(Paint.Style.STROKE);
		newData(_width, _height, NewDialog.FillBut.WHITE);
		onChange(_nav);
	}
	
	/** The navigator has changed (zoom, displacement) or the image bitmap itself */
	public void onChange(SketchbookNavigator _nav) {
		if (mBitmap!=null) {
			cleanRT();
			try {
				Matrix zoomMatrix = new Matrix();
				zoomMatrix.setScale(mNav.getZoom(), mNav.getZoom());
				mDispBitmap = Bitmap.createBitmap( LAYER==LAYERUPX?mBitmapUp:mBitmap, mNav.getPictureOffset()[0], mNav.getPictureOffset()[1], 
															mNav.getCropSize()[0], mNav.getCropSize()[1],
															zoomMatrix, true);
				Canvas canvas = new Canvas(mDispBitmap);
				
				if (LAYER==LAYERUP || LAYER==LAYERDOWN) {
					Bitmap bitmapUp = Bitmap.createBitmap( mBitmapUp, mNav.getPictureOffset()[0], mNav.getPictureOffset()[1], 
							mNav.getCropSize()[0], mNav.getCropSize()[1],
							zoomMatrix, true);
					canvas.drawBitmap(bitmapUp, 0, 0, null);
				}
				
				mDispBitmapRT = Bitmap.createBitmap(mDispBitmap);
				mDispCanvasRT = new Canvas(mDispBitmapRT);
			}
			catch (java.lang.IllegalArgumentException e) { cleanRT(); }
			catch (java.lang.OutOfMemoryError e) { cleanRT(); }
			mPath.onChange(_nav);
		}
	}
	
	/** The brush settings has changed */
	public void onChange() { mPath = new SketchbookPath(mNav); }

}
