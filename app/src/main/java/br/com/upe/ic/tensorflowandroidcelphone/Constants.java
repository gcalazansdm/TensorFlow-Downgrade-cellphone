package br.com.upe.ic.tensorflowandroidcelphone;

/**
 * Created by Gabriel on 13.fev.2018.
 */

public class Constants {
    public static final long TIME_THRESHOLD = 30;
    public static final double THRESHOLD_TO_TALK = 0.3;
    public static final int INPUT_SIZE = 224;
    public static final int IMAGE_MEAN = 117;
    public static final float IMAGE_STD = 1;
    public static final String INPUT_NAME = "input";
    public static final String OUTPUT_NAME = "output";

    public final static String MODEL_FILE =
            "file:///android_asset/tensorflow_inception_graph.pb";
    ;
    public final static String LABEL_FILE =
            "file:///android_asset/imagenet_comp_graph_label_strings_pt_br.txt";

    public static final boolean MAINTAIN_ASPECT = true;

}
