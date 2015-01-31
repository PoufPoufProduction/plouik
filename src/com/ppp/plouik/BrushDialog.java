package com.ppp.plouik;

import java.text.DecimalFormat;

import org.jdom.Element;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * The brush selection dialog
 * Note: We save the seek bar value as a float between [0..1]
 * @author Pouf-Pouf Production
 *
 */
public class BrushDialog extends SketchbookDialog {

	/** The XML Tag values */
	static public final String				TAG				= "brush";
	static public final String				mTagT			= "brush_tool";
	static public final String				mTagM			= "brush_mode";
	static public final String				mTagSize		= "brush_size";
	static public final String				mTagSizeP		= "brush_size_pressure";
	static public final String				mTagSizeS		= "brush_size_speed";
	static public final String				mTagSizeFI		= "brush_size_fadein";
	static public final String				mTagSizeFO		= "brush_size_fadeout";
	static public final String				mTagO			= "brush_opacity";
	static public final String				mTagOpP			= "brush_opacity_pressure";
	static public final String				mTagOpS			= "brush_opacity_speed";
	static public final String				mTagOpFI		= "brush_opacity_fadein";
	static public final String				mTagOpFO		= "brush_opacity_fadeout";
	static public final String				mTagH			= "brush_hardness";
	static public final String				mTagSm			= "brush_smudge";
	static public final String				mTagL			= "brush_smudgeL";
	static public final String				mTagP			= "brush_pencil";
	static public final String				mTagS			= "brush_stamp";
	
	/** THE BRUSH SETTINGS */
	static public float						size			= Plouik.mStrokeSizeMax/4;
	static public float						size_pressure	= 0f;
	static public float						size_speed		= 0f;
	static public float						size_fadein		= 0f;
	static public float						size_fadeout	= 0f;
	static public int						opacity			= 255;
	static public float						opacity_pressure= 0f;
	static public float						opacity_speed	= 0f;
	static public float						opacity_fadein	= 0f;
	static public float						opacity_fadeout	= 0f;
	static public float						hardness		= 1.0f;
	static public float						smudge			= 0.0f;
	static public float						length			= Plouik.mStrokeSizeMax/2;
	static public BrushDialog.PaintMode		mode 			= PaintMode.NORMAL;
	static public boolean					tool			= true;
	static public BrushDialog.PencilShape	pencil			= PencilShape.NORMAL;
	static public String					stamp			= "default";
	
	/** Set the current brush from the provided element information */
    static public void importCfg(Element _elt) {
    	if (_elt!=null) {
			size 			= (_elt.getChildText(mTagSize)!= null)?Float.parseFloat(_elt.getChildText(mTagSize)):Plouik.mStrokeSizeMax/4;
			size_pressure	= (_elt.getChildText(mTagSizeP)!= null)?Float.parseFloat(_elt.getChildText(mTagSizeP)):0f;
			size_speed		= (_elt.getChildText(mTagSizeS)!= null)?Float.parseFloat(_elt.getChildText(mTagSizeS)):0f;
			size_fadein		= (_elt.getChildText(mTagSizeFI)!= null)?Float.parseFloat(_elt.getChildText(mTagSizeFI)):0f;
			size_fadeout	= (_elt.getChildText(mTagSizeFO)!= null)?Float.parseFloat(_elt.getChildText(mTagSizeFO)):0f;
			opacity			= (_elt.getChildText(mTagO)!= null)?(int)Float.parseFloat(_elt.getChildText(mTagO)):255;
			opacity_pressure= (_elt.getChildText(mTagOpP)!= null)?Float.parseFloat(_elt.getChildText(mTagOpP)):0f;
			opacity_speed	= (_elt.getChildText(mTagOpS)!= null)?Float.parseFloat(_elt.getChildText(mTagOpS)):0f;
			opacity_fadein	= (_elt.getChildText(mTagOpFI)!= null)?Float.parseFloat(_elt.getChildText(mTagOpFI)):0f;
			opacity_fadeout	= (_elt.getChildText(mTagOpFO)!= null)?Float.parseFloat(_elt.getChildText(mTagOpFO)):0f;
			hardness		= (_elt.getChildText(mTagH)!=null)?Float.parseFloat(_elt.getChildText(mTagH)):1.0f;
			smudge			= (_elt.getChildText(mTagSm)!=null)?Float.parseFloat(_elt.getChildText(mTagSm)):0.0f;
			length			= (_elt.getChildText(mTagL)!=null)?Float.parseFloat(_elt.getChildText(mTagL)):Plouik.mStrokeSizeMax/2;
			mode			= (_elt.getChildText(mTagM)!=null)?PaintMode.getMode(_elt.getChildText(mTagM)):PaintMode.NORMAL;
			tool			= (_elt.getChildText(mTagT)!=null)?((int)Float.parseFloat(_elt.getChildText(mTagT))==1):true;
			pencil			= (_elt.getChildText(mTagP)!=null)?PencilShape.getMode(_elt.getChildText(mTagP)):PencilShape.NORMAL;
			stamp			= (_elt.getChildText(mTagS)!=null)?_elt.getChildText(mTagS):"default";
		}
    }
    
    /** Write the current brush settings */
    static public Element exportCfg() {
		Element ret = new Element(TAG);
		
		Element eltSi = new Element(mTagSize);	eltSi.setText(String.valueOf(size));			ret.addContent(eltSi);
		Element eltSp = new Element(mTagSizeP);	eltSp.setText(String.valueOf(size_pressure));	ret.addContent(eltSp);
		Element eltSs = new Element(mTagSizeS);	eltSs.setText(String.valueOf(size_speed));		ret.addContent(eltSs);
		Element eltSFI= new Element(mTagSizeFI);eltSFI.setText(String.valueOf(size_fadein));	ret.addContent(eltSFI);
		Element eltSFO= new Element(mTagSizeFO);eltSFO.setText(String.valueOf(size_fadeout));	ret.addContent(eltSFO);
		Element eltO = new Element(mTagO);		eltO.setText(String.valueOf(opacity));			ret.addContent(eltO);
		Element eltOp = new Element(mTagOpP);	eltOp.setText(String.valueOf(opacity_pressure));ret.addContent(eltOp);
		Element eltOs = new Element(mTagOpS);	eltOs.setText(String.valueOf(opacity_speed));	ret.addContent(eltOs);
		Element eltOFI= new Element(mTagOpFI);	eltOFI.setText(String.valueOf(opacity_fadein));	ret.addContent(eltOFI);
		Element eltOFO= new Element(mTagOpFO);	eltOFO.setText(String.valueOf(opacity_fadeout));ret.addContent(eltOFO);
		Element eltH = new Element(mTagH);		eltH.setText(String.valueOf(hardness));			ret.addContent(eltH);
		Element eltSm = new Element(mTagSm);	eltSm.setText(String.valueOf(smudge));			ret.addContent(eltSm);
		Element eltL = new Element(mTagL);		eltL.setText(String.valueOf(length));			ret.addContent(eltL);
		Element eltP = new Element(mTagM);		eltP.setText(mode.getName()); 					ret.addContent(eltP);
		Element eltT = new Element(mTagT);		eltT.setText(tool?"1":"0"); 					ret.addContent(eltT);
		Element eltPe= new Element(mTagP);		eltPe.setText(pencil.getName()); 				ret.addContent(eltPe);
		Element eltS = new Element(mTagS);		eltS.setText(stamp); 							ret.addContent(eltS);
		
		return ret;
	}


	public interface OnBrushChangedListener { void brushChanged(); }
	
	
	
	/** The size button and bar */
	private class SizeSettings {
		
		final private ImageView 	mButton;
		final private PlouikBar		mSeekBar;
		final private TextView		mPop;
		
		private boolean				isPlus;
		
		SizeSettings(ImageView _view, PlouikBar _seekBar, TextView _pop) {
			mButton 	= _view;
			mSeekBar 	= _seekBar;
			mPop 		= _pop;
			
			mSeekBar.setType(PlouikBar.Type.BRUSH);
			
			isPlus = false;
			mButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					isPlus = isPlus?false:true;	
					if (isPlus && size>Plouik.mStrokeSizeThreshold) { size = Plouik.mStrokeSizeThreshold; }
					mSeekBar.setProgress((int)(isPlus?size*Plouik.mStrokeSizeMax/Plouik.mStrokeSizeThreshold :size));
					updateIcon();
				}
			});
			
			mSeekBar.setMax(Plouik.mStrokeSizeMax); mSeekBar.setProgress((int) size);
			
			mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.INVISIBLE); }
				public void onStartTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.VISIBLE); }
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					DecimalFormat df = new DecimalFormat();
					df.setMaximumFractionDigits(1);
					df.setMinimumFractionDigits(1);
					size=isPlus?(float)progress*Plouik.mStrokeSizeThreshold/Plouik.mStrokeSizeMax:progress;
					mPop.setText(""+(isPlus?df.format(size):((int)size)+"px"));
				}
			});
		}
		
		public void updateIcon() {
			mButton.setImageResource(isPlus?(tool?R.drawable.iconsizeplus:R.drawable.iconsizeplus2):
											(tool?R.drawable.iconsize:R.drawable.iconsize2));
		}
		
		public void setValue(float _value) {
			size = _value;
			isPlus = (_value<0.75f*Plouik.mStrokeSizeThreshold);
			mSeekBar.setProgress((int)(isPlus?_value*Plouik.mStrokeSizeMax/Plouik.mStrokeSizeThreshold:_value));
			updateIcon();
		}
	}
	
	/** The size pressure */
	private class SizePressureSettings {
		final private PlouikBar		mSeekBar;
		final private TextView		mPop;
		
		SizePressureSettings( PlouikBar _seekBar, TextView _pop) {
			mSeekBar 	= _seekBar;
			mPop 		= _pop;

			mSeekBar.setType(PlouikBar.Type.HALFBRUSH);
			mSeekBar.setMax(100); mSeekBar.setProgress((int)size_pressure);
			
			mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.INVISIBLE); }
				public void onStartTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.VISIBLE); }
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					size_pressure = progress; mPop.setText(""+progress+"%");
				}
			});
		}
		
		public void setValue(float _value) { size_pressure = _value; mSeekBar.setProgress((int)_value); }
	}
	
	/** The size speed */
	private class SizeSpeedSettings {
		final private PlouikBar		mSeekBar;
		final private TextView		mPop;
		
		SizeSpeedSettings( PlouikBar _seekBar, TextView _pop) {
			mSeekBar 	= _seekBar;
			mPop 		= _pop;

			mSeekBar.setType(PlouikBar.Type.HALFBRUSH);
			mSeekBar.setMax(100); mSeekBar.setProgress((int)size_speed);
			
			mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.INVISIBLE); }
				public void onStartTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.VISIBLE); }
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					size_speed = progress; mPop.setText(""+progress+"%");
				}
			});
		}
		
		public void setValue(float _value) { size_speed = _value; mSeekBar.setProgress((int)_value); }
	}
	
	/** The opacity button and bar */
	private class SizeFadeInSettings {
		final private PlouikBar		mSeekBar;
		final private TextView		mPop;
		
		SizeFadeInSettings( PlouikBar _seekBar, TextView _pop) {
			mSeekBar 	= _seekBar;
			mPop 		= _pop;

			mSeekBar.setType(PlouikBar.Type.HALFBRUSH);
			mSeekBar.setMax(100); mSeekBar.setProgress((int)(size_fadein*100));
			
			mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.INVISIBLE); }
				public void onStartTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.VISIBLE); }
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					size_fadein = (float)progress/100; mPop.setText(""+progress+"%");
				}
			});
		}
		
		public void setValue(float _value) { size_fadein = _value; mSeekBar.setProgress((int)(_value*100)); }
	}
	
	/** The opacity button and bar */
	private class SizeFadeOutSettings {
		final private PlouikBar		mSeekBar;
		final private TextView		mPop;
		
		SizeFadeOutSettings( PlouikBar _seekBar, TextView _pop) {
			mSeekBar 	= _seekBar;
			mPop 		= _pop;

			mSeekBar.setType(PlouikBar.Type.HALFBRUSH);
			mSeekBar.setMax(100); mSeekBar.setProgress((int)(size_fadeout*100));
			
			mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.INVISIBLE); }
				public void onStartTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.VISIBLE); }
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					size_fadeout = (float)progress/100; mPop.setText(""+progress+"%");
				}
			});
		}
		
		public void setValue(float _value) { size_fadeout = _value; mSeekBar.setProgress((int)(_value*100)); }
	}
	
	/** The opacity button and bar */
	private class OpacitySettings {
		
		final private ImageView 	mButton;
		final private PlouikBar		mSeekBar;
		final private TextView		mPop;
		
		OpacitySettings(ImageView _view, PlouikBar _seekBar, TextView _pop) {
			mButton 	= _view;
			mSeekBar 	= _seekBar;
			mPop 		= _pop;
			
			mSeekBar.setType(PlouikBar.Type.BRUSH);
			mSeekBar.setMax(255); mSeekBar.setProgress(opacity);
			
			mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.INVISIBLE); }
				public void onStartTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.VISIBLE); }
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					opacity = progress; mPop.setText(""+(100*progress/255)+"%");
				}
			});
			
			mButton.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { setValue(255); } });
		}
		
		public void updateIcon() { mButton.setImageResource(tool?R.drawable.iconopacity:R.drawable.iconopacity2); }
		
		public void setValue(int _value) { opacity = _value; mSeekBar.setProgress(_value); }
	}
	

	/** The size pressure */
	private class OpacityPressureSettings {
		final private PlouikBar		mSeekBar;
		final private TextView		mPop;
		
		OpacityPressureSettings( PlouikBar _seekBar, TextView _pop) {
			mSeekBar 	= _seekBar;
			mPop 		= _pop;

			mSeekBar.setType(PlouikBar.Type.HALFBRUSH);
			mSeekBar.setMax(100); mSeekBar.setProgress((int)opacity_pressure);
			
			mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.INVISIBLE); }
				public void onStartTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.VISIBLE); }
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					opacity_pressure = progress; mPop.setText(""+progress+"%");
				}
			});
		}
		
		public void setValue(float _value) { opacity_pressure = _value; mSeekBar.setProgress((int)_value); }
	}
	
	/** The size speed */
	private class OpacitySpeedSettings {
		final private PlouikBar		mSeekBar;
		final private TextView		mPop;
		
		OpacitySpeedSettings( PlouikBar _seekBar, TextView _pop) {
			mSeekBar 	= _seekBar;
			mPop 		= _pop;

			mSeekBar.setType(PlouikBar.Type.HALFBRUSH);
			mSeekBar.setMax(100); mSeekBar.setProgress((int)opacity_speed);
			
			mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.INVISIBLE); }
				public void onStartTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.VISIBLE); }
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					opacity_speed = progress; mPop.setText(""+progress+"%");
				}
			});
		}
		
		public void setValue(float _value) { opacity_speed = _value; mSeekBar.setProgress((int)_value); }
	}
	
	/** The opacity button and bar */
	private class OpacityFadeInSettings {
		final private PlouikBar		mSeekBar;
		final private TextView		mPop;
		
		OpacityFadeInSettings( PlouikBar _seekBar, TextView _pop) {
			mSeekBar 	= _seekBar;
			mPop 		= _pop;

			mSeekBar.setType(PlouikBar.Type.HALFBRUSH);
			mSeekBar.setMax(100); mSeekBar.setProgress((int)(opacity_fadein*100));
			
			mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.INVISIBLE); }
				public void onStartTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.VISIBLE); }
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					opacity_fadein = (float)progress/100; mPop.setText(""+progress+"%");
				}
			});
		}
		
		public void setValue(float _value) { opacity_fadein = _value; mSeekBar.setProgress((int)(_value*100)); }
	}
	
	/** The opacity button and bar */
	private class OpacityFadeOutSettings {
		final private PlouikBar		mSeekBar;
		final private TextView		mPop;
		
		OpacityFadeOutSettings( PlouikBar _seekBar, TextView _pop) {
			mSeekBar 	= _seekBar;
			mPop 		= _pop;

			mSeekBar.setType(PlouikBar.Type.HALFBRUSH);
			mSeekBar.setMax(100); mSeekBar.setProgress((int)(opacity_fadeout*100));
			
			mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.INVISIBLE); }
				public void onStartTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.VISIBLE); }
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					opacity_fadeout = (float)progress/100; mPop.setText(""+progress+"%");
				}
			});
		}
		
		public void setValue(float _value) { opacity_fadeout = _value; mSeekBar.setProgress((int)(_value*100)); }
	}
	
	/** The hardness button and bar */
	private class HardnessSettings {
		
		final private ImageView 	mButton;
		final private PlouikBar		mSeekBar;
		final private TextView		mPop;
		
		HardnessSettings(ImageView _view, PlouikBar _seekBar, TextView _pop) {
			mButton 	= _view;
			mSeekBar 	= _seekBar;
			mPop 		= _pop;

			mSeekBar.setType(PlouikBar.Type.BRUSH);
			mSeekBar.setMax(100); mSeekBar.setProgress((int)hardness*100);
			
			mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.INVISIBLE); }
				public void onStartTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.VISIBLE); }
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					hardness = (float)progress/100; mPop.setText(""+progress+"%");
				}
			});
			
			mButton.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { setValue(1.0f); } });
		}
		
		public void setValue(float _value) { hardness = _value; mSeekBar.setProgress((int)(_value*100)); }
	}
	
	/** The smudge button and bar */
	private class SmudgeSettings {
		
		final private ImageView 	mButton;
		final private PlouikBar		mSeekBar;
		final private PlouikBar		mSeekBarL;
		final private TextView		mPop;
		
		SmudgeSettings(ImageView _view, PlouikBar _seekBar, PlouikBar _seekBarL, TextView _pop) {
			mButton 		= _view;
			mSeekBar 		= _seekBar;
			mSeekBarL		= _seekBarL;
			mPop 			= _pop;

			mSeekBar.setType(PlouikBar.Type.BRUSH);
			mSeekBarL.setType(PlouikBar.Type.BRUSH);
			mSeekBar.setMax(100); mSeekBar.setProgress((int)smudge*100);
			mSeekBarL.setMax(Plouik.mStrokeSizeMax); mSeekBarL.setProgress((int)length);
			
			mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.INVISIBLE); }
				public void onStartTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.VISIBLE); }
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					smudge = (float)progress/100; mPop.setText(""+progress+"%");
					setRes(progress>0);
				}
			});
			
			mSeekBarL.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.INVISIBLE); }
				public void onStartTrackingTouch(SeekBar seekBar) 	{ mPop.setVisibility(View.VISIBLE); }
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					length = (float) progress; mPop.setText(""+progress+"px");
				}
			});
		}
		
		public void setRes(boolean _notnull) { mButton.setImageResource(_notnull?R.drawable.iconsmudgeon:R.drawable.iconsmudge); }
		
		public void setValue(float _value, float _length) {
			smudge = _value; mSeekBar.setProgress((int)(smudge*100));
			length = _length; mSeekBarL.setProgress((int)length);
			setRes(_value>0.f);
		}
	}
	
	/** Pencil shapes */
	public enum PencilShape {
		NORMAL 	(R.drawable.iconround, 	"default", 1f, new float[][]{{0f,0f}} ),
		THREE01 (R.drawable.icondots03, "three01",.3f, new float[][]{{-.6f,-0.7f},{.5f,-.2f},{-.2f,.7f}} ),
		THREE02 (R.drawable.icondots04, "three02",.2f, new float[][]{{-.8f,.8f},{-.1f,-.8f},{.7f,.1f}} ),
		FIVE01 	(R.drawable.icondots02, "five01", .1f, new float[][]{{-.75f,-0.6f},{-.5f,.5f},{0f,0f},{.75f,-.75f},{.5f,.75f}} ),
		FIVE02 	(R.drawable.icondots01, "five02",.25f, new float[][]{{-.75f,-0.6f},{-.5f,.5f},{0f,0f},{.75f,-.75f},{.5f,.75f}} ),
		TEN01 	(R.drawable.icondots05, "ten01",.2f, new float[][]{{-.75f,-0.6f},{-.5f,.5f},{0f,0f},{.75f,-.75f},{.5f,.75f},{-.15f,-0.5f},{-.8f,.2f},{0.35f,-0.1f},{.8f,-.35f},{.2f,.3f}} ),
		TEN02 	(R.drawable.icondots06, "ten02",.3f, new float[][]{{-.6f,-0.7f},{.5f,-.5f},{0f,0f},{-.7f,.7f},{.7f,.5f},{-.5f,-.15f},{.2f,-.7f},{-.1f,.35f},{-.35f,.7f},{.3f,.2f}} );
		
		final private int			mRes;
		final private String		mName;
		final private float			mSize;
		final private float			mDots[][];
		
		PencilShape(int _res, String _name, float _size, float[][] _dots) {
			mRes = _res;
			mName = _name;
			mSize = _size;
			mDots = _dots;
		}
		
		/** Useful accessors */
		public int				getRes() 	{ return mRes; }
		public String 			getName() 	{ return mName; }
		public float			getSize()	{ return mSize; }
		public float[][]		getDots()	{ return mDots; }
		
		/** Get the shape from the integer id */
		static PencilShape getMode( String _name) {
			PencilShape ret = NORMAL;
			for (PencilShape i: PencilShape.values()) { if (i.getName().equals(_name) ) { ret = i; } }
			return ret;
		}
		
		/** Get the shape index into the PencilShape values */
		static int getModeId( PencilShape _mode) {
			int ret = 0; int count = 0;
			for (PencilShape i: PencilShape.values()) { if (i==_mode) { ret = count; } count++; }
			return ret;
		}
	}
	
	/** The icon grid adapter */
	private class PencilAdapter extends BaseAdapter {
		private class ViewListener implements View.OnClickListener {
			private int pos;
			public ViewListener(int _pos) { super(); pos = _pos; }
			public void onClick(View v) {
				mBrushStuff.setVisibility(View.VISIBLE);
				mShapesGrid.setVisibility(View.GONE);
				pencil = PencilShape.values()[pos];
				updateTool();
			}
		}
		/** Some accessors */
		public int 		getCount()			{ return PencilShape.values().length; }
		public Object 	getItem(int arg0) 	{ return null; }
		public long 	getItemId(int arg0) { return 0;	}
		
		/** Return the preset icon button as ImageView with the good listener */
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			ImageView vRet = new ImageView(getContext());
			vRet.setImageResource(PencilShape.values()[arg0].getRes());
			vRet.setOnClickListener(new ViewListener(arg0));
			vRet.setLayoutParams(new GridView.LayoutParams(
					getContext().getResources().getDimensionPixelSize(R.dimen.icon),
					getContext().getResources().getDimensionPixelSize(R.dimen.icon)));
			vRet.setPadding(0, 0, 0, 0);
			return vRet;
		}
	}
	
	private class StampListener implements SketchbookStamp.Listener {
		public void onClick() {
			stamp = mStampAdapter.getName();
			mBrushStuff.setVisibility(View.VISIBLE);
			mStampsGrid.setVisibility(View.GONE);
			updateTool();
		}
	}
	
	/** Painting modes buttons handling */
	public enum PaintMode {
		NORMAL (R.drawable.iconsrcover, "SRC_OVER"),
		DARKEN (R.drawable.icondarken, "DARKEN"),
		SCREEN (R.drawable.iconscreen, "SCREEN"),
		ERASE  (R.drawable.iconerase, "MULTIPLY");
		
		final private int			mRes;
		final private String		mName;
		
		PaintMode(int res, String value) {
			mRes = res;
			mName = new String(value);
		}
		
		/** Useful accessors */
		public int				getRes() 	{ return mRes; }
		public String 			getName() 	{ return mName; }
		
		/** Get the mode from the integer id */
		static PaintMode getMode( String _name) {
			PaintMode ret = NORMAL;
			for (PaintMode i: PaintMode.values()) { if (i.getName().equals(_name) ) { ret = i; } }
			return ret;
		}
		
		/** Get the mode index into the PaintMode values */
		static int getModeId( PaintMode _mode) {
			int ret = 0;
			int count = 0;
			for (PaintMode i: PaintMode.values()) { if (i==_mode) { ret = count; } count++; }
			return ret;
		}
		
		/** get the next Paint mode */
		static public void next() { mode = PaintMode.values()[(getModeId(mode)+1)%PaintMode.values().length]; }
	}

	/** The dialog listener for returning the chosen brush */
    private OnBrushChangedListener	mListener;
    
    /** The brush stuff */
    private LinearLayout			mBrushStuff;
    private ImageView				mOkButton;
    private ImageView				mModeButton;
    private ImageView				mShapeButton;
    
    private SizeSettings			mSizeSettings;
    private LinearLayout			mSizeDynamic;
    private LinearLayout			mSizeFade;
    private SizePressureSettings	mSizePressureSettings;
    private SizeSpeedSettings		mSizeSpeedSettings;
    private SizeFadeInSettings		mSizeFadeInSettings;
    private SizeFadeOutSettings		mSizeFadeOutSettings;
    
    private OpacitySettings			mOpacitySettings;
    private LinearLayout			mOpacityDynamic;
    private LinearLayout			mOpacityFade;
    private OpacityPressureSettings	mOpacityPressureSettings;
    private OpacitySpeedSettings	mOpacitySpeedSettings;
    private OpacityFadeInSettings	mOpacityFadeInSettings;
    private OpacityFadeOutSettings	mOpacityFadeOutSettings;
    
    private HardnessSettings		mHardnessSettings;
    private SmudgeSettings 			mSmudgeSettings;
    private GridView				mShapesGrid;
    private GridView				mStampsGrid;
    private ImageView				mToggleTool;
    private LinearLayout			mSetHardness;
    
    /** The stamp adapter */
    private SketchbookStamp			mStampAdapter;
    
	 /** The Brush dialog constructor */
    public BrushDialog(Context context, OnBrushChangedListener listener )
    {
        super(context);
        mListener = listener;
    }
    
    public void updateTool() {
    	mToggleTool.setImageResource(tool?R.drawable.title_pencil:R.drawable.title_stamp);
    	mShapeButton.setImageResource(tool?pencil.getRes():mStampAdapter.getResIcon());
        mSetHardness.setVisibility(tool?View.VISIBLE:View.GONE);
        mSizeDynamic.setVisibility(tool?View.GONE:View.VISIBLE);
        mSizeFade.setVisibility(tool?View.GONE:View.VISIBLE);
        mSizeSettings.updateIcon();
        mOpacityDynamic.setVisibility(tool?View.GONE:View.VISIBLE);
        mOpacityFade.setVisibility(tool?View.GONE:View.VISIBLE);
        mOpacitySettings.updateIcon();
    }
    
    /** Update the dialog before showing it */
    @Override public void onShow() {
    	mSizeSettings.setValue(size);
    	mSizePressureSettings.setValue(size_pressure);
        mSizeSpeedSettings.setValue(size_speed);
        mSizeFadeInSettings.setValue(size_fadein);
        mSizeFadeOutSettings.setValue(size_fadeout);
     
    	mOpacitySettings.setValue(opacity);
    	mOpacityPressureSettings.setValue(opacity_pressure);
        mOpacitySpeedSettings.setValue(opacity_speed);
        mOpacityFadeInSettings.setValue(opacity_fadein);
        mOpacityFadeOutSettings.setValue(opacity_fadeout);
        
    	mHardnessSettings.setValue(hardness);
    	mSmudgeSettings.setValue(smudge, length);
    	mModeButton.setImageResource(mode.getRes());
    	mStampAdapter.setStamp(stamp);
    	
    	updateTool();
    }
    
    /** The classical onCreate method */
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.brushdialog);
        
        // The brush stuff
        mBrushStuff = (LinearLayout) findViewById(R.id.brushdialog_stuff);
        
        // Get the validation button and handles the press event
        mOkButton = (ImageView) findViewById(R.id.brushdialog_ok);
        mOkButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { if (mListener!= null) { mListener.brushChanged(); } dismiss(); }
        });
        
        // The toggle tool and managed stuff
        mToggleTool = (ImageView) findViewById(R.id.brushdialog_toggletool);
        mToggleTool.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { tool=!tool; updateTool(); } });
        mSetHardness= (LinearLayout) findViewById(R.id.brushdialog_sethardness);
        
        // Handle the mode button
        mModeButton = (ImageView) findViewById(R.id.brushdialog_togglemodes);
        mModeButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) { PaintMode.next(); mModeButton.setImageResource(mode.getRes()); }
        });

        // The size settings
        mSizeSettings = new SizeSettings((ImageView) findViewById(R.id.brushdialog_sizebut),
        		 (PlouikBar) findViewById(R.id.brushdialog_size),
        		 (TextView) findViewById(R.id.brushdialog_sizepop));
        
        mSizePressureSettings = new SizePressureSettings((PlouikBar) findViewById(R.id.brushdialog_size_pressure),(TextView) findViewById(R.id.brushdialog_sizepop));
        mSizeSpeedSettings = new SizeSpeedSettings((PlouikBar) findViewById(R.id.brushdialog_size_speed),(TextView) findViewById(R.id.brushdialog_sizepop));
        mSizeFadeInSettings = new SizeFadeInSettings((PlouikBar) findViewById(R.id.brushdialog_size_fadein),(TextView) findViewById(R.id.brushdialog_sizepop));
        mSizeFadeOutSettings = new SizeFadeOutSettings((PlouikBar) findViewById(R.id.brushdialog_size_fadeout),(TextView) findViewById(R.id.brushdialog_sizepop));
        mSizeDynamic = (LinearLayout) findViewById(R.id.brushdialog_size_dynamic);
        mSizeFade	 = (LinearLayout) findViewById(R.id.brushdialog_size_fade);
        
        // The opacity settins
        mOpacitySettings = new OpacitySettings((ImageView) findViewById(R.id.brushdialog_opacitybut),
        		 (PlouikBar) findViewById(R.id.brushdialog_opacity),
        		 (TextView) findViewById(R.id.brushdialog_opacitypop));
        mOpacityPressureSettings = new OpacityPressureSettings((PlouikBar) findViewById(R.id.brushdialog_opacity_pressure),(TextView) findViewById(R.id.brushdialog_opacitypop));
        mOpacitySpeedSettings = new OpacitySpeedSettings((PlouikBar) findViewById(R.id.brushdialog_opacity_speed),(TextView) findViewById(R.id.brushdialog_opacitypop));
        mOpacityFadeInSettings = new OpacityFadeInSettings((PlouikBar) findViewById(R.id.brushdialog_opacity_fadein),(TextView) findViewById(R.id.brushdialog_opacitypop));
        mOpacityFadeOutSettings = new OpacityFadeOutSettings((PlouikBar) findViewById(R.id.brushdialog_opacity_fadeout),(TextView) findViewById(R.id.brushdialog_opacitypop));
        mOpacityDynamic = (LinearLayout) findViewById(R.id.brushdialog_opacity_dynamic);
        mOpacityFade	 = (LinearLayout) findViewById(R.id.brushdialog_opacity_fade);
        
        mHardnessSettings = new HardnessSettings((ImageView) findViewById(R.id.brushdialog_hardnessbut),
				 (PlouikBar) findViewById(R.id.brushdialog_hardness),
				 (TextView) findViewById(R.id.brushdialog_hardnesspop));
        
        mSmudgeSettings = new SmudgeSettings((ImageView) findViewById(R.id.brushdialog_smudgebut),
        		 (PlouikBar) findViewById(R.id.brushdialog_smudge),
        		 (PlouikBar) findViewById(R.id.brushdialog_smudgeLength),
        		 (TextView) findViewById(R.id.brushdialog_smudgepop));
        
        mShapesGrid = (GridView) findViewById(R.id.brushdialog_shapes);
        mShapesGrid.setVisibility(View.GONE);
        mShapesGrid.setAdapter(new PencilAdapter());
        
        mStampsGrid = (GridView) findViewById(R.id.brushdialog_stamps);
        mStampsGrid.setVisibility(View.GONE);
        mStampAdapter = new SketchbookStamp(getContext(), new StampListener());
        mStampsGrid.setAdapter(mStampAdapter);

        mShapeButton = (ImageView) findViewById(R.id.brushdialog_shape);
        mShapeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mBrushStuff.setVisibility(View.GONE);
				if (tool) { mShapesGrid.setVisibility(View.VISIBLE); } else { mStampsGrid.setVisibility(View.VISIBLE); }
			}
		});
        
    }
    
}
