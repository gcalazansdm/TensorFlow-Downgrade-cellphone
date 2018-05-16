package br.com.upe.ic.tensorflowandroidcelphone;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Gabriel on 18.fev.2018.
 */

class IOutil {

    public static void Saveimage(byte[] img,String aux) throws IOException {
        FileOutputStream input = new FileOutputStream(aux);
        input.write(img);
        input.close();
    }

    public static void Saveimage(File img, String aux) throws IOException {
        byte[] imagem;
        FileInputStream input = new FileInputStream(img);
        imagem = new byte[input.available()];
        input.read(imagem);
        input.close();
        Saveimage(imagem,aux);
    }
}
