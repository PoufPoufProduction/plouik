package com.ppp.plouik;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class FileBrowserDialog extends SketchbookDialog {
	/** The log TAG */
	static final String TAG = "filebrowser";
	
	public interface Listener {
		public void onValid(String _filename, World _world);
	};
	
	/** The words for name building */
	static final String[] NAMES = {
		"no", "na", "nu", "ni", "shi", "sha", "sho", "shu", "ten", "ta", "ton", "to", "ti", "tu",
		"mo", "mon", "ma", "mi", "men", "mu", "li", "lu", "lo", "la", "do", "da", "di", "du",
		"koa", "ko", "ka", "kan", "ku", "kun", "ki", "ba", "ban", "bu", "bun", "bi", "bo", "bon"
		};
	public final String	NEWFOLDER = new String("Plouik");
	
	/** The type of browser */
	static enum Type { FILE, FOLDER }; 
	
	/** The optionnary button */
	static enum Option { NONE, NEWNAME, NEWFOLDER, WORLD };
	
	/** The world button state */
	static enum World { ON, OFF, LAYER };
	
	/**
	 * 
	 * The mandatory adapter class for the grid view filling 
	 * @author Pouf-Pouf Production
	 * 
	 */
	private class FileAdapter extends BaseAdapter {
		
		/**
		 * The intern class for file items in the file picker
		 */
		private class FileObject {
			
			/** The FilesListener for navigating */
			private class FilesListener implements View.OnClickListener { public void onClick(View v) { setFile(mFile); }}
			
			private TextView		mView;
			private File			mFile;
			
			/**
			 * Build a text view regarding its _file and and name
			 * @param _file is the file of the object
			 * @param _name is its name (different from the filename for . and ..)
			 * @return a textview with a mime thumbnail
			 */
			private TextView getMimeView(File _file, String _name) {
				TextView vView = new TextView(getContext());
				vView.setText(_name);
				vView.setTextAppearance(getContext(), R.style.SketchbookTextSmall);

				Drawable img = getContext().getResources().getDrawable(R.drawable.mimedir);
				
				if (_file.getName().endsWith("xml") || _file.getName().endsWith("XML")) {
					img = getContext().getResources().getDrawable(R.drawable.mimexml);
				}
				else
				if (_file.getName().endsWith("png") || _file.getName().endsWith("PNG") ||
				    _file.getName().endsWith("jpg") || _file.getName().endsWith("JPG")) {
					img = getContext().getResources().getDrawable(R.drawable.mimepng);
				}
				
				img.setBounds(0,0,getContext().getResources().getDimensionPixelSize(R.dimen.mime),
						getContext().getResources().getDimensionPixelSize(R.dimen.mime));
				vView.setCompoundDrawables(null, img, null, null);
				vView.setOnClickListener(new FilesListener());
				vView.setGravity(Gravity.CENTER_HORIZONTAL);
				return vView;
			}

			/** Constructor of a file Object */
			public FileObject (File _file, String _name) {
				mFile = _file;
				mView = getMimeView(_file, _name.length()>11?_name.substring(0,8)+"...":_name);
			}
			public FileObject (File _file) {
				mFile = _file;
				mView = getMimeView(_file, _file.getName().length()>11?_file.getName().substring(0,8)+"...":_file.getName());
			}

			/** Some accessors */
			public View 			getView()		{ return mView; }
		} // End of FileObject
		
		/** The listFiles */
		private ArrayList<FileObject>	mFiles;
		
		/**
		 * Fill the files list
		 * @param _file is the asked path
		 */
		private void fillList(File _file) {
			mFiles.clear();
			
			updatePreview(_file);
			updateButton(_file);
				
			// If _file is a file, we get its parent to browse its folder
			if (!_file.isDirectory()) {
				Plouik.trace(TAG,"[FILL]    "+_file.getAbsolutePath()+" is a file, get its parent");
				_file = _file.getParentFile();
			}
			
			// Add the . and .. folder
			mFiles.add(new FileObject( _file, "."));
			if (mRootPath!=null && _file.getAbsolutePath().length()>mRootPath.length()+1 ) {
				mFiles.add(new FileObject( _file.getParentFile(), ".."));
			}
				
			// Add all the files inside the folder
			if (_file.listFiles() != null)
			{
				for (File file : _file.listFiles()) {
					if (file.getName().charAt(0)!='.') {
						FileObject f = new FileObject(file);
						mFiles.add(f);
					}
				}
			}
			else
			{
				Plouik.error(TAG,"[FILL]    "+_file.getName()+" is empty");
			}
			
			this.notifyDataSetChanged();
		}
		
		/**
		 * The FileAdapter constructor
		 * @param ctxt is the context
		 */
		public FileAdapter() {
			mFiles = new ArrayList<FileObject>();
		}

		/** A lot of override methods */
		public View getView(int position, View convertView, ViewGroup parent)	{ return mFiles.get(position).getView(); }
		public int getCount()													{ return mFiles.size(); }
		public Object getItem(int position)										{ return null; }
		public long getItemId(int position)										{ return 0;	}
		
	} // End of the implements ListAdapter
	
	
	/** The view attributes */
	private ImageView	mNewNameButton;			// The new name button (with a light bulb icon)
	private ImageView	mNewFolderButton;		// The new folder button
	private ImageView	mWorldButton;			// The earth button (for getting the data or not)
	private ImageView	mOkButton;				// The validation button
	private TextView	mFilename;				// The filename text
	private ImageView	mThumbnail;				// The preview thumbnail
	private Listener	mListener;				// The dialog listener
	private Type		mType;					// The type of browser (file or folder)
	private String		mExtension;				// The extension (only for file browser, useless else)
	private File		mFile;					// The current selected file
	private World		mWorldState=World.ON;	// The world button state
	private GridView	mGrid;					// The grid browser
	private boolean		mOkButtonState;			// The Ok button state
	private String		mRootPath;				// The root path
	
	public FileBrowserDialog(Context context) { super(context); mFile = new File(Plouik.root.getAbsolutePath()); }
	
	/** Set the listener and update the file browser behaviour */
	public void setListener(Listener _listener, Type _type, Option _option, String _extension, String _rootPath)
	{
		mListener 	= _listener;
		mType 		= _type;
		mExtension 	= _extension;
		mRootPath	= _rootPath;
		
		// Display the optionary buttons
		if (_option==Option.NEWNAME && mNewNameButton!=null)	{ mNewNameButton.setVisibility(View.VISIBLE); }
		if (_option==Option.NEWFOLDER && mNewFolderButton!=null){ mNewFolderButton.setVisibility(View.VISIBLE); }
		if (_option==Option.WORLD && mWorldButton!=null)		{ mWorldButton.setVisibility(View.VISIBLE); }
		
		// Choose between filename or thumbnail
		if (_type==Type.FOLDER || mExtension.length()>0) { mFilename.setVisibility(View.VISIBLE); }
		else { mThumbnail.setVisibility(View.VISIBLE); }
		
		if (!mFile.getAbsolutePath().startsWith(_rootPath)) { setFile(_rootPath); }
		else { updateButton(mFile); }
	}
	
	
	/** Set the current file */
	private void setFile(String _path) { setFile(new File(_path)); }
	private void setFile(File _file) { mFile = _file; refresh(); }
	
	/** Prepare the panel before showing */
	@Override public void onShow() {
		
		if (mFilename!=null)		{ mFilename.setVisibility(View.GONE); }
		if (mThumbnail!=null)		{ mThumbnail.setVisibility(View.GONE); }
		if (mNewNameButton!=null)	{ mNewNameButton.setVisibility(View.GONE); }
		if (mNewFolderButton!=null)	{ mNewFolderButton.setVisibility(View.GONE); }
		if (mWorldButton!=null)		{ mWorldButton.setVisibility(View.GONE); }
		if (mOkButton!=null)		{ mOkButton.setImageResource(R.drawable.iconvalidoff); }
		
		refresh();
		updateButton(mFile);
	}
	
	/** Update the OK button regarding the selected file (not really a strong test, but enough with the good optionary buttons)*/
	private void updateButton(File _file) {
		mOkButtonState =(_file!=null);
		if (_file!=null && _file.exists()) {
			mOkButtonState = (_file.isDirectory())?(mType==Type.FOLDER):(mType==Type.FILE);
		}
		mOkButton.setImageResource(mOkButtonState?R.drawable.iconvalidon:R.drawable.iconvalidoff);
	}
	
	/** The refresh action */
	public void refresh() {
		((FileAdapter) mGrid.getAdapter()).fillList(mFile);
		mGrid.invalidate();
	}
	
	/** The classical onCreate method */
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filebrowser);
        
        // Get the panel elements
        mFilename 		= (TextView)  findViewById(R.id.filepicker_filename);
        mThumbnail 		= (ImageView) findViewById(R.id.filepicker_snapshot);
        mNewNameButton 	= (ImageView) findViewById(R.id.filepicker_new);
        mNewFolderButton= (ImageView) findViewById(R.id.filepicker_folder);
        mWorldButton 	= (ImageView) findViewById(R.id.filepicker_world);
        mGrid			= (GridView)  findViewById(R.id.filepicker_grid);
        mOkButton		= (ImageView) findViewById(R.id.filepicker_ok);
        
        // New folder button
        mNewFolderButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mFile!=null && mFile.exists()) {
					String vFileStr = mFile.isDirectory()?mFile.getPath():mFile.getParent();
					
					Plouik.trace(TAG, "[MKDIR]   create new folder in "+vFileStr);
					File vFile = new File(vFileStr);
					File vNewDir = new File(vFile.getAbsolutePath()+File.separator+NEWFOLDER);
					
					// If a folder uses the new dir name already, try with a numerical suffix
					if (vNewDir.exists()) {
						for (int vIt=1; vIt<10; vIt++) {
							vNewDir = new File(vFile.getAbsolutePath()+File.separator+NEWFOLDER+vIt);
							if (!vNewDir.exists()) break;
						}
					}
					
					// Building the new directory
					if (!vNewDir.exists()) {
						if (vNewDir.mkdir()) {
							Plouik.trace(TAG, "[MKDIR]   Create "+vNewDir.getAbsolutePath());
							refresh();
						}
						else {
							Plouik.trace(TAG, "[MKDIR]   Can NOT create "+vNewDir.getAbsolutePath());
						}
					}
					else {
						Plouik.trace(TAG, "[MKDIR]   Too much folders already created");
					}
				}
				else {
					Plouik.trace(TAG,"[FILL]    "+(mFile!=null?mFile.getName():"NOFILE")+" does not exist");
				}
			}
		});
		
        // New name button
		mNewNameButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mFile!=null) {
					String newname = new String();
					if (mFile.isDirectory()) {
						newname = mFile.getAbsolutePath()+File.separator;
					}
					else
					if (mFile.getAbsolutePath().lastIndexOf("/")>0) {
						newname = mFile.getAbsolutePath().substring(0, mFile.getAbsolutePath().lastIndexOf("/")+1);
					}
						
					newname += 	NAMES[(int) (NAMES.length*Math.random())]+
							  	NAMES[(int) (NAMES.length*Math.random())]+
							  	NAMES[(int) (NAMES.length*Math.random())];
					if ((mExtension != null) && (mExtension.length()>0)) { newname += "."+mExtension; }
					
					File tmp = new File(newname);
					mFile = tmp.getAbsoluteFile();
					
					refresh();
				}
			}
		});
		
		// The world button
		mWorldButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mWorldState==World.ON) {
					mWorldState=World.OFF;
					mWorldButton.setImageResource(R.drawable.iconearthoff);
				}
				else if (mWorldState==World.OFF) {
					mWorldState=World.LAYER;
					mWorldButton.setImageResource(R.drawable.iconnewlayer);
				}
				else {
					mWorldState=World.ON;
					mWorldButton.setImageResource(R.drawable.iconearthon);
				}
	
			}
		});
        
        // Cancel button
        findViewById(R.id.filepicker_cancel).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { dismiss(); }
		});
        
        // Valid button
        mOkButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mOkButtonState) {
					if (mListener!=null) { mListener.onValid(mFile.getAbsolutePath(), mWorldState); }
					dismiss();
				}
			}
		});
        
        // The Grid
        mGrid.setAdapter(new FileAdapter());
        
    }
    
    /** Update the preview */
	private void updatePreview(File _file) {
		Plouik.trace(TAG,	"[PREVIEW] load the preview "+_file.getName() + (_file.isDirectory()?"/":"") + 
				" (preview bitmap "+((mThumbnail.getVisibility()==View.GONE)?"NO)":((mThumbnail.getWidth()>0)?"OK)":"KO)")));
		String vPath = _file.getPath();
		
		// Handle the filename
		int length = mFilename.getHeight()>0?2*mFilename.getWidth()/mFilename.getHeight():16;
		if (vPath.length()>length) { vPath = "..."+vPath.substring(vPath.length()-length+3); }
		mFilename.setText(vPath);
		
		// Handle the thumbnail
		if (_file.isDirectory()) {
			mThumbnail.setImageBitmap(null);
		}
		else {
			if (mThumbnail.getWidth()>0)
			{
				try {
					Bitmap img = BitmapFactory.decodeFile(_file.getPath());
					if (img!=null)
					{
						int x = img.getWidth()-mThumbnail.getWidth();
						x=(x<0)?0:x;
						
						int y = img.getHeight() - mThumbnail.getHeight();
						y=(y<0)?0:y;
						
						Bitmap scaledImg = Bitmap.createBitmap(img, x/2, y/2, img.getWidth()-x, img.getHeight()-y);
							
						mThumbnail.setImageBitmap(scaledImg);
						img.recycle();
					}
				}
				catch(java.lang.OutOfMemoryError e) {
					Plouik.trace(TAG,	"[PREVIEW] Out of memory for preview");
				}
				
			}
		}
	}

}
