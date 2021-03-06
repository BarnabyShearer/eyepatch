package is.zi.eyepatch;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class Eyepatch extends Activity {
	private MainView mView;
	private WakeLock mWL;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// full screen & full brightness
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mWL = ((PowerManager) getSystemService(Context.POWER_SERVICE))
				.newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
		mWL.acquire();
		mView = new MainView(this);
		mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		mView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			}
		});

		setContentView(mView);
	}

	@Override
	protected void onPause() {
		if (mWL.isHeld())
			mWL.release();
		mView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mView.onResume();
		mWL.acquire();
	}
}

class MainView extends GLSurfaceView {
	MainRenderer mRenderer;

	MainView ( Context context ) {
		super ( context );
		mRenderer = new MainRenderer(this);
		setEGLContextClientVersion ( 2 );
		setRenderer ( mRenderer );
		setRenderMode ( GLSurfaceView.RENDERMODE_WHEN_DIRTY );
	}

	public void surfaceCreated ( SurfaceHolder holder ) {
		super.surfaceCreated ( holder );
	}

	public void surfaceDestroyed ( SurfaceHolder holder ) {
		mRenderer.close();
		super.surfaceDestroyed ( holder );
	}

	public void surfaceChanged ( SurfaceHolder holder, int format, int w, int h ) {
		super.surfaceChanged ( holder, format, w, h );
	}
}