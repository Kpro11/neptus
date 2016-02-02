/*
 * Copyright (c) 2004-2016 Universidade do Porto - Faculdade de Engenharia
 * Laboratório de Sistemas e Tecnologia Subaquática (LSTS)
 * All rights reserved.
 * Rua Dr. Roberto Frias s/n, sala I203, 4200-465 Porto, Portugal
 *
 * This file is part of Neptus, Command and Control Framework.
 *
 * Commercial Licence Usage
 * Licencees holding valid commercial Neptus licences may use this file
 * in accordance with the commercial licence agreement provided with the
 * Software or, alternatively, in accordance with the terms contained in a
 * written agreement between you and Universidade do Porto. For licensing
 * terms, conditions, and further information contact lsts@fe.up.pt.
 *
 * European Union Public Licence - EUPL v.1.1 Usage
 * Alternatively, this file may be used under the terms of the EUPL,
 * Version 1.1 only (the "Licence"), appearing in the file LICENSE.md
 * included in the packaging of this file. You may not use this work
 * except in compliance with the Licence. Unless required by applicable
 * law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific
 * language governing permissions and limitations at
 * https://www.lsts.pt/neptus/licence.
 *
 * For more information please see <http://lsts.fe.up.pt/neptus>.
 *
 * Author: tsmarques
 * 28 Jan 2016
 */
package pt.lsts.neptus.plugins.mvplanning.utils.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import pt.lsts.neptus.plugins.mvplanning.utils.PayloadProfile;

/**
 * @author tsmarques
 *
 */
@XmlRootElement (name="PayloadProfiles")
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({PayloadProfile.class})
public class PayloadProfiles {
    
    @XmlElement(name = "Profile")
    private List<PayloadProfile> profiles;
    
    @XmlAttribute(name = "Type")
    private String profilesType;
    
    public PayloadProfiles() {
        
    }
    
    public PayloadProfiles(String pType) {
        profiles = new ArrayList<PayloadProfile>();
        profilesType = pType;
    }
    
    public List<PayloadProfile> getVehicleProfiles(String vehicleId) {
        List<PayloadProfile> vehicleProfiles = new ArrayList<PayloadProfile>();
        for(PayloadProfile prf : profiles) {
            if(prf.getPayloadVehicles().contains(vehicleId))
                vehicleProfiles.add(prf);
        }
        return vehicleProfiles;
    }
    
    public String getType() {
        return profilesType;
    }
       
    
    public void addProfile(PayloadProfile payload) {
        profiles.add(payload);
    }
    
    public List<PayloadProfile> getProfiles() {
        return profiles;
    }
}
