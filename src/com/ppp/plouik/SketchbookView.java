package com.ppp.plouik;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * View the image, takes keystrokes, etc.
 * 
 * @author Pouf-Pouf Production
 *
 */
public class SketchbookView extends SurfaceView implements SurfaceHolder.Callback{
	static final String TAG = "SurfaceView";
	
	static int WIDTH = 100;
	static int HEIGHT= 100;
	
	static int BACKGROUND = Color.GRAY;
	
	/**
	 * The Handling thread
	 * @author Johann Charlot
	 *
	 */
	public class SketchbookThread extends Thread {

		private SketchbookData		mData;				// The picture bitmap
		private SurfaceHolder		mSurfaceHolder;		// Handle to the surface manager object we interact with
        private boolean				mRun = false;		// Indicate whether the surface has been created & is ready to draw
        private SketchbookNavigator	mNav;				// The picture navigator
        private float []			mBase = { -1.0f, -1.0f };
        private	Context				mContext;
        
        public void 	setRunning(boolean b) 	{ mRun = b; }
        public String 	getZoomName()			{ return (mNav!=null)?mNav.getZoomName():""; }
    	private void 	doDraw(Canvas canvas) 	{ if (mData!=null) { canvas.drawColor(SketchbookView.BACKGROUND); mData.doDraw(canvas, mContext); } }
    	public void		update()				{ synchronized(mSurfaceHolder) { if (mData!=null) { mData.onChange(); } } }
        public void 	nextZoom(boolean b) 	{ synchronized(mSurfaceHolder) { if (mData!=null && mNav!=null ) { mNav.nextZoom(b); mData.onChange(mNav); } } }
        public void 	center()				{ synchronized(mSurfaceHolder) { if (mData!=null && mNav!=null ) { mNav.center(); mData.onChange(mNav); } } }
        public void		change()				{ synchronized(mSurfaceHolder) { if (mData!=null && mNav!=null ) { mData.onChange(mNav); } } }
    	public Bitmap 	getBitmap(boolean _up)	{ synchronized(mSurfaceHolder) { return (mData!=null)?mData.getBitmap(_up):null; } }
		public void 	rotateImage90()			{ synchronized(mSurfaceHolder) { if (mData!=null) { mData.rotate(SketchbookData.DEG90); } } }
		public void 	rotateImage180()		{ synchronized(mSurfaceHolder) { if (mData!=null) { mData.rotate(SketchbookData.DEG180); } } }
		public void 	rotateImage270()		{ synchronized(mSurfaceHolder) { if (mData!=null) { mData.rotate(SketchbookData.DEG270); } } }
		public void 	flipImageHoriz()		{ synchronized(mSurfaceHolder) { if (mData!=null) { mData.flip(SketchbookData.HORIZ); } } }
		public void 	flipImageVert()			{ synchronized(mSurfaceHolder) { if (mData!=null) { mData.flip(SketchbookData.VERT);  } } }
		public void		layer()					{ synchronized(mSurfaceHolder) { if (mData!=null) { mData.layer(); } } }
		public void		flipLayer()				{ synchronized(mSurfaceHolder) { if (mData!=null) { mData.flipLayer(); } } }
		public void		mergeLayer()			{ synchronized(mSurfaceHolder) { if (mData!=null) { mData.mergeLayer(); } } }
		public void 	undo()					{ synchronized(mSurfaceHolder) { if (mData!=null) { mData.undo(); } } }
		public SketchbookNavigator	getNav()	{ return mNav; }
		
		/**
		 * Create a new image
		 * @param _width
		 * @param _height
		 * @param _fill is the fill mode (see NewDialog)
		 */
		public void newImage(int _width, int _height, NewDialog.FillBut _fill) {
			synchronized(mSurfaceHolder) {
				if (mData!=null) { mData.newData(_width, _height, _fill);} 
				else { Plouik.error(TAG,"[NEW]     Data not ready"); }
			}
		}
		
		/** Create a new image from file */
		public void newImage(String pathName, boolean _up) {
			synchronized(mSurfaceHolder) {
				if (mData!=null) { mData.newData(pathName, _up); } 
				else { Plouik.error(TAG,"[NEW]     Data not ready"); }
			}
		}
		
		/** Create a new image from FileInputStream */
		public void newImage(FileInputStream _input, boolean _up) { 
			synchronized(mSurfaceHolder) {
				if (mData!=null) { mData.newData(_input, _up); }
				else { Plouik.error(TAG,"[NEW]     Data not ready"); }
			}
		}
		
		/**
		 * The thread run method
		 */
		@Override
		public void run() {			
			while (mRun) {
				Canvas c = null;
				try {
					c = mSurfaceHolder.lockCanvas(null);
					synchronized(mSurfaceHolder) { 	if (c!=null) { doDraw(c); } }
				} finally { 						if (c!=null) { mSurfaceHolder.unlockCanvasAndPost(c); }
				}
			}
			
			if (mData!=null) { mData.clean(true); }
			Plouik.trace(TAG,"[THREAD]  Finish");
		}
		
		/**
		 * Constructor of the Sketchbook Thread
		 * @param surfaceHolder
		 * @param context 
		 */
		public SketchbookThread(SurfaceHolder surfaceHolder, Context context) {
			Plouik.trace(TAG,"[THREAD]  Create");
			mSurfaceHolder = surfaceHolder;
			mNav = new SketchbookNavigator(WIDTH,HEIGHT);
			mData = new SketchbookData(WIDTH,HEIGHT, mNav);
			mContext = context;
		}
        
        public boolean doTouchEvent(MotionEvent event) {
        	synchronized (mSurfaceHolder) {
        		if (mData!=null && mNav!=null) {
	        		// Save the first click position
	        		if (event.getAction()==MotionEvent.ACTION_DOWN) { mBase[0] = event.getX(); mBase[1] = event.getY();	}
	        		
	        		if (Plouik.NavButton.PICKER.getState()!=0) {
	        			//===============================
	            		// Get color from pixel
	           			//===============================
	            		int color = mData.getPixel(new float[] {event.getX(), event.getY()}, mNav);
	            		if (color!=0) {
	            			Plouik.trace(TAG,"color "+Color.red(color)+" - "+Color.green(color)+" - "+Color.blue(color));
	            			ColorDialog.color = color;
	            			mData.onChange();
	            		}
	        		}
	        		else
	        		if (Plouik.NavButton.MOVE.getState()!=0) {
		        		//===============================
		        		// Move the bitmap
		        		//===============================
	        			float [] vBase = { mBase[0] - event.getX(), mBase[1] - event.getY() };
	        			mNav.move(vBase); mData.onChange(mNav);
	        			Plouik.NavButton.MOVE.setUser(1);
	        		}
	        		else {
	        			//===============================
	        			// Paint
	        			//===============================
	        			switch (event.getAction()) {
	        			case MotionEvent.ACTION_UP :
	        				mData.release(mContext);
	        				break;
	        			case MotionEvent.ACTION_DOWN:
	        				mData.drawPoint(new float[] { event.getX(), event.getY(), (event.getPressure()>0)?event.getPressure():1f}, mNav);
	        				break;
	        			case MotionEvent.ACTION_MOVE:
	        				if (event.getHistorySize()>0) {
	        					for (int i=0; i<event.getHistorySize(); i++) {
	        						mData.drawLine(new float[] { event.getHistoricalX(i), event.getHistoricalY(i),
	        													(event.getHistoricalPressure(i)>0?event.getHistoricalPressure(i):1f)}, mNav);
	        					}
	        				}
	        				else {
	        					mData.drawLine(new float[] { event.getX(), event.getY(), (event.getPressure()>0)?event.getPressure():1f}, mNav);
	        				}
	        				
	        				break;
	        			}
	        		}
	    			
	        		// Update events
	    			if (event.getAction()==MotionEvent.ACTION_UP)	{
	        			if (Plouik.NavButton.MOVE.getState()==1) { Plouik.NavButton.MOVE.setState(0); }
	            		Plouik.NavButton.PICKER.setState(0);
	    				mBase[0]=mBase[1]=-1.0f;
	    			}
	    			else 											{ mBase[0] = event.getX(); mBase[1] = event.getY(); }
        		}
        	}
    		return true;
        }
	}
	
	/** The Surface view thread */
	private SketchbookThread		mThread;
	
	/**
	 * Constructor of the surface view class
	 * @param context is the current context
	 */
	public SketchbookView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Plouik.trace(TAG,"[VIEW]    Create SketchbookView");
		setFocusable(false);
		
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
	}

	/** miscellaneous methods */
	public SketchbookThread getThread() { return mThread;}
    public void update() 				{ mThread.update(); }
	
    @Override
    public boolean onTouchEvent(MotionEvent event) { return (mThread!=null)?mThread.doTouchEvent(event):true; }
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }
	

	/**
	 * Callback invoked when the surface has been created and is ready to be used
	 * @param holder: the surface holder
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		boolean vFromFile = (SketchbookData.picture==null);
		
		mThread = new SketchbookThread(holder, getContext());
		mThread.setRunning(true);	
		mThread.start();

		Plouik.trace(TAG,"[VIEW]    get the previous drawing from "+(vFromFile?"file":"picture"));
		
		if (vFromFile) {
	      	FileInputStream vInput;
			try {
				vInput = getContext().openFileInput("Plouik.png");
				mThread.newImage(vInput, false);
				vInput.close();
				
				vInput = getContext().openFileInput("PlouikUp.png");
				if (vInput.available()>1) {
					mThread.newImage(vInput, true);
				}
				vInput.close();
			} catch (FileNotFoundException e)
			{
				Plouik.trace(TAG,"[VIEW]    File not found: "+e.getMessage());
			} catch (IOException e)
			{
				Plouik.trace(TAG,"[VIEW]    IO Exception: "+e.getMessage());
			}
	    }
	}

	/**
	 * Callback invoked when the surface has been destroyed and must no longer
	 * be touched. WARNING: after this method returns, the Surface/Canvas must
	 * never be touched again!
	 * @param holder: the surface holder
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		Plouik.trace(TAG,"[VIEW]    Save and destroy surface");
		
		try {
			FileOutputStream vOutput;
			
			if (mThread.getBitmap(false)!=null) {
				vOutput = getContext().openFileOutput("Plouik.png", Context.MODE_PRIVATE);
				mThread.getBitmap(false).compress(Bitmap.CompressFormat.PNG, 100, vOutput);
				vOutput.close();
			}
			
			vOutput = getContext().openFileOutput("PlouikUp.png", Context.MODE_PRIVATE);
			if (SketchbookData.LAYER != SketchbookData.NOLAYER && mThread.getBitmap(true)!=null) {
				mThread.getBitmap(true).compress(Bitmap.CompressFormat.PNG, 100, vOutput);
			}
			else {
				vOutput.write(0);
			}
			vOutput.close();
			
		} catch (FileNotFoundException e) {
			Plouik.trace(TAG,"[VIEW]    File not found: "+e.getMessage());
		} catch (IOException e) {
			Plouik.trace(TAG,"[VIEW]    IO Exception: "+e.getMessage());
		}
		
		boolean retry = true;
		mThread.setRunning(false);
		
		while (retry) {
			try {
				mThread.join();
				mThread = null;
				retry = false;
			} catch (InterruptedException e) {
				Plouik.error(TAG,"[VIEW]    Destroy surface error: "+e.getMessage());
			}
		}
		
	}

}
