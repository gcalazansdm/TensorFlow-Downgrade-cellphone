package br.com.upe.ic.tensorflowandroidcelphone;

/**
 * Created by Gabriel on 17.out.2018.
 */

public class LastTold {
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
