package com.ppp.plouik;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.ppp.plouik.IconDialog.PresetIcon;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Presets dialog
 * @author Pouf-Pouf Production
 *
 */
public class PresetDialog extends SketchbookDialog {
	
	/** The XML Tag values */
	static public final String			TAG			= "settings";
	static public final String			TAGValue	= "preset_value";
	static public final String			TAGP		= "preset";
	static public final String			TAGH		= "preset_hue";
	static public final String			TAGS		= "preset_colorsaturation";
	static public final String			TAGV		= "preset_colorvalue";
	static public final String			TAGD		= "preset_colordrawable";
	
	/** THE PRESET DATA */
	static public PresetArray[]			elements	= new PresetArray[] { null, null };
	static public boolean[]				changes		= { false, false };
	
	/** STATIC DATA FOR THE ICON DIALOG OVERVIEW */
	static public boolean[]				parts		= { true, true, true };
	static public PresetIcon			icon		= PresetIcon.BLUE01;
	static public float[]				hsv			= {-1f,-1f,-1f};
	static public boolean				tool		= true;
	static public float					size		= Plouik.mStrokeSizeMax/4;
	
	/** Set the current presets from the provided element information */
    static public void importCfg(Context _context, Element _elt, int _id) {
    	elements[_id-1].clear();
    	changes[_id-1]  = true;
    	if (_elt!=null) {
    		for (Object e :_elt.getChildren(TAGP).toArray() ) {
    			Element elt = (Element) e;
    			if (elt.getChild(TAGValue)!=null) {
        			Float vHue = (elt.getChildText(TAGH)!=null)?Float.parseFloat(elt.getChildText(TAGH)):-1.0f;
        			Float vSaturation = (elt.getChildText(TAGS)!=null)?Float.parseFloat(elt.getChildText(TAGS)):0f;
        			Float vValue = (elt.getChildText(TAGV)!=null)?Float.parseFloat(elt.getChildText(TAGV)):0f;
        			PresetIcon vIcon = (elt.getChildText(TAGD)!=null)?PresetIcon.getFromName(elt.getChildText(TAGD)):PresetIcon.BLUE01;
        			
    				SketchbookPreset vPreset = new SketchbookPreset(_context, elt.getChild(TAGValue));
    				vPreset.setIcon(vIcon, new float[] { vHue, vSaturation, vValue });
    				elements[_id-1].add(vPreset);
    			}
    		}
    	}
    }
    
    /** Write the presets information (not static) */
	static public Element exportCfg(int _id) {
		Element ret = new Element(TAG+_id);
		if ((_id==1) || (_id==2)) {
			for (int i=0; i<elements[_id-1].size(); i++) {
				SketchbookPreset vPreset = elements[_id-1].get(i);
				
				if (vPreset != null) {
					Element eltA = new Element(TAGP);
					Element eltH = new Element(TAGH); 	eltH.setText(String.valueOf(vPreset.getHSV()[0])); 	eltA.addContent(eltH);
					Element eltS = new Element(TAGS);	eltS.setText(String.valueOf(vPreset.getHSV()[1])); 	eltA.addContent(eltS);
					Element eltV = new Element(TAGV);	eltV.setText(String.valueOf(vPreset.getHSV()[2]));	eltA.addContent(eltV);
					Element eltD = new Element(TAGD);	eltD.setText(vPreset.mIcon.getName());				eltA.addContent(eltD);
					Element eltDa = (Element) vPreset.getElt().clone();										eltA.addContent(eltDa);
					
					ret.addContent(eltA);
				}
			}
		}

		return ret;
	}
	
	/** The static preset class */
	public static class PresetArray {
		private ArrayList<SketchbookPreset> presets;
		
		/** Quick methods */
		public 					PresetArray() 				{ presets = new ArrayList<SketchbookPreset>(); }
		public int 				size()						{ return presets.size(); }
		public void				clear()						{ presets.clear(); }
		public SketchbookPreset get(int _id) 				{ return presets.get(_id); }
		public int				add(SketchbookPreset _v)	{ presets.add(_v); update(); return size()-1; }
		public void				remove(int _location)		{ presets.remove(_location); update(); }
		public void				update()					{ for (int i=0; i<size(); i++) { get(i).setPosition(i); } }
	}

	
	
	/** The preset class */
	static public class SketchbookPreset {
		/** An ImageView derivative with just a ID more useful in the onClick listener */
		private class PresetView extends ImageView {
			public SketchbookPreset	mParent;
			public PresetView(Context context) { super(context); }
		}
		
		/** preset attributes */
		private Element			mElt;					// The stored settings
		private float []		mHSV;					// The HSV value
		private PresetIcon		mIcon;					// The corresponding PresetIcon
		private PresetView		mView;					// The view
		private boolean			mSelected;				// Is the current preset selected ?
		private	int				mPosition;				// The position in the table
		private boolean			mOnClick;				// Has OnClickListener
		
		/** Udate the view depending the selected status */
		private ImageView updateView(boolean _selected) {
			if (_selected!=mSelected) { mView.setImageResource(mIcon.getRes(_selected)); mSelected = _selected; }
			return mView;
		}
		
		/** Constructors - first used when add button - second on load */
		public SketchbookPreset(Context _context, Element _elt)	{ mElt = _elt; build(_context);}
		public SketchbookPreset(Context _context, boolean _brush, boolean _color, boolean _pattern) {
			mElt = new Element(PresetDialog.TAGValue);
			if (_brush) 	{ mElt.addContent(BrushDialog.exportCfg()); }
			if (_color) 	{ mElt.addContent(ColorDialog.exportCfg()); }
			if (_pattern) 	{ mElt.addContent(PatternDialog.exportCfg()); }
			build(_context);
		}
		
		private void build(Context _context) {
			mView 			= new PresetView(_context);
			int mSize 		= _context.getResources().getDimensionPixelSize(R.dimen.preset);
			mView.mParent 	= this;
			mOnClick 		= false;
			mView.setOnClickListener(null);
			mView.setLayoutParams(new GridView.LayoutParams(mSize, mSize));
			mView.setPadding(0, 0, 0, 0);
		}
		
		/** Classic accessors */
		public Element			getElt() 					{ return mElt; }
		public float []			getHSV() 					{ return mHSV; }
		public int				getRes(boolean _select)		{ return mIcon.getRes(_select); }
		public ImageView		getView(boolean _select)	{ return updateView(_select); }
		public int				getPosition()				{ return mPosition; }
		public void				setPosition(int _pos)		{ mPosition = _pos; }
		public boolean			hasBrush()					{ return (mElt.getChild(BrushDialog.TAG)!=null); }
		public boolean			hasColor()					{ return (mElt.getChild(ColorDialog.TAG)!=null); }
		public boolean			hasPattern()				{ return (mElt.getChild(PatternDialog.TAG)!=null); }
		public PresetIcon		getIcon()					{ return mIcon; }
		
		
		public boolean			getTool() {
			return ( hasBrush() &&
				((mElt.getChild(BrushDialog.TAG).getChildText(BrushDialog.mTagT)!=null)?((int)Float.parseFloat(mElt.getChild(BrushDialog.TAG).getChildText(BrushDialog.mTagT))==1):true));
		}
		
		public float			getSize() {
			return ( !hasBrush()? 0f : ((mElt.getChild(BrushDialog.TAG).getChildText(BrushDialog.mTagSize)!= null)?
						Float.parseFloat(mElt.getChild(BrushDialog.TAG).getChildText(BrushDialog.mTagSize)):Plouik.mStrokeSizeMax/4));
		}
		
		/** Set the icon */
		public void				setIcon(PresetIcon _icon) 	{ setIcon (_icon, getHSV()); }
		public void				setIcon(PresetIcon _icon, float [] _hsv)	{
			mIcon = _icon;
			mHSV = _hsv;
			mView.setBackgroundColor((_hsv[0]<0)?_icon.getBackground():Color.HSVToColor(_hsv));
			mSelected = false;
			mView.setImageResource(getRes(mSelected));
		}
		
		/** Set the listener */
		public void				setOnClickListener( View.OnClickListener _l) { mView.setOnClickListener(_l); mOnClick = true; }
		public boolean			hasOnClickListener() { return mOnClick; } 
	}
	
	/** The preset grip adapter */
	private class PresetAdapter extends BaseAdapter {
		public int 		getCount() 				{ return elements[mDialogId-1].size(); }
		public Object	getItem(int position) 	{ return position<getCount()?elements[mDialogId-1].get(position):null; }
		public long		getItemId(int position)	{ return 0;	}
		

		/** Return the view regarding its position */
		public View getView(int position, View convertView, ViewGroup parent) {
			View vRet = null;
			if (position<getCount()) {
				SketchbookPreset vPreset = elements[mDialogId-1].get(position);
				
				if (!vPreset.hasOnClickListener()) {
					vPreset.setOnClickListener( new View.OnClickListener() {
						public void onClick(View v) {
							SketchbookPreset.PresetView vView = (SketchbookPreset.PresetView) v;
							SketchbookPreset vPreset = vView.mParent;
							vPreset.updateView(true);
							mPresetId = vPreset.getPosition();
							mPresets.invalidateViews();
							
							mBrush = vPreset.hasBrush();
							((ImageView)findViewById(R.id.presetdialog_brush)).setImageResource(mBrush?R.drawable.desktopbrush:R.drawable.desktopbrushoff);

							mColor = vPreset.hasColor();
							((ImageView)findViewById(R.id.presetdialog_color)).setImageResource(mColor?R.drawable.desktopcolor:R.drawable.desktopcoloroff);

							mPattern = vPreset.hasPattern();
							((ImageView)findViewById(R.id.presetdialog_pattern)).setImageResource(mPattern?R.drawable.desktoppattern:R.drawable.desktoppatternoff);
							
							if (mListener!=null) { mListener.presetChanged(vPreset.getElt()); }
							if (ToolsDialog.isPresetLocked(mDialogId)) { dismiss(); }
						}
					});
				}
				vRet = vPreset.getView((position==mPresetId));
			}
			else {
				vRet = new View(getContext());
			}
			return vRet;
		}
	}
	
	/** The dialog listener */
	public interface OnPresetListener {
        void presetChanged(Element _elt);
        void presetCreated();
        void onLock();
        void onLoad();
        void onSave();
        void onIcon();
    }
	
	/** The preset dialog stuff */
	final private int				mDialogId;			// The dialog id (1 or 2 for the moment)
	private int						mPresetId = -1;		// The selected preset
	private OnPresetListener		mListener;			// The dialog listener
	private LinearLayout			mOpLayout;			// The buttons layout (invisible if locked)
	private GridView				mPresets;			// The presets grid
	private XMLOutputter			mOutputter;			// An XML outputter
	private boolean					mBrush	= true;		// Save the brush settings
	private boolean					mColor	= true;		// Save the color settings
	private boolean					mPattern= true;		// Save the pattern settings
	
	
	public PresetDialog(Context context, OnPresetListener listener, int _no) {
		super(context);
		mListener = listener;
		mDialogId = _no;
		mOutputter = new XMLOutputter(Format.getPrettyFormat());
	}
	
	/** Save some data in static in order to update the icon dialog */
	private void saveStatic() {
		if (mPresetId!=-1) {
			SketchbookPreset vPreset = elements[mDialogId-1].get(mPresetId);
			if (vPreset!=null) {
				parts[0] = vPreset.hasBrush();
				parts[1] = vPreset.hasColor();
				parts[2] = vPreset.hasPattern();
				icon 	 = vPreset.getIcon();
				hsv 	 = vPreset.getHSV();
				tool 	 = vPreset.getTool();
				size     = vPreset.getSize();
			}
		}
	}
	
	/** Set the lock parameter */
	public void setLock(boolean _lock) {
		mOpLayout.setVisibility(_lock?View.GONE:View.VISIBLE);
		mPresets.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
					_lock?LinearLayout.LayoutParams.WRAP_CONTENT:getContext().getResources().getDimensionPixelSize(R.dimen.gridView)));
	}
	
	/** Prepare the panel before showing */
	@Override public void onShow() {
		setLock(ToolsDialog.isPresetLocked(mDialogId));
		if (changes[mDialogId-1]) { refresh(true); }
	}
	
	/** The classical onCreate method */
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        ImageView	vButton;
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.presetdialog);
        
        // The operation layout
        mOpLayout = (LinearLayout) findViewById(R.id.presetdialog_op);
        
        // The new button
        vButton = (ImageView) findViewById(R.id.presetdialog_new);
        vButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mBrush || mColor || mPattern) {
					PresetIcon	vIcon = mBrush?(mColor?PresetIcon.BLUE01:PresetIcon.BLACK03):
											   (mColor?(mPattern?PresetIcon.CHECK01:PresetIcon.COLOR01):PresetIcon.CHECK01);
					float[] hsv = new float[] { -1f, -1f, -1f};
					if (mColor) { Color.colorToHSV(ColorDialog.color, hsv); }
					SketchbookPreset vNewPreset = new SketchbookPreset(getContext(), mBrush, mColor, mPattern);
					vNewPreset.setIcon(vIcon, hsv);
					mPresetId = elements[mDialogId-1].add(vNewPreset);
					mPresets.invalidateViews();
					if (mListener!=null) {
						mListener.presetCreated();
						if (mBrush || mPattern ) { saveStatic(); mListener.onIcon(); }
					}
				}
			}
        	
        });
        
        // The icon button
        vButton = (ImageView) findViewById(R.id.presetdialog_icon);
        vButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { if (mPresetId>=0 && mListener!=null) { saveStatic(); mListener.onIcon(); }}});
        
        // The load button
        vButton = (ImageView) findViewById(R.id.presetdialog_load);
        vButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { if (mListener!=null) { mListener.onLoad(); } }
        });
        
        // The save button
        vButton = (ImageView) findViewById(R.id.presetdialog_save);
        vButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { if (mListener!=null) { mListener.onSave(); } }	
        });
        
        // The trash button
        vButton = (ImageView) findViewById(R.id.presetdialog_trash);
        vButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mPresetId>=0) {
					elements[mDialogId-1].remove(mPresetId);
					mPresetId = -1;
					mPresets.invalidateViews();
				}
			}
        });
        
        // The ok button
        vButton = (ImageView) findViewById(R.id.presetdialog_ok);
        vButton.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { dismiss(); } });
        vButton.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View v) { 
				ToolsDialog.setPresetLocked(mDialogId); if (mListener!=null) {mListener.onLock(); } dismiss(); return true;
			}
		});
        
        // The option buttons
        vButton = (ImageView) findViewById(R.id.presetdialog_brush);
        vButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mBrush = !mBrush;
				((ImageView)v).setImageResource(mBrush?R.drawable.desktopbrush:R.drawable.desktopbrushoff);
			}
		});
        vButton = (ImageView) findViewById(R.id.presetdialog_color);
        vButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mColor = !mColor;
				((ImageView)v).setImageResource(mColor?R.drawable.desktopcolor:R.drawable.desktopcoloroff);
			}
		});
        vButton = (ImageView) findViewById(R.id.presetdialog_pattern);
        vButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mPattern = !mPattern;
				((ImageView)v).setImageResource(mPattern?R.drawable.desktoppattern:R.drawable.desktoppatternoff);
			}
		});
        
        // The preset grid
        mPresets = (GridView) findViewById(R.id.presetdialog_presets);
        mPresets.setAdapter(new PresetAdapter());
        
        changes[mDialogId-1] = false;
    }
    
    private void log(String _tag, String _msg) {
    	String lines[] = _msg.split("\n");
    	for (String f : lines) {
    		Plouik.trace(TAG, _tag+f.substring(0, f.length()-1));
    	}
    }
    
    
    /** Load xml file */
    public void load(String _filename) {
    	File dataFile = new File(_filename);
		if (dataFile.exists()) {
			SAXBuilder sxb = new SAXBuilder();
			try {
				Document doc = sxb.build(dataFile);
				Element root = doc.getRootElement();
				log("[LOAD]    ",mOutputter.outputString(root));
				importCfg(getContext(), root, mDialogId);
				refresh(true);
			}
			catch (Exception e) {}
		}	
    }
    
    /** save xml file */
    public void save(String _filename) {
    	if (!_filename.endsWith(".xml")) { _filename+=".xml"; }
    	try {
			DataOutputStream output = new DataOutputStream(new FileOutputStream(_filename));
			output.writeChars(mOutputter.outputString(exportCfg(mDialogId)));
			output.close();
		} catch (FileNotFoundException e) {
			Plouik.error(TAG,"Save data - FileNotFoundException: "+e.getMessage());
		} catch (IOException e) {
			Plouik.error(TAG,"Save data - IOException: " +e.getMessage());
		}
    }
    
    /** New icon callback */
    public void icon(IconDialog.PresetIcon _icon) {
		// Change the icon image of the current selected preset
		if ((mPresetId>=0) && (mPresetId<elements[mDialogId-1].size())) {
			SketchbookPreset vPreset = elements[mDialogId-1].get(mPresetId);
			vPreset.setIcon(_icon);
			mPresets.invalidateViews();
		}
	}

	/** Refresh the panel */
	public void refresh(boolean _unselect) { if (_unselect) { mPresetId = -1; } mPresets.invalidateViews(); changes[mDialogId-1] = false;}
	
	/** Clear all the presets (called when user changed its root folder) */
	public void clearPresets() { elements[mDialogId-1].clear(); mPresetId = -1; refresh(true);	}
    
}
