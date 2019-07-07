package com.myronmarston;

import com.myronmarston.music.Instrument;
import com.myronmarston.synth.InstrumentSequencer;
import com.myronmarston.synth.Output;
import com.myronmarston.synth.Resources;
import com.myronmarston.synth.Sequencer;
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
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;

public class JavafxSample extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        final Output output = new Output();
        Parent root = null;
        try {
            root = FXMLLoader.load(new File("data/gui.fxml").toURL());
        } catch (FileNotFoundException e) {
            ClassLoader cl = Resources.class.getClassLoader();
            URL url = this.getClass().getClassLoader().getResource("gui.fxml");
            root = FXMLLoader.load(url);
        }

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

        for (Sequencer s : output.getSequencer()) {
            s.setBpm(120d);
            s.randomizeSequence();
        }
//        output.setVolume(1d);
        output.start();
        System.out.println("acid audio system started");
        for (int i = 0; i < 5; i++) {
            final Regulator regulator1= (Regulator) scene.lookup("#synth1-knob-" + (i + 1));
            final Regulator regulator2= (Regulator) scene.lookup("#synth2-knob-" + (i + 1));
            final int finalI = i;
            regulator1.targetValueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    System.out.println("synth1:"+finalI+"\t"+newValue);
                }
            });
            regulator2.targetValueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    System.out.println("synth2:"+finalI+"\t"+newValue);
                }
            });
        }
        for (int i = 0; i < 20; i++) {
            final Slider slider = (Slider) scene.lookup("#midi-sl-" + (i + 1));
            final ToggleButton onButton = (ToggleButton) scene.lookup("#midi-bt-" + (i + 1));
            final Button shuffleButton = (Button) scene.lookup("#midi-shuffle-" + (i + 1));
            final int finalI = i;
            slider.valueProperty().addListener(new ChangeListener<Number>() {
                public void changed(ObservableValue<? extends Number> ov,
                                    Number old_val, Number new_val) {
                    if (onButton.isSelected()) {
                        output.getSequencer()[finalI].setVolume(new_val.doubleValue() / 127d);
                    }
                }
            });
            slider.setValue(63);
            GridPane.setFillHeight(slider, true);
//            GridPane.setHalignment(slider, HPos.CENTER);
            onButton.setText(" ");
            onButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        output.getSequencer()[finalI].setVolume(slider.getValue() / 127d);
                        onButton.setStyle("-fx-background-color: #99ff99");
                    } else {
                        output.getSequencer()[finalI].setVolume(0);
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
                    output.getSequencer()[finalI].randomizeSequence();
                    output.getSequencer()[finalI].randomizeRhythm();
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
        for (int i = 0; i < 16; i++) {

            final ChoiceBox cb = (ChoiceBox) scene.lookup("#midi-instrument-" + (i + 1));
            if (i == 9) {
                cb.setVisible(false);
                continue;
            }
            System.out.println(cb);
            cb.setItems(observableList);
            cb.getSelectionModel().select(((InstrumentSequencer) output.getSequencer()[i]).getInstrument());
            cb.setMinWidth(150d);
            GridPane.setHalignment(cb, HPos.CENTER);
            GridPane.setValignment(cb, VPos.CENTER);
            final int finalI = i;
            cb.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    ((InstrumentSequencer) output.getSequencer()[finalI]).instrument = (String) cb.getItems().get(newValue.intValue());
                    ((InstrumentSequencer) output.getSequencer()[finalI]).setChannel();
                    System.out.println("changing to instrument:" + cb.getSelectionModel().getSelectedItem().toString() + "\t" + "on channel:" + ((InstrumentSequencer) output.getSequencer()[finalI]).channel);
                }
            });
        }

        primaryStage.setScene(scene);
        primaryStage.show();

    }


    public static void main(String args[]) {
        launch(args);
    }
} 