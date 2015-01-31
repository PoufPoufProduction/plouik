package com.ppp.plouik;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 * Dialog which provides some simple operation on the bitmap
 * @author Johann Charlot
 *
 */
public class OpDialog extends SketchbookDialog {

	/** Button values */
	static final int	BUTTON_TURN90 		= 1;
	static final int	BUTTON_TURN180 		= 2;
	static final int	BUTTON_TURN270 		= 3;
	static final int	BUTTON_FLIPH 		= 4;
	static final int	BUTTON_FLIPV 		= 5;
	static final int	BUTTON_LAYER 		= 6;
	static final int	BUTTON_LAYERFLIP	= 7;
	static final int	BUTTON_LAYERMERGE	= 8;
	
	/** The NewDialogListener used for getting results */
	public interface OpDialogListener {
		public void onValid(int id);
	}
	
	/** The ToolsDialogListener instance */
	private OpDialogListener		mListener;
	
	/** The operations buttons */
	private ImageView		mLayerButton;
	private ImageView		mLayerMergeButton;
	private ImageView		mLayerFlipButton;
	private ImageView		mTurn90Button;
	private ImageView		mTurn180Button;
	private ImageView		mTurn270Button;
	private ImageView		mFlipHButton;
	private ImageView		mFlipVButton;
	private ImageView		mCancel;
	
	/** The classic dialog constructor, nothing to say */
	public OpDialog(Context context, OpDialogListener listener) {
		super(context);	
		mListener = listener;
	}
	
	/** Update the dialog before showing it */
    @Override public void onShow() {
    	if (SketchbookData.LAYER != SketchbookData.NOLAYER) {
    		mTurn90Button.setImageResource(R.drawable.iconturnoff);
    		mTurn270Button.setImageResource(R.drawable.iconturnoff);
    		mLayerButton.setImageResource(R.drawable.icondellayer);
    		mLayerMergeButton.setImageResource(R.drawable.iconmergelayer);
    		mLayerFlipButton.setImageResource(R.drawable.iconfliplayer);
    	}
    	else {
    		mTurn90Button.setImageResource(R.drawable.iconturn90);
    		mTurn270Button.setImageResource(R.drawable.iconturn270);
    		mLayerButton.setImageResource(R.drawable.iconnewlayer);
    		mLayerMergeButton.setImageResource(R.drawable.iconlayeroff);
    		mLayerFlipButton.setImageResource(R.drawable.iconlayeroff);
    	}
    }
	
	/** The onCreate method which initialize a bunch of stuffs */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opdialog);
        
        // Handle the operation buttons
        

        mLayerButton = (ImageView) findViewById(R.id.opdialog_layer);
        mLayerButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { mListener.onValid(BUTTON_LAYER); dismiss(); }
        });
        
        mLayerMergeButton = (ImageView) findViewById(R.id.opdialog_mergelayer);
        mLayerMergeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { if (SketchbookData.LAYER != SketchbookData.NOLAYER) {mListener.onValid(BUTTON_LAYERMERGE); dismiss(); }}
        });
        
        mLayerFlipButton = (ImageView) findViewById(R.id.opdialog_fliplayer);
        mLayerFlipButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { if (SketchbookData.LAYER != SketchbookData.NOLAYER) {mListener.onValid(BUTTON_LAYERFLIP); dismiss(); }}
        });
        
        mTurn90Button = (ImageView) findViewById(R.id.opdialog_turn90);
        mTurn90Button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { if (SketchbookData.LAYER == SketchbookData.NOLAYER) {mListener.onValid(BUTTON_TURN90); dismiss(); }}
        });
        
        mTurn180Button = (ImageView) findViewById(R.id.opdialog_turn180);
        mTurn180Button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { mListener.onValid(BUTTON_TURN180); dismiss(); }
        });
        
        mTurn270Button = (ImageView) findViewById(R.id.opdialog_turn270);
        mTurn270Button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { if (SketchbookData.LAYER == SketchbookData.NOLAYER) {mListener.onValid(BUTTON_TURN270); dismiss(); }}
        });
        
        mFlipHButton = (ImageView) findViewById(R.id.opdialog_horizontalflip);
        mFlipHButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { mListener.onValid(BUTTON_FLIPH); dismiss(); }
        });
        
        mFlipVButton = (ImageView) findViewById(R.id.opdialog_verticalflip);
        mFlipVButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { mListener.onValid(BUTTON_FLIPV); dismiss(); }
        });
        
        // The cancel button
        mCancel = (ImageView) findViewById(R.id.opdialog_cancel);
        mCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { cancel(); }
        });
        
        
	}
}
