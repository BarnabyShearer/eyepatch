package is.zi.eyepatch;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

public class MainRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
	private final String vss =
		"attribute vec2 vPosition;\n" +
		"attribute vec2 vTexCoord;\n" +
		"varying vec2 texCoord;\n" +
		"void main() {\n" +
		"  texCoord = vTexCoord;\n" +
		"  gl_Position = vec4 ( vPosition.x, vPosition.y/2.0, 0.0, 1.0 );\n" +
		"}";

	private final String fss =
		"#extension GL_OES_EGL_image_external : require\n" +
		"precision mediump float;\n" +
		"uniform samplerExternalOES sTexture;\n" +
		"varying vec2 texCoord;\n" +
		"void main() {\n" +
		"  if(texCoord.y > 0.970) {\n" +
		"   gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);\n" +
		"  } else {\n" +
		"    if(texCoord.x < 0.475) {\n" +
		"      gl_FragColor = texture2D(sTexture, vec2(texCoord.x * 2.0, texCoord.y));\n" +
		"    } else {" +
		"      if(texCoord.x < 0.5) {\n" +
		"        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);\n" +
		"      } else {\n" +
		"        if(texCoord.x > 0.975) {\n" +
		"          gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);\n" +
		"        } else {\n" +
		"          gl_FragColor = texture2D(sTexture, vec2(texCoord.x * 2.0 - 1.0, texCoord.y));\n" +
		"        }\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"}";

	private int[] hTex;
	private FloatBuffer pVertex;
	private FloatBuffer pTexCoord;
	private int hProgram;

	private Camera mCamera;
	private SurfaceTexture mSTexture;

	private boolean mUpdateST = false;

	private MainView mView;

	MainRenderer ( MainView view ) {
		mView = view;
		float[] vtmp = { 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f };
		float[] ttmp = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };
		pVertex = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		pVertex.put ( vtmp );
		pVertex.position(0);
		pTexCoord = ByteBuffer.allocateDirect(8*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		pTexCoord.put ( ttmp );
		pTexCoord.position(0);
	}

	public void close()
	{
		mUpdateST = false;
		//API14? mSTexture.release();
		mCamera.stopPreview();
		mCamera = null;
		deleteTex();
	}

	public void onSurfaceCreated ( GL10 unused, EGLConfig config ) {
		initTex();
		mSTexture = new SurfaceTexture ( hTex[0] );
		mSTexture.setOnFrameAvailableListener(this);

		mCamera = Camera.open();
		try {
			Parameters parameters = mCamera.getParameters();
			List<int[]> fpslist = parameters.getSupportedPreviewFpsRange();
			parameters.set("orientation", "landscape");
			parameters.setPreviewFpsRange(fpslist.get(fpslist.size()-1)[0], fpslist.get(fpslist.size()-1)[1]);
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE );
			mCamera.setParameters(parameters);
			mCamera.setPreviewTexture(mSTexture);
		} catch ( IOException ioe ) {
		}

		GLES20.glClearColor ( 0.0f, 0.0f, 0.0f, 1.0f );

		hProgram = loadShader ( vss, fss );
	}

	public void onDrawFrame ( GL10 unused ) {
		GLES20.glClear( GLES20.GL_COLOR_BUFFER_BIT );

		synchronized(this) {
			if ( mUpdateST ) {
				mSTexture.updateTexImage();
				mUpdateST = false;
			}
		}

		GLES20.glUseProgram(hProgram);

		int ph = GLES20.glGetAttribLocation(hProgram, "vPosition");
		int tch = GLES20.glGetAttribLocation ( hProgram, "vTexCoord" );
		int th = GLES20.glGetUniformLocation ( hProgram, "sTexture" );

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0]);
		GLES20.glUniform1i(th, 0);

		GLES20.glVertexAttribPointer(ph, 2, GLES20.GL_FLOAT, false, 4*2, pVertex);
		GLES20.glVertexAttribPointer(tch, 2, GLES20.GL_FLOAT, false, 4*2, pTexCoord );
		GLES20.glEnableVertexAttribArray(ph);
		GLES20.glEnableVertexAttribArray(tch);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
		GLES20.glFlush();
	}

	public void onSurfaceChanged ( GL10 unused, int width, int height ) {
		GLES20.glViewport( 0, 0, width, height );
		Camera.Parameters param = mCamera.getParameters();
		List<Size> psize = param.getSupportedPreviewSizes();
		Size bestSize = psize.get(0);
		for(int i = 1; i < psize.size(); i++){
			if (
				Math.abs(bestSize.width-(width/2)) + Math.abs(bestSize.height-(height/2))
			>
				Math.abs(psize.get(i).width-(width/2)) + Math.abs(psize.get(i).height-(height/2))
			){
				bestSize = psize.get(i);
			}
		}
		param.setPreviewSize(bestSize.width, bestSize.height);
		Log.d("Size", Integer.toString(bestSize.width) + ',' + Integer.toString(bestSize.height));
		mCamera.setParameters ( param );
		mCamera.startPreview();
	}

	private void initTex() {
		hTex = new int[1];
		GLES20.glGenTextures ( 1, hTex, 0 );
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0]);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
	}

	private void deleteTex() {
		GLES20.glDeleteTextures ( 1, hTex, 0 );
	}

	public synchronized void onFrameAvailable ( SurfaceTexture st ) {
		mUpdateST = true;
		mView.requestRender();
	}

	private static int loadShader ( String vss, String fss ) {
		int vshader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
		GLES20.glShaderSource(vshader, vss);
		GLES20.glCompileShader(vshader);
		int[] compiled = new int[1];
		GLES20.glGetShaderiv(vshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			Log.e("Shader", "Could not compile vshader");
			Log.v("Shader", "Could not compile vshader:"+GLES20.glGetShaderInfoLog(vshader));
			GLES20.glDeleteShader(vshader);
			vshader = 0;
		}

		int fshader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		GLES20.glShaderSource(fshader, fss);
		GLES20.glCompileShader(fshader);
		GLES20.glGetShaderiv(fshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			Log.e("Shader", "Could not compile fshader");
			Log.v("Shader", "Could not compile fshader:"+GLES20.glGetShaderInfoLog(fshader));
			GLES20.glDeleteShader(fshader);
			fshader = 0;
		}

		int program = GLES20.glCreateProgram();
		GLES20.glAttachShader(program, vshader);
		GLES20.glAttachShader(program, fshader);
		GLES20.glLinkProgram(program);

		return program;
	}
}