package com.klemstine;

import com.klemstine.fft.FFT;
import com.klemstine.fft.GaussWindow;
import com.klemstine.fft.HannWindow;
import com.klemstine.fft.RectangularWindow;
import com.klemstine.synth.*;
import com.myronmarston.music.Instrument;
import eu.hansolo.fx.regulators.GradientLookup;
import eu.hansolo.fx.regulators.Regulator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import static com.klemstine.synth.BasslineSynthesizer.MSG_CC_ACCENT;
import static com.klemstine.synth.BasslineSynthesizer.MSG_CC_CUTOFF;
import static com.klemstine.synth.BasslineSynthesizer.MSG_CC_DECAY;
import static com.klemstine.synth.BasslineSynthesizer.MSG_CC_ENVMOD;
import static com.klemstine.synth.BasslineSynthesizer.MSG_CC_RESONANCE;
import static com.klemstine.synth.BasslineSynthesizer.MSG_CC_TUNE;

public class TheHorde extends Application {
    private Canvas sequencerCanvas;
    private Canvas visualizerCanvas;
    private int selectedSequencer = 0;
    Output output;
    private int canvasYHeight;
    private int canvasYoffset;
    private GradientLookup gradientLookup;
//    FFT fft = new FFT(Output.BUFFER_SIZE, (float) Output.SAMPLE_RATE);

    @Override
    public void start(Stage primaryStage) throws Exception {
        Stop[] stops = {
                new Stop(0, Color.rgb(0, 0, 255)),
                new Stop(0.2, Color.rgb(0, 127, 255)),
                new Stop(0.4, Color.rgb(0, 255, 0)),
                new Stop(0.6, Color.rgb(255, 255, 0)),
                new Stop(0.8, Color.rgb(255, 127, 0)),
                new Stop(1, Color.rgb(255, 0, 0)),
        };

        gradientLookup = new GradientLookup(stops);
        output = new Output(this);
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(new File("data/gui.fxml").toURL());
        try {
            root = loader.load();
        } catch (FileNotFoundException e) {
            URL url = this.getClass().getClassLoader().getResource("gui.fxml");
            loader = new FXMLLoader(url);
            root = loader.load();
        }
        Map<String, Object> fxmlNamespace = loader.getNamespace();
        Scene scene = new Scene(root);

        primaryStage.setTitle("The Horde");
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                output.dispose();
                Platform.exit();
                System.exit(0);
            }
        });
        System.out.println("acid audio system starting");

        ToggleGroup toggleGroup = (ToggleGroup) fxmlNamespace.get("selectsequencer");

        toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) {
                RadioButton chk = (RadioButton) t1.getToggleGroup().getSelectedToggle(); // Cast object to radio button
//                System.out.println("Selected Radio Button - "+chk.getText());
                selectedSequencer = Integer.parseInt(chk.getId().replace("select-channel-", "")) - 1;
                drawSequencer();
            }
        });

        for (Sequencer s : output.getSequencers()) {
            s.setBpm(120d);
            s.randomizeSequence();
        }
//        output.setVolume(1d);
        output.start();
        System.out.println("acid audio system started");

        //synth1 knobs
        for (int i = 0; i < 6; i++) {
            final Regulator regulator1 = (Regulator) scene.lookup("#synth1-knob-" + (i + 1));
//            regulator1.se

            final Regulator regulator2 = (Regulator) scene.lookup("#synth2-knob-" + (i + 1));
            final int finalI = i;
            regulator1.targetValueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    System.out.println("synth1:" + finalI + "\t" + newValue);
                    switch (finalI) {
                        case 0:
                            ((BasslineSynthesizer) output.getSynthesizers()[3]).controlChange(MSG_CC_TUNE, newValue.intValue());
                            break;
                        case 1:
                            ((BasslineSynthesizer) output.getSynthesizers()[3]).controlChange(MSG_CC_CUTOFF, newValue.intValue());
                            break;
                        case 2:
                            ((BasslineSynthesizer) output.getSynthesizers()[3]).controlChange(MSG_CC_RESONANCE, newValue.intValue());
                            break;
                        case 3:
                            ((BasslineSynthesizer) output.getSynthesizers()[3]).controlChange(MSG_CC_ENVMOD, newValue.intValue());
                            break;
                        case 4:
                            ((BasslineSynthesizer) output.getSynthesizers()[3]).controlChange(MSG_CC_DECAY, newValue.intValue());
                            break;
                        case 5:
                            ((BasslineSynthesizer) output.getSynthesizers()[3]).controlChange(MSG_CC_ACCENT, newValue.intValue());
                            break;
                    }
                }
            });
            regulator2.targetValueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    System.out.println("synth2:" + finalI + "\t" + newValue);
                    switch (finalI) {
                        case 0:
                            ((BasslineSynthesizer) output.getSynthesizers()[2]).controlChange(MSG_CC_TUNE, newValue.intValue());
                            break;
                        case 1:
                            ((BasslineSynthesizer) output.getSynthesizers()[2]).controlChange(MSG_CC_CUTOFF, newValue.intValue());
                            break;
                        case 2:
                            ((BasslineSynthesizer) output.getSynthesizers()[2]).controlChange(MSG_CC_RESONANCE, newValue.intValue());
                            break;
                        case 3:
                            ((BasslineSynthesizer) output.getSynthesizers()[2]).controlChange(MSG_CC_ENVMOD, newValue.intValue());
                            break;
                        case 4:
                            ((BasslineSynthesizer) output.getSynthesizers()[2]).controlChange(MSG_CC_DECAY, newValue.intValue());
                            break;
                        case 5:
                            ((BasslineSynthesizer) output.getSynthesizers()[2]).controlChange(MSG_CC_ACCENT, newValue.intValue());
                            break;
                    }
                }
            });
        }


        for (int i = 0; i < 16; i++) {
            final Slider slider = (Slider) scene.lookup("#midi-sl-" + (i + 1));
            final ToggleButton onButton = (ToggleButton) scene.lookup("#midi-bt-" + (i + 1));
            final Button shuffleButton = (Button) scene.lookup("#midi-shuffle-" + (i + 1));
            final int finalI = i;
            slider.valueProperty().addListener(new ChangeListener<Number>() {
                public void changed(ObservableValue<? extends Number> ov,
                                    Number old_val, Number new_val) {
                    if (onButton.isSelected()) {
                        output.getSequencers()[finalI].setVolume(new_val.doubleValue() / 127d);
                    }

                }
            });
            slider.setValue(63);
            GridPane.setFillHeight(slider, true);
//            GridPane.setHalignment(slider, HPos.CENTER);
            onButton.setText("  ");
            onButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        output.getSequencers()[finalI].setVolume(slider.getValue() / 127d);
                        onButton.setStyle("-fx-background-color: #99ff99");
                    } else {
                        output.getSequencers()[finalI].setVolume(0);
                        onButton.setStyle("-fx-background-color: #dddddd");
                    }
                }
            });
            onButton.setStyle("-fx-background-color: #dddddd");
            onButton.setSelected(true);
            GridPane.setFillWidth(onButton, true);
            GridPane.setFillHeight(onButton, true);
            GridPane.setHalignment(onButton, HPos.CENTER);
            GridPane.setValignment(onButton, VPos.CENTER);

            shuffleButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    output.getSequencers()[finalI].randomizeSequence();
                    output.getSequencers()[finalI].randomizeRhythm();
                    System.out.println("shuffle clicked:" + (finalI + 1));
                }
            });
            shuffleButton.setFont(new Font(shuffleButton.getFont().getName(), 12));
            shuffleButton.setText("\u21BB");
            GridPane.setFillWidth(shuffleButton, true);
            GridPane.setFillHeight(shuffleButton, true);
            GridPane.setHalignment(shuffleButton, HPos.CENTER);
            GridPane.setValignment(shuffleButton, VPos.CENTER);

            onButton.setSelected(false);

        }
        ArrayList<String> arr = new ArrayList<String>();
        ObservableList<String> observableList = FXCollections.observableList(arr);
        observableList.addAll(Instrument.AVAILABLE_INSTRUMENTS);
        for (int i = 0; i < 12; i++) {

            final ChoiceBox cb = (ChoiceBox) scene.lookup("#midi-instrument-" + (i + 1));
            if (i == 9) {
                cb.setVisible(false);
                continue;
            }
            cb.setItems(observableList);
            cb.getSelectionModel().select(((InstrumentSequencer) output.getSequencers()[i]).getInstrument());
            cb.setMinWidth(150d);
            GridPane.setHalignment(cb, HPos.CENTER);
            GridPane.setValignment(cb, VPos.CENTER);
            final int finalI = i;
            cb.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    ((InstrumentSequencer) output.getSequencers()[finalI]).instrument = (String) cb.getItems().get(newValue.intValue());
                    ((InstrumentSequencer) output.getSequencers()[finalI]).setChannel();
                    System.out.println("changing to instrument:" + cb.getSelectionModel().getSelectedItem().toString() + "\t" + "on channel:" + ((InstrumentSequencer) output.getSequencers()[finalI]).channel);
                }
            });
        }

        sequencerCanvas = (Canvas) scene.lookup("#sequencer");
        visualizerCanvas = (Canvas) scene.lookup("#vis");

        sequencerCanvas.setOnMousePressed(new EventHandler<MouseEvent>() {
            private int state;

            @Override
            public void handle(MouseEvent e) {
                if (!sequencerCanvas.contains(e.getX(), e.getY())) {
                    return;
                }
                int x = (int) (e.getX() / (sequencerCanvas.getWidth() / 16));
                int y = (int) ((sequencerCanvas.getHeight() - e.getY()) / (sequencerCanvas.getHeight() / canvasYHeight) - canvasYoffset);
                System.out.println("xy" + x + "\t" + y);
                BasslinePattern bassline = output.getSequencers()[selectedSequencer].getBassline();
                if (bassline != null) {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        bassline.note[x] = (byte) y;
                        bassline.pause[x] = false;
                    } else if (e.getButton() == MouseButton.SECONDARY) {
//                        bassline.note[x] = (byte) y;
                        state++;
                        switch (state % 6) {
                            case 0:
                                bassline.pause[x] = false;
                                bassline.accent[x] = false;
                                bassline.slide[x] = false;
                                break;
                            case 1:
                                bassline.pause[x] = false;
                                bassline.accent[x] = true;
                                bassline.slide[x] = false;
                                break;
                            case 2:
                                bassline.pause[x] = true;
                                bassline.accent[x] = false;
                                bassline.slide[x] = false;

                                break;
                            case 3:
                                bassline.pause[x] = false;
                                bassline.accent[x] = false;
                                bassline.slide[x] = true;
                                break;
                            case 4:
                                bassline.pause[x] = false;
                                bassline.accent[x] = true;
                                bassline.slide[x] = true;
                                break;
                            case 5:
                                bassline.pause[x] = true;
                                bassline.accent[x] = false;
                                bassline.slide[x] = false;
                        }

                    }
                } else {
                    int[][] rhythm = output.getSequencers()[selectedSequencer].getRhythm();
                    rhythm[y][x] = (rhythm[y][x] + 1) % 3;
                }
            }
        });
        sequencerCanvas.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (!sequencerCanvas.contains(e.getX(), e.getY()) || e.getButton() != MouseButton.PRIMARY) {
                    return;
                }
                int x = (int) (e.getX() / (sequencerCanvas.getWidth() / 16d));
                int y = (int) ((sequencerCanvas.getHeight() - e.getY()) / (sequencerCanvas.getHeight() / canvasYHeight) - canvasYoffset);
                BasslinePattern bassline = output.getSequencers()[selectedSequencer].getBassline();
                if (bassline != null) {
                    bassline.note[x] = (byte) y;
                    System.out.println("note:" + y);
                    bassline.pause[x] = false;
                } else {
                }
            }
        });

        //        System.out.println(canvas);
        drawSequencer();
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public void drawSequencer() {
        BasslinePattern bassline = output.getSequencers()[selectedSequencer].getBassline();
        if (sequencerCanvas == null) return;
        int step = output.getSequencers()[selectedSequencer].step;
        double width = sequencerCanvas.getWidth();
        double height = sequencerCanvas.getHeight();
        double widthDist = width / 16d;
        if (bassline == null) {
            canvasYoffset = 0;
            canvasYHeight = 7;
        } else {
            canvasYHeight = 96;
            canvasYoffset = 23;
        }

        double heightDist = height / canvasYHeight;
        GraphicsContext gc = sequencerCanvas.getGraphicsContext2D();

        gc.setFill(new Color(0, 0, .9d, 1));
        gc.fillRect(0, 0, sequencerCanvas.getWidth(), sequencerCanvas.getHeight());

        gc.setStroke(Color.BLUE);
        gc.setLineWidth(1);
        for (int i = 0; i < canvasYHeight + 1; i++) {
            if (i % 12 == 0) {
                gc.setStroke(new Color(.2d, .2d, 1d, 1));
            } else {
                gc.setStroke(Color.BLUE);
            }
            gc.strokeLine(0, i * heightDist, width, i * heightDist);

        }

        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2);
        for (int i = 0; i < 17; i++) {
            if (i % 4 == 0) {
                gc.setStroke(new Color(.2d, .2d, 1d, 1));
            } else {
                gc.setStroke(Color.BLUE);
            }
            gc.strokeLine(i * widthDist, 0, i * widthDist, height);

        }


        gc.setStroke(Color.WHITE);
        gc.strokeLine(step * widthDist, 0, step * widthDist, height);

        if (bassline != null) {
            for (int i = 0; i < 16; i++) {
                int note = bassline.getNote(i);
                int pitch = bassline.note[i] + canvasYoffset;

                if (!bassline.pause[i]) {
                    gc.setFill(new Color(.0d, 1d, .0d, 1d));
                    gc.setStroke(new Color(.0d, 1d, .0d, 1d));
                    gc.setLineWidth(3);
                    int vel = (int) ((bassline.accent[i] ? 127 : 80));

                    if (!bassline.accent[i]) {
                        gc.setStroke(new Color(.0d, 1d, .0d, 1d));
                        gc.setFill(new Color(.0d, 1d, .0d, 1d));
                    } else {
                        gc.setStroke(new Color(1d, .7d, 0d, 1d));
                        gc.setFill(new Color(1d, .7d, .0d, 1d));
                    }
                    gc.fillRoundRect(i * widthDist, height - pitch * heightDist, widthDist, heightDist, 10, 10);
                    gc.strokeRoundRect(i * widthDist, height - pitch * heightDist, widthDist, heightDist, 10, 10);

//                    gc.strokeRoundRect(i * widthDist, height - pitch * heightDist, widthDist, heightDist, 10, 10);
                }
            }
            for (int i = 0; i < 16; i++) {
                if (bassline.slide[i]) {
                    int pitch = bassline.note[i] + canvasYoffset;
                    int nextpitch = bassline.note[(i + 1) % 16] + canvasYoffset;
                    gc.setLineWidth(5);
                    gc.setStroke(new Color(1d, 1d, .0d, 1d));
                    gc.strokeLine(((i + 1) % 16) * widthDist, height - (pitch * heightDist) + heightDist / 2, ((i + 1) % 16) * widthDist, height - (nextpitch * heightDist) + heightDist / 2);
                }
            }
        } else {
            int[][] rhythm = output.getSequencers()[selectedSequencer].getRhythm();
//            System.out.println(Arrays.deepToString(rhythm));
            for (int j = 0; j < rhythm.length; j++) {
                for (int i = 0; i < rhythm[j].length; i++) {
                    if (rhythm[j][i] > 0) {
//                if (!bassline.accent[i]) {
//                    gc.setStroke(new Color(.0d, 1d, .0d, 1d));
//                    gc.setFill(new Color(.0d, 1d, .0d, 1d));
//                } else {
                        if (rhythm[j][i] == 1) {
                            gc.setStroke(new Color(1d, 1d, 0d, 1d));
                            gc.setFill(new Color(1d, 1d, .0d, 1d));
                        }
                        if (rhythm[j][i] == 2) {
                            gc.setStroke(new Color(1d, .7d, 0d, 1d));
                            gc.setFill(new Color(1d, .7d, .0d, 1d));
                        }
//                }

                        gc.fillRoundRect(i * widthDist, height - (j + 1) * heightDist, widthDist, heightDist, 10, 10);
                        gc.strokeRoundRect(i * widthDist, height - (j + 1) * heightDist, widthDist, heightDist, 10, 10);
                    }
                }
            }
        }


//        gc.setLineWidth(5);
//        gc.strokeLine(40, 10, 10, 40);
//        gc.fillOval(10, 60, 30, 30);
//        gc.strokeOval(60, 60, 30, 30);
//        gc.fillRoundRect(110, 60, 30, 30, 10, 10);
//        gc.strokeRoundRect(160, 60, 30, 30, 10, 10);
//        gc.fillArc(10, 110, 30, 30, 45, 240, ArcType.OPEN);
//        gc.fillArc(60, 110, 30, 30, 45, 240, ArcType.CHORD);
//        gc.fillArc(110, 110, 30, 30, 45, 240, ArcType.ROUND);
//        gc.strokeArc(10, 160, 30, 30, 45, 240, ArcType.OPEN);
//        gc.strokeArc(60, 160, 30, 30, 45, 240, ArcType.CHORD);
//        gc.strokeArc(110, 160, 30, 30, 45, 240, ArcType.ROUND);
//        gc.fillPolygon(new double[]{10, 40, 10, 40},
//                new double[]{210, 210, 240, 240}, 4);
//        gc.strokePolygon(new double[]{60, 90, 60, 90},
//                new double[]{210, 210, 240, 240}, 4);
//        gc.strokePolyline(new double[]{110, 140, 110, 140},
//                new double[]{210, 210, 240, 240}, 4);
    }


    public static void main(String args[]) {
        launch(args);
    }

    //double max=Double.MIN_VALUE;
//double min=Double.MAX_VALUE;
    double[] lastBytes = new double[256];
    double[] accel = new double[256];
    long lastTime;

    synchronized public void drawVisualizer(final byte[] buffer5) {
        if (System.currentTimeMillis() - lastTime < 30) {
            return;
        }
        lastTime = System.currentTimeMillis();
        if (visualizerCanvas != null) {


            double width = visualizerCanvas.getWidth();
            double height = visualizerCanvas.getHeight();
            float[] fft = calculateFFT(buffer5, (int) 256);
            GraphicsContext gc = visualizerCanvas.getGraphicsContext2D();
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, width, height);
            gc.setStroke(new Color(1d, 1d, 1d, 1d));
            double dw = width / 256d;

            for (int i = 0; i < 256; i++) {
                double perc = (double) i / width;
                int l = (int) (perc * fft.length);
                double mag = fft[l];
                Color co = gradientLookup.getColorAt(Math.min(1, mag / 2));
//                Color co1=new Color(co.getRed(),co.getGreen(),co.getBlue(),1d);
                Color lastco = gradientLookup.getColorAt(Math.min(1, lastBytes[l] / 2));
//                gc.setStroke(Color.WHITE);

                gc.setStroke(lastco.darker());
                gc.setLineWidth(2);
                gc.strokeLine(i * dw, height - lastBytes[l] / 3 * height, i * dw + dw, height - lastBytes[l] / 3 * height);
                if (mag > lastBytes[l]) {
                    lastBytes[l] = mag;
                    accel[l] = 0;
                } else {
                    lastBytes[l] -= accel[l];
                    accel[l] +=.001d;
                }
//                lastBytes[l] /= 100d;
                    gc.setStroke(co);
                    gc.setLineWidth(dw);
                    gc.strokeLine(i * dw, height, i * dw, height - mag / 3 * height);


//                max=Math.max(mag,max);
//                min=Math.min(mag,min);
                }
//            for (int i = 0; i < width; i++) {
////                System.out.println("mag:"+mag);
//                double perc = (double) i / width;
//                int l = (int) (perc * fft.length);
//                double mag=fft[l];
//                double p=(mag+min)/max;
//                System.out.println(p);
//
//            }


            }
        }

        FFT fft = new FFT(Output.BUFFER_SIZE / 2, (float) Output.SAMPLE_RATE);

        public float[] calculateFFT ( byte[] signal, int width){
            fft.window(new GaussWindow());
            fft.linAverages(width);
//            fft.logAverages(44100/640,256/8);
            final int mNumberOfFFTPoints = signal.length / 2;
//        double temp;
            float[] buf = new float[mNumberOfFFTPoints];
//        Complex[] y;
//        Complex[] complexSignal = new Complex[mNumberOfFFTPoints];
//        double[] absSignal = new double[mNumberOfFFTPoints / 2];
//
            for (int i = 0; i < mNumberOfFFTPoints; i++) {
                buf[i] = (float) ((signal[2 * i] & 0xFF) | (signal[2 * i + 1] << 8)) / 32768.0F;
            }
//
            fft.forward(buf);
            float[] ret = new float[width];
            for (int i = 0; i < width; i++) {
                ret[i] = fft.getAvg(i);
            }
            return ret;
//        y = FFT6.fft(complexSignal);
//
//        for (int i = 0; i < (mNumberOfFFTPoints / 2); i++) {
//            absSignal[i] = Math.sqrt(Math.pow(y[i].re(), 2) + Math.pow(y[i].im(), 2));
//        }
//
//        return absSignal;

        }

    }