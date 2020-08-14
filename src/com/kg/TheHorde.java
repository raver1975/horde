package com.kg;

import com.kg.fft.FFT;
import com.kg.python.SpotifyDLTest;
import com.kg.synth.*;
import com.kg.wub.AudioObject;
import com.kg.wub.system.AudioUtils;
import com.kg.wub.system.CentralCommand;
import com.kg.wub.system.Song;
import com.kg.wub.system.SongManager;
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
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static com.kg.python.SpotifyDLTest.STEMS.*;
import static com.kg.synth.BasslineSynthesizer.MSG_CC_ACCENT;
import static com.kg.synth.BasslineSynthesizer.MSG_CC_CUTOFF;
import static com.kg.synth.BasslineSynthesizer.MSG_CC_DECAY;
import static com.kg.synth.BasslineSynthesizer.MSG_CC_ENVMOD;
import static com.kg.synth.BasslineSynthesizer.MSG_CC_RESONANCE;
import static com.kg.synth.BasslineSynthesizer.MSG_CC_TUNE;

public class TheHorde extends Application {
    public static SpotifyDLTest.STEMS stem = STEM0;
    private Canvas sequencerCanvas;
    private Canvas visualizerCanvas;
    private Canvas trackerCanvas;
    private int selectedSequencer = 0;
    public static Output output;
    private int canvasYHeight;
    private int canvasYoffset;
    private GradientLookup gradientLookup;
    private static double main_vol;
    private static final double SCALE_FACTOR = 0.80;
    private boolean drawSequencerPosition = true;
    ArrayList<SequencerData>[] sd = new ArrayList[16];
    public static Regulator bpm;

    //    FFT fft = new FFT(Output.BUFFER_SIZE, (float) Output.SAMPLE_RATE);

    @Override
    public void start(Stage primaryStage) throws Exception {
        Stop[] stops = {
                new Stop(0, Color.rgb(0, 0, 255, 1)),
                new Stop(0.2, Color.rgb(0, 127, 255, 1)),
                new Stop(0.4, Color.rgb(0, 255, 0, 1)),
                new Stop(0.6, Color.rgb(255, 255, 0, 1)),
                new Stop(0.8, Color.rgb(255, 127, 0, 1)),
                new Stop(.99, Color.rgb(255, 0, 0, 1)),
                new Stop(1.2, Color.rgb(255, 0, 255, 1))
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
        BorderPane bp = new BorderPane(root);
        bp.setBorder(new Border(new BorderStroke(Color.RED,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

//        Scene scene = new Scene(root);
        Scene scene = new Scene(new Group(bp), 1400, 680);

        bp.setPrefWidth(scene.getWidth() * 1 / SCALE_FACTOR);
        scene.widthProperty().addListener(observable -> {
            bp.setPrefWidth(scene.getWidth() * 1 / SCALE_FACTOR);
        });

        bp.setPrefHeight(scene.getHeight() * 1 / SCALE_FACTOR);
        scene.heightProperty().addListener(observable -> {
            bp.setPrefHeight(scene.getHeight() * 1 / SCALE_FACTOR);
        });
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
//            s.setBpm(120d);
            s.randomizeSequence();
        }
//        output.setVolume(1d);
        output.start();
        System.out.println("acid audio system started");

        //vol knob
        final Regulator vol = (Regulator) scene.lookup("#midi-vol");
        vol.setTargetValue(50);
        vol.targetValueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println("main vol:\t" + newValue);
                main_vol = newValue.doubleValue() / 127d;
//                for (Sequencer seq : output.getSequencers()) {
//                    main_vol =seq.getVolume()*newValue.doubleValue() / 127d;
//                    System.out.println("val="+val);
//                    seq.setVolume(val);
//                }
            }
        });

        final Button wub = (Button) scene.lookup("#wub");
        wub.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
//                        Song song = SongManager.getRandom();
//                        System.out.println("bpm:" + output.getSequencers()[selectedSequencer].getBpm() + "\t" + song.analysis.getTempo());
//                        Song song1 = AudioUtils.timeStretch(song, output.getSequencers()[selectedSequencer].getBpm());
//                        new AudioObject(song1, null);
                        AudioObject.factory();
                    }
                }).start();
            }
        });

        //midi start
        final Button midiStart = (Button) scene.lookup("#midi-start");
        midiStart.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("midistart clicked");
                Sequencer seq = output.getSequencers()[0];
                if (seq instanceof MidiSequencer) {
                    MidiDevice md = ((MidiSequencer) seq).midiDeviceReceiver;
                    if (md != null) {
                        try {
//                            md.getReceiver().send(new ShortMessage(ShortMessage.STOP, ((MidiSequencer) seq).channel, 0), -1);
                            md.getReceiver().send(new ShortMessage(ShortMessage.START, ((MidiSequencer) seq).channel, 0), -1);

//                            md.getReceiver().send(new ShortMessage(ShortMessage.PROGRAM_CHANGE, selectedSequencer, (int) (Math.random() * 127), 0), -1);
                        } catch (MidiUnavailableException e) {
                            e.printStackTrace();
                        } catch (InvalidMidiDataException e) {
                            e.printStackTrace();
                        }
                    }
                }
                for (Sequencer seq1 : output.getSequencers()) {
                    seq1.reset();
                }

                output.resume();
            }
        });
        midiStart.fire();


        //midi stop
        final Button midiStop = (Button) scene.lookup("#midi-stop");
        midiStop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("midistop clicked");
                Sequencer seq = output.getSequencers()[0];
                if (seq instanceof MidiSequencer) {
                    MidiDevice md = ((MidiSequencer) seq).midiDeviceReceiver;
                    if (md != null) {
                        try {
                            md.getReceiver().send(new ShortMessage(ShortMessage.STOP, ((MidiSequencer) seq).channel, 0), -1);
//                            md.getReceiver().send(new ShortMessage(ShortMessage.PROGRAM_CHANGE, selectedSequencer, (int) (Math.random() * 127), 0), -1);
                        } catch (MidiUnavailableException e) {
                            e.printStackTrace();
                        } catch (InvalidMidiDataException e) {
                            e.printStackTrace();
                        }
                    }
                }
                output.pause();
            }
        });


        //program change
        final Button progDown = (Button) scene.lookup("#midi-prog-change-down");
        progDown.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Sequencer seq = output.getSequencers()[selectedSequencer];
                if (seq instanceof MidiSequencer) {
                    MidiDevice md = ((MidiSequencer) seq).midiDeviceReceiver;
                    if (md != null) {
                        try {
                            int pp = 0;
                            int d = (int) (Math.random() * 127f);
                            System.out.println("selected=" + selectedSequencer);
//                            md.getReceiver().send(new ShortMessage(ShortMessage.CONTROL_CHANGE, selectedSequencer, 0, pp>>7), -1);
//                            md.getReceiver().send(new ShortMessage(ShortMessage.CONTROL_CHANGE, selectedSequencer, 32, pp & 0x7f), -1);
                            md.getReceiver().send(new ShortMessage(ShortMessage.PROGRAM_CHANGE, selectedSequencer, d, 0), -1);
                            System.out.println("prog down clicked");
                        } catch (MidiUnavailableException e) {
                            e.printStackTrace();
                        } catch (InvalidMidiDataException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        });
        final Button progUp = (Button) scene.lookup("#midi-prog-change-up");
        progUp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Sequencer seq = output.getSequencers()[selectedSequencer];
                if (seq instanceof MidiSequencer) {
                    MidiDevice md = ((MidiSequencer) seq).midiDeviceReceiver;
                    if (md != null) {
                        try {
                            int pp = 0;
                            int d = (int) (Math.random() * 127f);
                            System.out.println("selected=" + selectedSequencer);
//                            md.getReceiver().send(new ShortMessage(ShortMessage.CONTROL_CHANGE, selectedSequencer, 0, pp>>7), -1);
//                            md.getReceiver().send(new ShortMessage(ShortMessage.CONTROL_CHANGE, selectedSequencer, 32, pp & 0x7f), -1);
                            md.getReceiver().send(new ShortMessage(ShortMessage.PROGRAM_CHANGE, selectedSequencer, d, 0), -1);
                            System.out.println("prog up clicked");
                        } catch (MidiUnavailableException e) {
                            e.printStackTrace();
                        } catch (InvalidMidiDataException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        });

        //transpose
        final Button transposeUp = (Button) scene.lookup("#transpose-up");
        transposeUp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("transpose up clicked");
                BasslinePattern bassline = output.getSequencers()[selectedSequencer].getBassline();
                for (int i = 0; i < bassline.note.length; i++) {
                    bassline.note[i]++;
                }
                output.getSequencers()[selectedSequencer].pitch_offset++;
            }
        });
        final Button transposeDown = (Button) scene.lookup("#transpose-down");
        transposeDown.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("transpose down clicked");
                BasslinePattern bassline = output.getSequencers()[selectedSequencer].getBassline();
                for (int i = 0; i < bassline.note.length; i++) {
                    bassline.note[i]--;
                }
                output.getSequencers()[selectedSequencer].pitch_offset--;
            }
        });

        final Button transposeLeft = (Button) scene.lookup("#transpose-left");
        transposeLeft.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("transpose left clicked");
                BasslinePattern bassline = output.getSequencers()[selectedSequencer].getBassline();
                for (int i = 0; i < bassline.note.length; i++) {
                    bassline.note[i % bassline.note.length] = bassline.note[(i + 1) % bassline.note.length];
                    bassline.pause[i % bassline.note.length] = bassline.pause[(i + 1) % bassline.note.length];
                    bassline.accent[i % bassline.note.length] = bassline.accent[(i + 1) % bassline.note.length];
                    bassline.slide[i % bassline.note.length] = bassline.slide[(i + 1) % bassline.note.length];
                }
            }
        });

        final Button transposeRight = (Button) scene.lookup("#transpose-right");
        transposeRight.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("transpose right clicked");
                BasslinePattern bassline = output.getSequencers()[selectedSequencer].getBassline();
                for (int i = bassline.note.length - 1; i >= 0; i--) {
                    bassline.note[i % bassline.note.length] = bassline.note[(bassline.note.length + i - 1) % bassline.note.length];
                    bassline.pause[i % bassline.note.length] = bassline.pause[(bassline.note.length + i - 1) % bassline.note.length];
                    bassline.accent[i % bassline.note.length] = bassline.accent[(bassline.note.length + i - 1) % bassline.note.length];
                    bassline.slide[i % bassline.note.length] = bassline.slide[(bassline.note.length + i - 1) % bassline.note.length];
                }
            }
        });

        final Button trackSave = (Button) scene.lookup("#track-save");
        trackSave.setOnAction(new EventHandler<ActionEvent>() {
            int width = 72;
            int height = 48;
            int margin = 2;

            @Override
            public void handle(ActionEvent event) {
                drawSequencerPosition = false;
                drawSequencer();
                System.out.println("track save clicked");
                SnapshotParameters sp = new SnapshotParameters();
                sequencerCanvas.snapshot(new Callback<SnapshotResult, Void>() {
                    @Override
                    public Void call(SnapshotResult param) {
                        drawSequencerPosition = true;
                        if (sd[selectedSequencer] == null) {
                            sd[selectedSequencer] = new ArrayList<SequencerData>();
                        }
                        GraphicsContext gc = trackerCanvas.getGraphicsContext2D();
                        gc.drawImage(param.getImage(), (width + margin) * (sd[selectedSequencer].size()) + margin / 2, (height + margin) * (selectedSequencer) + margin / 2 + 20, width, height);
                        sd[selectedSequencer].add(new SequencerData(param.getImage(), output.getSequencers()[selectedSequencer]));
                        System.out.println("got image!");
//                        JDialog dialog = new JDialog();
////                dialog.setUndecorated(true);
//                        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//                        JLabel label = new JLabel(new ImageIcon(image));
//                        dialog.add(label);
//                        dialog.pack();
//                        dialog.setVisible(true);
                        return null;
                    }
                }, sp, null);

            }
        });


        //bpm knob
        bpm = (Regulator) scene.lookup("#midi-bpm");
        bpm.setTargetValue(Sequencer.bpm);
        bpm.targetValueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println("bpm:\t" + newValue);
                for (Sequencer seq : output.getSequencers()) {
                    seq.setBpm(newValue.doubleValue());
                }
            }
        });

        //pan knobs
        for (int i = 0; i < 16; i++) {
            final Regulator pan = (Regulator) scene.lookup("#midi-pan-" + (i + 1));
            final Regulator delay = (Regulator) scene.lookup("#midi-delay-" + (i + 1));
            final Regulator reverb = (Regulator) scene.lookup("#midi-reverb-" + (i + 1));
            final int finalI = i;

            pan.targetValueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    System.out.println("pan:" + finalI + "\t" + newValue);
                    output.pan[finalI] = newValue.floatValue();
                }
            });

            delay.targetValueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    System.out.println("delay:" + finalI + "\t" + newValue);
//                    output.reverb[finalI].
                }
            });

            reverb.targetValueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    System.out.println("reverb:" + finalI + "\t" + newValue);
//                    output.pan[finalI]=newValue.floatValue();
                }
            });
        }

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
                            ((BasslineSynthesizer) output.synthesizers[3]).controlChange(MSG_CC_TUNE, newValue.intValue());
                            break;
                        case 1:
                            ((BasslineSynthesizer) output.synthesizers[3]).controlChange(MSG_CC_CUTOFF, newValue.intValue());
                            break;
                        case 2:
                            ((BasslineSynthesizer) output.synthesizers[3]).controlChange(MSG_CC_RESONANCE, newValue.intValue());
                            break;
                        case 3:
                            ((BasslineSynthesizer) output.synthesizers[3]).controlChange(MSG_CC_ENVMOD, newValue.intValue());
                            break;
                        case 4:
                            ((BasslineSynthesizer) output.synthesizers[3]).controlChange(MSG_CC_DECAY, newValue.intValue());
                            break;
                        case 5:
                            ((BasslineSynthesizer) output.synthesizers[3]).controlChange(MSG_CC_ACCENT, newValue.intValue());
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
                            ((BasslineSynthesizer) output.synthesizers[2]).controlChange(MSG_CC_TUNE, newValue.intValue());
                            break;
                        case 1:
                            ((BasslineSynthesizer) output.synthesizers[2]).controlChange(MSG_CC_CUTOFF, newValue.intValue());
                            break;
                        case 2:
                            ((BasslineSynthesizer) output.synthesizers[2]).controlChange(MSG_CC_RESONANCE, newValue.intValue());
                            break;
                        case 3:
                            ((BasslineSynthesizer) output.synthesizers[2]).controlChange(MSG_CC_ENVMOD, newValue.intValue());
                            break;
                        case 4:
                            ((BasslineSynthesizer) output.synthesizers[2]).controlChange(MSG_CC_DECAY, newValue.intValue());
                            break;
                        case 5:
                            ((BasslineSynthesizer) output.synthesizers[2]).controlChange(MSG_CC_ACCENT, newValue.intValue());
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
//            onButton.setText("  ");
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
//        observableList.add("MIDI Out");
        observableList.addAll(Instrument.AVAILABLE_INSTRUMENTS);

        for (int i = 0; i < 12; i++) {

            final ChoiceBox cb = (ChoiceBox) scene.lookup("#midi-instrument-" + (i + 1));
            if (i == 9 || ((output.getSequencers()[i] instanceof MidiSequencer && ((MidiSequencer) output.getSequencers()[i]).midiDeviceReceiver != null))) {
                cb.setVisible(false);
                continue;
            }
            cb.setItems(observableList);
            if (output.getSequencers()[i] instanceof InstrumentSequencer) {
                cb.getSelectionModel().select(((InstrumentSequencer) output.getSequencers()[i]).getInstrument());
            }
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
        trackerCanvas = (Canvas) scene.lookup("#tracker");

        sequencerCanvas.setOnMousePressed(new EventHandler<MouseEvent>() {
            private int state;

            @Override
            public void handle(MouseEvent e) {
                if (!sequencerCanvas.contains(e.getX(), e.getY())) {
                    return;
                }
                int x = (int) (e.getX() / (sequencerCanvas.getWidth() / 16));
                int y = (int) ((sequencerCanvas.getHeight() - e.getY()) / (sequencerCanvas.getHeight() / canvasYHeight) - canvasYoffset);
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

        trackerCanvas.setOnMousePressed(new EventHandler<MouseEvent>() {
            private int state;
            int width = 72;
            int height = 48;
            int margin = 2;
//(x-margin/2)/(width+margin)=(xloc)

            //(y-margin/2+20)/(height+margin)=(yloc)
            @Override
            public void handle(MouseEvent e) {
                if (!trackerCanvas.contains(e.getX(), e.getY())) {
                    return;
                }
                int xLoc = (int) ((e.getX() - (margin / 2)) / (width + margin));
                int yLoc = (int) ((e.getY() - (margin / 2 + 20)) / (height + margin));
                System.out.println("Clicked on tracker canvas " + xLoc + "\t" + yLoc);
                if (sd[yLoc] != null) {
                    if (xLoc < sd[yLoc].size() && sd[yLoc].get(xLoc) != null) {
                        output.getSequencers()[selectedSequencer].setSequence(sd[yLoc].get(xLoc));
                    }
                }
            }
        });
        trackerCanvas.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (!trackerCanvas.contains(e.getX(), e.getY()) || e.getButton() != MouseButton.PRIMARY) {
                    return;
                }
                System.out.println("dragged on tracker canvas");
            }
        });

        drawSequencer();
        primaryStage.setScene(scene);
        primaryStage.show();
        Scale scale = new Scale(SCALE_FACTOR, SCALE_FACTOR);
        scale.setPivotX(0);
        scale.setPivotY(0);
        bp.getTransforms().setAll(scale);

    }

    public void drawTracker() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                //draw tracker here
            }
        });
    }

    boolean seqLock = false;

    public void drawSequencer() {
        if (seqLock) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                seqLock = true;
                BasslinePattern bassline = output.getSequencers()[selectedSequencer].getBassline();
                if (sequencerCanvas == null) return;
                int step = output.getSequencers()[selectedSequencer].step;
                double width = sequencerCanvas.getWidth();
                double height = sequencerCanvas.getHeight();
                double widthDist = width / 16d;
                if (output.getSequencers()[selectedSequencer] instanceof RhythmSequencer) {
                    canvasYoffset = 0;
                    canvasYHeight = 7;
                } else {
                    canvasYHeight = 96;
                    canvasYoffset = 23;
                }

                double heightDist = height / canvasYHeight;
                GraphicsContext gc = sequencerCanvas.getGraphicsContext2D();
                Color gl = gradientLookup.getColorAt(selectedSequencer / 16d).darker().darker().darker();
                gc.setFill(gl.darker());
                gc.fillRect(0, 0, sequencerCanvas.getWidth(), sequencerCanvas.getHeight());

                gc.setStroke(gl);
                gc.setLineWidth(2);
                for (int i = 0; i < 17; i++) {
                    if (i % 4 == 0) {
                        gc.setStroke(gl.brighter());
                    } else {
                        gc.setStroke(gl);
                    }
                    gc.strokeLine(i * widthDist, 0, i * widthDist, height);

                }


                gc.setStroke(gl);
                gc.setLineWidth(1);
                for (int i = 0; i < canvasYHeight + 1; i++) {
                    if (i % 12 == 0) {
                        gc.setStroke(gl.brighter());
                    } else {
                        gc.setStroke(gl);
                    }
                    gc.strokeLine(0, i * heightDist, width, i * heightDist);

                }


                if (drawSequencerPosition) {
                    gc.setStroke(Color.WHITE);
                    gc.strokeLine(step * widthDist, 0, step * widthDist, height);

//                    gc.setStroke(Color.BLACK);
//                    gc.setLineWidth(1);
//                    gc.strokeLine(0, sequencerCanvas.getHeight() - output.getSequencers()[selectedSequencer].pitch_offset * heightDist, width, sequencerCanvas.getHeight() - output.getSequencers()[selectedSequencer].pitch_offset * heightDist);

                }
                gc.setLineWidth(2);
                if (bassline != null) {
                    for (int i = 0; i < 16; i++) {
                        int note = bassline.getNote(i);
                        int pitch = bassline.note[i] + canvasYoffset;

                        if (!bassline.pause[i]) {
                            gc.setFill(new Color(.0d, 1d, .0d, 1d));
                            gc.setStroke(new Color(.0d, 1d, .0d, 1d));
                            gc.setLineWidth(3);
                            int vel = (bassline.accent[i] ? 127 : 80);

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
                seqLock = false;
            }
        });
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
        System.out.println("horde started");
        if (args.length == 1) {
            String[] temp = args[0].split(" ");
            if (temp.length > 1) {
                args = temp;
            }
        }
        System.out.println("args=" + Arrays.toString(args));
        String wub = null;
        double tempo=0;
        top:
        for (String arg : args) {
            switch (arg) {
                default:
                    break;
                case "STEM0":
                    TheHorde.stem = STEM0;
                    System.out.println("STEM0");
                    continue top;
                case "STEM2":
                    TheHorde.stem = STEM2;
                    System.out.println("STEM2");
                    continue top;
                case "STEM4":
                    TheHorde.stem = STEM4;
                    System.out.println("STEM4");
                    continue top;
                case "STEM5":
                    TheHorde.stem = STEM5;
                    System.out.println("STEM5");
                    continue top;
            }
            try {
                Sequencer.bpm = Integer.parseInt(arg);
                System.out.println("set tempo to:" + Sequencer.bpm);
                tempo=Sequencer.bpm;
            } catch (NumberFormatException e) {
                wub = arg;
            }

        }
        Sequencer.bpm=120;

        if (wub!=null){
                Sequencer.bpm=tempo;
            String finalWub = wub;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    AudioObject.factory(finalWub);
                }
            }).start();
        }
        launch(args);
    }

    //double max=Double.MIN_VALUE;
//double min=Double.MAX_VALUE;
    private double[] lastBytes = new double[256];
    private double[] accel = new double[256];

    boolean visLock = false;

    public void drawVisualizer(final byte[] buffer5) {
        if (visLock) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                visLock = true;
                if (visualizerCanvas != null) {
                    double width = visualizerCanvas.getWidth();
                    double height = visualizerCanvas.getHeight();
                    float[] fft = calculateFFT(buffer5, 256);
                    GraphicsContext gc = visualizerCanvas.getGraphicsContext2D();
                    gc.setFill(Color.BLACK);
                    gc.fillRect(0, 0, width, height);
                    gc.setStroke(new Color(1d, 1d, 1d, 1d));
                    double dw = width / 256d;
                    gc.setLineWidth(dw + .03f);
                   for (int i = 0; i < 256; i++) {
                        double perc = (double) i / width;
                        int l = (int) (perc * fft.length);
                        double mag = fft[l];
                        Color co = gradientLookup.getColorAt(Math.min(1, mag / 2));
//                Color co1=new Color(co.getRed(),co.getGreen(),co.getBlue(),1d);
                        Color lastco = gradientLookup.getColorAt(Math.min(1, lastBytes[l] / 2));
//                gc.setStroke(Color.WHITE);

                        gc.setStroke(lastco);

//                gc.strokeLine(i * dw-dw/2, height - lastBytes[l] / 3 * height, i * dw + dw/2, height - lastBytes[l] / 3 * height);
                        gc.strokeLine(i * dw, 1 + height - lastBytes[l] / 3 * height, i * dw, height - lastBytes[l] / 3 * height);
                        if (mag > lastBytes[l]) {
                            lastBytes[l] = mag;
                            accel[l] = 0;
                        } else {
                            lastBytes[l] -= accel[l];
                            accel[l] += .001d;
                        }
//                lastBytes[l] /= 100d;
                        gc.setStroke(co);

                        gc.strokeLine(i * dw, height, i * dw, height - mag / 3 * height);
                    }
                    visLock = false;
                }

            }
        });
    }

    FFT fft = new FFT(Output.BUFFER_SIZE / 2, (float) Output.SAMPLE_RATE);

    public float[] calculateFFT(byte[] signal, int width) {
        //fft.window(new GaussWindow());
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