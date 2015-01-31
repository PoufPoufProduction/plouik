package com.ppp.plouik;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class SketchbookNavigator {
	static final String 	TAG 	= "nav";
	
	/**  The zoom handling class */
	private class SketchbookZoom {
		
		/** The zoom value */
		private class Value {
			public float	value;
			public String	name;
			public Value(float _value, String _name) { value = _value; name = new String(_name); }
		}
	
		private ArrayList<Value>	mZooms;			// The list of handled zooms
		private int					mZoomId;		// The current zoom id
		
		public 			SketchbookZoom()					{ mZooms = new ArrayList<Value>(); clearZooms(); }
		public int 		add(float _value, String _name) 	{ return add(new Value(_value, _name));	}
		public float 	get()								{ return (mZooms.size()>0)?mZooms.get(mZoomId).value:1.0f; }
		public String 	getName()							{ return (mZooms.size()>0)?mZooms.get(mZoomId).name:"none"; }
		public void		set(int _id)						{ mZoomId = (_id<mZooms.size())?_id:mZoomId; }
		
		/**
		 * Add a new zoom
		 * @param _value is the new zoom
		 */
		private int add(Value _value) {
			int i;
			boolean inserted = false;
			for (i=0; i<mZooms.size(); i++) {
				
				if (mZooms.get(i).value==_value.value) {
					inserted = true;
					break;
				}
				
				if (mZooms.get(i).value<_value.value) {
					if (i<=mZoomId) { mZoomId++; }
					mZooms.add(i, _value);
					inserted = true;
					break;
				}
			}
			if (!inserted) { mZooms.add(_value);}
			return i;
		}
		
		/**
		 * Clear all zoom and add the classical ones
		 */
		public void clearZooms() {
			mZooms.clear();
			add(2.0f, "x2"); add(4.0f, "x4"); add(8.0f, "x8");
			add(0.5f, "/2"); add(0.25f, "/4"); add(0.125f, "/8"); add(0.0625f, "/16");
			mZoomId = add(1.0f, "x1");
		}
		
		/**
		 * Next zoom selection
		 * @param _up is true for zoom in
		 */
		public float nextZoom(boolean _up) {
			if (_up) {
				if (mZoomId<mZooms.size()-1) { mZoomId++; }
			}
			else {
				if (mZoomId>0) { mZoomId--; }
			}
			return mZooms.get(mZoomId).value;
		}
		
	}
	
	/** The computed offset regarding the current zoom */
	final private int []	mScreenOffset = { 0, 0 };	// screen offset
	private int []			mPictureOffset = { 0, 0 };	// picture offset
	private int	[]			mCropSize = { 0, 0 };		// The displayed cropped and zoomed picture size
	private int []			mBitmapHalfSize = { 0, 0 };	// The bitmap half size
	private int []			mScreenSize = { 0, 0 };		// The screen size
	private float []		mFocus = { 0f, 0f };		// The focus
	private SketchbookZoom	mZoom;						// The zoom value (1.0f is a no zoom value)
	private Matrix			mMatrix;					// Convert from picture to display (zoom and focus offset)
	
	SketchbookNavigator(int _width, int _height) 	{
		mMatrix = new Matrix();
		mZoom = new SketchbookZoom();
		mScreenSize[0] = _width;
		mScreenSize[1] = _height;
	}
	
	/** Callback for a zoom or move action */
	private void refresh() {
		int		vTmp;										// A temporary integer
		int [] 	vSizeZoom 		= { 0, 0 };					// The zoomed picture size
		
		for (int i=0; i<2; i++) {
			mScreenOffset[i] 	= (int) ( mScreenSize[i]/2 - (mBitmapHalfSize[i]+mFocus[i])*mZoom.get());
			vTmp 				= (int) ((mScreenOffset[i]>0)?0:(-mScreenOffset[i]/mZoom.get()));
			if ( mPictureOffset[i] != vTmp ) { mPictureOffset[i] = vTmp; }
			mScreenOffset[i] 	= (mScreenOffset[i]<0)?0:mScreenOffset[i];
			vSizeZoom[i] 		= mBitmapHalfSize[i]*2-mPictureOffset[i];
			vTmp				= (vSizeZoom[i]<(mScreenSize[i]-mScreenOffset[i])/mZoom.get())?
									vSizeZoom[i]:(int)((mScreenSize[i]-mScreenOffset[i])/mZoom.get());	
			if ( mCropSize[i] != vTmp ) { mCropSize[i] = vTmp; }
		}
		
		mMatrix.setValues(new float[] { mZoom.get(),0,-mPictureOffset[0]*mZoom.get(), 0,mZoom.get(),-mPictureOffset[1]*mZoom.get(), 0,0,1});
	}
	
	/** Quick accessors */
	public float 	getZoom()											{ return mZoom.get(); }
	public String	getZoomName()										{ return mZoom.getName(); }
	public void		nextZoom(boolean _up)								{ mZoom.nextZoom(_up); refresh(); }
	public Matrix	getMatrix()											{ return mMatrix; }
	public void		center()											{ mFocus[0] = mFocus[1] = 0; refresh(); }
	public int[]	getScreenOffset()									{ return mScreenOffset; }
	public int[]	getPictureOffset()									{ return mPictureOffset; }
	public int[]	getCropSize()										{ return mCropSize; }
	public void		setScreenSize(int _width, int _height)				{ mScreenSize[0] = _width; mScreenSize[1] = _height; }
	
	/**
	 * Set the bitmap picture parameters (and define the screen zoom)
	 * @param _bitmap is the bitmap picture
	 */
	public void setBitmap(Bitmap _bitmap) {
		if ((_bitmap!=null)&&(_bitmap.getWidth()/2!=mBitmapHalfSize[0])) {
			mBitmapHalfSize[0] = _bitmap.getWidth()/2;
			mBitmapHalfSize[1] = _bitmap.getHeight()/2;
		
			if (mBitmapHalfSize[0]>0 && mBitmapHalfSize[1]>0) {
				float vZoom = Math.min( (float) ((float)mScreenSize[0] / (float)mBitmapHalfSize[0]),
										(float) ((float)mScreenSize[1] / (float)mBitmapHalfSize[1]));
				mZoom.clearZooms();
				int id = mZoom.add(vZoom/2.0f, "screen");
				if (vZoom<1.0f) { mZoom.set(id); }
			}
			refresh();
		}
	}
	
	/**
	 * Change the bitmap focus on the screen
	 * @param _d is the move offset
	 */
	public void	move(float [] _d) {
		for (int i=0; i<2; i++) {
			mFocus[i] += _d[i]/mZoom.get();
			mFocus[i] = (mFocus[i] < -mBitmapHalfSize[i]) ? -mBitmapHalfSize[i] : ((mFocus[i] > mBitmapHalfSize[i]) ? mBitmapHalfSize[i] : mFocus[i]);
		}
		refresh();
	}
	
	/**
	 * Convert a point from the screen to a bitmap pixel coordinates
	 * @param _pos
	 * @return
	 */
	public float[]	s2i(float [] _pos) {
		float [] vRet = { 	(_pos[0]-mScreenOffset[0])/mZoom.get() + mPictureOffset[0], 
							(_pos[1]-mScreenOffset[1])/mZoom.get() + mPictureOffset[1]	};
		return vRet;
	}

}
