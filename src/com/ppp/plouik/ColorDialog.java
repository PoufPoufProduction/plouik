package com.ppp.plouik;

import org.jdom.Element;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * The color picker dialog
 * @author Johann Charlot from the APIDemos
 *
 */
public class ColorDialog extends SketchbookDialog {
	
	/** The XML Tag values */
	static public final String		TAG			= "color";
	static public final String		mTagH		= "color_hue";
	static public final String		mTagS		= "color_saturation";
	static public final String		mTagV		= "color_value";

	/** THE COLOR VALUES */
	static public int				color		= Color.BLACK;				// The current color
	
    /** Set the current color from the provided element information */
    static public void importCfg(Element _elt) {
    	if (((_elt!=null)&&(_elt.getChildText(mTagH)!=null))) {
    		color = Color.HSVToColor(new float[] {
    				(_elt.getChildText(mTagH)!=null)?Float.parseFloat(_elt.getChildText(mTagH)):0.f,
    				(_elt.getChildText(mTagS)!=null)?Float.parseFloat(_elt.getChildText(mTagS)):0.f,
    				(_elt.getChildText(mTagV)!=null)?Float.parseFloat(_elt.getChildText(mTagV)):0.f
    		});
    	}
    }
	
	/** Write the current color information */
	static public Element exportCfg() {
		Element ret = new Element(TAG);
		float hsv[] = new float[3]; Color.colorToHSV(color, hsv);
			
		Element eltH = new Element(mTagH);	eltH.setText(String.valueOf(hsv[0]));	ret.addContent(eltH);
		Element eltS = new Element(mTagS);	eltS.setText(String.valueOf(hsv[1]));	ret.addContent(eltS);
		Element eltV = new Element(mTagV);	eltV.setText(String.valueOf(hsv[2]));	ret.addContent(eltV);		
		return ret;
	}
	
	public interface OnColorChangedListener { void colorChanged(float[] color); }

	/** The dialog listener for returning the chosen color */
    private OnColorChangedListener	mListener;
 
    /** The content elements */
    private ColorPickerView			mColorView;
    private ImageView				mOkButton;
    private ImageView				mSwitchButton;
    private View					mLayoutColor;
    private TextView				mOverview;
    private PlouikBar				mSeekBarRed;
    private PlouikBar				mSeekBarGreen;
    private PlouikBar				mSeekBarBlue;
    private PlouikBar				mSeekBarHue;
    private PlouikBar				mSeekBarSaturation;
    private PlouikBar				mSeekBarValue;
    private LinearLayout[]			mLastLayouts = new LinearLayout[3];
    private int[]					mLastColors = new int[] { -1, -1, -1 };
    private int						mLastColor = -1;
    
    /** The settings panels */
    final private int[]				mSet = new int[] { R.id.colordialog_colorview, R.id.colordialog_rgb, R.id.colordialog_hsv };
    private int						mSetId = 0;
    
    /**
     * The Color dialog constructor
     * @param context is the current dialog context
     * @param listener is the listener for the returned value
     */
    public ColorDialog(Context context, OnColorChangedListener listener )
    {
        super(context);
        mListener = listener;
    }
    
    /**
     * Return the current color
     * @return
     */
    public float getHue() { return mColorView.isOk()?mColorView.getHSVColor()[0]:-1.0f; }
    
    /**
     * Return the current color
     * @return
     */
    public float getSaturation() { return mColorView.isOk()?mColorView.getHSVColor()[1]:0f; }
    
    /**
     * Return the current color
     * @return
     */
    public float getValue() { return mColorView.isOk()?mColorView.getHSVColor()[2]:0f; }
    
    /**
     * Update the last colors
     */
    private void updateLastColors() {
    	mLastLayouts[0].setBackgroundColor(mLastColors[0]);
    	mLastLayouts[1].setBackgroundColor(mLastColors[1]);
    	mLastLayouts[2].setBackgroundColor(mLastColors[2]);
    }
    
    /**
     * The classical onCreate method
     */
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.colordialog);
        
        // Get the color picker view thanks to its parent. I am not able to get it directly with
        // mColorView = (ColorPickerView) findViewById(R.id.colordialog_colorview);
        LinearLayout foo = (LinearLayout) findViewById(R.id.colordialog_foo);
        int padding = getContext().getResources().getDimensionPixelSize(R.dimen.padding);
        foo.setPadding(padding, padding, padding, padding);
        mColorView = (ColorPickerView) foo.getChildAt(0);
    	
        mColorView.setOnColorChange(new ColorPickerView.ColorPickerListener() {
			public void onChange(float [] hsv, int eventId) {
				color=Color.HSVToColor(hsv);
				
				mLayoutColor.setBackgroundColor(color);
				String rHex = Integer.toHexString(Color.red(color)).toUpperCase();
				String gHex = Integer.toHexString(Color.green(color)).toUpperCase();
				String bHex = Integer.toHexString(Color.blue(color)).toUpperCase();
				rHex = rHex.length()<2?"0"+rHex:rHex;
				gHex = gHex.length()<2?"0"+gHex:gHex;
				bHex = bHex.length()<2?"0"+bHex:bHex;
				
				String rDec = Integer.toString(Color.red(color));
				String gDec = Integer.toString(Color.green(color));
				String bDec = Integer.toString(Color.blue(color));
				while (rDec.length()<3) { rDec = "0" + rDec; }
				while (gDec.length()<3) { gDec = "0" + gDec; }
				while (bDec.length()<3) { bDec = "0" + bDec; }
				
				String hDec = Integer.toString((int)hsv[0]);
				String sDec = Integer.toString((int)(hsv[1]*100));
				String vDec = Integer.toString((int)(hsv[2]*100));
				while (hDec.length()<3) { hDec = "0" + hDec; }
				while (sDec.length()<3) { sDec = "0" + sDec; }
				while (vDec.length()<3) { vDec = "0" + vDec; }
				
				mOverview.setTextColor(hsv[2]<0.4?Color.WHITE:Color.BLACK);
				
				mOverview.setText("WEB: #"+rHex+gHex+bHex+"\nRGB: "+rDec+","+gDec+","+bDec+"\nHSV: "+hDec+","+sDec+","+vDec);
				
				mSeekBarRed.setProgress(Color.red(color));
				mSeekBarRed.setRGB(color);
				
				mSeekBarGreen.setProgress(Color.green(color));
				mSeekBarGreen.setRGB(color);
				
				mSeekBarBlue.setProgress(Color.blue(color));
				mSeekBarBlue.setRGB(color);
				
				mSeekBarHue.setProgress((int)hsv[0]);
				mSeekBarHue.setHSV(hsv);
				
				mSeekBarSaturation.setProgress((int)(hsv[1]*100));
				mSeekBarSaturation.setHSV(hsv);
				
				mSeekBarValue.setProgress((int)(hsv[2]*100));
				mSeekBarValue.setHSV(hsv);
				
				mLastColor = -1;
			}	
	    });
        
        // Get the validation button and handles the press event
        mOkButton = (ImageView) findViewById(R.id.colordialog_ok);
        mOkButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int newColor = Color.HSVToColor(mColorView.getHSVColor());
				if (mLastColor != 0) {
					if (mLastColor != 1) {
						mLastColors[2]=mLastColors[1];
						mLastColors[1]=mLastColors[0];
						mLastColors[0]=newColor;
						mLastColor=0;
					}
					else {
						mLastColors[1]=mLastColors[0];
						mLastColors[0]=newColor;
						mLastColor=0;
					}
				}
				
				if (mListener!= null) { mListener.colorChanged(mColorView.getHSVColor()); }
				dismiss();
				
				updateLastColors();
			}
        });
        
        // The last colors
		mLastLayouts[0]=(LinearLayout) findViewById(R.id.colordialog_last1);
		mLastLayouts[1]=(LinearLayout) findViewById(R.id.colordialog_last2);
		mLastLayouts[2]=(LinearLayout) findViewById(R.id.colordialog_last3);
		mLastLayouts[0].setOnClickListener(new View.OnClickListener() {
			public void onClick(View v){ mColorView.setColor(mLastColors[0]); mLastColor=0; mColorView.invalidate();}});
		mLastLayouts[1].setOnClickListener(new View.OnClickListener() {
			public void onClick(View v){ mColorView.setColor(mLastColors[1]); mLastColor=1; mColorView.invalidate();}});
		mLastLayouts[2].setOnClickListener(new View.OnClickListener() {
			public void onClick(View v){ mColorView.setColor(mLastColors[2]); mLastColor=2; mColorView.invalidate();}});
        
        // Initialize the overview layout color
        mLayoutColor = (View) findViewById(R.id.colordialog_prev);
        mLayoutColor.setBackgroundColor(Color.BLACK);
        mLayoutColor.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mColorView.isOk()) {
					SketchbookView.BACKGROUND = Color.HSVToColor(mColorView.getHSVColor());
				}
				else {
					SketchbookView.BACKGROUND = Color.GRAY;
				}
			}
		});
        
        // The pop up overview
        mOverview = (TextView) findViewById(R.id.colordialog_pop);
        
        // The switch button
        mSwitchButton = (ImageView) findViewById(R.id.colordialog_switch);
        mSwitchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mSetId = (mSetId+1)%mSet.length;
				for (int i=0; i<mSet.length; i++) {
					if (i==0) {
						mColorView.setVisibility((mSetId==i)?View.VISIBLE:View.GONE);
					}
					else {
						findViewById(mSet[i]).setVisibility((mSetId==i)?View.VISIBLE:View.GONE);
					}
				}
			}
		});
        
        // The seekBar
        mSeekBarRed = (PlouikBar) findViewById(R.id.colordialog_redSeek);
        mSeekBarGreen = (PlouikBar) findViewById(R.id.colordialog_greenSeek);
        mSeekBarBlue = (PlouikBar) findViewById(R.id.colordialog_blueSeek);
        mSeekBarHue = (PlouikBar) findViewById(R.id.colordialog_hueSeek);
        mSeekBarSaturation = (PlouikBar) findViewById(R.id.colordialog_saturationSeek);
        mSeekBarValue = (PlouikBar) findViewById(R.id.colordialog_valueSeek);
        
        mSeekBarRed.setMax(255);mSeekBarGreen.setMax(255);mSeekBarBlue.setMax(255);
        mSeekBarHue.setMax(359);mSeekBarSaturation.setMax(100);mSeekBarValue.setMax(100);
        
        mSeekBarRed.setType(PlouikBar.Type.RED);
        mSeekBarGreen.setType(PlouikBar.Type.GREEN);
        mSeekBarBlue.setType(PlouikBar.Type.BLUE);
        mSeekBarHue.setType(PlouikBar.Type.HUE);
        mSeekBarSaturation.setType(PlouikBar.Type.SATURATION);
        mSeekBarValue.setType(PlouikBar.Type.VALUE);
        
        SeekBar.OnSeekBarChangeListener rgbListener = new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) { }
			public void onStartTrackingTouch(SeekBar seekBar) { }
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					float[] hsv = new float[3];
					Color.colorToHSV(Color.rgb(mSeekBarRed.getProgress(), mSeekBarGreen.getProgress(), mSeekBarBlue.getProgress()), hsv);
					mColorView.setHSVColor(hsv[0], hsv[1], hsv[2]);
				}
			}
		};
		mSeekBarRed.setOnSeekBarChangeListener(rgbListener);
		mSeekBarGreen.setOnSeekBarChangeListener(rgbListener);
		mSeekBarBlue.setOnSeekBarChangeListener(rgbListener);
        
        SeekBar.OnSeekBarChangeListener hsvListener = new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) { }
			public void onStartTrackingTouch(SeekBar seekBar) { }
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					mColorView.setHSVColor((float)mSeekBarHue.getProgress(),
						(float)mSeekBarSaturation.getProgress()/100f,
						(float)mSeekBarValue.getProgress()/100f);
				}
			}
		};
		mSeekBarHue.setOnSeekBarChangeListener(hsvListener);
		mSeekBarSaturation.setOnSeekBarChangeListener(hsvListener);
		mSeekBarValue.setOnSeekBarChangeListener(hsvListener);
		
		// The seekbutton
		findViewById(R.id.colordialog_red).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mSeekBarRed.setProgress(mSeekBarRed.getProgress()+1);
				float[] hsv = new float[3];
				Color.colorToHSV(Color.rgb(mSeekBarRed.getProgress(), mSeekBarGreen.getProgress(), mSeekBarBlue.getProgress()), hsv);
				mColorView.setHSVColor(hsv[0], hsv[1], hsv[2]);
			}
		});
		findViewById(R.id.colordialog_green).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mSeekBarGreen.setProgress(mSeekBarGreen.getProgress()+1);
				float[] hsv = new float[3];
				Color.colorToHSV(Color.rgb(mSeekBarRed.getProgress(), mSeekBarGreen.getProgress(), mSeekBarBlue.getProgress()), hsv);
				mColorView.setHSVColor(hsv[0], hsv[1], hsv[2]);
			}
		});
		findViewById(R.id.colordialog_blue).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mSeekBarBlue.setProgress(mSeekBarBlue.getProgress()+1);
				float[] hsv = new float[3];
				Color.colorToHSV(Color.rgb(mSeekBarRed.getProgress(), mSeekBarGreen.getProgress(), mSeekBarBlue.getProgress()), hsv);
				mColorView.setHSVColor(hsv[0], hsv[1], hsv[2]);
			}
		});
		findViewById(R.id.colordialog_hue).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mSeekBarHue.setProgress(mSeekBarHue.getProgress()+1);
				mColorView.setHSVColor((float)mSeekBarHue.getProgress(),
						(float)mSeekBarSaturation.getProgress()/100f,
						(float)mSeekBarValue.getProgress()/100f);
			}
		});
		findViewById(R.id.colordialog_saturation).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mSeekBarSaturation.setProgress(mSeekBarSaturation.getProgress()+1);
				mColorView.setHSVColor((float)mSeekBarHue.getProgress(),
						(float)mSeekBarSaturation.getProgress()/100f,
						(float)mSeekBarValue.getProgress()/100f);
			}
		});
		findViewById(R.id.colordialog_value).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mSeekBarValue.setProgress(mSeekBarValue.getProgress()+1);
				mColorView.setHSVColor((float)mSeekBarHue.getProgress(),
						(float)mSeekBarSaturation.getProgress()/100f,
						(float)mSeekBarValue.getProgress()/100f);
			}
		});
    }
    
    // Not really pretty, but need to not modify the last color used
    @Override public void onShow() { int last=mLastColor; mColorView.setColor(color); mLastColor=last; }
	
}
