package com.ppp.plouik;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.jdom.Element;

/**
 * Lock dialog from options menu
 * @author Pouf-Pouf Production
 *
 */
public class ToolsDialog extends SketchbookDialog{
	
	/** The XML Tag values
	 *  mTagV is static because, even if the dialog is not created,
	 *  we have to be able to extract the tools visibility value (from a data file)
	 */
	static public final String		TAG		= "tools";
	static public final String		mTagV	= "tools_visibility";
	static public final String		mTagO	= "tools_orientation";
	static public final String		mTagMi	= "tools_pressure_min";
	static public final String		mTagMa	= "tools_pressure_max";
	
	static public final int 		GONE 	= 0x00;
	static public final int 		VISIBLE	= 0x01;
	static public final int 		LOCK 	= 0x02;
	
	/** THE TOOLS VALUES */
	static public int				visibility 			= 321;
	static public boolean			orientationSensor 	= false;
	
	static public float				pressure_min		= 0f;
	static public float				pressure_max		= 1.0f;
	
	static public int				getOrientation() {
		return orientationSensor?
				android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR:android.content.pm.ActivityInfo.SCREEN_ORIENTATION_NOSENSOR;
	}
	
	/** Read and configure */
	static public void importCfg(Element _elt) {
		if (_elt!=null) {
			visibility 			= (_elt.getChildText(mTagV)!= null)?Integer.parseInt(_elt.getChildText(mTagV)):321;
			orientationSensor 	= (_elt.getChildText(mTagO)!=null)?_elt.getChildText(mTagO).equals("Sensor"):false;
			
			Plouik.mScreenOrientation = getOrientation();
			
			if (_elt.getChildText(mTagMi)!=null) { pressure_min = Float.parseFloat(_elt.getChildText(mTagMi)); }
			if (_elt.getChildText(mTagMa)!=null) { pressure_max = Float.parseFloat(_elt.getChildText(mTagMa)); }
			
		}	
	}

	/** Write the current tools configuration */
	static public Element exportCfg(boolean _save) {
		Element ret = new Element(TAG);
		
		Element retV = new Element(mTagV);	retV.setText(String.valueOf(visibility));				ret.addContent(retV);
		Element retO = new Element(mTagO);	retO.setText(orientationSensor?"Sensor":"NoSensor");	ret.addContent(retO);
		
		if (!_save) {
			Element retMi= new Element(mTagMi);	retMi.setText(String.valueOf(pressure_min));				ret.addContent(retMi);
			Element retMa = new Element(mTagMa);retMa.setText(String.valueOf(pressure_max));	ret.addContent(retMa);
		}
		
		return ret;
	}

	/** The validation button */
	private ImageView				mOkButton;
	private ImageView				mPreset1Button;
	private ImageView				mPreset2Button;
	private ImageView				mBrushButton;
	private ImageView				mPatternButton;
	private ImageView				mColorButton;
	private ImageView				mOperationButton;
	private ImageView				mUndoButton;
	
	private ImageView				mOrientationButton;
	private ImageView				mPressureButton;
	private TextView				mPressureButtonPop;
	private ArrayList<Float>		mPressureValues;
	
	/** Some helpful methods */
	static public boolean isPresetLocked(int _id) 	{ return ((visibility>>(_id==1?12:10))%4==LOCK); }
	static public boolean isUndoVisible()			{ return ((visibility%2)!=0); }
	static public boolean isOperationVisible()		{ return (((visibility>>2)%2)!=0); }

	static public void	  setPresetLocked(int _id) 	{
		int current = (visibility>>(_id==1?12:10))%4;
		visibility = visibility - (current<<(_id==1?12:10)) + (LOCK<<(_id==1?12:10));
	}
	
	/** The ToolsDialogListener used for getting results */
	public interface ToolsDialogListener 			{ public void onOKClick(); }
	
	/** The ToolsDialogListener instance */
	private ToolsDialogListener mListener;
	
	/**
	 * The tools dialog constructor
	 * @param context is the current context
	 * @param listener is the listener for getting the results
	 */
	public ToolsDialog(Context context, ToolsDialogListener listener) { super(context); mListener = listener; }
	
	private void updateButtons(boolean _compute) {
		int value;
		
		if (_compute) {
			visibility = ( Integer.parseInt(mPreset1Button.getTag().toString())<<12 )
			  	| ( Integer.parseInt(mPreset2Button.getTag().toString())<<10 )
			  	| ( Integer.parseInt(mBrushButton.getTag().toString())<<8 )
			  	| ( Integer.parseInt(mColorButton.getTag().toString())<<6 )
			  	| ( Integer.parseInt(mPatternButton.getTag().toString())<<4 )
			  	| ( Integer.parseInt(mOperationButton.getTag().toString())<<2 )
			  	| ( Integer.parseInt(mUndoButton.getTag().toString()) );
		}
		
		mOrientationButton.setImageResource(orientationSensor?R.drawable.iconorientation:R.drawable.iconorientationoff);
		
		value = Integer.parseInt(mPreset1Button.getTag().toString());
		mPreset1Button.setImageResource(value==0?R.drawable.desktoppresetoff:(value==1?R.drawable.desktoppreset:R.drawable.desktoppresetlock));
		value = Integer.parseInt(mPreset2Button.getTag().toString());
		mPreset2Button.setImageResource(value==0?R.drawable.desktoppresetoff:(value==1?R.drawable.desktoppreset:R.drawable.desktoppresetlock));
		value = Integer.parseInt(mBrushButton.getTag().toString());
		mBrushButton.setImageResource(value==0?R.drawable.desktopbrushoff:R.drawable.desktopbrush);
		value = Integer.parseInt(mPatternButton.getTag().toString());
		mPatternButton.setImageResource(value==0?R.drawable.desktoppatternoff:R.drawable.desktoppattern);
		value = Integer.parseInt(mColorButton.getTag().toString());
		mColorButton.setImageResource(value==0?R.drawable.desktopcoloroff:R.drawable.desktopcolor);
		value = Integer.parseInt(mOperationButton.getTag().toString());
		mOperationButton.setImageResource(value==0?R.drawable.desktopoperationoff:R.drawable.desktopoperation);
		value = Integer.parseInt(mUndoButton.getTag().toString());
		mUndoButton.setImageResource(value==0?R.drawable.desktopundooff:R.drawable.desktopundo);
	}
	
	@Override public void onShow() {
		mPreset1Button.setTag((visibility>>12)%4);
		mPreset2Button.setTag((visibility>>10)%4);
		mBrushButton.setTag((visibility>>8)%4);
		mColorButton.setTag((visibility>>6)%4);
		mPatternButton.setTag((visibility>>4)%4);
		mOperationButton.setTag((visibility>>2)%4);
		mUndoButton.setTag(visibility%4);
			
		updateButtons(false);
	}
	
	/**
	 * Create the tools dialog and manage the buttons behaviour
	 * @param savedInstanceState which may do something. I'm not sure
	 */
	@Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toolsdialog);
        
        View.OnClickListener listener1 = new View.OnClickListener() {
			public void onClick(View _v) {
				ImageView v = (ImageView) _v;
				int value = Integer.parseInt(v.getTag().toString());
				switch (value) {
				case 0: v.setTag("1"); break;
				case 1: v.setTag("2"); break;
				case 2: v.setTag("0"); break;
				}
				updateButtons(true);
			}
		};
		View.OnClickListener listener2 = new View.OnClickListener() {
			public void onClick(View _v) {
				ImageView v = (ImageView) _v;
				int value = Integer.parseInt(v.getTag().toString());
				v.setTag(value==0?"1":"0");
				updateButtons(true);
			}
		};
		
        mPreset1Button = (ImageView) findViewById(R.id.toolsdialog_preset1);
        mPreset1Button.setOnClickListener(listener1);
        mPreset2Button = (ImageView) findViewById(R.id.toolsdialog_preset2);
        mPreset2Button.setOnClickListener(listener1);
        mBrushButton = (ImageView) findViewById(R.id.toolsdialog_brush);
        mBrushButton.setOnClickListener(listener2);
        mPatternButton = (ImageView) findViewById(R.id.toolsdialog_pattern);
        mPatternButton.setOnClickListener(listener2);
        mColorButton = (ImageView) findViewById(R.id.toolsdialog_color);
        mColorButton.setOnClickListener(listener2);
        mOperationButton = (ImageView) findViewById(R.id.toolsdialog_operation);
        mOperationButton.setOnClickListener(listener2);
        mUndoButton = (ImageView) findViewById(R.id.toolsdialog_undo);
        mUndoButton.setOnClickListener(listener2);
        
        // The pressure button
        mPressureButtonPop = (TextView) findViewById(R.id.toolsdialog_pressurepop);
        mPressureButton = (ImageView) findViewById(R.id.toolsdialog_pressure);
        mPressureButton.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
				case MotionEvent.ACTION_UP		:
					// Compute the min and max pressure
					// 1. Get the mean
					float mean = 0;
					for (int i=0; i<mPressureValues.size(); i++ ) { mean+=mPressureValues.get(i); }
					mean/=mPressureValues.size();
					// 2. Get the variance
					float variance = 0;
					for (int i=0; i<mPressureValues.size(); i++) { variance+=Math.pow(mPressureValues.get(i)-mean, 2f); }
					variance/=mPressureValues.size();
					
					pressure_min = Math.max(0, (float) (mean - Math.sqrt(variance)));
					pressure_max = Math.min(1f, (float) (mean + Math.sqrt(variance)));
					
					mPressureButtonPop.setVisibility(View.GONE); break;
				case MotionEvent.ACTION_DOWN	:
					mPressureValues = new ArrayList<Float>();
					mPressureButtonPop.setVisibility(View.VISIBLE); break;
				default:
					if (mPressureValues!=null) { mPressureValues.add(event.getPressure()>0?event.getPressure():event.getSize()); }
					
					DecimalFormat df = new DecimalFormat();
					df.setMaximumFractionDigits(2);
					df.setMinimumFractionDigits(2);
			        mPressureButtonPop.setText(""+df.format(event.getPressure()));
			        break;
				}
				return true;
			}
		});
        
        // The orientation button
        mOrientationButton = (ImageView) findViewById(R.id.toolsdialog_orientation);
        mOrientationButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				orientationSensor = !orientationSensor;
				mOrientationButton.setImageResource(orientationSensor?R.drawable.iconorientation:R.drawable.iconorientationoff);
			}
		});
                
        // Validation button handling
        mOkButton = (ImageView) findViewById(R.id.toolsdialog_9);
        mOkButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mListener.onOKClick();
				dismiss();
			}
        });        
	}
	
	
}
