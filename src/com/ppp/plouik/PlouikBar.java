package com.ppp.plouik;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class PlouikBar extends SeekBar {
	public PlouikBar(Context context) { super(context); }
	public PlouikBar(Context context, AttributeSet attrs) { super(context, attrs); }
	public PlouikBar(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	
	private float	mWidth = 0;
	private float	mHeight= 20f;
	private Type 	mType = Type.RED;
	
	private int[]	mRGB = new int[] { 0, 0, 0 };
	private float[] mHSV = new float[] { 0, 0, 0 };
	
	public enum Type {
		RED, GREEN, BLUE, HUE, SATURATION, VALUE, BRUSH, HALFBRUSH
	};
	

	public void setRGB(int _color) { mRGB[0] = Color.red(_color); mRGB[1] = Color.green(_color); mRGB[2] = Color.blue(_color); invalidate(); }
	public void setHSV(float _hsv[]) { mHSV[0] = _hsv[0]; mHSV[1] = _hsv[1]; mHSV[2] = _hsv[2]; invalidate(); }
	
	public void setType(Type _type){
		mType = _type;
		mHeight=getContext().getResources().getDimension(R.dimen.seekbar);
		
		switch(mType) {
		case RED: case GREEN: case BLUE : case HUE : case SATURATION : case VALUE :
			mWidth=getContext().getResources().getDimension(R.dimen.seekbarColor);	break;
		case BRUSH:
			mWidth=getContext().getResources().getDimension(R.dimen.seekbarWidth);	break;
		case HALFBRUSH:
			mWidth=getContext().getResources().getDimension(R.dimen.icon);			break;
		default:
			mWidth = 0;break;
		}
	}
	
	@Override protected synchronized void onDraw(Canvas canvas) {
		if (mWidth==0) { mWidth = canvas.getWidth(); }
		
		Paint paint = new Paint();
		paint.setStrokeWidth(mHeight/1.2f);
		paint.setStrokeCap(Paint.Cap.BUTT);
		
		switch(mType) {
		case RED:
			paint.setShader(
					new LinearGradient(0, 0, mWidth, 0, Color.rgb(0, mRGB[1], mRGB[2]), Color.rgb(255, mRGB[1], mRGB[2]), Shader.TileMode.CLAMP));
			break;
		case GREEN:
			paint.setShader(
					new LinearGradient(0, 0, mWidth, 0, Color.rgb(mRGB[0], 0, mRGB[2]), Color.rgb(mRGB[0], 255, mRGB[2]), Shader.TileMode.CLAMP));
			break;
		case BLUE:
			paint.setShader(
					new LinearGradient(0, 0, mWidth, 0, Color.rgb(mRGB[0], mRGB[1], 0), Color.rgb(mRGB[0], mRGB[1], 255), Shader.TileMode.CLAMP));
			break;
		case HUE:
			int [] colors = new int[11];
			for (int i=0; i<11; i++) {
				colors[i] = Color.HSVToColor(new float[] {360f*i/10f, mHSV[1], mHSV[2]});
			}
			paint.setShader(new LinearGradient(0,0, mWidth, 0, colors, null, Shader.TileMode.CLAMP));
			break;
		case SATURATION:
			paint.setShader(
					new LinearGradient(0, 0, mWidth, 0, Color.HSVToColor(new float[] {mHSV[0], 0, mHSV[2]}), Color.HSVToColor(new float[] {mHSV[0], 1, mHSV[2]}), Shader.TileMode.CLAMP));
			break;
		case VALUE:
			paint.setShader(
					new LinearGradient(0, 0, mWidth, 0, Color.HSVToColor(new float[] {mHSV[0], mHSV[1], 0}), Color.HSVToColor(new float[] {mHSV[0], mHSV[1], 1}), Shader.TileMode.CLAMP));
			break;
		case BRUSH:
			paint.setShader(
					new LinearGradient(0, 0, mWidth, 0, Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP));
			break;
		case HALFBRUSH:
			paint.setShader(
					new LinearGradient(0, 0, mWidth, 0, Color.WHITE, Color.GRAY, Shader.TileMode.CLAMP));
			break;
		}
		
		Paint thumb = new Paint();
		thumb.setStrokeWidth(3);
		thumb.setColor(Color.argb(200, 0, 0, 0));
		
		canvas.drawLine(0, mHeight/2, mWidth, mHeight/2, paint);
		
		if (mType==Type.BRUSH || mType==Type.HALFBRUSH) {
			Paint over = new Paint();
			over.setStrokeWidth(mHeight/1.2f);
			over.setStrokeCap(Paint.Cap.BUTT);
			over.setARGB(100, 128, 192, 255);
			canvas.drawLine(0, mHeight/2, ((float)getProgress()/getMax())*mWidth, mHeight/2, over);
		}

		canvas.drawLine(((float)getProgress()/getMax())*mWidth, 0, ((float)getProgress()/getMax())*mWidth, mHeight, thumb);
	}
}
