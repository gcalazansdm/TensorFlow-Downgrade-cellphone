package org.tensorflow.demo;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.tensorflow.demo.env.ImageUtils;

public abstract class CameraActivity extends Activity {

	private Handler handler;
	private HandlerThread handlerThread;
	private boolean isProcessingFrame = false;
	private byte[][] yuvBytes = new byte[3][];
	private int[] rgbBytes = null;
	private CameraPreview mCameraPreview;
	private Lock mutex = new ReentrantLock();

	protected int previewWidth = 0;
	protected int previewHeight = 0;

	private Runnable postInferenceCallback;
	private Runnable imageConverter;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_camera);
		initCamera();
	}

	private void initCamera() {
		FrameLayout mFrameLayout = (FrameLayout) findViewById(R.id.camera_preview);
		mCameraPreview = new CameraPreview(this, new CameraPreviewCallback());

		mFrameLayout.addView(mCameraPreview);
		boolean isCameraEnabled = mCameraPreview.isCameraConnected();
		if (isCameraEnabled) {
			previewWidth = CameraPreview.getCamera().getParameters().getPreviewSize().width;
			previewHeight = CameraPreview.getCamera().getParameters().getPreviewSize().height;
		}
	}

	private byte[] lastPreviewFrame;

	public byte[] getLastPreviewFrame() {
		return lastPreviewFrame;
	}

	protected int[] getRgbBytes() {
		imageConverter.run();
		return rgbBytes;
	}

	private class CameraPreviewCallback implements Camera.PreviewCallback {
		@Override
		public void onPreviewFrame(final byte[] bytes, final Camera camera) {
			if (isProcessingFrame) {
				return;
			}
			final byte[] bitmapdata = bytes;
			try {
				if (rgbBytes == null) {
					Camera.Size previewSize = camera.getParameters().getPreviewSize();
					previewHeight = previewSize.height;
					previewWidth = previewSize.width;
					rgbBytes = new int[previewWidth * previewHeight];
					onPreviewSizeChosen(previewSize.height,previewSize.width, 90,camera);
				}
			} catch (final Exception e) {
				return;
			}

			isProcessingFrame = true;
			lastPreviewFrame = bitmapdata;
			yuvBytes[0] = bitmapdata;
			mutex.lock();
			try {
				Camera.Parameters parameters = camera.getParameters();
				Camera.Size size = parameters.getPreviewSize();
				YuvImage image = new YuvImage(bytes, parameters.getPreviewFormat(),
						size.width, size.height, null);
				File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "tensorflow" + "/out.jpg");
				FileOutputStream filecon = new FileOutputStream(file);
				image.compressToJpeg(
						new Rect(0, 0, image.getWidth(), image.getHeight()), 90,
						filecon);
			}catch (Exception e){}
			mutex.unlock();
			imageConverter =
					new Runnable() {
						@Override
						public void run() {
							ImageUtils.convertYUV420SPToARGB8888(bitmapdata, previewWidth, previewHeight, rgbBytes);
						}
					};

			postInferenceCallback =
					new Runnable() {
						@Override
						public void run() {
							camera.addCallbackBuffer(bitmapdata);
							isProcessingFrame = false;
						}
					};
			processImage();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		handlerThread = new HandlerThread("inference");
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper());
	}

	@Override
	public synchronized void onPause() {
		if (!isFinishing()) {
			finish();
		}
		if (handlerThread != null) {
			handlerThread.quit();
			try {
				handlerThread.join();
				handlerThread = null;
				handler = null;
			} catch (final InterruptedException e) {

			}
		}
		super.onPause();
	}

	@Override
	public synchronized void onStop() {
		super.onStop();
	}

	@Override
	public synchronized void onDestroy() {
		super.onDestroy();
	}

	protected synchronized void runInBackground(final Runnable r) {
		if (handler != null) {
			handler.post(r);
		}
	}

	protected void readyForNextImage() {
		if (postInferenceCallback != null) {
			postInferenceCallback.run();
		}
	}

	protected int getScreenOrientation() {
		switch (getWindowManager().getDefaultDisplay().getRotation()) {
			case Surface.ROTATION_270:
				return 270;
			case Surface.ROTATION_180:
				return 180;
			case Surface.ROTATION_90:
				return 90;
			default:
				return 0;
		}
	}

	protected abstract void processImage();

	protected abstract void onPreviewSizeChosen(final int height, final int width, final int rotation,final Camera camera);

}
