package br.com.upe.ic.tensorflowandroidcelphone;

import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;

import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Gabriel on 18.fev.2018.
 */

public class TextToSpeachControler extends TextToSpeech {
    private static TextToSpeachControler instance;
    private static final Lock mutex = new ReentrantLock();

    public static TextToSpeachControler getInstance(Context mContext) {
        if (instance == null) {
            instance = new TextToSpeachControler(mContext);
        }
        return instance;
    }

    private TextToSpeachControler(Context context) {
        super(context, new OnInitListener() {
            @Override
            public void onInit(int i) {

            }
        });
        this.setLanguage(Locale.US);
    }

    public void speak(String text) {
        mutex.lock();
        this.speak(text, TextToSpeech.QUEUE_ADD, null);
        mutex.unlock();
    }

    public void pause() {
        this.stop();
        this.shutdown();
        if (!mutex.tryLock()) {
            mutex.unlock();
        }
    }
}
