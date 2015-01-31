package com.ppp.plouik;

import java.util.Vector;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * The new dialog for building a new image
 * @author Pouf-Pouf Production
 *
 */
public class NewDialog extends SketchbookDialog {
	
	/** Button values */
	static final int	BUTTON_CANCEL =	0;
	static final int	BUTTON_OK =		1;
	
	/** The NewDialogListener used for getting results */
	public interface NewDialogListener { public void onValid(int width, int height, FillBut _fill, EnvMode _env); }
	
	/** The ToolsDialogListener instance */
	private NewDialogListener		mListener;
	
	/** The model class **/
	private class Model {
		public int		mWidth;
		public int		mHeight;
		public String	mName;
		public Model(String name, int width, int height) {
			mName = name;
			mWidth = width;
			mHeight = height;
		}
	}
	
	/** The models **/
	private Vector<Model>			mModels;
	
	/** The adapter class for spinner */
	private class NewAdapter extends BaseAdapter {
		
		/** The context */
		private Context					mContext;
		
		/** Not very useful override methods */
		public int		getCount()				{ return mModels.size(); }
		public Object	getItem(int position)	{ return null; }
		public long		getItemId(int position)	{ return 0;	}
		
		/** Fill the view */
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView text = new TextView(mContext);
			text.setText(""+mModels.get(position).mWidth+" x "+mModels.get(position).mHeight);
			text.setGravity(Gravity.CENTER_HORIZONTAL);
			text.setTextAppearance(mContext, R.style.SketchbookSpinner);
			return text;
		}
		
		/** The NewAdapter constructor */
		public NewAdapter(Context ctxt) { mContext = ctxt; }
		
		/** Display the drop down elements */
		@Override public View getDropDownView(int position, View convertView, ViewGroup parent) {
			TextView text = new TextView(mContext);
			text.setText(mModels.get(position).mName);
			text.setGravity(Gravity.CENTER_HORIZONTAL);
			return text;
		}
	}
	
	/** New document filling */
	public enum FillBut {
		WHITE 		(R.drawable.iconwhite),
		BLACK 		(R.drawable.iconblack),
		// COLOR 		(R.drawable.iconground),
		ALPHA 		(R.drawable.iconalpha),
		CAMERA 		(R.drawable.iconcamera);
		
		private int mRes;
		
		FillBut(int _res) 							{ mRes = _res; }
		static public FillBut 	get(int _id) 		{ return FillBut.values()[_id]; }
		static public int		getRes(int _id)		{ return FillBut.values()[_id].mRes; }
		static public int 		next(int _id)		{ return (_id+1)%FillBut.values().length; }
	};
	
	/** New document environment */
	public enum EnvMode {
		NONE 		( R.drawable.iconearthoff),
		DRAWING		( R.drawable.pencilrotring03off),
		PAINTING	( R.drawable.pencilbrush05off),
		PURIKURA	( R.drawable.iconcamera),
		COLOR		( R.drawable.pencilred03off);
		
		private int mRes;
		
		EnvMode(int _res)							{ mRes = _res; }
		static public EnvMode get(int _id)			{ return EnvMode.values()[_id]; }
		static public int next(int _id)				{ return (_id+1)%EnvMode.values().length; }
		static public int getRes(int _id)			{ return EnvMode.values()[_id].mRes; }
	}
	
	/** The widgets */
	private ImageView	mEnvButton;
	private ImageView 	mFillButton;
	private Spinner		mSpinner;
	private int			mArg = -1;
	private int			mFillId = 0;
	private int			mEnvId = 0;
	
	/** Constructor */
	public NewDialog(Context context, NewDialogListener listener) {
		super(context);
		
		// Initialize the screen size model
        mModels = new Vector<Model>();
		mModels.add(new Model("Screen    "+SketchbookView.WIDTH+"x"+SketchbookView.HEIGHT, SketchbookView.WIDTH, SketchbookView.HEIGHT));
		
		// Initialize the models
		mModels.add(new Model("CGA       "+context.getResources().getIntArray(R.array.CGA)[0]+"x"+context.getResources().getIntArray(R.array.CGA)[1],
					context.getResources().getIntArray(R.array.CGA)[0],context.getResources().getIntArray(R.array.CGA)[1]));
		mModels.add(new Model("QVGA      "+context.getResources().getIntArray(R.array.QVGA)[0]+"x"+context.getResources().getIntArray(R.array.QVGA)[1],
					context.getResources().getIntArray(R.array.QVGA)[0],context.getResources().getIntArray(R.array.QVGA)[1]));
		mModels.add(new Model("VGA       "+context.getResources().getIntArray(R.array.VGA)[0]+"x"+context.getResources().getIntArray(R.array.VGA)[1],
					context.getResources().getIntArray(R.array.VGA)[0],context.getResources().getIntArray(R.array.VGA)[1]));
		mModels.add(new Model("NTSC      "+context.getResources().getIntArray(R.array.NTSC)[0]+"x"+context.getResources().getIntArray(R.array.NTSC)[1],
					context.getResources().getIntArray(R.array.NTSC)[0],context.getResources().getIntArray(R.array.NTSC)[1]));
		mModels.add(new Model("PAL       "+context.getResources().getIntArray(R.array.PAL)[0]+"x"+context.getResources().getIntArray(R.array.PAL)[1],
					context.getResources().getIntArray(R.array.PAL)[0],context.getResources().getIntArray(R.array.PAL)[1]));
		mModels.add(new Model("WVGA      "+context.getResources().getIntArray(R.array.WVGA)[0]+"x"+context.getResources().getIntArray(R.array.WVGA)[1],
					context.getResources().getIntArray(R.array.WVGA)[0],context.getResources().getIntArray(R.array.WVGA)[1]));
		mModels.add(new Model("SVGA      "+context.getResources().getIntArray(R.array.SVGA)[0]+"x"+context.getResources().getIntArray(R.array.SVGA)[1],
					context.getResources().getIntArray(R.array.SVGA)[0],context.getResources().getIntArray(R.array.SVGA)[1]));

		// Get the listener for return values
		mListener = listener;
	}
	
	/** Update the dialog before showing it */
    @Override public void onShow() {
    	//mFillButton.setBackgroundColor(ColorDialog.color);
    }
	
	/** The onCreate method which initialize a bunch of stuffs */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        ImageView vButton;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newdialog);
        
        // Validation button
        vButton = (ImageView) findViewById(R.id.newdialog_ok);
        vButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mArg>=0) {
					if (mListener!=null) {
						mListener.onValid(mModels.get(mArg).mWidth, mModels.get(mArg).mHeight, FillBut.get(mFillId), EnvMode.get(mEnvId));
					}
					dismiss();
				}
			}
        });
        
        // Cancel button
        vButton = (ImageView) findViewById(R.id.newdialog_cancel);
        vButton.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { cancel(); } });
        
        // Fill button 
        mFillButton = (ImageView) findViewById(R.id.newdialog_cam);
        mFillButton.setOnClickListener(new View.OnClickListener() {
  			public void onClick(View v) {
   				mFillId = FillBut.next(mFillId);
   				mFillButton.setImageResource(FillBut.getRes(mFillId));
   			}
        });
  
        // Environment button
        mEnvButton = (ImageView) findViewById(R.id.newdialog_env);
        mEnvButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mEnvId = EnvMode.next(mEnvId);
				mEnvButton.setImageResource(EnvMode.getRes(mEnvId));
			}
		});
 
        // Spinner handling
        mSpinner = (Spinner) findViewById(R.id.newdialog_spinner);
        mSpinner.setAdapter(new NewAdapter(getContext()));
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) { mArg = arg2; }
			public void onNothingSelected(AdapterView<?> arg0) { }
        });
	}

}
