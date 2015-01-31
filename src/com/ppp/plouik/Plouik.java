package com.ppp.plouik;

import java.io.*;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.helpers.DefaultHandler;

import com.ppp.plouik.IconDialog.PresetIcon;
import com.ppp.plouik.NewDialog.EnvMode;
import com.ppp.plouik.NewDialog.FillBut;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * The main Activity class
 * @author Pouf-Pouf Production
 *
 */
public class Plouik extends Activity {
	static final String 		LOG 				= "Sketch.";
	static final String 		TAG 				= "activity";
	static final String			CONFIGURATION 		= "configuration";
	static final String			LAYER				= "_";
	static final String			DATA				= ".";
	static String 				VERSION;
	
	static final int			DIALOG_SPLASH_ID	= 0;
	static final int			DIALOG_NEW_ID		= 1;
	static final int			DIALOG_LOAD_ID		= 2;	// deprecated
	static final int			DIALOG_SAVE_ID		= 3;	// deprecated
	static final int			DIALOG_TOOLS_ID		= 4;
	static final int			DIALOG_EXPORT_ID	= 5;
	static final int			DIALOG_QUIT_ID		= 6;
	static final int			DIALOG_PRESET1_ID	= 7;
	static final int			DIALOG_PRESET2_ID	= 8;
	static final int			DIALOG_BRUSH_ID		= 9;
	static final int			DIALOG_COLOR_ID		= 10;
	static final int			DIALOG_PATTERN_ID	= 11;
	static final int			DIALOG_OP_ID		= 12;
	static final int			DIALOG_BROWSER_ID	= 13;
	static final int			DIALOG_XML_ID		= 14;
	static final int			DIALOG_ICON_ID		= 15;
	
	
	static public int			mScreenOrientation;
	static public File			root 				= new File("/");
	
	/** The confirmation dialog */
	public ConfirmDialog		mConfirmDialog;
	public SplashDialog			mSplashDialog;
	private FileBrowserDialog	mBrowserDialog;
	private FileBrowserDialog	mXMLDialog;
	private IconDialog			mIconDialog;
	
	/** The action menus buttons */
	private ImageView			mOpButton;
	private ImageView			mColorButton;
	private ImageView			mBrushButton;
	private ImageView			mPatternButton;
	private ImageView			mPresetButton1;
	private ImageView			mPresetButton2;
	private ImageView			mUndoButton;
	
	/** The dialogs */
	private PresetDialog		mPresetDialog1;
	private PresetDialog		mPresetDialog2;
	private OpDialog			mOpDialog;
	private int					mColor = Color.TRANSPARENT;

	
	/** Not really pretty but...*/
	static int					mStrokeSizeMax;
	static int					mStrokeSizeThreshold;

	/** The sketchbook surface view */
	private SketchbookView		mSketchbookView; 
	
	/** The XML outputter for char sequence conversion */
	private XMLOutputter		mOutputter;
	
	/** The osd layout */
	private LinearLayout		mOSD;
	
	static void trace(String _tag, String _msg) {   Log.i(LOG+_tag, _msg);    }
	static void error(String _tag, String _msg) {  Log.e(LOG+_tag, _msg); 	}
    private void log(String _tag, String _msg) {
    	/*
    	String lines[] = _msg.split("\n");
    	for (String f : lines) { Plouik.trace(TAG, _tag+f.substring(0, f.length()-1)); }
    	*/
    }
	
    /** May be do remove the enum ? */
	public enum NavButton {
		MOVE (R.id.main_nav_move, R.drawable.desktopmoveoff, R.drawable.desktopmovehalf, R.drawable.desktopmoveon),
		PICKER (R.id.main_nav_picker, R.drawable.desktoppickeroff, R.drawable.desktoppickeron, 0),
		ZOOMIN (R.id.main_nav_zoomin, R.drawable.desktopzoomin, 0, 0),
		ZOOMOUT (R.id.main_nav_zoomout, R.drawable.desktopzoomout, 0, 0),
		SPLASH (R.id.main_nav_splash, R.drawable.desktopabout, 0, 0);
		
		private int 		mId;								// The desktop button id
		private int []		mRes 	= new int[] { 0, 0, 0 };	// The drawable ressources (depending on the state)
		private int			mState 	= 0;						// The navigation button state
		private int			mUser 	= 0;						// A special user value
		
		public int			getId()						{ return mId; }
		public int			getRes()					{ return mRes[mState]; }
		public int			getState()					{ return mState; }
		public int			getUser()					{ return mUser; }
		public void			setUser(int _value)			{ mUser = _value; }
		public void			setState(int _value)		{ if ((_value>=0) && (_value<3) && (mRes[_value]!=0)) { mState = _value; } }
		NavButton(int id, int res0, int res1, int res2) { mId = id; mRes[0] = res0; mRes[1] = res1; mRes[2] = res2; }
		
	}
	
	/**
	 * Update the button area regarding the displayed tools
	 */
	private void updateButtonArea() {
		// Add the operations buttons
		ImageView 	button;
		
		button = (ImageView) findViewById(R.id.main_preset1);
		button.setVisibility(((ToolsDialog.visibility>>12)%4!=0)?View.VISIBLE:View.GONE);
		button.setImageResource(((ToolsDialog.visibility>>12)%4==1)?R.drawable.desktoppreset:R.drawable.desktoppresetlock);
		
		button = (ImageView) findViewById(R.id.main_preset2);
		button.setVisibility(((ToolsDialog.visibility>>10)%4!=0)?View.VISIBLE:View.GONE);
		button.setImageResource(((ToolsDialog.visibility>>10)%4==1)?R.drawable.desktoppreset:R.drawable.desktoppresetlock);
		
		button = (ImageView) findViewById(R.id.main_brush);
		button.setVisibility(((ToolsDialog.visibility>>8)%4!=0)?View.VISIBLE:View.GONE);
		
		button = (ImageView) findViewById(R.id.main_color);
		button.setVisibility(((ToolsDialog.visibility>>6)%4!=0)?View.VISIBLE:View.GONE);
		if (mColor!=ColorDialog.color) {
			Bitmap colorBitmapIm = BitmapFactory.decodeResource(getResources(), R.drawable.desktopvoid);
			Bitmap colorBitmap = colorBitmapIm.copy(Config.ARGB_8888, true);
			colorBitmapIm.recycle(); colorBitmapIm = null;
			Canvas colorCanvas = new Canvas(colorBitmap);
			colorCanvas.drawColor(ColorDialog.color, PorterDuff.Mode.MULTIPLY);
			button.setImageBitmap(colorBitmap);
			mColor = ColorDialog.color;
		}
		
		button = (ImageView) findViewById(R.id.main_pattern);
		button.setVisibility(((ToolsDialog.visibility>>4)%4!=0)?View.VISIBLE:View.GONE);
		
		button = (ImageView) findViewById(R.id.main_operation);
		button.setVisibility(((ToolsDialog.visibility>>2)%4!=0)?View.VISIBLE:View.GONE);
		
		button = (ImageView) findViewById(R.id.main_undo);
		button.setVisibility((ToolsDialog.visibility%4!=0)?View.VISIBLE:View.GONE);
		
		button = (ImageView) findViewById(R.id.main_nav_noturn);
		button.setVisibility(BrushDialog.smudge>0?View.VISIBLE:View.INVISIBLE);
		
		setRequestedOrientation(mScreenOrientation);
	}
	
	private void updateNav() {
		/** Generic nav button */
		for (NavButton i: NavButton.values()) {
			ImageView img = (ImageView) findViewById(i.getId());
			img.setImageResource(i.getRes());
		}
		/** layer button */
		ImageView img = (ImageView) findViewById(R.id.main_nav_layer);
		img.setImageResource(SketchbookData.LAYERS[SketchbookData.LAYER]);
		updateButtonArea();
	}
	
	public void displayText(String _text) {
		TextView elt = new TextView(getBaseContext());
		elt.setText(_text);
		displayStuff(elt);
	}
	
	private void displayStuff(View _view) {
		mOSD.removeAllViews();
		mOSD.addView(_view);
	}
	
	/** Initialized the Content View */
	private void initContentView()
	{
		setContentView(R.layout.desktop);
		
		// get handles to the SketchbookView from XML and its SketchbookThread
        mSketchbookView = (SketchbookView) findViewById(R.id.main_surface);
        mSketchbookView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				boolean ret = mSketchbookView.onTouchEvent(event);
				if (event.getAction()==MotionEvent.ACTION_UP) { updateNav(); }
				return ret;
			}
		});
        
        // store the osd layout for animated information
        mOSD = (LinearLayout) findViewById(R.id.main_anim);
        
        //=================================
        // Create the options menu dialogs
        //=================================  
        
        //=====================================
        // Handle the tools menu from the dock
        //=====================================
        
        // Undo button
        //--------------------
        mUndoButton = (ImageView) findViewById(R.id.main_undo);
        mUndoButton.setOnTouchListener(new OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction()==MotionEvent.ACTION_DOWN) { mSketchbookView.getThread().undo(); }
				return true;
			}
        });
        
        // Operation dialog
        //--------------------
        mOpButton = (ImageView) findViewById(R.id.main_operation);
        mOpButton.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction()==MotionEvent.ACTION_DOWN) { showDialog(DIALOG_OP_ID); }
				return true;
			}
        });
        
        // Preset dialog #1
        //--------------------
        mPresetButton1 = (ImageView) findViewById(R.id.main_preset1);
        mPresetButton1.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction()==MotionEvent.ACTION_DOWN) { showDialog(DIALOG_PRESET1_ID); }
				return true;
			}
        });
        
        // Preset dialog #2
        //--------------------
        mPresetButton2 = (ImageView) findViewById(R.id.main_preset2);
        mPresetButton2.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction()==MotionEvent.ACTION_DOWN) { showDialog(DIALOG_PRESET2_ID); }
				return true;
			}
        });
        
        // Color dialog
        //--------------------
        mColorButton = (ImageView) findViewById(R.id.main_color);
        mColorButton.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction()==MotionEvent.ACTION_DOWN) { showDialog(DIALOG_COLOR_ID); }
				return true;
			}
        });
        
        // Brush dialog
        //--------------------
        mBrushButton = (ImageView) findViewById(R.id.main_brush);
        mBrushButton.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction()==MotionEvent.ACTION_DOWN) { showDialog(DIALOG_BRUSH_ID); }
				return true;
			}
        });
        
        // Pattern dialog
        //--------------------
        mPatternButton = (ImageView) findViewById(R.id.main_pattern);
        mPatternButton.setOnTouchListener(new OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
        		if (event.getAction()==MotionEvent.ACTION_DOWN) { showDialog(DIALOG_PATTERN_ID); }
        		return true;
        	}
        });
        
        //=====================================
        // The navigations buttons
        //=====================================
        for (NavButton i: NavButton.values()) {
        	findViewById(i.getId()).setOnClickListener(new View.OnClickListener(){ public void onClick(View v){doNavEvent(v.getId());}});
        }
        findViewById(NavButton.MOVE.getId()).setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View v) {NavButton.MOVE.setState(2); NavButton.MOVE.setUser(0); updateNav(); return true; }
		});
        
        findViewById(R.id.main_nav_layer).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (SketchbookData.LAYER == SketchbookData.NOLAYER) {
					if (ToolsDialog.isOperationVisible()) {	showDialog(DIALOG_OP_ID); }
				}
				else {
					SketchbookData.LAYER = 1+(SketchbookData.LAYER)%4;
					updateNav();
					mSketchbookView.getThread().change();
				}
				
			}
        });
        
        // Initialize the buttons states
        updateButtonArea();
        
	}
	
    /** Called when the activity is first created. */
    @Override public void onCreate(Bundle savedInstanceState) {
    	
    	Plouik.trace(TAG,"[CREATE]  Launch the activity");
    	
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        
        //=================================
        // Some initializations
        //=================================
        Display screen 				= getWindowManager().getDefaultDisplay();
        SketchbookView.WIDTH 		= screen.getWidth();
        SketchbookView.HEIGHT		= screen.getHeight();
        VERSION 					= getString(R.string.version);
        mScreenOrientation 			= this.getPackageManager().resolveActivity(getIntent(), 0).activityInfo.screenOrientation;
        PresetDialog.elements[0] 	= new PresetDialog.PresetArray();
        PresetDialog.elements[1] 	= new PresetDialog.PresetArray();
        mStrokeSizeMax 				= getApplicationContext().getResources().getDimensionPixelSize(R.dimen.strokeSizeMax);
        mStrokeSizeThreshold 		= getApplicationContext().getResources().getDimensionPixelSize(R.dimen.strokeSizeThreshold);
        mConfirmDialog 				= new ConfirmDialog(this);
        mOutputter 					= new XMLOutputter(Format.getPrettyFormat());
        
        //=================================
        // Initialization of the content view
        //=================================
        
        initContentView();
        
    }
    
    /**
	 * Touch a nav button (The nav button event has been removed, but
	 * we continue to use this 'events' when touching one of the left side buttons)
	 * @param idValue is the id value of the button (or simulated by a left side buttons)
	 */
	public void doNavEvent(int idValue) {
		if (mSketchbookView.getThread()!=null) {
			switch(idValue) {
				case R.id.main_nav_zoomin:		mSketchbookView.getThread().nextZoom(false); displayText(mSketchbookView.getThread().getZoomName()); break;
				case R.id.main_nav_zoomout:		mSketchbookView.getThread().nextZoom(true); displayText(mSketchbookView.getThread().getZoomName()); break;
				case R.id.main_nav_move:
					// If the use touchs the move buttons two times, the picture is centered
					switch(NavButton.MOVE.getState()) {
					case 0:	NavButton.MOVE.setState(NavButton.MOVE.getState()+1); break;
					case 1: NavButton.MOVE.setState(NavButton.MOVE.getState()+1); NavButton.MOVE.setUser(0); break;
					case 2: if (NavButton.MOVE.getUser()==0) { mSketchbookView.getThread().center(); } NavButton.MOVE.setState(0); break;
					}
					updateNav();
					break;
				case R.id.main_nav_picker:
					NavButton.PICKER.setState(1-NavButton.PICKER.getState());
					updateNav();
					break;
				case R.id.main_nav_splash:
					if (mSplashDialog!=null) { mSplashDialog.updateOverview(); }
					showDialog(Plouik.DIALOG_SPLASH_ID);
					break;
			}
		}
	}
    
    @Override protected void onStart() { super.onStart(); if (SketchbookData.picture==null) { showDialog(DIALOG_SPLASH_ID); } }
    
    @Override protected void onPrepareDialog(int _id, Dialog _dialog) {
    	SketchbookDialog dialog = (SketchbookDialog)_dialog;
    	dialog.onShow();
    }
    
    @Override public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      trace(TAG,"onConfigurationChanged");
      
      Display screen 			= getWindowManager().getDefaultDisplay();
      SketchbookView.WIDTH 		= screen.getWidth();
      SketchbookView.HEIGHT		= screen.getHeight();
  
      initContentView();
      
    }
    
    /**
     * Build the dialog when needed
     * Note: this is the big point of the soft. It is necessary to handle the settings
     * even if the corresponding dialog is not still created. That's why the whole configuration
     * is stored in the static SketchbookConfig class (and not in each of these dialogs)
     */
    @Override protected Dialog onCreateDialog(int id) {
    	Dialog dialog;
    	switch (id) {
    	case DIALOG_SPLASH_ID:
    		dialog = new SplashDialog(this, new SplashDialog.SplashDialogListener() {
				public void onSplashClick() { displayText(VERSION); updateNav(); }
				public void onRootClick()
				{
					showDialog(DIALOG_BROWSER_ID);
					mBrowserDialog.setListener(new FileBrowserDialog.Listener() {
							public void onValid(String _filename, FileBrowserDialog.World _world) { setRoot(_filename, true); }}
						, FileBrowserDialog.Type.FOLDER, FileBrowserDialog.Option.NEWFOLDER, "", "/");
				}
            });
    		mSplashDialog=(SplashDialog) dialog;
    		break;
    	case DIALOG_NEW_ID:
    		dialog = new NewDialog(this, new NewDialog.NewDialogListener() {
    			public void onValid(int width, int height, FillBut fill,EnvMode env) {
    				if (env!=NewDialog.EnvMode.NONE) {
    					int res = R.raw.inking;
    					if (env==NewDialog.EnvMode.PURIKURA) 		{ res = R.raw.purikura; }
    					else if (env==NewDialog.EnvMode.PAINTING)	{ res = R.raw.painting; }
    					else if (env==NewDialog.EnvMode.COLOR)		{ res = R.raw.colorcontext; }
    					InputStream vStream = getApplicationContext().getResources().openRawResource(res);
    					SAXBuilder sxb = new SAXBuilder();
            			try {
            				Document doc = sxb.build(vStream);
            				setConfiguration(doc.getRootElement());
            			}
            			catch (Exception e) {}
    				}
    				
    				if (fill==NewDialog.FillBut.CAMERA) {
    					PlouikCamera.mWidth = width;
    					PlouikCamera.mHeight = height;
    					Plouik.trace(TAG,"[LOAD]    Camera preview");
    					Intent vIntent = new Intent(getBaseContext(), PlouikCamera.class);
    					startActivityForResult(vIntent, 0);
    				}
    				else {
	    				mSketchbookView.getThread().newImage(width, height, fill);
    				}

					mSketchbookView.update();
					updateNav();
    			}
            });
    		break;
    	case DIALOG_TOOLS_ID:
    		dialog = new ToolsDialog(this, new ToolsDialog.ToolsDialogListener() {
    			public void onOKClick() {
    				mScreenOrientation = ToolsDialog.getOrientation();
    				getPackageManager().resolveActivity(getIntent(), 0).activityInfo.screenOrientation = mScreenOrientation;
            		updateButtonArea();
    			}
            });
    		break;
    	case DIALOG_BROWSER_ID:
    		dialog = new FileBrowserDialog(this);
    		mBrowserDialog = (FileBrowserDialog) dialog;
    		break;
    	case DIALOG_XML_ID:
    		dialog = new FileBrowserDialog(this);
    		mXMLDialog = (FileBrowserDialog) dialog;
    		break;
    	case DIALOG_ICON_ID:
    		dialog = new IconDialog(this);
    		mIconDialog = (IconDialog) dialog;
    		break;
    	case DIALOG_PRESET1_ID:
    		dialog = new PresetDialog(this, new PresetDialog.OnPresetListener() {
				public void presetChanged(Element _elt) {
					if (_elt!=null) {
						if (mPresetDialog2!=null) { mPresetDialog2.refresh(true); }
						BrushDialog.importCfg(_elt.getChild(BrushDialog.TAG));
						ColorDialog.importCfg(_elt.getChild(ColorDialog.TAG));
						PatternDialog.importCfg(_elt.getChild(PatternDialog.TAG), getApplicationContext().getResources());
						mSketchbookView.update();
            			updateButtonArea();
					}
				}
				public void presetCreated() { if (mPresetDialog2!=null) { mPresetDialog2.refresh(true); } }
				public void onLock() { updateButtonArea(); }
				public void onLoad() {
					showDialog(DIALOG_XML_ID);
					mXMLDialog.setListener(new FileBrowserDialog.Listener() {
						public void onValid(String _filename, FileBrowserDialog.World _world) {
							mPresetDialog1.load(_filename);
						}}
					, FileBrowserDialog.Type.FILE, FileBrowserDialog.Option.NONE, "xml", root.getAbsolutePath());
				}
				public void onSave() {
					showDialog(DIALOG_XML_ID);
					mXMLDialog.setListener(new FileBrowserDialog.Listener() {
						public void onValid(String _filename, FileBrowserDialog.World _world) {
							mPresetDialog1.save(_filename);
						}}
					, FileBrowserDialog.Type.FILE, FileBrowserDialog.Option.NEWNAME, "xml", root.getAbsolutePath());
				}
				public void onIcon() {
					showDialog(DIALOG_ICON_ID);
					mIconDialog.setListener(new IconDialog.Listener() {
						public void onValid(PresetIcon _icon) { mPresetDialog1.icon(_icon); }
					});
				}
            }, 1);
    		mPresetDialog1 = (PresetDialog) dialog;
    		break;
    	case DIALOG_PRESET2_ID:
    		dialog = new PresetDialog(this, new PresetDialog.OnPresetListener() {
				public void presetChanged(Element _elt) { 
					if (_elt!=null) {
						if (mPresetDialog1!=null) { mPresetDialog1.refresh(true); }
						BrushDialog.importCfg(_elt.getChild(BrushDialog.TAG));
						ColorDialog.importCfg(_elt.getChild(ColorDialog.TAG));
						PatternDialog.importCfg(_elt.getChild(PatternDialog.TAG), getApplicationContext().getResources());
						mSketchbookView.update();
            			updateButtonArea();
					}
				}
				public void presetCreated() { if (mPresetDialog1!=null) { mPresetDialog1.refresh(true); } }
				public void onLock() { updateButtonArea(); }
				public void onLoad() {
					showDialog(DIALOG_XML_ID);
					mXMLDialog.setListener(new FileBrowserDialog.Listener() {
						public void onValid(String _filename, FileBrowserDialog.World _world) {
							mPresetDialog2.load(_filename);
						}}
					, FileBrowserDialog.Type.FILE, FileBrowserDialog.Option.NONE, "xml", root.getAbsolutePath());
				}
				public void onSave() {
					showDialog(DIALOG_XML_ID);
					mXMLDialog.setListener(new FileBrowserDialog.Listener() {
						public void onValid(String _filename, FileBrowserDialog.World _world) {
							mPresetDialog2.save(_filename);
						}}
					, FileBrowserDialog.Type.FILE, FileBrowserDialog.Option.NEWNAME, "xml", root.getAbsolutePath());
				}
				public void onIcon() {
					showDialog(DIALOG_ICON_ID);
					mIconDialog.setListener(new IconDialog.Listener() {
						public void onValid(PresetIcon _icon) { mPresetDialog2.icon(_icon); }
					});
				}
            }, 2);
    		mPresetDialog2 = (PresetDialog) dialog;
    		break;
    	case DIALOG_BRUSH_ID:
    		dialog = new BrushDialog(this, new BrushDialog.OnBrushChangedListener() {
    			public void brushChanged() { mSketchbookView.update(); updateButtonArea(); }
            });
    		break; 
    	case DIALOG_COLOR_ID:
            dialog = new ColorDialog(this, new ColorDialog.OnColorChangedListener() {
    			public void colorChanged(float[] hsv) { mSketchbookView.update(); updateButtonArea(); }
            });
            break;
    	case DIALOG_PATTERN_ID:
    		dialog = new PatternDialog(this, new PatternDialog.OnPatternChangedListener() {
				public void patternChanged(PatternDialog.Pattern _pat) { }
    		});
    		break;
    	case DIALOG_OP_ID:
    		mOpDialog = new OpDialog(this, new OpDialog.OpDialogListener() {
            	public void onValid(int id) {
            		switch(id) {
            		case OpDialog.BUTTON_TURN90:		mSketchbookView.getThread().rotateImage90(); 	break;
            		case OpDialog.BUTTON_TURN180:		mSketchbookView.getThread().rotateImage180(); 	break;
            		case OpDialog.BUTTON_TURN270:		mSketchbookView.getThread().rotateImage270(); 	break;
            		case OpDialog.BUTTON_FLIPH:			mSketchbookView.getThread().flipImageHoriz(); 	break;
            		case OpDialog.BUTTON_FLIPV:			mSketchbookView.getThread().flipImageVert(); 	break;
            		case OpDialog.BUTTON_LAYER:			mSketchbookView.getThread().layer();			break;
            		case OpDialog.BUTTON_LAYERMERGE:	mSketchbookView.getThread().mergeLayer();		break;
            		case OpDialog.BUTTON_LAYERFLIP:		mSketchbookView.getThread().flipLayer();		break;
            		}
            		updateNav();
            	}
            });
    		dialog = mOpDialog;
    		break;
    	default:
    		dialog=null;	
    	}
    	return dialog;
    }
    
    /** Upon being resumed we can retrieve the current state. */
    @Override protected void onResume() {
        super.onResume();
 
	    SharedPreferences prefs = getPreferences(0);
	        
	    Plouik.trace(TAG,"[RESUME]  (1/4) Initialise the settings ("+((SketchbookData.picture!=null)?"P":"")+")");
	        
	    Plouik.trace(TAG,"[RESUME]  (2/4) Get the preferences back");
	        
	    setRoot(prefs.getString("root", "/"), false);
	        
	    Plouik.trace(TAG,"[RESUME]  (3/4) Get the root: "+ Plouik.root.getPath());
	
	    String vPresets = prefs.getString("presets", null);
	    if (vPresets!=null) {
	      	SAXBuilder sxb = new SAXBuilder();
	      	sxb.setDTDHandler(new DefaultHandler());
			try {
				// TO DO: figure out the warning "DTD handlers aren't supported" 
				Document doc = sxb.build(new StringReader(vPresets));
		    	log(TAG, mOutputter.outputString(doc));
				setConfiguration(doc.getRootElement());
				updateButtonArea();
					
				Plouik.trace(TAG,"[RESUME]  (4/4) Get XML settings description");
			}
			catch (Exception e) {		
				Plouik.error(TAG,"[RESUME]  (4/4) Error reading the XML settings description: "+e.getMessage());
			}
	    }
	    else {
	    	Plouik.trace(TAG,"[RESUME]  (4/4) No XML settings description");
	    }
    }
    
    /** Any time we are paused we need to save away the current state, so it will be restored correctly when we are resumed. */
    @Override protected void onPause() {
    	super.onPause();
    	
    	Plouik.trace(TAG,"[PAUSE]   (1/4) Save the preferences");
    	
    	SharedPreferences.Editor editor = getPreferences(0).edit();

    	Document doc = new Document(getConfiguration(false));
		doc.setDocType(new DocType(CONFIGURATION));
    	editor.putString("presets", mOutputter.outputString(doc));
    	log(TAG, mOutputter.outputString(doc));
    	
    	Plouik.trace(TAG,"[PAUSE]   (2/4)  Save the XML settings description");
    	
    	editor.putString("root", Plouik.root.getPath());
    	
    	Plouik.trace(TAG,"[PAUSE]   (3/4)  Save the path root: "+Plouik.root.getPath());
    	
    	if (editor.commit()) { 
    		Plouik.trace(TAG,"[PAUSE]   (4/4) Commit succeed");
    	}
    	else {
    		Plouik.error(TAG,"[PAUSE]   (4/4) Commit failed");
    	}
    }
    
    /** Build the configuration element */
    private Element getConfiguration(boolean _save) {
    	Element mElt = new Element(Plouik.TAG);
		mElt.addContent(PresetDialog.exportCfg(1));
		mElt.addContent(PresetDialog.exportCfg(2));
		mElt.addContent(BrushDialog.exportCfg());
		mElt.addContent(ColorDialog.exportCfg());
		mElt.addContent(PatternDialog.exportCfg());
		mElt.addContent(ToolsDialog.exportCfg(_save));
		return mElt;
    }
    
    /** Set the whole configuration */
    private void setConfiguration(Element _root) {
    	if (_root!=null) {
	    	PresetDialog.importCfg(getBaseContext(), _root.getChild(PresetDialog.TAG+"1"), 1);
			PresetDialog.importCfg(getBaseContext(), _root.getChild(PresetDialog.TAG+"2"), 2);
			BrushDialog.importCfg(_root.getChild(BrushDialog.TAG));
			ColorDialog.importCfg(_root.getChild(ColorDialog.TAG));
			PatternDialog.importCfg(_root.getChild(PatternDialog.TAG), getResources());
			ToolsDialog.importCfg(_root.getChild(ToolsDialog.TAG));
    	}
    }
    
    /** Notification that something is about to happens, to give the Activity a chance to save state */
    @Override protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    }
    
    /** Create the options menu from XML */
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
    
    /** Handles the options menu callbacks */
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	
    	case R.id.option_new :
    		showDialog(DIALOG_NEW_ID);
    		return true;
    		
    	case R.id.option_open:
    		showDialog(DIALOG_BROWSER_ID);
			mBrowserDialog.setListener(new FileBrowserDialog.Listener() {
					public void onValid(String _filename, FileBrowserDialog.World _world) {
						File bitmapFile = new File(_filename);
						Plouik.trace(TAG,"[LOAD]    filename: "+bitmapFile.getPath());
						
						if (_world==FileBrowserDialog.World.LAYER) {
							// Load the bitmap
							if (mSketchbookView.getThread()!=null) { mSketchbookView.getThread().newImage(bitmapFile.getPath(), true); }
						}
						else {					
							// Load the bitmap
							if (mSketchbookView.getThread()!=null) { mSketchbookView.getThread().newImage(bitmapFile.getPath(), false); }
							
							// Load the layer up
							File bitmapFileLayer = new File(bitmapFile.getParent()+File.separator+Plouik.LAYER+bitmapFile.getName());
							if ((bitmapFileLayer.exists()) && (mSketchbookView.getThread()!=null)) {
								mSketchbookView.getThread().newImage(bitmapFileLayer.getPath(), true);
							}
		            		
		            		// Load the data (if any) (and update, offcourse)
		            		if (_world==FileBrowserDialog.World.ON) {
			            		File dataFile = new File(bitmapFile.getParent()+File.separator+"."+bitmapFile.getName());
			            		Plouik.trace(TAG,"[LOAD]    check data: "+dataFile.getPath());
			            		if (dataFile.exists()) {
			            			Plouik.trace(TAG,"[LOAD]    load data: "+dataFile.getPath());
			            			SAXBuilder sxb = new SAXBuilder();
			            			try {
			            				Document doc = sxb.build(dataFile);
			            				//log(TAG, mOutputter.outputString(doc));
			            				setConfiguration(doc.getRootElement());
			            			}
			            			catch (Exception e) {}
									mSketchbookView.update();
			            		}	
		            		}
						}
						updateNav();
					}}
				, FileBrowserDialog.Type.FILE, FileBrowserDialog.Option.WORLD, "", root.getAbsolutePath());
    		return true;
    		
    	case R.id.option_save:
    		showDialog(DIALOG_BROWSER_ID);
    		mBrowserDialog.setListener(new FileBrowserDialog.Listener() {
				public void onValid(String _filename, FileBrowserDialog.World _world) {
					File bitmapFile = new File(_filename);
					Plouik.trace(TAG,"[SAVE]    filename: "+bitmapFile.getPath());
					
					// Save the bitmap
					try {
						// First layer
						DataOutputStream output = new DataOutputStream(new FileOutputStream(bitmapFile.getAbsolutePath()));
						mSketchbookView.getThread().getBitmap(false).compress(Bitmap.CompressFormat.PNG, 100, output);
						output.close();
						
						// Second layer
						File bitmapFileLayer = new File(bitmapFile.getParent()+File.separator+Plouik.LAYER+bitmapFile.getName());
						if (SketchbookData.LAYER!=SketchbookData.NOLAYER) {
							output = new DataOutputStream(new FileOutputStream(bitmapFileLayer.getAbsolutePath()));
							mSketchbookView.getThread().getBitmap(true).compress(Bitmap.CompressFormat.PNG, 100, output);
							output.close();
						}
						else {
							if (bitmapFileLayer.exists()) { bitmapFileLayer.delete(); }
						}
					} catch (FileNotFoundException e) {
						Plouik.error(TAG,"[SAVE]    bitmap - FileNotFoundException: "+e.getMessage());
					} catch (IOException e) {
						Plouik.error(TAG,"[SAVE]    bitmap - IOException: " +e.getMessage());
					}
					
					// Save the data
					try {
						DataOutputStream output = new DataOutputStream(new FileOutputStream(
								bitmapFile.getParent()+File.separator+Plouik.DATA+bitmapFile.getName()));
						output.writeChars(mOutputter.outputString(getConfiguration(true)));
						output.close();
					} catch (FileNotFoundException e) {
						Plouik.error(TAG,"[SAVE]    data - FileNotFoundException: "+e.getMessage());
					} catch (IOException e) {
						Plouik.error(TAG,"[SAVE]    data - IOException: " +e.getMessage());
					}
				}}
			, FileBrowserDialog.Type.FILE, FileBrowserDialog.Option.NEWNAME, "png", root.getAbsolutePath());
    		return true;
    		
    	case R.id.option_web:
    		//File bitmapFile = new File(getCacheDir()+File.separator+"Plouik.png");
    		File bitmapFile = new File(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPath()+File.separator+"plouik.png");
    		Plouik.trace(TAG,"[EXPORT]  filename: "+bitmapFile.getAbsolutePath());
    		Plouik.trace(TAG,"[EXPORT]  uri:      "+Uri.parse("file://"+bitmapFile.getAbsolutePath()).getPath());
			
			// Save the bitmap
			try {
				DataOutputStream output = new DataOutputStream(new FileOutputStream(bitmapFile.getAbsolutePath()));
				mSketchbookView.getThread().getBitmap(false).compress(Bitmap.CompressFormat.PNG, 100, output);
				output.close();
				
			} catch (FileNotFoundException e) {
				Plouik.error(TAG,"[EXPORT]  bitmap - FileNotFoundException: "+e.getMessage());
			} catch (IOException e) {
				Plouik.error(TAG,"[EXPORT]  bitmap - IOException: " +e.getMessage());
			}
    		
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			
			sendIntent .putExtra(Intent.EXTRA_SUBJECT, "Plouik picture");
			sendIntent .putExtra(Intent.EXTRA_TEXT, "Designed by Android Plouik");
			sendIntent.setType("image/jpeg");
			sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+bitmapFile.getAbsolutePath()));			
			sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(Intent.createChooser(sendIntent, "Plouik"));
    		return true;
    		
    	case R.id.option_tool:
    		showDialog(DIALOG_TOOLS_ID);
    		return true;
    		
    	case R.id.option_quit:
    		mConfirmDialog.setOnConfirmListener(new ConfirmDialog.OnConfirmListener() {
				public void buttonClicked(boolean confirm) { if (confirm) { Plouik.trace(TAG,"[ACTIVITY]Finish"); finish(); } }
    		});
    		mConfirmDialog.show();
    		return true;
    	}
    	return false;
    }
    
    /** Set the path where will be save the sketchbook ressources */
	public void setRoot(String path, boolean build) {
		String rootPath = (path!=null)?path:"/";
		Plouik.trace(Plouik.LOG+TAG, "[ROOT]    Root path: "+rootPath);
		
		// Build the Plouik root folders
		Plouik.root = new File(rootPath);
		if (!Plouik.root.exists() || !Plouik.root.isDirectory() || Plouik.root.list()==null) {
			Plouik.root = new File("/");
		}
		else if (build) {
			boolean rootEmpty = (Plouik.root.list().length==0);
			
			File mPictFile = new File(Plouik.root.getPath()+File.separator+"PPPicture");
			boolean pictExist = mPictFile.exists();
			if (rootEmpty||pictExist) {
				if (!pictExist) { mPictFile.mkdir(); }
				saveRaw(R.raw.littleredridinghood, mPictFile.getPath()+File.separator+"redhood.png");
				saveRaw(R.raw.orientaldragon, mPictFile.getPath()+File.separator+"orientaldragon.png");
				saveRaw(R.raw.foxncrow, mPictFile.getPath()+File.separator+"foxncrow.png");
				
				saveRaw(R.raw.colorcontext, mPictFile.getPath()+File.separator+".redhood.png");
				saveRaw(R.raw.colorcontext, mPictFile.getPath()+File.separator+".orientaldragon.png");
				saveRaw(R.raw.colorcontext, mPictFile.getPath()+File.separator+".foxncrow.png");
			}
			
			File mXmlFile = new File(Plouik.root.getPath()+File.separator+"PPPencil");
			boolean xmlExists = mXmlFile.exists();

			if (rootEmpty||xmlExists) {
				if (!xmlExists) { mXmlFile.mkdir(); }
				saveRaw(R.raw.colorpens, mXmlFile.getPath()+File.separator+"colorpens.xml");
				saveRaw(R.raw.palettegray, mXmlFile.getPath()+File.separator+"palettegray.xml");
				saveRaw(R.raw.paletteskin, mXmlFile.getPath()+File.separator+"paletteskin.xml");
			}
		}
	}

	/** Save a raw data stream */
	private void saveRaw(int _id, String _name) {
		try {
			File vFileTest = new File(_name);
			if (!vFileTest.exists()) {
				InputStream vStream = getApplicationContext().getResources().openRawResource(_id);
				FileOutputStream vFile = new FileOutputStream(_name);
				byte buf[]=new byte[1024];
			    int len;
			    while((len=vStream.read(buf))>0) { vFile.write(buf,0,len); }
			    vFile.close();
			    vStream.close();
			}
		} catch (FileNotFoundException e) {} catch (IOException e) { }
	}
    
}
