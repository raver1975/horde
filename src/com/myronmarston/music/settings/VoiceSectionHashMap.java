/*
 * Copyright 2008, Myron Marston <myron DOT marston AT gmail DOT com>
 * 
 * This file is part of Fractal Composer.
 * 
 * Fractal Composer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option any later version.
 * 
 * Fractal Composer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Fractal Composer.  If not, see <http://www.gnu.org/licenses/>. 
 */

package com.myronmarston.music.settings;

import org.simpleframework.xml.*;
import org.simpleframework.xml.core.Commit;
import org.simpleframework.xml.core.Complete;
import org.simpleframework.xml.core.Persist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Contains all voice sections for the entire fractal piece, keyed by 
 * voice and section.  In addition, this provides support to serialize the
 * map to XML in a deterministic fashion.
 * 
 * @author Myron
 */
@Root
public class VoiceSectionHashMap extends HashMap<VoiceSectionHashMapKey, VoiceSection> {    
    // Used to serialize the class.  Change this if the class has a change significant enough to change the way the class is serialized.
    private static final long serialVersionUID = 1L;
    
    @ElementList(type=VoiceSection.class, required=true)
    private List<VoiceSection> serializableVoiceSections;
    
    @Persist
    private void prepareForXmlSerialization() {
        // fractalcomposer.com compares serialized xml strings to see if
        // any fractal piece settings have changed, so we want the xml 
        // serialization to always produce a consistent result.  HashMaps
        // don't have any guarenteed ordering, so we put the voice sections
        // in an array list and sort it.
    
        assert this.serializableVoiceSections == null;
        this.serializableVoiceSections = new ArrayList<VoiceSection> (this.values());
        Collections.sort(serializableVoiceSections, new Comparator<VoiceSection>() {
            public int compare(VoiceSection vs1, VoiceSection vs2) {                        
                int voiceDiff = vs2.getVoice().getUniqueIndex() - vs1.getVoice().getUniqueIndex();
                if (voiceDiff != 0) return voiceDiff;
                return vs2.getSection().getUniqueIndex() - vs1.getSection().getUniqueIndex();    
            }
        });
    }
    
    @Complete
    private void xmlSerializationCompleted() {        
        assert this.serializableVoiceSections != null;
        this.serializableVoiceSections = null;
    }
    
    @Commit
    private void xmlDeserializationCompleted() {      
        assert this.serializableVoiceSections != null;        
        assert this.size() == 0;
        for (VoiceSection vs : this.serializableVoiceSections) {            
            this.put(vs.createHashMapKey(), vs);
        }        
        
        this.serializableVoiceSections = null;
    }
    
}
