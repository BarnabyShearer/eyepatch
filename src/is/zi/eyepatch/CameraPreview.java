package is.zi.eyepatch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
	private ImageView eyeLeft;
	private ImageView eyeRight;
    Bitmap rgb;
    
    public CameraPreview(Context context, Camera camera, ImageView l, ImageView r) {
        super(context);
        mCamera = camera;
		eyeLeft = l;
		eyeRight = r;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    private class TwoEyePreviewCallback implements PreviewCallback {
    	private ImageView eyeLeft;
    	private ImageView eyeRight;
    	public TwoEyePreviewCallback(ImageView l, ImageView r) {
    		eyeLeft = l;
    		eyeRight = r;
    	}

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
        	Parameters parameters = mCamera.getParameters();
        	int w = parameters.getPreviewSize().width;
            int h = parameters.getPreviewSize().height;
            YuvImage yuvimage = new YuvImage(data,ImageFormat.NV21,w,h,null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0,0,w,h), 100, baos);
            Bitmap rgb =  BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
            eyeLeft.setImageBitmap(rgb);
            eyeRight.setImageBitmap(rgb);			
		}
    	
    }
    
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            Parameters parameters = mCamera.getParameters();
            //Max FPS
            List<int[]> fpslist = parameters.getSupportedPreviewFpsRange();
            parameters.setPreviewFpsRange(fpslist.get(fpslist.size()-1)[0], fpslist.get(fpslist.size()-1)[1]);
              	
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE );
            mCamera.setParameters(parameters);
            mCamera.setPreviewCallback(new TwoEyePreviewCallback(eyeLeft, eyeRight));
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("Eyepatch", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}
}