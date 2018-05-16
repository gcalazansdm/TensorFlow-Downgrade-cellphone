package br.com.upe.ic.tensorflowandroidcelphone;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.util.Log;


import org.tensorflow.demo.CameraActivity;
import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.TensorFlowImageClassifier;
import org.tensorflow.demo.env.ImageUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by gcmoura on 15/02/18.
 */

public class CassifierActivity extends CameraActivity {
    class LastTold {
        private String value;
        private long timeStamp;

        LastTold(String value) {
            this.value = value;
            this.timeStamp = System.currentTimeMillis() / 1000;
        }

        public String getValue() {
            return value;
        }

        public boolean old() {
            return (timeStamp - (System.currentTimeMillis() / 1000)) >= Constants.TIME_THRESHOLD;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof String) {
                return this.old() && this.value.equals((String) obj);
            } else if (obj instanceof LastTold) {
                return this.old() && this.value.equals(((LastTold) obj).getValue());
            }
            return super.equals(obj);
        }
    }

    private TextToSpeachControler tts;

    private Lock mutex = new ReentrantLock();
    private ArrayList<LastTold> oldvalues = new ArrayList<LastTold>();
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;

    private Classifier classifier;
    private Matrix frameToCropTransform;

    @Override
    protected void processImage() {
        Bitmap bitmap = BitmapFactory.decodeByteArray(getLastPreviewFrame(), 0, getLastPreviewFrame().length);
        if (bitmap == null) {
            rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        } else {
            rgbFrameBitmap.sameAs(bitmap);
        }
        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        tts = TextToSpeachControler.getInstance(this);
        ImageUtils.saveBitmap(croppedBitmap);

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        final List<Classifier.Recognition> results = classifier.recognizeImage(croppedBitmap);
                        String text = "";
                        mutex.lock();
                        for (Classifier.Recognition resultado : results) {
                            Log.d("reconhecidos", resultado.getTitle());
                            if (resultado.getConfidence() > Constants.THRESHOLD_TO_TALK) {
                                text = resultado.getTitle();
                                if (oldvalues.contains(text)) {
                                    oldvalues.add(new LastTold(text));
                                    tts.speak(text);
                                }
                            }
                        }
                        mutex.unlock();

                        readyForNextImage();
                    }
                });
        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        mutex.lock();
                        for (int i = 0; i < oldvalues.size(); i++) {
                            if(oldvalues.get(i).old()){
                                oldvalues.remove(i);
                            }
                        }
                        mutex.unlock();
                    }
                });
    }

    protected void onPreviewSizeChosen(int height, int width, int rotation, Camera camera) {
        classifier =
                TensorFlowImageClassifier.create(
                        getAssets(),
                        Constants.MODEL_FILE,
                        Constants.LABEL_FILE,
                        Constants.INPUT_SIZE,
                        Constants.IMAGE_MEAN,
                        Constants.IMAGE_STD,
                        Constants.INPUT_NAME,
                        Constants.OUTPUT_NAME);

        previewWidth = width;
        previewHeight = height;

        Integer sensorOrientation = rotation - getScreenOrientation();

        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(Constants.INPUT_SIZE, Constants.INPUT_SIZE, Bitmap.Config.ARGB_8888);

        frameToCropTransform = ImageUtils.getTransformationMatrix(
                previewWidth, previewHeight,
                Constants.INPUT_SIZE, Constants.INPUT_SIZE,
                sensorOrientation, Constants.MAINTAIN_ASPECT);

        frameToCropTransform.invert(new Matrix());
    }

}
