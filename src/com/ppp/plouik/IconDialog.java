package com.ppp.plouik;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class IconDialog extends SketchbookDialog {
	public IconDialog(Context context) { super(context); }

	/** The log TAG */
	static final String TAG = "filebrowser";
	
	public interface Listener {	public void onValid(PresetIcon _icon); };
	private Listener mListener;
	
	/** The preset icon */
	public enum PresetIcon {
		BLUE01		("blue01",		R.drawable.pencilblue01on,		R.drawable.pencilblue01off, 		Color.WHITE),
		RED01		("red01",		R.drawable.pencilred01on,		R.drawable.pencilred01off, 			Color.WHITE),
		GREEN01		("green01",		R.drawable.pencilgreen01on,		R.drawable.pencilgreen01off, 		Color.WHITE),
		BLACK01		("black01",		R.drawable.pencilblack01on,		R.drawable.pencilblack01off, 		Color.WHITE),
		PEN01		("pen01",		R.drawable.pencilpen01on,		R.drawable.pencilpen01off, 			Color.WHITE),
		PEN02		("pen02",		R.drawable.pencilpen02on,		R.drawable.pencilpen02off, 			Color.WHITE),
		PEN03		("pen03",		R.drawable.pencilpen03on,		R.drawable.pencilpen03off, 			Color.WHITE),
		BLUE03		("blue03",		R.drawable.pencilblue03on,		R.drawable.pencilblue03off, 		Color.WHITE),
		RED03		("red03",		R.drawable.pencilred03on,		R.drawable.pencilred03off, 			Color.WHITE),
		PINK03		("pink03",		R.drawable.pencilpink03on,		R.drawable.pencilpink03off, 		Color.WHITE),
		PURPLE03	("purple03",	R.drawable.pencilpurple03on,	R.drawable.pencilpurple03off, 		Color.WHITE),
		GREEN03		("green03",		R.drawable.pencilgreen03on,		R.drawable.pencilgreen03off, 		Color.WHITE),
		CYAN03		("cyan03",		R.drawable.pencilcyan03on,		R.drawable.pencilcyan03off, 		Color.WHITE),
		YELLOW03	("yellow03",	R.drawable.pencilyellow03on,	R.drawable.pencilyellow03off, 		Color.WHITE),
		ORANGE03	("orange03",	R.drawable.pencilorange03on,	R.drawable.pencilorange03off, 		Color.WHITE),
		BROWN03		("brown03",		R.drawable.pencilbrown03on,		R.drawable.pencilbrown03off, 		Color.WHITE),
		GREENLIGHT03("greenlight03",R.drawable.pencilgreenlight03on,R.drawable.pencilgreenlight03off,	Color.WHITE),
		GRAY03		("gray03",		R.drawable.pencilgray03on,		R.drawable.pencilgray03off, 		Color.WHITE),
		BLACK03		("black03",		R.drawable.pencilblack03on,		R.drawable.pencilblack03off, 		Color.WHITE),
		MARKER01	("marker01",	R.drawable.pencilmarker01on,	R.drawable.pencilmarker01off, 		Color.WHITE),
		MARKER02	("marker02",	R.drawable.pencilmarker02on,	R.drawable.pencilmarker02off, 		Color.WHITE),
		ROTRING03	("rotring03",	R.drawable.pencilrotring03on,	R.drawable.pencilrotring03off, 		Color.WHITE),
		ROTRING05	("rotring05",	R.drawable.pencilrotring05on,	R.drawable.pencilrotring05off, 		Color.WHITE),
		ROTRING07	("rotring07",	R.drawable.pencilrotring07on,	R.drawable.pencilrotring07off, 		Color.WHITE),
		ROTRING10	("rotring10",	R.drawable.pencilrotring10on,	R.drawable.pencilrotring10off, 		Color.WHITE),
		FEATHER01	("feather01",	R.drawable.pencilfeather01on,	R.drawable.pencilfeather01off, 		Color.WHITE),
		FEATHER02	("feather02",	R.drawable.pencilfeather02on,	R.drawable.pencilfeather02off, 		Color.WHITE),
		BRUSH01		("brush01",		R.drawable.pencilbrush01on,		R.drawable.pencilbrush01off, 		Color.WHITE),
		BRUSH02		("brush02",		R.drawable.pencilbrush02on,		R.drawable.pencilbrush02off, 		Color.WHITE),
		BRUSH03		("brush03",		R.drawable.pencilbrush03on,		R.drawable.pencilbrush03off, 		Color.WHITE),
		BRUSH04		("brush04",		R.drawable.pencilbrush04on,		R.drawable.pencilbrush04off, 		Color.WHITE),
		BRUSH05		("brush05",		R.drawable.pencilbrush05on,		R.drawable.pencilbrush05off, 		Color.WHITE),
		ERASER01	("eraser01",	R.drawable.pencileraser01on,	R.drawable.pencileraser01off, 		Color.WHITE),
		ERASER02	("eraser02",	R.drawable.pencileraser02on,	R.drawable.pencileraser02off, 		Color.WHITE),
		STABILO01	("stabilo01",	R.drawable.pencilstabilo01on,	R.drawable.pencilstabilo01off, 		Color.WHITE),
		TIPPEX01	("tippex01",	R.drawable.penciltippex01on,	R.drawable.penciltippex01off, 		Color.WHITE),
		TIPPEX02	("tippex02",	R.drawable.penciltippex02on,	R.drawable.penciltippex02off, 		Color.WHITE),
		STAMP01		("stamp01",		R.drawable.pencilstamp01on,		R.drawable.pencilstamp01off, 		Color.WHITE),
		SPRAY01		("spray01",		R.drawable.pencilspray01on,		R.drawable.pencilspray01off, 		Color.WHITE),
		AERO01		("aero01",		R.drawable.pencilaero01on,		R.drawable.pencilaero01off, 		Color.WHITE),
		HAND01		("hand01",		R.drawable.pencilhand01on,		R.drawable.pencilhand01off, 		Color.WHITE),
		KNIFE01		("knife01",		R.drawable.pencilknife01on,		R.drawable.pencilknife01off, 		Color.WHITE),
		CHECK01		("check01",		R.drawable.patterncheck01on,	R.drawable.patterncheck01off, 		Color.BLACK),
		DOTS01		("dots01",		R.drawable.patterndots01on,		R.drawable.patterndots01off, 		Color.BLACK),
		DOTS02		("dots02",		R.drawable.patterndots02on,		R.drawable.patterndots02off, 		Color.BLACK),
		LINES01		("lines01",		R.drawable.patternlines01on,	R.drawable.patternlines01off, 		Color.BLACK),
		LINES02		("lines02",		R.drawable.patternlines02on,	R.drawable.patternlines02off, 		Color.BLACK),
		STARS01		("stars01",		R.drawable.patternstars01on,	R.drawable.patternstars01off, 		Color.BLACK),
		STARS02		("stars02",		R.drawable.patternstars02on,	R.drawable.patternstars02off, 		Color.BLACK),
		HEART01		("heart01",		R.drawable.patternheart01on,	R.drawable.patternheart01off, 		Color.BLACK),
		FLOWERS01	("flowers01",	R.drawable.patternflowers01on,	R.drawable.patternflowers01off,		Color.BLACK),
		STONES01	("stones01",	R.drawable.patternstones01on,	R.drawable.patternstones01off, 		Color.BLACK),
		STONES02	("stones02",	R.drawable.patternstones02on,	R.drawable.patternstones02off, 		Color.BLACK),
		STONES03	("stones03",	R.drawable.patternstones03on,	R.drawable.patternstones03off, 		Color.BLACK),
		WOOD01		("wood01",		R.drawable.patternwood01on,		R.drawable.patternwood01off, 		Color.BLACK),
		WOOD02		("wood02",		R.drawable.patternwood02on,		R.drawable.patternwood02off, 		Color.BLACK),
		PAPER01		("paper01",		R.drawable.patternpaper01on,	R.drawable.patternpaper01off, 		Color.BLACK),
		COLOR01		("color01",		R.drawable.icongroundon,		R.drawable.iconground, 				Color.BLACK);
		
		/** The icon attributes */
		final private int		mResOn;
		final private int		mResOff;
		final private String	mName;
		final private int		mBgColor;
		
		/** Constructor */
		PresetIcon(String _name, int _resOn, int _resOff, int _bg) { mName = _name; mResOn = _resOn; mResOff = _resOff; mBgColor = _bg; }
		
		/** Accessor */
		public int getRes(boolean _on)				{ return _on?mResOn:mResOff; }
		public String getName()						{ return mName; }
		public int getBackground()					{ return mBgColor; }
		
		/** Return the PresetIcon regarding its name */
		static PresetIcon getFromName(String _name) {
			PresetIcon ret = BLUE01;
			for (PresetIcon i : PresetIcon.values()) { if (i.getName().equals(_name)) { ret = i; break; }}
			return ret;
		}
	}
	
	/** The icon grid adapter */
	private class IconAdapter extends BaseAdapter {
		private class IconListener implements View.OnClickListener {
			private int pos;
			public IconListener(int _pos) { super(); pos = _pos; }
			public void onClick(View v) { if (mListener!=null) { mListener.onValid(PresetIcon.values()[pos]); } dismiss(); }
		}
		
		/** Some accessors */
		public int 		getCount()			{ return PresetIcon.values().length; }
		public Object 	getItem(int arg0) 	{ return null; }
		public long 	getItemId(int arg0) { return 0;	}
		
		/** Return the preset icon button as ImageView with the good listener */
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			ImageView vRet = new ImageView(getContext());
			vRet.setImageResource(PresetIcon.values()[arg0].getRes(false));
			vRet.setOnClickListener(new IconListener(arg0));
			vRet.setLayoutParams(new GridView.LayoutParams(
					getContext().getResources().getDimensionPixelSize(R.dimen.preset),
					getContext().getResources().getDimensionPixelSize(R.dimen.preset)));
			vRet.setPadding(0, 0, 0, 0);
			vRet.setBackgroundColor(PresetIcon.values()[arg0].getBackground());
			return vRet;
		}
	}
	
	/** Set the listener */
	public void setListener(Listener _listener) { mListener = _listener; }
	

	/** Prepare the panel before showing */
	@Override public void onShow() {
		ImageView 	vImage;
		View 		vView;
		TextView	vText;
		
		vImage = (ImageView) findViewById(R.id.icondialog_brush);
		vImage.setImageResource(PresetDialog.parts[0]?R.drawable.desktopbrush:R.drawable.desktopbrushoff);
		
		vImage = (ImageView) findViewById(R.id.icondialog_color);
		vImage.setImageResource(PresetDialog.parts[1]?R.drawable.desktopcolor:R.drawable.desktopcoloroff);
		
		vImage = (ImageView) findViewById(R.id.icondialog_pattern);
		vImage.setImageResource(PresetDialog.parts[2]?R.drawable.desktoppattern:R.drawable.desktoppatternoff);
		
		vImage = (ImageView) findViewById(R.id.icondialog_preset);
		vImage.setImageResource(PresetDialog.icon.getRes(false));
		vImage.setPadding(0, 0, 0, 0);
		vImage.setBackgroundColor((PresetDialog.parts[1] && PresetDialog.hsv[0]>=0)?Color.HSVToColor(PresetDialog.hsv):Color.WHITE);
		vImage.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { dismiss(); } });
		
		vImage = (ImageView) findViewById(R.id.icondialog_tool);
		vImage.setImageResource(PresetDialog.tool?R.drawable.title_pencil:R.drawable.title_stamp);
		vImage.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { dismiss(); } });
    	
		vView = findViewById(R.id.icondialog_brushoverview);
		vView.setVisibility(PresetDialog.parts[0]?View.VISIBLE:View.INVISIBLE);
		vView.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { dismiss(); } });
		
		vText = (TextView) findViewById(R.id.icondialog_size);
		vText.setText(String.valueOf(PresetDialog.size)+" px");
	}
	
	/** The classical onCreate method */
    @Override
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.icondialog);
        
    	GridView grid = (GridView) findViewById(R.id.icondialog_grid);
    	if (grid!=null) { grid.setAdapter(new IconAdapter()); }
    }

}
