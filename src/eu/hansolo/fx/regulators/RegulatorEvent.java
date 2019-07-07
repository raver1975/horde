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

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;


/**
 * Created by hansolo on 02.03.16.
 */
public class RegulatorEvent extends Event {
    public static final EventType<RegulatorEvent> TARGET_SET = new EventType(ANY, "targetSet");
    public static final EventType<RegulatorEvent> ADJUSTING  = new EventType(ANY, "adjusting");
    public static final EventType<RegulatorEvent> ADJUSTED   = new EventType(ANY, "adjusted");


    // ******************** Constructors **********************************
    public RegulatorEvent(final EventType<RegulatorEvent> TYPE) { super(TYPE); }
    public RegulatorEvent(final Object SRC, final EventTarget TARGET, final EventType<RegulatorEvent> TYPE) { super(SRC, TARGET, TYPE); }
}
