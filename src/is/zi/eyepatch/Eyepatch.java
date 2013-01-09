package is.zi.eyepatch;

import is.zi.eyepatch.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class Eyepatch extends Activity {
	
    private Camera mCamera;
    private CameraPreview mPreview;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Fullscreen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
        // Create an instance of Camera
        mCamera = getCameraInstance();

		setContentView(R.layout.activity_eyepatch);
		
		final View contentView = findViewById(R.id.camera);
		contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		
		final ImageView eyeLeft = (ImageView)findViewById(R.id.eye_left);
		final ImageView eyeRight = (ImageView)findViewById(R.id.eye_right);
		eyeLeft.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				final View contentView = findViewById(R.id.camera);
				contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			}
		});
		

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera, eyeLeft, eyeRight);
        FrameLayout preview = (FrameLayout)contentView;
        preview.addView(mPreview);
        
	}
	
	/** A safe way to get an instance of the Camera object. */
	private static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}
}
