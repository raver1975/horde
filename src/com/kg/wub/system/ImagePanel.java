package com.kg.wub.system;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel{

    private BufferedImage image;

    public ImagePanel() {
//        super();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(image!=null)g.drawImage(getImage(), 0, 0, null); // see javadoc for more info on the parameters
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        invalidate();
    }
}