package com.ppp.plouik;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
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
public class SketchbookPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PictureCallback{
	static public final String		TAG			= "preview";
	SurfaceHolder 					mHolder;
    Camera 							mCamera;
    int								mWidth;
    int								mHeight;
   
    public interface OnPictureSaved {
        void done();
    }
    
    /** The local listener */
	private OnPictureSaved	mListener;
	
	public void setListener(OnPictureSaved _listener) { mListener = _listener; }
    
    public SketchbookPreview(Context context) {
    	super(context);
		
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public SketchbookPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
		if (mCamera!=null) {
			mCamera.stopPreview();
	        mCamera.release();
	        mCamera = null;
		}
		return true;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Plouik.trace(TAG,"[CAMERA]  width: "+width+" height: "+height);
		mWidth = width;
		mHeight = height;
		
		if (mCamera!=null) {
			Camera.Parameters parameters = mCamera.getParameters();
	        parameters.setPreviewSize(width, height);
	        try { mCamera.setParameters(parameters); } catch(RuntimeException e) { Plouik.error(TAG,"[CAMERA] error setParameters"); }
	        Plouik.trace(TAG,"[CAMERA]  Start the preview ("+parameters.getPreviewFormat()+"/"+parameters.getPictureFormat()+")");
	        mCamera.startPreview();
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Plouik.trace(TAG,"[CAMERA]  Surface View Created");
		
		mCamera = Camera.open();
		if (mCamera!=null) {
	        try { mCamera.setPreviewDisplay(mHolder); }
	        catch (Exception exception) { mCamera.release(); mCamera = null; }
		}
	}
	
	public void takePicture() {
		if (mCamera!=null) 	{ mCamera.takePicture(null, null, this); }
		else 				{ if (mListener!=null) { mListener.done(); } }
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Plouik.trace(TAG,"[CAMERA]  Surface View Destroyed");
		if (mCamera!=null) {
			mCamera.stopPreview();
	        mCamera.release();
	        mCamera = null;
	        Plouik.trace(TAG,"[CAMERA]  Stop the preview - release the camera");
		}
	}

	public void onPictureTaken(byte[] data, Camera camera) {
		if (data!=null) {
			int width = (PlouikCamera.mWidth!=0?PlouikCamera.mWidth:mWidth);
			int height = (PlouikCamera.mHeight!=0?PlouikCamera.mHeight:mHeight);
			Camera.Parameters params = camera.getParameters();
			
			Plouik.trace(TAG,"[CAMERA]  get picture (size: "+data.length+") ("+
					params.getPictureSize().width+"x"+params.getPictureSize().height+")");
			
			boolean getPortrait = (params.getPictureSize().width<params.getPictureSize().height);
			boolean askPortrait = (width<height);
				
			if (askPortrait==getPortrait) {
				float 	wRatio 		= params.getPictureSize().width/width;
				float 	hRatio 		= params.getPictureSize().height/height;
				float 	ratio 		= (wRatio>hRatio)?wRatio:hRatio;
				int 	sampleSize 	= 1;
				while (sampleSize<ratio) { sampleSize = sampleSize * 2; }
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = sampleSize;
					
				Plouik.trace(TAG,"[CAMERA]   No rotation (scale: "+sampleSize+")");
				try 	{ SketchbookData.picture = BitmapFactory.decodeByteArray(data, 0, data.length, options); }
				catch	( java.lang.OutOfMemoryError e) { SketchbookData.picture = null; }
			}
			else {
				float wRatio = params.getPictureSize().width/height;
				float hRatio = params.getPictureSize().height/width;
				float ratio = (wRatio>hRatio)?wRatio:hRatio;
				int 	sampleSize 	= 1;
				while (sampleSize<ratio) { sampleSize = sampleSize * 2; }
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = sampleSize;
					
				Plouik.trace(TAG,"[CAMERA]   Rotation (scale: "+sampleSize+")");
				
				try { 
					Bitmap vBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
					Matrix	matrix = new Matrix();
					matrix.postRotate(90);
					SketchbookData.picture = Bitmap.createBitmap(vBitmap, 0, 0, vBitmap.getWidth(), vBitmap.getHeight(), matrix, true);
					vBitmap.recycle(); vBitmap = null;
				}
				catch (java.lang.OutOfMemoryError e) { SketchbookData.picture = null; }
			}
			
			if (SketchbookData.picture != null) {
				Plouik.trace(TAG,"[CAMERA]   convert: "+SketchbookData.picture.getWidth()+"x"+SketchbookData.picture.getHeight());
			}
		}
		else {
			Plouik.trace(TAG,"[CAMERA]  no data");
		}
		if (mListener!=null) { mListener.done(); }
		
	}


}
