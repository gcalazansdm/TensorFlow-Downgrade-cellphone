package br.com.upe.ic.tensorflowandroidcelphone;

import android.app.Activity;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.tensorflow.demo.PermissionsUtil;

public class MainActivity extends AppCompatActivity {

    private Button bt;
    private Activity mActivity;
    private TextToSpeech mTts;
    private TextToSpeachControler ttsc;

    public void checkTTS(){
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, 90);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       PermissionsUtil.getInstance().requestCameraPermission(this,PermissionsUtil.CAMERA_REQUEST);
        mActivity = this;
        bt = (Button) findViewById(R.id.button);
        checkTTS();
        setOnClick();
    }

    private void setOnClick() {
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(mActivity, ClassifierActivity.class);
                startActivity(it);
            }

        });
    }

    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        if (requestCode == 90) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                mTts = TextToSpeachControler.getInstance(this);
            } else {
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermissionsUtil.CAMERA_REQUEST: {
                if (!PermissionsUtil.getInstance().hasCameraPermission(this)) {
                    PermissionsUtil.getInstance().requestCameraPermission(this,PermissionsUtil.CAMERA_REQUEST);
                }
            }

        }
    }
}
