package com.kg;

import com.kg.synth.Sequencer;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.Arrays;

public class SequencerData {
    Image image;
    public byte[] note;
    public boolean[] pause;
    public boolean[] accent;
    public boolean[] slide;
    public int[][] rhythm;

    public SequencerData(Image image, byte[] note, boolean[] pause, boolean[] accent, boolean[] slide, int[][] rhythm) {
    }

    public SequencerData(WritableImage image, Sequencer sequencer) {
        this.image = copyImage(image);
        if (sequencer.getBassline() != null) {
            this.note = sequencer.getBassline().note.clone();
            this.pause = sequencer.getBassline().pause.clone();
            this.accent = sequencer.getBassline().accent.clone();
            this.slide = sequencer.getBassline().slide.clone();
        }
        if (sequencer.rhythm != null) {
            this.rhythm  = Arrays.stream(sequencer.rhythm).map(int[]::clone).toArray(int[][]::new);
        }
    }


    /**
     * copy the given image to a writeable image
     *
     * @param image
     * @return a writeable image
     */
    public static WritableImage copyImage(Image image) {
        int height = (int) image.getHeight();
        int width = (int) image.getWidth();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(x, y, color);
            }
        }
        return writableImage;
    }
}
