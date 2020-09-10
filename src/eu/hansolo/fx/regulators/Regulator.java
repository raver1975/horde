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

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


public class Regulator extends Region implements RegulatorControl {
    //    private static final Color          DEFAULT_COLOR    = Color.rgb(66,71,79);
    private static final Color DEFAULT_COLOR = Color.rgb(66, 71, 79);
    private static final double PREFERRED_WIDTH = 30;
    private static final double PREFERRED_HEIGHT = 30;
    private static final double MINIMUM_WIDTH = 30;
    private static final double MINIMUM_HEIGHT = 30;
    private static final double MAXIMUM_WIDTH = 1024;
    private static final double MAXIMUM_HEIGHT = 1024;
    private static final double BAR_START_ANGLE = -130;
    private static final double ANGLE_RANGE = 360;
    private final RegulatorEvent TARGET_SET_EVENT = new RegulatorEvent(RegulatorEvent.TARGET_SET);
    private double size;
    //    private Arc barArc;
    private Shape ring;
    private Circle mainCircle;
    private Text text;
    private Circle indicator;
    private Region symbol;
    private Pane pane;
    private DropShadow dropShadow;
    private InnerShadow highlight;
    private InnerShadow innerShadow;
    private DropShadow indicatorGlow;
    private InnerShadow indicatorInnerShadow;
    private InnerShadow indicatorHighlight;
    private Rotate indicatorRotate;
    private double scaleFactor;
    private DoubleProperty minValue;
    private DoubleProperty maxValue;
    private DoubleProperty targetValue;
    private IntegerProperty decimals;
    private StringProperty unit;
    private ObjectProperty<Color> symbolColor;
    private ObjectProperty<Color> iconColor;
    private ObjectProperty<Color> textColor;
    //    private ObjectProperty<Color> barColor;
    private ObjectProperty<Color> color;
    private ObjectProperty<Color> indicatorColor;
    private BooleanProperty selected;
    private BooleanProperty knobRotateLock;
    private String formatString;
    private double angleStep;
    private GradientLookup gradientLookup;


    // ******************** Constructors **************************************
    public Regulator() {
//        try {
//            getStylesheets().add(Regulator.class.getResource("regulator.css").toExternalForm());
//        }
//        catch(Exception e){
//            try {
//                getStylesheets().add(new File("C:/Users/paulk/Documents/horde/data/regulator.css").toURI().toURL().toExternalForm());
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
        Stop[] stops = {new Stop(0.0, Color.rgb(0, 0, 0)),
                new Stop(0.125, Color.rgb(0, 0, 255)),
                new Stop(0.25, Color.rgb(0, 255, 255)),
                new Stop(0.375, Color.rgb(0, 255, 0)),
                new Stop(0.5, Color.rgb(255, 255, 0)),
                new Stop(0.625, Color.rgb(255, 127, 0)),
                new Stop(0.75, Color.rgb(255, 0, 0)),
                new Stop(0.875, Color.rgb(255, 0, 255)),
                new Stop(1.0, Color.rgb(255, 255, 255))};

        List<Stop> reorderedStops = reorderStops(stops);

        gradientLookup = new GradientLookup(stops);

        scaleFactor = 1.0;

        knobRotateLock=new BooleanPropertyBase() {
            @Override
            public Object getBean() {
                return Regulator.this;
            }

            @Override
            public String getName() {
                return "knobRotateLock";
            }
        };

        minValue = new DoublePropertyBase(0) {
            @Override
            public void set(final double VALUE) {
                super.set(clamp(-Double.MAX_VALUE, maxValue.get(), VALUE));
                angleStep = ANGLE_RANGE / (maxValue.get() - minValue.get());
            }

            @Override
            public Object getBean() {
                return Regulator.this;
            }

            @Override
            public String getName() {
                return "minValue";
            }
        };
        maxValue = new DoublePropertyBase(100) {
            @Override
            public void set(final double VALUE) {
                super.set(clamp(minValue.get(), Double.MAX_VALUE, VALUE));
                angleStep = ANGLE_RANGE / (maxValue.get() - minValue.get());
            }

            @Override
            public Object getBean() {
                return Regulator.this;
            }

            @Override
            public String getName() {
                return "maxValue";
            }
        };
        targetValue = new DoublePropertyBase(0) {
            @Override
            public void set(final double VALUE) {
                super.set(clamp(minValue.get(), maxValue.get(), VALUE));
            }

            @Override
            public Object getBean() {
                return Regulator.this;
            }

            @Override
            public String getName() {
                return "targetValue";
            }
        };
        decimals = new IntegerPropertyBase(1) {
            @Override
            public void set(final int VALUE) {
                super.set(clamp(0, 2, VALUE));
                formatString = new StringBuilder("%.").append(Integer.toString(decimals.get())).append("f").append(getUnit()).toString();
                redraw();
            }

            @Override
            public Object getBean() {
                return Regulator.this;
            }

            @Override
            public String getName() {
                return "decimals";
            }
        };
        unit = new StringPropertyBase("") {
            @Override
            public void set(final String VALUE) {
                super.set(VALUE.equals("%") ? "%%" : VALUE);
                formatString = new StringBuilder("%.").append(Integer.toString(decimals.get())).append("f").append(get()).toString();
                redraw();
            }

            @Override
            public Object getBean() {
                return Regulator.this;
            }

            @Override
            public String getName() {
                return "unit";
            }
        };
        symbolColor = new ObjectPropertyBase<Color>(Color.TRANSPARENT) {
            @Override
            protected void invalidated() {
                set(null == get() ? Color.WHITE : get());
                redraw();
            }

            @Override
            public Object getBean() {
                return Regulator.this;
            }

            @Override
            public String getName() {
                return "symbolColor";
            }
        };
        iconColor = new ObjectPropertyBase<Color>(Color.TRANSPARENT) {
            @Override
            protected void invalidated() {
                set(null == get() ? Color.WHITE : get());
                redraw();
            }

            @Override
            public Object getBean() {
                return Regulator.this;
            }

            @Override
            public String getName() {
                return "iconColor";
            }
        };
        textColor = new ObjectPropertyBase<Color>(Color.WHITE) {
            @Override
            protected void invalidated() {
                set(null == get() ? Color.WHITE : get());
                redraw();
            }

            @Override
            public Object getBean() {
                return Regulator.this;
            }

            @Override
            public String getName() {
                return "textColor";
            }
        };
        color = new ObjectPropertyBase<Color>(DEFAULT_COLOR) {
            @Override
            protected void invalidated() {
                super.set(null == get() ? DEFAULT_COLOR : get());
                redraw();
            }

            @Override
            public Object getBean() {
                return Regulator.this;
            }

            @Override
            public String getName() {
                return "color";
            }
        };
        indicatorColor = new ObjectPropertyBase<Color>(Color.WHITE) {
            @Override
            protected void invalidated() {
                indicatorGlow.setColor(get());
            }

            @Override
            public Object getBean() {
                return Regulator.this;
            }

            @Override
            public String getName() {
                return "indicatorColor";
            }
        };
        selected = new BooleanPropertyBase(false) {
            @Override
            protected void invalidated() {
                if (get()) {
                    indicator.setFill(getIndicatorColor());
                    indicator.setStroke(getIndicatorColor().darker().darker());
                    indicator.setEffect(indicatorGlow);
                } else {
                    indicator.setFill(getColor().darker());
                    indicator.setStroke(getColor().darker().darker());
                    indicator.setEffect(null);
                }
            }

            @Override
            public Object getBean() {
                return Regulator.this;
            }

            @Override
            public String getName() {
                return "selected";
            }
        };
        formatString = new StringBuilder("%.").append(Integer.toString(decimals.get())).append("f").append(unit.get()).toString();
        angleStep = ANGLE_RANGE / (maxValue.get() - minValue.get());
        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 ||
                Double.compare(getWidth(), 0.0) <= 0 || Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }
        if (Double.compare(getMinWidth(), 0.0) <= 0 || Double.compare(getMinHeight(), 0.0) <= 0) {
            setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }
        if (Double.compare(getMaxWidth(), 0.0) <= 0 || Double.compare(getMaxHeight(), 0.0) <= 0) {
            setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }
    }

    private void initGraphics() {
        dropShadow = new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), PREFERRED_WIDTH * 0.016, 0.0, 0, PREFERRED_WIDTH * 0.028);
        highlight = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(255, 255, 255, 0.2), PREFERRED_WIDTH * 0.008, 0.0, 0, PREFERRED_WIDTH * 0.008);
        innerShadow = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.2), PREFERRED_WIDTH * 0.008, 0.0, 0, -PREFERRED_WIDTH * 0.008);
        highlight.setInput(innerShadow);
        dropShadow.setInput(highlight);

//        barArc = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.46, PREFERRED_HEIGHT * 0.46, BAR_START_ANGLE, 0);
//        barArc.setType(ArcType.OPEN);
//        barArc.setStrokeLineCap(StrokeLineCap.ROUND);
//        barArc.setFill(null);
//        barArc.setStroke(barGradient.getImagePattern(new Rectangle(0, 0, PREFERRED_WIDTH, PREFERRED_HEIGHT)));

        double center = PREFERRED_WIDTH * 0.5;
        ring = Shape.subtract(new Circle(center, center, PREFERRED_WIDTH * 0.42),
                new Circle(center, center, PREFERRED_WIDTH * 0.3));
        ring.setFill(color.get());
        ring.setEffect(dropShadow);

        mainCircle = new Circle();
        mainCircle.setFill(color.get().darker().darker());

        text = new Text(String.format(Locale.US, formatString, getTargetValue()));
        text.setFill(Color.WHITE);
        text.setTextOrigin(VPos.CENTER);

        indicatorRotate = new Rotate(-ANGLE_RANGE * 0.5, center, center);

        indicatorGlow = new DropShadow(BlurType.TWO_PASS_BOX, getIndicatorColor(), PREFERRED_WIDTH * 0.02, 0.0, 0, 0);
        indicatorInnerShadow = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.5), PREFERRED_WIDTH * 0.008, 0.0, 0, PREFERRED_WIDTH * 0.008);
        indicatorHighlight = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(255, 255, 255, 0.35), PREFERRED_WIDTH * 0.008, 0.0, 0, -PREFERRED_WIDTH * 0.008);
        indicatorHighlight.setInput(indicatorInnerShadow);

        indicator = new Circle();
        indicator.setFill(Color.WHITE);
        indicator.setStroke(Color.BLACK);
        indicator.setStrokeWidth(2);
        indicator.setMouseTransparent(true);
        indicator.getTransforms().add(indicatorRotate);

        Group indicatorGroup = new Group(indicator);
        indicatorGroup.setEffect(indicatorHighlight);

        symbol = new Region();
        symbol.getStyleClass().setAll("symbol");
        symbol.setCacheHint(CacheHint.SPEED);

        pane = new Pane(ring, mainCircle, text, indicatorGroup);
        pane.setPrefSize(PREFERRED_HEIGHT, PREFERRED_HEIGHT);
        pane.setBackground(new Background(new BackgroundFill(color.get().darker(), new CornerRadii(1024), Insets.EMPTY)));
        pane.setEffect(highlight);
        pane.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (Regulator.this.isDisabled()) return;
                Regulator.this.touchRotate(e.getSceneX(), e.getSceneY());
            }
        });
        pane.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (Regulator.this.isDisabled()) return;
                Regulator.this.touchRotate(e.getSceneX(), e.getSceneY());
            }
        });
        pane.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (Regulator.this.isDisabled()) return;
                Regulator.this.fireEvent(TARGET_SET_EVENT);
            }
        });

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                Regulator.this.resize();
            }
        });
        heightProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                Regulator.this.resize();
            }
        });
        disabledProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                Regulator.this.setOpacity(Regulator.this.isDisabled() ? 0.4 : 1.0);
            }
        });
        targetValueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                Regulator.this.rotate(targetValue.get());
            }
        });
        ring.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (Regulator.this.isDisabled()) return;
                Regulator.this.touchRotate(e.getSceneX(), e.getSceneY());
            }
        });
        ring.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (Regulator.this.isDisabled()) return;
                Regulator.this.touchRotate(e.getSceneX(), e.getSceneY());
            }
        });
        ring.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (Regulator.this.isDisabled()) return;
                Regulator.this.fireEvent(TARGET_SET_EVENT);
            }
        });
    }


    // ******************** Methods *******************************************
    public double getMinValue() {
        return minValue.get();
    }

    public void setMinValue(final double VALUE) {
        minValue.set(VALUE);
    }

    public DoubleProperty minValueProperty() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue.get();
    }

    public void setMaxValue(final double VALUE) {
        maxValue.set(VALUE);
    }

    public DoubleProperty maxValueProperty() {
        return maxValue;
    }

    @Override
    public double getTargetValue() {
        return targetValue.get();
    }

    @Override
    public void setTargetValue(final double VALUE) {
        targetValue.set(VALUE);
    }

    @Override
    public DoubleProperty targetValueProperty() {
        return targetValue;
    }

    public int getDecimals() {
        return decimals.get();
    }

    public void setDecimals(final int VALUE) {
        decimals.set(VALUE);
    }

    public IntegerProperty decimalsProperty() {
        return decimals;
    }

    public String getUnit() {
        return unit.get();
    }

    public void setUnit(final String UNIT) {
        unit.set(UNIT);
    }

    public StringProperty unitProperty() {
        return unit;
    }

    public Color getSymbolColor() {
        return symbolColor.get();
    }

    public void setSymbolColor(final Color COLOR) {
        symbolColor.set(COLOR);
    }

    public ObjectProperty<Color> symbolColorProperty() {
        return symbolColor;
    }

    public Color getIconColor() {
        return iconColor.get();
    }

    public void setIconColor(final Color COLOR) {
        iconColor.set(COLOR);
    }

    public ObjectProperty<Color> iconColorProperty() {
        return iconColor;
    }

    @Override
    public Color getTextColor() {
        return textColor.get();
    }

    @Override
    public void setTextColor(final Color COLOR) {
        textColor.set(COLOR);
    }

    @Override
    public ObjectProperty<Color> textColorProperty() {
        return textColor;
    }

    @Override
    public Color getColor() {
        return color.get();
    }

    @Override
    public void setColor(final Color COLOR) {
        color.set(COLOR);
    }

    @Override
    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    @Override
    public Color getIndicatorColor() {
        return indicatorColor.get();
    }

    @Override
    public void setIndicatorColor(final Color COLOR) {
        indicatorColor.set(COLOR);
    }

    @Override
    public ObjectProperty<Color> indicatorColorProperty() {
        return indicatorColor;
    }

    @Override
    public boolean isSelected() {
        return selected.get();
    }

    @Override
    public void setSelected(final boolean SELECTED) {
        selected.set(SELECTED);
    }

    @Override
    public BooleanProperty selectedProperty() {
        return selected;
    }


    public boolean isKnobRotateLock() {
        return knobRotateLock.get();
    }

    public void setKnobRotateLock(final boolean knobRotate) {
        knobRotateLock.set(knobRotate);
    }

    public BooleanProperty KnobRotateLockProperty() {
        return knobRotateLock;
    }

    public void setSymbolPath(final double SCALE_X, final double SCALE_Y, final String PATH) {
        if (PATH.isEmpty()) {
            symbol.setVisible(false);
        } else {
            symbol.setStyle(new StringBuilder().append("-fx-scale-x:").append(clamp(0.0, 1.0, SCALE_X)).append(";")
                    .append("-fx-scale-y:").append(clamp(0.0, 1.0, SCALE_Y)).append(";")
                    .append("-fx-shape:\"").append(PATH).append("\";")
                    .toString());
            symbol.setVisible(true);
        }
        symbol.setCache(false);
        resize();
        symbol.setCache(true);
    }

    private <T extends Number> T clamp(final T MIN, final T MAX, final T VALUE) {
        if (VALUE.doubleValue() < MIN.doubleValue()) return MIN;
        if (VALUE.doubleValue() > MAX.doubleValue()) return MAX;
        return VALUE;
    }

    private void adjustTextSize(final Text TEXT, final double MAX_WIDTH, double fontSize) {
        final String FONT_NAME = TEXT.getFont().getName();
        while (TEXT.getLayoutBounds().getWidth() > MAX_WIDTH && fontSize > 0) {
            fontSize -= 0.005;
            TEXT.setFont(new Font(FONT_NAME, fontSize));
        }
    }

    private void touchRotate(final double X, final double Y) {
        Point2D p = sceneToLocal(X, Y);
        double deltaX = p.getX() - (pane.getLayoutX() + size * 0.5);
        double deltaY = p.getY() - (pane.getLayoutY() + size * 0.5);
        double radius = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
        double nx = deltaX / radius;
        double ny = deltaY / radius;
        double theta = Math.atan2(ny, nx);
        theta = Double.compare(theta, 0.0) >= 0 ? Math.toDegrees(theta) : Math.toDegrees((theta)) + 360.0;
        double angle = (theta + 270) % 360;
        double oldval = getTargetValue();
        double newval = angle / angleStep + minValue.get();
        double per = Math.abs(newval - oldval) / (getMaxValue() - getMinValue());
        if (!knobRotateLock.getValue() || per < 0.25d) {
            setTargetValue(newval);
        }
    }


    // ******************** Resizing ******************************************
    private void rotate(final double VALUE) {
        drawBar(VALUE);
        indicatorRotate.setAngle((VALUE - minValue.get()) * angleStep - ANGLE_RANGE * 0.5);
        text.setText(String.format(Locale.US, formatString, VALUE));
//        adjustTextSize(text, size * 0.48, size * 0.216);
        text.setLayoutX((size - text.getLayoutBounds().getWidth()) * 0.5);
    }

    private void drawBar(final double VALUE) {
//        barArc.setLength(-(VALUE - minValue.get()) * angleStep);
        mainCircle.setFill(gradientLookup.getColorAt(getTargetValue() / getMaxValue()).darker().darker().darker());
        ring.setFill(gradientLookup.getColorAt(getTargetValue() / getMaxValue()).darker());
        ring.setStroke(gradientLookup.getColorAt(getTargetValue() / getMaxValue()).darker());

//        indicator.setStroke(gradientLookup.getColorAt(getTargetValue()/127d));
    }

    private void resize() {
        double width = getWidth() - getInsets().getLeft() - getInsets().getRight();
        double height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.setPrefSize(size, size);
            pane.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);

//            barArc.setCenterX(size * 0.5);
//            barArc.setCenterY(size * 0.5);
//            barArc.setRadiusX(size * 0.46);
//            barArc.setRadiusY(size * 0.46);
//            barArc.setStrokeWidth(size * 0.04);
            drawBar(targetValue.get());

            double shadowRadius = clamp(1.0, 2.0, size * 0.004);
            dropShadow.setRadius(shadowRadius);
            dropShadow.setOffsetY(shadowRadius);
            highlight.setRadius(shadowRadius);
            highlight.setOffsetY(shadowRadius);
            innerShadow.setRadius(shadowRadius);
            innerShadow.setOffsetY(-shadowRadius);

            double center = size * 0.5;
            scaleFactor = size / PREFERRED_WIDTH;
            ring.setCache(false);
            ring.getTransforms().setAll(new Scale(scaleFactor, scaleFactor, 0, 0));
            ring.setCache(true);
            ring.setCacheHint(CacheHint.SPEED);

            mainCircle.setCache(false);
            mainCircle.setRadius(size * 0.3);
            mainCircle.setCenterX(center);
            mainCircle.setCenterY(center);
            mainCircle.setCache(true);
            mainCircle.setCacheHint(CacheHint.SPEED);


//            text.setFont(Fonts.robotoMedium(size * 0.216));
            text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.25);

            indicatorGlow.setRadius(size * 0.02);
            indicatorInnerShadow.setRadius(size * 0.008);
            indicatorInnerShadow.setOffsetY(size * 0.006);
            indicatorHighlight.setRadius(size * 0.008);
            indicatorHighlight.setOffsetY(-size * 0.004);

            indicator.setRadius(size * 0.1);
            indicator.setCenterX(center);
            indicator.setCenterY(size * 0.05);

            indicatorRotate.setPivotX(center);
            indicatorRotate.setPivotY(center);

            redraw();
        }
    }

    private void redraw() {
        ring.setFill(color.get());
//        indicator.setStroke(isSelected() ? indicatorColor.get().darker().darker() : color.get().darker().darker());
        symbol.setBackground(new Background(new BackgroundFill(symbolColor.get(), CornerRadii.EMPTY, Insets.EMPTY)));
        text.setFill(textColor.get());
//        barArc.setStroke(barGradient.getImagePattern(new Rectangle(0, 0, PREFERRED_WIDTH, PREFERRED_HEIGHT)));
        rotate(targetValue.get());
    }

    private List<Stop> reorderStops(final Stop... STOPS) {
        return reorderStops(Arrays.asList(STOPS));
    }

    private List<Stop> reorderStops(final List<Stop> STOPS) {
        /*
        0.0 -> 0.611
        0.5 -> 0.0 & 1.0
        1.0 -> 0.389
         */
        double range = 0.778;
        double halfRange = range * 0.5;

        Map<Double, Color> stopMap = new HashMap<Double, Color>();
        for (Stop stop : STOPS) {
            stopMap.put(stop.getOffset(), stop.getColor());
        }

        List<Stop> sortedStops = new ArrayList<Stop>(STOPS.size());
        SortedSet<Double> sortedFractions = new TreeSet<Double>(stopMap.keySet());
        if (sortedFractions.last() < 1) {
            stopMap.put(1.0, stopMap.get(sortedFractions.last()));
            sortedFractions.add(1.0);
        }
        if (sortedFractions.first() > 0) {
            stopMap.put(0.0, stopMap.get(sortedFractions.first()));
            sortedFractions.add(0.0);
        }
        for (double fraction : sortedFractions) {
            double offset = fraction * range - halfRange;
            offset = offset < 0 ? 1.0 + offset : offset;
            sortedStops.add(new Stop(offset, stopMap.get(fraction)));
        }
        return sortedStops;
    }


    // ******************** Event Handling ************************************
    public void setOnTargetSet(final EventHandler<RegulatorEvent> HANDLER) {
        addEventHandler(RegulatorEvent.TARGET_SET, HANDLER);
    }

    public void removeOnTargetSet(final EventHandler<RegulatorEvent> HANDLER) {
        removeEventHandler(RegulatorEvent.TARGET_SET, HANDLER);
    }
}
