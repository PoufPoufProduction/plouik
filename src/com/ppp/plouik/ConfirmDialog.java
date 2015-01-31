package com.ppp.plouik;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 * A simple confirmation dialog
 * @author Pouf-Pouf Production
 *
 */
public class ConfirmDialog extends SketchbookDialog {

	public ConfirmDialog(Context context) {
		super(context);
	}
	
	/** The interface for getting the result */
	public interface OnConfirmListener {
        void buttonClicked(boolean confirm);
    }
	
	/** The confirmation button */
	private ImageView			mOkButton;
	
	/** The cancellation button */
	private ImageView			mCancelButton;
	
	/** The listener */
	public OnConfirmListener	mListener;
	
	/**
	 * Create the PatternDialog
	 * @param savedInstanceState is certainly something valuable
	 */
	@Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirmdialog);
        
        mOkButton = (ImageView) findViewById(R.id.confirmdialog_ok);
        mCancelButton = (ImageView) findViewById(R.id.confirmdialog_cancel);
        
        mOkButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mListener!=null) { mListener.buttonClicked(true); }
				dismiss();
			}
		});
        
        mCancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mListener!=null) { mListener.buttonClicked(false); }
				cancel();
			}
		});
	}
	
	/**
	 * Change the buttons behaviour regarding a provided listener
	 * @param listener is the new listener for button click callback
	 */
	public void setOnConfirmListener(OnConfirmListener listener) {
		mListener = listener;
	}

}
