package com.ppp.plouik;

import org.jdom.Element;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Dialog for patterns selection
 * @author Pouf-Pouf Production
 *
 */
public class PatternDialog extends SketchbookDialog {
	
	/** The XML Tag values */
	static public final String		TAG				= "pattern";
	static public final String		mTagP			= "pattern_pointer";
	static public final String		mTagN			= "pattern_name";
	static public final String		mTagI			= "pattern_neg1";
	static public final String		mTagZ			= "pattern_zoom1";
	static public final String		mTagR			= "pattern_rotate1";
	
	/** THE PATTERN VALUES */
	static public Pattern			pattern			= Pattern.NONE;
	static public PointerState		pointer			= PointerState.ALIGN; 
	static public Bitmap			patternBitmap	= null;
	static public boolean			patternNeg		= false;
	static public int				patternZoom		= 0;
	static public int				patternRot		= 0;
	static boolean 					patternChange	= true;
	
	
	/** Set the current pattern from the provided element information */
    static public void importCfg(Element _elt, Resources _res) {
    	if (_elt!=null) {
    		pattern		= (_elt.getChildText(mTagN)!=null)?Pattern.getFromName(_elt.getChildText(mTagN)):Pattern.NONE;
    		pointer		= (_elt.getChildText(mTagP)!=null)?PointerState.getFromId(Integer.parseInt(_elt.getChildText(mTagP))):PointerState.ALIGN;
    		patternNeg	= (_elt.getChildText(mTagI)!=null)?Boolean.parseBoolean(_elt.getChildText(mTagI)):false;
    		patternZoom	= (_elt.getChildText(mTagZ)!=null)?Integer.parseInt(_elt.getChildText(mTagZ)):0;
    		patternRot 	= (_elt.getChildText(mTagR)!=null)?Integer.parseInt(_elt.getChildText(mTagR)):0;
    		patternChange = true;
    		updatePattern(_res);
    	}
    }
    	
	/** Write the current pattern settings */
	static public Element exportCfg() {
		Element ret = new Element(TAG);
		
		Element eltN = new Element(mTagN);		eltN.setText(pattern.getName());				ret.addContent(eltN);
		Element eltP  = new Element(mTagP);		eltP.setText(String.valueOf(pointer.getId()));	ret.addContent(eltP);
		Element eltI = new Element(mTagI);		eltI.setText(Boolean.toString(patternNeg));		ret.addContent(eltI);
		Element eltZ = new Element(mTagZ);		eltZ.setText(String.valueOf(patternZoom)); 		ret.addContent(eltZ);
		Element eltR = new Element(mTagR);		eltR.setText(String.valueOf(patternRot)); 		ret.addContent(eltR);
		
		return ret;
	}
	
	/** Update the pattern bitmap */
	static private void updatePattern(Resources _res) {
		if (patternChange) {
			if (pattern==Pattern.NONE) {
				if (patternBitmap!=null) { patternBitmap.recycle(); patternBitmap = null; }
			}
			else {
				if (patternBitmap==null) { patternBitmap=Bitmap.createBitmap(256, 256, Config.ARGB_8888); }
				Canvas vCanvas = new Canvas(patternBitmap);
				vCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.DST_ATOP);
				
				BitmapFactory.Options vOptions = new BitmapFactory.Options();
				vOptions.inSampleSize = 1<<patternZoom;
				Bitmap vPatTmp = BitmapFactory.decodeResource(_res, pattern.getRes(false), vOptions);
				Bitmap vPattern = vPatTmp;
				int vSize = vPatTmp.getWidth();
				if (vPatTmp!=null && patternRot>0) {
					Matrix vMatrix = new Matrix();
					vMatrix.postRotate(90*patternRot);
					vPattern = Bitmap.createBitmap(vPatTmp, 0, 0, vSize, vSize, vMatrix, true);
				}
				if (vPattern!=null) {
					int vNb = 256/vSize;
					int vOffset = (vNb==1?0:1);
					for (int i=0; i<vNb+vOffset; i++) {
						for (int j=0; j<vNb+vOffset; j++) {
							vCanvas.drawBitmap(vPattern, i*vSize-(vOffset*vSize/2), j*vSize-(vOffset*vSize/2), null);
						}
					}
				}
			}
			patternChange = false;
		}
	}
	
	/** Update the pattern bitmap from dialog */
	private void updatePattern() {
		updatePattern(getContext().getResources());
		int vSize = getContext().getResources().getDimensionPixelSize(R.dimen.pattern);
		mThumbnail.setBackgroundColor(Color.BLACK);
		if (pattern!=Pattern.NONE) {
			Bitmap vBitmap = Bitmap.createBitmap(vSize, vSize, Config.ARGB_8888);
			Canvas vCanvas = new Canvas(vBitmap);
			
			int vOffset = (patternBitmap.getWidth()-vSize)/2;
			
			if (patternNeg) {
				vCanvas.drawColor(Color.BLACK);
				vCanvas.drawBitmap(patternBitmap, -vOffset, -vOffset, null);
			}
			else {
				vCanvas.drawColor(Color.WHITE);
				Paint vPaint = new Paint();
				vPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
				vCanvas.drawBitmap(patternBitmap, -vOffset, -vOffset, vPaint);
			}
			mThumbnail.setImageBitmap(vBitmap);
		}
		else {
			mThumbnail.setImageBitmap(null);
		}
	}
	
	/** The patterns */
	public enum Pattern {
		NONE		("none",		R.drawable.iconground,			0),
		STONES01	("stones01",	R.drawable.patternstones01off,	R.drawable.patternstones01),
		STONES02	("stones02",	R.drawable.patternstones02off,	R.drawable.patternstones02),
		STONES03	("stones03",	R.drawable.patternstones03off,	R.drawable.patternstones03),
		WOOD01		("wood01",		R.drawable.patternwood01off,	R.drawable.patternwood01),
		WOOD02		("wood02",		R.drawable.patternwood02off,	R.drawable.patternwood02),
		PAPER01		("paper01",		R.drawable.patternpaper01off,	R.drawable.patternpaper01),
		CHECK01		("check01",		R.drawable.patterncheck01off,	R.drawable.patterncheck01),
		DOTS01		("dots01",		R.drawable.patterndots01off,	R.drawable.patterndots01),
		DOTS02		("dots02",		R.drawable.patterndots02off,	R.drawable.patterndots02),
		LINES01		("lines01",		R.drawable.patternlines01off,	R.drawable.patternlines01),
		LINES02		("lines02",		R.drawable.patternlines02off,	R.drawable.patternlines02),
		STARS01		("stars01",		R.drawable.patternstars01off,	R.drawable.patternstars01),
		STARS02		("stars02",		R.drawable.patternstars02off,	R.drawable.patternstars02),
		HEART01		("heart01",		R.drawable.patternheart01off,	R.drawable.patternheart01),
		FLOWERS02	("flowers02",	R.drawable.patternflowers01off,	R.drawable.patternflowers01);
		
		/** The pattern drawable ressources */
		final private int		mResThumb;
		final private int		mRes;
		
		/** The pattern name for import/export */
		final private String	mName;
		
		/** Constructor */
		Pattern(String _name, int _resThumb, int _res) {
			mName = _name; mResThumb = _resThumb; mRes = _res; }
		
		/** Some accessors */
		public int getRes(boolean _tumb)			{ return _tumb?mResThumb:mRes; }
		public String getName()						{ return mName; }
		
		/** Return the PresetIcon regarding its name */
		static Pattern getFromName(String _name) {
			Pattern ret = NONE;
			for (Pattern i : Pattern.values()) {
				if (i.getName().equals(_name)) { ret = i; break; }
			}
			return ret;
		}
	}
	
	/** The values of the pointer state */
	public enum PointerState {	
		ALIGN(R.drawable.iconalign, 0),			// The center of the pattern is the first ACTION_DOWN event (forever)
		PIN(R.drawable.iconpin, 1),				// The center of the pattern is the ACTION_DOWN event (new each time)
		RAND(R.drawable.icondice, 2);			// The center of the pattern is randomly chosen
		
		private int			mRes;				// The ressource bitmap
		private int			mId;				// The pointer id
		
		/** Some useful accessors */
		public int getRes()	{ return mRes; }
		public int getId()	{ return mId; }
		
		/** Get the pointer from its id */
		static public PointerState getFromId (int value) {
			PointerState ret = ALIGN;
			for (PointerState t : PointerState.values()) { if (t.mId==value) { ret = t; } }
			return ret;
		}
		
		/** Constructor */
		PointerState(int _res, int _id) { mRes = _res; mId = _id; }
	}
	
	/** The mandatory adapter class for the grid view filling */
	private class PatternAdapter extends BaseAdapter {
		private class IconListener implements View.OnClickListener {
			private int pos;
			public IconListener(int _pos) { super(); pos = _pos; }
			public void onClick(View v) {
				if (pattern!=Pattern.values()[pos]) {
					patternChange 	= true;
					pattern			= Pattern.values()[pos];
					patternNeg 		= false;
					patternZoom		= 0;
					patternRot 		= 0;
					updatePattern();
				}
				mLayout.setVisibility(View.VISIBLE);
				mGridView.setVisibility(View.GONE);
			}
		}
		
		public int 		getCount()			{ return Pattern.values().length; }
		public Object 	getItem(int arg0) 	{ return null; }
		public long 	getItemId(int arg0) { return 0;	}
		
		/** Return the preset icon button as ImageView with the good listener */
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			
			ImageView vRet = new ImageView(getContext());
			vRet.setImageResource(Pattern.values()[arg0].getRes(true));
			vRet.setOnClickListener(new IconListener(arg0));
			vRet.setLayoutParams(new GridView.LayoutParams(
					getContext().getResources().getDimensionPixelSize(R.dimen.icon),
					getContext().getResources().getDimensionPixelSize(R.dimen.icon)));
			vRet.setPadding(0, 0, 0, 0);
			vRet.setBackgroundColor(Color.BLACK);
			return vRet;
		}
	}
	
	/** the panel stuff */
	private LinearLayout				mLayout;
	private GridView					mGridView;
	private ImageView					mPointerButton;
	private ImageView					mThumbnail;
	
	public interface 					OnPatternChangedListener { void patternChanged(PatternDialog.Pattern _pat); }
    private OnPatternChangedListener	mListener;

    /** Constructor */
	public PatternDialog(Context context, OnPatternChangedListener listener) { super(context); mListener = listener; }
	
	/** Handle the different states of the pointer button */
	private void togglePointerButton(boolean _change) {
		if (_change) { pointer=PointerState.getFromId((pointer.getId()+1)%PointerState.values().length); }
		mPointerButton.setImageResource(pointer.getRes());
	}
	
	/** Prepare the panel before showing */
	@Override public void onShow() {
		togglePointerButton(false);
		updatePattern();
	}
	
	/** Create the PatternDialog */
	@Override protected void onCreate(Bundle savedInstanceState) {
		ImageView	vButton;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patterndialog);
        
        mLayout = (LinearLayout) findViewById(R.id.patterndialog_stuff);
        
        mGridView = (GridView) findViewById(R.id.patterndialog_gallery);
        mGridView.setVisibility(View.GONE);
        mGridView.setAdapter(new PatternAdapter());
        
        //=============================
        // The buttons settings
        //=============================
        
        // The Validation button
        vButton = (ImageView) findViewById(R.id.patterndialog_ok);
        vButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { if (mListener!= null) { mListener.patternChanged(Pattern.NONE) ; } dismiss(); }
        });
        
        // The Fixed Button
    	mPointerButton = (ImageView) findViewById(R.id.patterndialog_pointer);
    	mPointerButton.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { togglePointerButton(true); } });
    	
    	// The Operation buttons
    	vButton = (ImageView) findViewById(R.id.patterndialog_neg);
    	vButton.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { patternChange = true; patternNeg=!patternNeg; updatePattern(); }});
    	vButton = (ImageView) findViewById(R.id.patterndialog_zoom);
    	vButton.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { patternChange = true; patternZoom=(patternZoom+1)%3; updatePattern(); }});
    	vButton = (ImageView) findViewById(R.id.patterndialog_rot);
    	vButton.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { patternChange = true; patternRot=(patternRot+1)%4; updatePattern(); }});
    	
    	// The thumbnail
    	mThumbnail = (ImageView) findViewById(R.id.patterndialog_thumbnail);
    	mThumbnail.setBackgroundColor(Color.BLACK);
    	mThumbnail.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mLayout.setVisibility(View.GONE); mGridView.setVisibility(View.VISIBLE); }
        });
	}
}
