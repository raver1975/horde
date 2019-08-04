/*
 * Copyright (c) 2016 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.regulators;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;


/**
 * User: hansolo
 * Date: 01.03.16
 * Time: 13:18
 */
public class Main extends Application {
    private Regulator         regulator;
    private FeedbackRegulator feedbackRegulator;
    private ColorRegulator    colorRegulator;
    private long              lastTimerCall;
    private AnimationTimer    timer;


    @Override public void init() {
        regulator = RegulatorBuilder.create()
                                    .prefSize(400, 400)
                                    .barColor(Color.rgb(255, 222, 102))
                                    .unit("%")
                                    //.textColor(Color.YELLOW)
                                    //.symbolColor(Color.MAGENTA)
                                    .iconColor(Color.WHITE)
                                    //.color(Color.PURPLE)
                                    .onTargetSet(new EventHandler() {
                                        @Override
                                        public void handle(Event e) {
                                            System.out.println("New target set to " + regulator.getTargetValue());
                                        }
                                    })
                                    .build();

        feedbackRegulator = FeedbackRegulatorBuilder.create()
                                                    .prefSize(400, 400)
                                                    .minValue(0)
                                                    .maxValue(100)
                                                    .targetValue(80)
                                                    .currentValue(25)
                                                    .unit("%")
                                                    .gradientStops(new Stop(0.0, Color.RED),
                                                                   new Stop(0.5, Color.YELLOW),
                                                                   new Stop(0.75, Color.GREEN),
                                                                   new Stop(1.0, Color.LIME))
                                                    /*.symbolPath(1, 0.71428571, "M 11.7829 11.7647 L 9.3333 20 L 17.5 8.2353 L 12.7171 " +
                                                                               "8.2353 L 15.1667 0 L 7 11.7647 L 11.7829 11.7647 ZM 1.1667 " +
                                                                               "17.6471 L 8.8138 17.6471 L 9.5156 15.2941 L 2.3333 15.2941 " +
                                                                               "L 2.3333 4.7059 L 10.4749 4.7059 L 12.1087 2.3529 L 1.1667 " +
                                                                               "2.3529 C 0.5218 2.3529 0 2.8791 0 3.5294 L 0 16.4706 C 0 " +
                                                                               "17.1209 0.5218 17.6471 1.1667 17.6471 ZM 26.8333 5.8824 L " +
                                                                               "24.5 5.8824 L 24.5 3.5294 C 24.5 2.8791 23.9782 2.3529 23.3333" +
                                                                               " 2.3529 L 15.6839 2.3529 L 14.9844 4.7059 L 22.1667 4.7059 " +
                                                                               "L 22.1667 15.2941 L 14.0228 15.2941 L 12.3913 17.6471 " +
                                                                               "L 23.3333 17.6471 C 23.9782 17.6471 24.5 17.1209 24.5 16.4706 " +
                                                                               "L 24.5 14.1176 L 26.8333 14.1176 C 27.4782 14.1176 28 13.5915 " +
                                                                               "28 12.9412 L 28 7.0588 C 28 6.4085 27.4782 5.8824 26.8333 5.8824 Z")
                                                    */
                                                    //.symbolColor(Color.CRIMSON)
                                                    .iconColor(Color.WHITE)
                                                    //.textColor(Color.MAGENTA)
                                                    //.color(Color.RED)
                                                    .onTargetSet(new EventHandler() {
                                                        @Override
                                                        public void handle(Event e) {
                                                            System.out.println("New target set to " + feedbackRegulator.getTargetValue());
                                                        }
                                                    })
                                                    .onAdjusted(new EventHandler() {
                                                        @Override
                                                        public void handle(Event e) {
                                                            System.out.println("Battery charge is " + feedbackRegulator.getCurrentValue() + "%");
                                                        }
                                                    })
                                                    .build();

        colorRegulator = ColorRegulatorBuilder.create()
                                              .prefSize(400, 400)
                                              //.textColor(Color.YELLOW)
                                              //.color(Color.PURPLE)
                                              .brightness(0.5)
                                              .onButtonOnPressed(new EventHandler() {
                                                  @Override
                                                  public void handle(Event e) {
                                                      System.out.println("Light ON");
                                                  }
                                              })
                                              .onButtonOffPressed(new EventHandler() {
                                                  @Override
                                                  public void handle(Event e) {
                                                      System.out.println("Light OFF");
                                                  }
                                              })
                                              .build();

        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTimerCall + 1000000000l) {
                    double currentValue = feedbackRegulator.getCurrentValue();
                    double targetValue  = feedbackRegulator.getTargetValue();
                    if ((int) currentValue != (int) targetValue) {
                        if (currentValue < targetValue) {
                            feedbackRegulator.setCurrentValue(currentValue+1);
                        } else if (currentValue > targetValue) {
                            feedbackRegulator.setCurrentValue(currentValue-1);
                        }
                    }
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        int cnt=0;
        HBox pane = new HBox(regulator, feedbackRegulator, colorRegulator);
        System.out.println("here:"+cnt++);
        pane.setSpacing(20);
        System.out.println("here:"+cnt++);
        pane.setPadding(new Insets(10));
        System.out.println("here:"+cnt++);
        pane.setBackground(new Background(new BackgroundFill(Color.rgb(66,71,79), CornerRadii.EMPTY, Insets.EMPTY)));
        System.out.println("here:"+cnt++);
        Scene scene = new Scene(pane);
        System.out.println("here:"+cnt++);
        stage.setScene(scene);
        System.out.println("here:"+cnt++);
        stage.show();
        System.out.println("here:"+cnt++);
        timer.start();
        System.out.println("here:"+cnt++);
    }

    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
