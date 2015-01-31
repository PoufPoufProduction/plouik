package com.ppp.plouik;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class SketchbookStamp extends BaseAdapter{
	
	public enum StampType { NORMAL, THIN, RANDOM, BOLD, BLUR, SQUARE, FREE };
	
	public interface Stamp 		{ int getResIcon(); String getName();
								  public void init();
								  public void draw(Canvas _canvas, float _x, float _y, float _size, float _opacity, Paint _paint, int _position); }
	public interface Listener	{ void 	onClick(); }
	private class StampListener implements View.OnClickListener {
		private int pos;
		public StampListener(int _pos) 	{ super(); pos = _pos; }
		public void onClick(View v) 	{ stamp = pos; if (listener!=null) { listener.onClick();} }
	}
	
	private int							stamp = 0;
	
	final private Stamp 				stamps[];
	final private Context				context;
	final private Listener				listener;
	
	private class StampLine implements Stamp {
		final private int 			mResIcon;
		final private String 		mName;
		final private StampType		mType;
		
		private ArrayList<Float>	mValues;
		
		StampLine(int _resIcon, String _name, StampType _type) 	{
			mResIcon = _resIcon; mName = _name; mType=_type;
			mValues = new ArrayList<Float>();
		}
		public int getResIcon() 								{ return mResIcon; }
		public String getName()									{ return mName; }
		public void init() 										{ mValues.clear(); }
		
		public void draw(Canvas _canvas, float _x, float _y, float _size, float _opacity, Paint _paint, int _position) {
			Paint vPaint = new Paint(_paint);
			float vSize = vPaint.getStrokeWidth()*_size;
			
			int vAlpha = (int) (vPaint.getAlpha()*_opacity);
			vPaint.setAlpha(vAlpha);
			vPaint.setStrokeCap(Paint.Cap.ROUND);
			
			switch(mType) {
				case NORMAL :	vPaint.setStrokeWidth(vSize/10); break;
				case THIN	:	vPaint.setStrokeWidth(vSize/20); break;
				case RANDOM :	vPaint.setStrokeWidth(vSize/5); break;
				case BOLD	:	vPaint.setStrokeWidth(vSize/2); break;
				case BLUR	:	vPaint.setStrokeWidth(vSize/2.5f);
								if (vSize>0) { vPaint.setMaskFilter(new BlurMaskFilter(vSize/10, BlurMaskFilter.Blur.NORMAL)); } break;
				case SQUARE	: 	vPaint.setStrokeWidth(vSize/2); vPaint.setStrokeCap(Paint.Cap.SQUARE); break;
			}
			if (vPaint.getStrokeWidth()<.2f) { vPaint.setStrokeWidth(.2f); }
			Float l = (_position*2<mValues.size())?mValues.get(_position*2):null;
			if (l==null) {
				l = .45f;
				if (mType==StampType.RANDOM) 							{ l = (float) Math.sqrt(Math.random())*.40f; }
				if ((mType==StampType.BOLD) || (mType==StampType.BLUR) || (mType==StampType.SQUARE) ) {
					l = (float) Math.sqrt(Math.random())*.30f; }
				mValues.add(_position*2, l);
			}
			l*=vSize;
			
			Float a = (_position*2+1<mValues.size())?mValues.get(_position*2+1):null;
			if (a==null) {
				a = (float) (Math.random()*Math.PI);
				mValues.add(_position*2+1, a);
			}
			_canvas.drawLine((float)(_x-l*Math.cos(a)), (float)(_y-l*Math.sin(a)), (float)(_x+l*Math.cos(a)), (float)(_y+l*Math.sin(a)), vPaint);
		}

	}


	private class StampPath implements Stamp {
		final private int 			mResIcon;
		final private String 		mName;
		final private StampType		mType;
		final private int			mNb;
		
		private ArrayList<Float>	mValues;
		
		StampPath(int _resIcon, String _name, StampType _type) 	{
			mResIcon = _resIcon; mName = _name; mType=_type;
			mValues = new ArrayList<Float>();
			switch (mType) {
			case FREE: 		mNb = 14; 	break;
			default:		mNb = 10;	break;
			}
		}
		public int getResIcon() 								{ return mResIcon; }
		public String getName()									{ return mName; }
		public void init() 										{ mValues.clear(); }
		
		public void draw(Canvas _canvas, float _x, float _y, float _size, float _opacity, Paint _paint, int _position) {
			Paint vPaint = new Paint(_paint);
			float vSize = vPaint.getStrokeWidth()*_size;
			vPaint.setStyle(Paint.Style.FILL);
			
			int vAlpha = (int) (vPaint.getAlpha()*_opacity);
			vPaint.setAlpha(vAlpha);
			
			int sizeValue = mNb+1;
			Float vOffset = (_position*sizeValue<mValues.size())?mValues.get(_position*sizeValue):null;
			if (vOffset==null) {
				vOffset = (float) ((2f*Math.PI)*Math.random());
				mValues.add(_position*sizeValue, vOffset);
			}
			
			Path path = new Path();
			for (int i=0; i<mNb; i++) {
				float rad = (float) (2f*Math.PI*i/mNb) + vOffset;
				
				Float l = (_position*sizeValue+(i+1)<mValues.size())?mValues.get(_position*sizeValue+(i+1)):null;
				if (l==null) {
					switch(mType) {
					case SQUARE:				l = (i%2==0)?1f/2f:1f/4.4f; break;
					case FREE:					l = (i%2==0)?(float) ((Math.random()+1)/4f):1f/4.2f; break;
					case BOLD:					l = (float) ((Math.random()+1)/4f); break;
					default: 					l = (float) (Math.random()/2f); break;
					}
					mValues.add(_position*sizeValue+(i+1), l);
				}
				l*=vSize;
				
				if (i==0) 	{ path.moveTo((float)(_x+l*Math.cos(rad)), (float)(_y+l*Math.sin(rad))); }
				else		{ path.lineTo((float)(_x+l*Math.cos(rad)), (float)(_y+l*Math.sin(rad))); }
			}
			_canvas.drawPath(path, vPaint);
		}

	}
	
	private class StampPoint implements Stamp {
		final private int 			mResIcon;
		final private String 		mName;
		final private float			mHardness;
		
		
		StampPoint(int _resIcon, String _name, float _hardness) { mResIcon = _resIcon; mName = _name; mHardness=_hardness; }
		StampPoint(int _resIcon, String _name) 					{ mResIcon = _resIcon; mName = _name; mHardness=1f;	}
		public int getResIcon() 								{ return mResIcon; }
		public String getName()									{ return mName; }
		public void init() 										{}
		
		public void draw(Canvas _canvas, float _x, float _y, float _size, float _opacity, Paint _paint, int _position) {
			Paint vPaint = new Paint(_paint);
			float vSize = vPaint.getStrokeWidth()*_size;
			vPaint.setStrokeWidth(mHardness*vSize);
			
			int vAlpha = (int) (vPaint.getAlpha()*_opacity);
			vPaint.setAlpha(vAlpha);
			
			if (mHardness<1.0f && vSize>0f) {
				vPaint.setMaskFilter(new BlurMaskFilter((1.0f-mHardness)*vSize/2, BlurMaskFilter.Blur.NORMAL));
			}
			_canvas.drawLine(_x, _y, _x+.01f, _y, vPaint);
		}
	}
	
	private void init() {
		stamps[0] = new StampPoint(R.drawable.iconround, "default");
		stamps[1] = new StampPoint(R.drawable.iconround03, "soft", .65f);
		stamps[2] = new StampPoint(R.drawable.iconround02, "blur", .4f);
		stamps[3] = new StampLine(R.drawable.iconline01, "line", StampType.NORMAL);
		stamps[4] = new StampLine(R.drawable.iconline02, "linethin", StampType.THIN);
		stamps[5] = new StampLine(R.drawable.iconline03, "linerandom", StampType.RANDOM);
		stamps[6] = new StampLine(R.drawable.iconline04, "linebold", StampType.BOLD);
		stamps[7] = new StampLine(R.drawable.iconline05, "lineblur", StampType.BLUR);
		stamps[8] = new StampLine(R.drawable.iconline06, "linesquare", StampType.SQUARE);
		stamps[9] = new StampPath(R.drawable.iconpath01, "path", StampType.NORMAL);
		stamps[10] = new StampPath(R.drawable.iconpath02, "pathbold", StampType.BOLD);
		stamps[11] = new StampPath(R.drawable.iconpath03, "pathfree", StampType.FREE);
		stamps[12] = new StampPath(R.drawable.iconstar, "pathsquare", StampType.SQUARE);
	}
	
	public SketchbookStamp() {
		context = null; listener = null;
		stamps = new Stamp[13];
		init();
	}
	
	public SketchbookStamp(Context _context, Listener _listener) {
		context 	= _context;
		listener 	= _listener;
		stamps 		= new Stamp[13];
		init();
	}
	
	public int getResIcon()					{ return stamps[stamp].getResIcon(); }
	public String getName()					{ return stamps[stamp].getName(); }
	public void setStamp(int _val)			{ if (_val>0 && _val<size()) { stamp = _val; } }
	public void setStamp(String _val)		{
		int value = -1;
		for (int i=0; i<size(); i++) { if (stamps[i].getName().equalsIgnoreCase(_val)) { value = i; } }
		if (value>0) { stamp = value; stamps[stamp].init(); }
	}
	public void release()					{ stamps[stamp].init(); }
	
	public int size() 						{ return stamps.length; }
	public int getCount() 					{ return size();		}
	public Object getItem(int position) 	{ return null;			}
	public long getItemId(int position) 	{ return 0;  			}
	
	public void draw(Canvas _canvas, float _x, float _y, float _size, float _opacity, Paint _paint, int _position) {
		stamps[stamp].draw(_canvas, _x, _y, _size, _opacity, _paint, _position);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView vRet = null;
		if (context!=null) {
			vRet = new ImageView(context);
			vRet.setImageResource(stamps[position].getResIcon());
			//vRet.setBackgroundColor(Color.rgb(11, 16, 51));
			vRet.setOnClickListener(new StampListener(position));
			vRet.setLayoutParams(new GridView.LayoutParams(
					context.getResources().getDimensionPixelSize(R.dimen.icon),
					context.getResources().getDimensionPixelSize(R.dimen.icon)));
			vRet.setPadding(0, 0, 0, 0);
		}
		return vRet;
	}

}
