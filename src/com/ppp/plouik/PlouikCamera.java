package com.ppp.plouik;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class PlouikCamera extends Activity {
	static final String TAG = "camera";
	
	static SketchbookPreview 	mCamera;
	static int					mWidth;
	static int					mHeight;
	
    /** Called when the activity is first created. */
    @Override public void onCreate(Bundle savedInstanceState) {
    	
    	super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	
        setContentView(R.layout.camera);
        
        mCamera = (SketchbookPreview) findViewById(R.id.main_camera);
        mCamera.setListener(new SketchbookPreview.OnPictureSaved() {
			public void done() {
				Intent intent = new Intent();
				setResult(RESULT_OK, intent);
				finish();
			}
		});
        
        ImageView button = (ImageView) findViewById(R.id.camera_ok);
        button.setOnClickListener(new View.OnClickListener() {	
			public void onClick(View v) {
				mCamera.takePicture();
			}
		});
    }

}
