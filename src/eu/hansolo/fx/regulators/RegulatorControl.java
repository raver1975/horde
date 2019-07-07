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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Color;


/**
 * Created by hansolo on 13.12.16.
 */
public interface RegulatorControl {

    public double getTargetValue();
    public void setTargetValue(final double VALUE);
    public DoubleProperty targetValueProperty();

    public Color getTextColor();
    public void setTextColor(final Color COLOR);
    public ObjectProperty<Color> textColorProperty();

    public Color getColor();
    public void setColor(final Color COLOR);
    public ObjectProperty<Color> colorProperty();

    public Color getIndicatorColor();
    public void setIndicatorColor(final Color COLOR);
    public ObjectProperty<Color> indicatorColorProperty();

    public boolean isSelected();
    public void setSelected(final boolean SELECTED);
    public BooleanProperty selectedProperty();

}
