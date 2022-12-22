package me.nullx.comport;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class App {
    static MyWindow w;

    public static void main(String[] args) throws IOException {

        w = new MyWindow("ComPort");
        setIcon();
        w.setVisible(true);

        // on window close
        w.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                w.dispose();
            }
        });
    }

    private static void setIcon() {
        try {
            w.setIconImage(ImageIO.read(new File("resources/icon.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
