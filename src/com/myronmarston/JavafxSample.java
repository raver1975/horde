package com.myronmarston;

import com.myronmarston.synth.Output;
import com.myronmarston.synth.Sequencer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;

public class JavafxSample extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        final Output output = new Output();
       /* //creating a Group object
        Group group = new Group();

        //Creating a Scene by passing the group object, height and width
        Scene scene = new Scene(group, 600, 300);

        //setting color to the scene
        scene.setFill(Color.BROWN);

        //Setting the title to Stage.
        primaryStage.setTitle("The Horde");

        //Adding the scene to Stage
        primaryStage.setScene(scene);

        //Displaying the contents of the stage
        primaryStage.show();*/

        Parent root = FXMLLoader.load(new File("gui.fxml").toURL());

        Scene scene = new Scene(root);

        primaryStage.setTitle("The Horde");
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                output.dispose();
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
        for (int i = 0; i < 16; i++) {
            final Slider slider = (Slider) scene.lookup("#midi-sl-" + (i + 1));
            final ToggleButton onButton = (ToggleButton) scene.lookup("#midi-bt-" + (i + 1));
            final Button shuffuleButton = (Button) scene.lookup("#midi-shuffle-" + (i + 1));
            final int finalI = i;
            slider.valueProperty().addListener(new ChangeListener<Number>() {
                public void changed(ObservableValue<? extends Number> ov,
                                    Number old_val, Number new_val) {
                    if (onButton.isSelected()) {
                        output.getSequencer()[finalI].setVolume(new_val.doubleValue() / 127d);
                    }
//                    if (new_val.intValue() == 0) {
//                        button.setSelected(false);
//                    } else if (!button.isSelected()) {
//                        button.setSelected(true);
//                    }
                }
            });
            slider.setValue(63);
            GridPane.setFillHeight(slider, true);
            GridPane.setHalignment(slider, HPos.CENTER);
            onButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        output.getSequencer()[finalI].setVolume(slider.getValue() / 127d);
                    } else {
                        output.getSequencer()[finalI].setVolume(0);
                    }
                }
            });
            GridPane.setFillWidth(onButton, true);
            GridPane.setFillHeight(onButton, true);
            GridPane.setHalignment(onButton, HPos.CENTER);
            GridPane.setValignment(onButton, VPos.CENTER);

            shuffuleButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    output.getSequencer()[finalI].randomizeSequence();
                    System.out.println("shuffle clicked:"+finalI);
                }
            });
            GridPane.setFillWidth(shuffuleButton, true);
            GridPane.setFillHeight(shuffuleButton, true);
            GridPane.setHalignment(shuffuleButton, HPos.CENTER);
            GridPane.setValignment(shuffuleButton, VPos.CENTER);

            onButton.setSelected(false);

        }
        primaryStage.setScene(scene);
        primaryStage.show();

    }


    public static void main(String args[]) {
        launch(args);
    }
} 