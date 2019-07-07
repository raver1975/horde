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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Created by hansolo on 01.03.16.
 */
public class FeedbackRegulator extends Region implements RegulatorControl {
    private static final Color          DEFAULT_COLOR    = Color.rgb(66,71,79);
    private static final double         PREFERRED_WIDTH  = 250;
    private static final double         PREFERRED_HEIGHT = 250;
    private static final double         MINIMUM_WIDTH    = 50;
    private static final double         MINIMUM_HEIGHT   = 50;
    private static final double         MAXIMUM_WIDTH    = 1024;
    private static final double         MAXIMUM_HEIGHT   = 1024;
    private              double         BAR_START_ANGLE  = -130;
    private              double         ANGLE_RANGE      = 280;
    private final        RegulatorEvent ADJUSTING_EVENT  = new RegulatorEvent(RegulatorEvent.ADJUSTING);
    private final        RegulatorEvent ADJUSTED_EVENT   = new RegulatorEvent(RegulatorEvent.ADJUSTED);
    private final        RegulatorEvent TARGET_SET_EVENT = new RegulatorEvent(RegulatorEvent.TARGET_SET);
    private double                      size;
    private Arc                         barArc;
    private Arc                         overlayBarArc;
    private Shape                       ring;
    private Circle                      mainCircle;
    private Text                        text;
    private Text                        targetText;
    private Circle                      indicator;
    private Region                      symbol;
    private Pane                        pane;
    private DropShadow                  dropShadow;
    private InnerShadow                 highlight;
    private InnerShadow                 innerShadow;
    private DropShadow                  indicatorGlow;
    private InnerShadow                 indicatorInnerShadow;
    private InnerShadow                 indicatorHighlight;
    private Rotate                      indicatorRotate;
    private double                      scaleFactor;
    private DoubleProperty              minValue;
    private DoubleProperty              maxValue;
    private DoubleProperty              targetValue;
    private DoubleProperty              currentValue;
    private IntegerProperty             decimals;
    private StringProperty              unit;
    private ObjectProperty<Color>       symbolColor;
    private ObjectProperty<Color>       iconColor;
    private ObjectProperty<Color>       textColor;
    private ObjectProperty<Color>       color;
    private ObjectProperty<Color>       indicatorColor;
    private BooleanProperty             selected;
    private String                      formatString;
    private double                      angleStep;
    private ConicalGradient             barGradient;


    // ******************** Constructors **************************************
    public FeedbackRegulator() {
        try {
            getStylesheets().add(FeedbackRegulator.class.getResource("feedback_regulator.css").toExternalForm());
        }
        catch(Exception e){
            try {
                getStylesheets().add(new File("C:/Users/paulk/Documents/horde/data/feedback_regulator.css").toURI().toURL().toExternalForm());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        scaleFactor    = 1.0;
        minValue       = new DoublePropertyBase(0) {
            @Override public void set(final double VALUE) {
                super.set(clamp(-Double.MAX_VALUE, maxValue.get(), VALUE));
                angleStep = ANGLE_RANGE / (maxValue.get() - minValue.get());
            }
            @Override public Object getBean() { return FeedbackRegulator.this; }
            @Override public String getName() { return "minValue"; }
        };
        maxValue       = new DoublePropertyBase(40) {
            @Override public void set(final double VALUE) {
                super.set(clamp(minValue.get(), Double.MAX_VALUE, VALUE));
                angleStep = ANGLE_RANGE / (maxValue.get() - minValue.get());
            }
            @Override public Object getBean() { return FeedbackRegulator.this; }
            @Override public String getName() { return "maxValue"; }
        };
        targetValue    = new DoublePropertyBase(0) {
            @Override public void set(final double VALUE) {
                super.set(clamp(minValue.get(), maxValue.get(), VALUE));
                if ((int) get() == (int) currentValue.get()) {
                    targetText.setVisible(false);
                    overlayBarArc.setVisible(false);
                } else {
                    targetText.setVisible(true);
                    overlayBarArc.setVisible(true);
                }
            }
            @Override public Object getBean() { return FeedbackRegulator.this; }
            @Override public String getName() { return "targetValue"; }
        };
        currentValue   = new DoublePropertyBase(0) {
            @Override public void set(final double VALUE) {
                super.set(clamp(minValue.get(), maxValue.get(), VALUE));
                if ((int) targetValue.get() == (int) get()) {
                    fireEvent(ADJUSTED_EVENT);
                    targetText.setVisible(false);
                    overlayBarArc.setVisible(false);
                } else {
                    fireEvent(ADJUSTING_EVENT);
                    targetText.setVisible(true);
                    overlayBarArc.setVisible(true);
                }
                setText(get());
                drawOverlayBar(get());
                redraw();
            }
            @Override public Object getBean() { return FeedbackRegulator.this; }
            @Override public String getName() { return "currentValue"; }
        };
        decimals       = new IntegerPropertyBase(0) {
            @Override public void set(final int VALUE) {
                super.set(clamp(0, 2, VALUE));
                formatString = new StringBuilder("%.").append(Integer.toString(decimals.get())).append("f").append(getUnit()).toString();
                redraw();
            }
            @Override public Object getBean() { return FeedbackRegulator.this; }
            @Override public String getName() { return "decimals"; }
        };
        unit           = new StringPropertyBase("\u00B0") {
            @Override public void set(final String VALUE) {
                super.set(VALUE.equals("%") ? "%%" : VALUE);
                formatString = new StringBuilder("%.").append(Integer.toString(decimals.get())).append("f").append(get()).toString();
                redraw();
            }
            @Override public Object getBean() { return FeedbackRegulator.this; }
            @Override public String getName() { return "unit"; }
        };
        symbolColor    = new ObjectPropertyBase<Color>(Color.TRANSPARENT) {
            @Override protected void invalidated() {
                set(null == get() ? Color.WHITE : get());
                redraw();
            }
            @Override public Object getBean() { return FeedbackRegulator.this; }
            @Override public String getName() { return "symbolColor"; }
        };
        iconColor      = new ObjectPropertyBase<Color>(Color.TRANSPARENT) {
            @Override protected void invalidated() {
                set(null == get() ? Color.WHITE : get());
                redraw();
            }
            @Override public Object getBean() { return FeedbackRegulator.this; }
            @Override public String getName() { return "iconColor"; }
        };
        textColor      = new ObjectPropertyBase<Color>(Color.WHITE) {
            @Override protected void invalidated() {
                set(null == get() ? Color.WHITE : get());
                redraw();
            }
            @Override public Object getBean() { return FeedbackRegulator.this; }
            @Override public String getName() { return "textColor"; }
        };
        color          = new ObjectPropertyBase<Color>(DEFAULT_COLOR) {
            @Override protected void invalidated() {
                super.set(null == get() ? DEFAULT_COLOR : get());
                redraw();
            }
            @Override public Object getBean() { return FeedbackRegulator.this; }
            @Override public String getName() { return "color"; }
        };
        indicatorColor = new ObjectPropertyBase<Color>(Color.WHITE) {
            @Override protected void invalidated() { indicatorGlow.setColor(get()); }
            @Override public Object getBean() { return FeedbackRegulator.this; }
            @Override public String getName() { return "indicatorColor"; }
        };
        selected       = new BooleanPropertyBase(false) {
            @Override protected void invalidated() {
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
            @Override public Object getBean() { return FeedbackRegulator.this; }
            @Override public String getName() { return "selected"; }
        };
        formatString   = new StringBuilder("%.").append(Integer.toString(decimals.get())).append("f").append(unit.get()).toString();
        angleStep      = ANGLE_RANGE / (maxValue.get() - minValue.get());
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
        dropShadow  = new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), PREFERRED_WIDTH * 0.016, 0.0, 0, PREFERRED_WIDTH * 0.028);
        highlight   = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(255, 255, 255, 0.2), PREFERRED_WIDTH * 0.008, 0.0, 0, PREFERRED_WIDTH * 0.008);
        innerShadow = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.2), PREFERRED_WIDTH * 0.008, 0.0, 0, -PREFERRED_WIDTH * 0.008);
        highlight.setInput(innerShadow);
        dropShadow.setInput(highlight);

        Stop[] stops = {
            new Stop(0.0, Color.rgb(135, 255, 190)),
            new Stop(0.125, Color.rgb(254, 190, 106)),
            new Stop(0.389, Color.rgb(252, 84, 68)),
            new Stop(0.611, Color.rgb(99, 195, 255)),
            new Stop(1.0, Color.rgb(125, 255, 190))
        };

        barGradient = new ConicalGradient(stops);

        barArc = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.46, PREFERRED_HEIGHT * 0.46, BAR_START_ANGLE, 0);
        barArc.setType(ArcType.OPEN);
        barArc.setStrokeLineCap(StrokeLineCap.ROUND);
        barArc.setFill(null);
        barArc.setStroke(barGradient.getImagePattern(new Rectangle(0, 0, PREFERRED_WIDTH, PREFERRED_HEIGHT)));

        overlayBarArc = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.46, PREFERRED_HEIGHT * 0.46, BAR_START_ANGLE, 0);
        overlayBarArc.setType(ArcType.OPEN);
        overlayBarArc.setStrokeLineCap(StrokeLineCap.ROUND);
        overlayBarArc.setFill(null);
        overlayBarArc.setStroke(Color.rgb(0, 0, 0, 0.3));
        overlayBarArc.setVisible((int) targetValue.get() != (int) currentValue.get());

        double center = PREFERRED_WIDTH * 0.5;
        ring = Shape.subtract(new Circle(center, center, PREFERRED_WIDTH * 0.42),
                              new Circle(center, center, PREFERRED_WIDTH * 0.3));
        ring.setFill(color.get());
        ring.setEffect(dropShadow);

        mainCircle = new Circle();
        mainCircle.setFill(color.get().darker().darker());

        text = new Text(String.format(Locale.US, formatString, currentValue.get()));
        text.setFill(textColor.get());
        text.setTextOrigin(VPos.CENTER);

        targetText = new Text(String.format(Locale.US, formatString, targetValue.get()));
        targetText.setFill(textColor.get().darker());
        targetText.setTextOrigin(VPos.CENTER);
        targetText.setVisible((int) targetValue.get() != (int) currentValue.get());

        indicatorRotate = new Rotate(-ANGLE_RANGE *  0.5, center, center);

        indicatorGlow        = new DropShadow(BlurType.TWO_PASS_BOX, getIndicatorColor(), PREFERRED_WIDTH * 0.02, 0.0, 0, 0);
        indicatorInnerShadow = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.5), PREFERRED_WIDTH * 0.008, 0.0, 0, PREFERRED_WIDTH * 0.008);
        indicatorHighlight   = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(255, 255, 255, 0.35), PREFERRED_WIDTH * 0.008, 0.0, 0, -PREFERRED_WIDTH * 0.008);
        indicatorHighlight.setInput(indicatorInnerShadow);

        indicator = new Circle();
        indicator.setFill(color.get().darker());
        indicator.setStroke(color.get().darker().darker());
        indicator.setMouseTransparent(true);
        indicator.getTransforms().add(indicatorRotate);

        Group indicatorGroup = new Group(indicator);
        indicatorGroup.setEffect(indicatorHighlight);

        symbol = new Region();
        symbol.getStyleClass().setAll("symbol");
        symbol.setCacheHint(CacheHint.SPEED);

        pane = new Pane(barArc, overlayBarArc, ring, mainCircle, text, targetText, indicatorGroup);
        pane.setPrefSize(PREFERRED_HEIGHT, PREFERRED_HEIGHT);
        pane.setBackground(new Background(new BackgroundFill(color.get().darker(), new CornerRadii(1024), Insets.EMPTY)));
        pane.setEffect(highlight);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                FeedbackRegulator.this.resize();
            }
        });
        heightProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                FeedbackRegulator.this.resize();
            }
        });
        disabledProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                FeedbackRegulator.this.setOpacity(FeedbackRegulator.this.isDisabled() ? 0.4 : 1.0);
            }
        });
        targetValueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                FeedbackRegulator.this.rotate(targetValue.get());
            }
        });
        currentValueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                FeedbackRegulator.this.setText(currentValue.get());
            }
        });
        ring.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (FeedbackRegulator.this.isDisabled()) return;
                FeedbackRegulator.this.touchRotate(e.getSceneX(), e.getSceneY());
            }
        });
        ring.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (FeedbackRegulator.this.isDisabled()) return;
                FeedbackRegulator.this.touchRotate(e.getSceneX(), e.getSceneY());
            }
        });
        ring.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (FeedbackRegulator.this.isDisabled()) return;
                FeedbackRegulator.this.fireEvent(TARGET_SET_EVENT);
            }
        });
    }


    // ******************** Methods *******************************************
    public double getMinValue() { return minValue.get(); }
    public void setMinValue(final double VALUE) { minValue.set(VALUE); }
    public DoubleProperty minValueProperty() { return minValue; }

    public double getMaxValue() { return maxValue.get(); }
    public void setMaxValue(final double VALUE) { maxValue.set(VALUE); }
    public DoubleProperty maxValueProperty() { return maxValue; }

    @Override public double getTargetValue() { return targetValue.get(); }
    @Override public void setTargetValue(final double VALUE) { targetValue.set(VALUE); }
    @Override public DoubleProperty targetValueProperty() { return targetValue; }

    public double getCurrentValue() { return currentValue.get(); }
    public void setCurrentValue(final double VALUE) { currentValue.set(VALUE); }
    public DoubleProperty currentValueProperty() { return currentValue; }

    public int getDecimals() { return decimals.get(); }
    public void setDecimals(final int VALUE) { decimals.set(VALUE); }
    public IntegerProperty decimalsProperty() { return decimals; }

    public String getUnit()  { return unit.get(); }
    public void setUnit(final String UNIT) { unit.set(UNIT); }
    public StringProperty unitProperty() { return unit; }

    public Color getSymbolColor() { return symbolColor.get(); }
    public void setSymbolColor(final Color COLOR) { symbolColor.set(COLOR); }
    public ObjectProperty<Color> symbolColorProperty() { return symbolColor; }

    public Color getIconColor() { return iconColor.get(); }
    public void setIconColor(final Color COLOR) { iconColor.set(COLOR); }
    public ObjectProperty<Color> iconColorProperty() { return iconColor; }

    @Override public Color getTextColor() { return textColor.get(); }
    @Override public void setTextColor(final Color COLOR) { textColor.set(COLOR); }
    @Override public ObjectProperty<Color> textColorProperty() { return textColor; }

    @Override public Color getColor() { return color.get(); }
    @Override public void setColor(final Color COLOR) { color.set(COLOR); }
    @Override public ObjectProperty<Color> colorProperty() { return color; }

    @Override public Color getIndicatorColor() { return indicatorColor.get(); }
    @Override public void setIndicatorColor(final Color COLOR) { indicatorColor.set(COLOR); }
    @Override public ObjectProperty<Color> indicatorColorProperty() { return indicatorColor; }

    @Override public boolean isSelected() { return selected.get(); }
    @Override public void setSelected(final boolean SELECTED) { selected.set(SELECTED); }
    @Override public BooleanProperty selectedProperty() { return selected; }

    public List<Stop> getGradientStops() { return barGradient.getStops(); }
    public void setGradientStops(final Stop... STOPS) { setGradientStops(Arrays.asList(STOPS)); }
    public void setGradientStops(final List<Stop> STOPS) {
        barGradient = new ConicalGradient(reorderStops(STOPS));
        barArc.setStroke(barGradient.getImagePattern(new Rectangle(0, 0, PREFERRED_WIDTH, PREFERRED_HEIGHT)));
    }

    private List<Stop> reorderStops(final Stop... STOPS) { return reorderStops(Arrays.asList(STOPS)); }
    private List<Stop> reorderStops(final List<Stop> STOPS) {
        /*
        0.0 -> 0.611
        0.5 -> 0.0 & 1.0
        1.0 -> 0.389
         */
        double range     = 0.778;
        double halfRange = range * 0.5;

        Map<Double, Color> stopMap = new HashMap<Double, Color>();
        for (Stop stop : STOPS) { stopMap.put(stop.getOffset(), stop.getColor()); }

        List<Stop>        sortedStops     = new ArrayList<Stop>(STOPS.size());
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
        Point2D p      = sceneToLocal(X, Y);
        double  deltaX = p.getX() - (pane.getLayoutX() + size * 0.5);
        double  deltaY = p.getY() - (pane.getLayoutY() + size * 0.5);
        double  radius = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
        double  nx     = deltaX / radius;
        double  ny     = deltaY / radius;
        double  theta  = Math.atan2(ny, nx);
        theta         = Double.compare(theta, 0.0) >= 0 ? Math.toDegrees(theta) : Math.toDegrees((theta)) + 360.0;
        double angle  = (theta + 230) % 360;
        if (angle > 320 && angle < 360) {
            angle = 0;
        } else if (angle <= 320 && angle > ANGLE_RANGE) {
            angle = ANGLE_RANGE;
        }
        setTargetValue(angle / angleStep + minValue.get());
    }


    // ******************** Resizing ******************************************
    private void rotate(final double VALUE) {
        indicatorRotate.setAngle((VALUE - minValue.get()) * angleStep - ANGLE_RANGE * 0.5);
        targetText.setText(String.format(Locale.US, formatString, VALUE));
        adjustTextSize(targetText, size * 0.24, size * 0.216);
        targetText.setLayoutX((size - targetText.getLayoutBounds().getWidth()) * 0.5);
    }

    private void setText(final double VALUE) {
        text.setText(String.format(Locale.US, formatString, VALUE));
        adjustTextSize(text, size * 0.48, size * 0.216);
        text.setLayoutX((size - text.getLayoutBounds().getWidth()) * 0.5);
    }

    private void drawBar(final double VALUE) {
        barArc.setLength(-(VALUE - minValue.get()) * angleStep);
    }

    private void drawOverlayBar(final double VALUE) {
        overlayBarArc.setLength(-(VALUE - minValue.get()) * angleStep);
    }

    private void resize() {
        double width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        double height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size   = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.setPrefSize(size, size);
            pane.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);

            barArc.setCache(false);
            barArc.setCenterX(size * 0.5);
            barArc.setCenterY(size * 0.5);
            barArc.setRadiusX(size * 0.46);
            barArc.setRadiusY(size * 0.46);
            barArc.setStrokeWidth(size * 0.04);
            barArc.setStroke(barGradient.getImagePattern(new Rectangle(0, 0, size, size)));
            drawBar(maxValue.get());
            barArc.setCache(true);
            barArc.setCacheHint(CacheHint.SPEED);

            overlayBarArc.setCenterX(size * 0.5);
            overlayBarArc.setCenterY(size * 0.5);
            overlayBarArc.setRadiusX(size * 0.46);
            overlayBarArc.setRadiusY(size * 0.46);
            overlayBarArc.setStrokeWidth(size * 0.03);
            drawOverlayBar(currentValue.get());

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
            mainCircle.setCenterX(center); mainCircle.setCenterY(center);
            mainCircle.setCache(true);
            mainCircle.setCacheHint(CacheHint.SPEED);

            text.setFont(Fonts.robotoMedium(size * 0.216));
            text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.33);

            targetText.setFont(Fonts.robotoLight(size * 0.082));
            targetText.relocate((size - targetText.getLayoutBounds().getWidth()) * 0.5, size * 0.23);

            indicatorGlow.setRadius(size * 0.02);
            indicatorInnerShadow.setRadius(size * 0.008);
            indicatorInnerShadow.setOffsetY(size * 0.006);
            indicatorHighlight.setRadius(size * 0.008);
            indicatorHighlight.setOffsetY(-size * 0.004);

            indicator.setRadius(size * 0.032);
            indicator.setCenterX(center);
            indicator.setCenterY(size * 0.148);

            indicatorRotate.setPivotX(center);
            indicatorRotate.setPivotY(center);
            redraw();
        }
    }

    private void redraw() {
        pane.setBackground(new Background(new BackgroundFill(color.get().darker(), new CornerRadii(1024), Insets.EMPTY)));
        mainCircle.setFill(color.get().darker().darker());
        ring.setFill(color.get());
        indicator.setFill(isSelected() ? indicatorColor.get() : color.get().darker());
        indicator.setStroke(isSelected() ? indicatorColor.get().darker().darker() : color.get().darker().darker());
        symbol.setBackground(new Background(new BackgroundFill(symbolColor.get(), CornerRadii.EMPTY, Insets.EMPTY)));
        targetText.setFill(textColor.get().darker());
        text.setFill(textColor.get());
        rotate(targetValue.get());
        setText(currentValue.get());
    }


    // ******************** Event Handling ************************************
    public void setOnTargetSet(final EventHandler<RegulatorEvent> HANDLER) { addEventHandler(RegulatorEvent.TARGET_SET, HANDLER); }
    public void removeOnTargetSet(final EventHandler<RegulatorEvent> HANDLER) { removeEventHandler(RegulatorEvent.TARGET_SET, HANDLER); }

    public void setOnAdjusting(final EventHandler<RegulatorEvent> HANDLER) { addEventHandler(RegulatorEvent.ADJUSTING, HANDLER); }
    public void removeOnAdjusting(final EventHandler<RegulatorEvent> HANDLER) { removeEventHandler(RegulatorEvent.ADJUSTING, HANDLER); }

    public void setOnAdjusted(final EventHandler<RegulatorEvent> HANDLER) { addEventHandler(RegulatorEvent.ADJUSTED, HANDLER); }
    public void removeOnAdjusted(final EventHandler<RegulatorEvent> HANDLER) { removeEventHandler(RegulatorEvent.ADJUSTED, HANDLER); }
}
