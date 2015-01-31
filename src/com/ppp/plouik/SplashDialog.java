package com.ppp.plouik;

import java.text.DecimalFormat;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * The Splash Dialog. Handle the root parameter
 * @author Pouf-Pouf Production
 *
 */
public class SplashDialog extends SketchbookDialog {
	
	/**
	 * The OK button listener
	 * @author Pouf-Pouf Production
	 *
	 */
	public interface SplashDialogListener {
		public void onSplashClick();
		public void onRootClick();
	}
	
	static final String TAG = "Splash";
	
	/** The listener */
	private SplashDialogListener	mListener;
	
	/** The relative layout */
	private RelativeLayout			mLayout;
	
	/** The root button */
	private ImageView				mButton;
	
	/** The memory overview */
	private TextView				mMemoryText;

	/** Splash dialog constructor */
	public SplashDialog(Context context, SplashDialogListener listener) {
		super(context);
		
		mListener=listener;
	}
	
	public String getSize(long _bytes) {
		String vRet = new String();
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2); 
		if (_bytes>=1000000000) { vRet = ""+ df.format(((float)_bytes/1000000000)) +"Gb"; }
		else if (_bytes>=1000000) { vRet = ""+ df.format(((float)_bytes/1000000)) +"Mb"; }
		else if (_bytes>=1000) { vRet = ""+ df.format(((float)_bytes/1000)) +"Kb"; }
		else { vRet = ""+ _bytes +"b"; }
		
		return vRet;
	}
	
	/** Fill the memory overview text view */
	public void updateOverview() {
		String vText = new String("");
		
		try { vText += getContext().getResources().getString(R.string.orientation)+"("+Plouik.mScreenOrientation+")"; }
		catch (java.lang.RuntimeException e) { vText += "(E)";}
		
		try { vText += " - "+getContext().getResources().getString(R.string.dimension); }
		catch (java.lang.RuntimeException e) { vText += " - (E)";}
		
		try { vText += " - "+getContext().getResources().getString(R.string.dpi)+"\n"; }
		catch (java.lang.RuntimeException e) { vText += " - (E)\n";}
		
		ActivityManager activityManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        
        /*
        List<RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        int PID = 0;
        for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses)
        {
        	if (getContext().getPackageName().equalsIgnoreCase(runningAppProcessInfo.processName)) {
        		PID = runningAppProcessInfo.pid;
        	}
        }
        
        vText += "["+PID+"] ("+getSize(memoryInfo.availMem)+"/"+getSize(memoryInfo.threshold)+")\n";
        */
        vText += "("+getSize(android.os.Debug.getNativeHeapAllocatedSize())+"/"+getSize(Runtime.getRuntime().maxMemory())+")";
        
        mMemoryText.setText(vText);
        
	}

	/**
	 * 
	 */
	@Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashdialog);
		
		// Get the main stuff
		mLayout = (RelativeLayout) findViewById(R.id.splashdialog_main);
		mLayout.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { if (mListener!=null) { mListener.onSplashClick(); } dismiss(); }});
		
		//Handle the root button
		mButton = (ImageView) findViewById(R.id.splashdialog_button);
		mButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { if (mListener!=null) { mListener.onRootClick(); } }});
		
		// Handle the help web site
		ImageView vHelp = (ImageView) findViewById(R.id.splashdialog_web);
		vHelp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String vUrl = "http://poufpoufproduction.fr/content/android/plouik/index.html";
				Intent vIntent = new Intent(Intent.ACTION_VIEW);
				vIntent.setData(Uri.parse(vUrl));
				getContext().startActivity(vIntent);
			}
		});
		
		// The memory overview
		mMemoryText = (TextView) findViewById(R.id.splashdialog_overviewEx);
		updateOverview();
	}

}
