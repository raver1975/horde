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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;

import java.util.HashMap;


/**
 * Created by hansolo on 02.03.16.
 */
public class RegulatorBuilder<B extends RegulatorBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<String, Property>();


    // ******************** Constructors **************************************
    protected RegulatorBuilder() {}


    // ******************** Methods *******************************************
    public static final RegulatorBuilder create() {
        return new RegulatorBuilder();
    }

    public final B minValue(final double VALUE) {
        properties.put("minValue", new SimpleDoubleProperty(VALUE));
        return (B)this;
    }

    public final B maxValue(final double VALUE) {
        properties.put("maxValue", new SimpleDoubleProperty(VALUE));
        return (B)this;
    }

    public final B targetValue(final double VALUE) {
        properties.put("targetValue", new SimpleDoubleProperty(VALUE));
        return (B)this;
    }

    public final B unit(final String TEXT) {
        properties.put("unit", new SimpleStringProperty(TEXT));
        return (B)this;
    }

    public final B decimals(final int VALUE) {
        properties.put("decimals", new SimpleIntegerProperty(VALUE));
        return (B)this;
    }

    public final B symbolColor(final Color COLOR) {
        properties.put("symbolColor", new SimpleObjectProperty<Color>(COLOR));
        return (B)this;
    }

    public final B iconColor(final Color COLOR) {
        properties.put("iconColor", new SimpleObjectProperty<Color>(COLOR));
        return (B)this;
    }

    public final B barColor(final Color COLOR) {
        properties.put("barColor", new SimpleObjectProperty<Color>(COLOR));
        return (B)this;
    }

    public final B textColor(final Color COLOR) {
        properties.put("textColor", new SimpleObjectProperty<Color>(COLOR));
        return (B)this;
    }

    public final B color(final Color COLOR) {
        properties.put("color", new SimpleObjectProperty<Color>(COLOR));
        return (B)this;
    }

    public final B symbolPath(final double SCALE_X, final double SCALE_Y, final String PATH) {
        properties.put("symbolScaleX", new SimpleDoubleProperty(SCALE_X));
        properties.put("symbolScaleY", new SimpleDoubleProperty(SCALE_Y));
        properties.put("symbolPath", new SimpleStringProperty(PATH));
        return (B)this;
    }


    public final B onTargetSet(final EventHandler<RegulatorEvent> HANDLER) {
        properties.put("onTargetSet", new SimpleObjectProperty<EventHandler<RegulatorEvent>>(HANDLER));
        return (B)this;
    }

    public final B prefSize(final double WIDTH, final double HEIGHT) {
        properties.put("prefSize", new SimpleObjectProperty<Dimension2D>(new Dimension2D(WIDTH, HEIGHT)));
        return (B)this;
    }
    public final B minSize(final double WIDTH, final double HEIGHT) {
        properties.put("minSize", new SimpleObjectProperty<Dimension2D>(new Dimension2D(WIDTH, HEIGHT)));
        return (B)this;
    }
    public final B maxSize(final double WIDTH, final double HEIGHT) {
        properties.put("maxSize", new SimpleObjectProperty<Dimension2D>(new Dimension2D(WIDTH, HEIGHT)));
        return (B)this;
    }

    public final B prefWidth(final double PREF_WIDTH) {
        properties.put("prefWidth", new SimpleDoubleProperty(PREF_WIDTH));
        return (B)this;
    }
    public final B prefHeight(final double PREF_HEIGHT) {
        properties.put("prefHeight", new SimpleDoubleProperty(PREF_HEIGHT));
        return (B)this;
    }

    public final B minWidth(final double MIN_WIDTH) {
        properties.put("minWidth", new SimpleDoubleProperty(MIN_WIDTH));
        return (B)this;
    }
    public final B minHeight(final double MIN_HEIGHT) {
        properties.put("minHeight", new SimpleDoubleProperty(MIN_HEIGHT));
        return (B)this;
    }

    public final B maxWidth(final double MAX_WIDTH) {
        properties.put("maxWidth", new SimpleDoubleProperty(MAX_WIDTH));
        return (B)this;
    }
    public final B maxHeight(final double MAX_HEIGHT) {
        properties.put("maxHeight", new SimpleDoubleProperty(MAX_HEIGHT));
        return (B)this;
    }

    public final B scaleX(final double SCALE_X) {
        properties.put("scaleX", new SimpleDoubleProperty(SCALE_X));
        return (B)this;
    }
    public final B scaleY(final double SCALE_Y) {
        properties.put("scaleY", new SimpleDoubleProperty(SCALE_Y));
        return (B)this;
    }

    public final B layoutX(final double LAYOUT_X) {
        properties.put("layoutX", new SimpleDoubleProperty(LAYOUT_X));
        return (B)this;
    }
    public final B layoutY(final double LAYOUT_Y) {
        properties.put("layoutY", new SimpleDoubleProperty(LAYOUT_Y));
        return (B)this;
    }

    public final B translateX(final double TRANSLATE_X) {
        properties.put("translateX", new SimpleDoubleProperty(TRANSLATE_X));
        return (B)this;
    }
    public final B translateY(final double TRANSLATE_Y) {
        properties.put("translateY", new SimpleDoubleProperty(TRANSLATE_Y));
        return (B)this;
    }

    public final B padding(final Insets INSETS) {
        properties.put("padding", new SimpleObjectProperty<Insets>(INSETS));
        return (B)this;
    }

    public final Regulator build() {
        final Regulator CONTROL = new Regulator();
        for (String key : properties.keySet()) {
            if ("prefSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                CONTROL.setPrefSize(dim.getWidth(), dim.getHeight());
            } else if("minSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                CONTROL.setMinSize(dim.getWidth(), dim.getHeight());
            } else if("maxSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                CONTROL.setMaxSize(dim.getWidth(), dim.getHeight());
            } else if("prefWidth".equals(key)) {
                CONTROL.setPrefWidth(((DoubleProperty) properties.get(key)).get());
            } else if("prefHeight".equals(key)) {
                CONTROL.setPrefHeight(((DoubleProperty) properties.get(key)).get());
            } else if("minWidth".equals(key)) {
                CONTROL.setMinWidth(((DoubleProperty) properties.get(key)).get());
            } else if("minHeight".equals(key)) {
                CONTROL.setMinHeight(((DoubleProperty) properties.get(key)).get());
            } else if("maxWidth".equals(key)) {
                CONTROL.setMaxWidth(((DoubleProperty) properties.get(key)).get());
            } else if("maxHeight".equals(key)) {
                CONTROL.setMaxHeight(((DoubleProperty) properties.get(key)).get());
            } else if("scaleX".equals(key)) {
                CONTROL.setScaleX(((DoubleProperty) properties.get(key)).get());
            } else if("scaleY".equals(key)) {
                CONTROL.setScaleY(((DoubleProperty) properties.get(key)).get());
            } else if ("layoutX".equals(key)) {
                CONTROL.setLayoutX(((DoubleProperty) properties.get(key)).get());
            } else if ("layoutY".equals(key)) {
                CONTROL.setLayoutY(((DoubleProperty) properties.get(key)).get());
            } else if ("translateX".equals(key)) {
                CONTROL.setTranslateX(((DoubleProperty) properties.get(key)).get());
            } else if ("translateY".equals(key)) {
                CONTROL.setTranslateY(((DoubleProperty) properties.get(key)).get());
            } else if ("padding".equals(key)) {
                CONTROL.setPadding(((ObjectProperty<Insets>) properties.get(key)).get());
            } else if ("targetValue".equals(key)) {
                CONTROL.setTargetValue(((DoubleProperty) properties.get(key)).get());
            } else if ("minValue".equals(key)) {
                CONTROL.setMinValue(((DoubleProperty) properties.get(key)).get());
            } else if ("maxValue".equals(key)) {
                CONTROL.setMaxValue(((DoubleProperty) properties.get(key)).get());
            } else if ("unit".equals(key)) {
                CONTROL.setUnit(((StringProperty) properties.get(key)).get());
            } else if ("symbolColor".equals(key)) {
                CONTROL.setSymbolColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("iconColor".equals(key)) {
                CONTROL.setIconColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("textColor".equals(key)) {
                CONTROL.setTextColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("color".equals(key)) {
                CONTROL.setColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("decimals".equals(key)) {
                CONTROL.setDecimals(((IntegerProperty) properties.get(key)).get());
            }
//            else if ("barColor".equals(key)) {
//                CONTROL.setBarColor(((ObjectProperty<Color>) properties.get(key)).get());
//            }
            else if ("symbolPath".equals(key)) {
                CONTROL.setSymbolPath(((DoubleProperty) properties.get("symbolScaleX")).get(),
                                      ((DoubleProperty) properties.get("symbolScaleY")).get(),
                                      ((StringProperty) properties.get(key)).get());
            } else if ("onTargetSet".equals(key)) {
                CONTROL.setOnTargetSet(((ObjectProperty<EventHandler>) properties.get(key)).get());
            }
        }
        return CONTROL;
    }
}
