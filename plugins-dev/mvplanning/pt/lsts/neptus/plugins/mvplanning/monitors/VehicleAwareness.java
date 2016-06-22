/*
 * Copyright (c) 2004-2015 Universidade do Porto - Faculdade de Engenharia
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
 * 15 Dec 2015
 */
package pt.lsts.neptus.plugins.mvplanning.monitors;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.eventbus.Subscribe;

import pt.lsts.neptus.NeptusLog;
import pt.lsts.neptus.comm.manager.imc.ImcSystem;
import pt.lsts.neptus.comm.manager.imc.ImcSystemsHolder;
import pt.lsts.neptus.console.events.ConsoleEventVehicleStateChanged;
import pt.lsts.neptus.console.events.ConsoleEventVehicleStateChanged.STATE;
import pt.lsts.neptus.plugins.mvplanning.interfaces.ConsoleAdapter;
import pt.lsts.neptus.types.coord.LocationType;
import pt.lsts.neptus.types.vehicle.VehicleType.SystemTypeEnum;

/**
 * Class responsible for keeping a list of available and
 * unavailable vehicles.
 * It listens to {@link ConsoleEventVehicleStateChanged} events
 * to have a sense of what the vehicles' current state is.
 **/
public class VehicleAwareness {
    private final ReadWriteLock RW_LOCK = new ReentrantReadWriteLock();

    private enum VEHICLE_STATE {
        AVAILABLE("Available"),
        UNAVAILABLE("Unavailable");

        protected String value;
        VEHICLE_STATE(String value) {
            this.value = value;
        }
    };

    private ConsoleAdapter console;
    private Map<String, LocationType> startLocations;
    private ConcurrentMap<String, VEHICLE_STATE> vehiclesState;

    public VehicleAwareness(ConsoleAdapter console) {
        this.console = console;
        startLocations = new ConcurrentHashMap<>();
        vehiclesState = new ConcurrentHashMap<>();

        /* Fetch available vehicles, on plugin start-up */
        for(ImcSystem vehicle : ImcSystemsHolder.lookupActiveSystemByType(SystemTypeEnum.VEHICLE))
            setVehicleState(vehicle.getName(), VEHICLE_STATE.AVAILABLE);
    }

    public void setVehicleStartLocation(String vehicleId, LocationType startLocation) {
        if(ImcSystemsHolder.getSystemWithName(vehicleId) != null) {
            startLocations.put(vehicleId, startLocation);
            NeptusLog.pub().info(vehicleId + " start location's set");
        }
        else
            NeptusLog.pub().warn("Trying to set location of " + vehicleId + ". which is not a vehicle");
    }

    public LocationType getVehicleStartLocation(String vehicleId) {
        return startLocations.getOrDefault(vehicleId, null);
    }

    @Subscribe
    public void on(ConsoleEventVehicleStateChanged event) {
        if(event == null || event.getState() == null) {
            NeptusLog.pub().error("I'm receiving null ConsoleEventVehicleStateChanged events");
            return;
        }

        String id = event.getVehicle();
        ConsoleEventVehicleStateChanged.STATE newState = event.getState();

        checkVehicleState(id, newState);
    }


    /* TODO also check vehicle's medium */
    private void checkVehicleState(String vehicle, STATE state) {
        VEHICLE_STATE st = VEHICLE_STATE.UNAVAILABLE;

        if(state == STATE.FINISHED || state == STATE.SERVICE)
            if(hasReliableComms(vehicle))
                st = VEHICLE_STATE.AVAILABLE;

        setVehicleState(vehicle, st);
    }

    private void setVehicleState(String vehicle, VEHICLE_STATE state) {
        RW_LOCK.writeLock().lock();
        vehiclesState.put(vehicle, state);
        RW_LOCK.writeLock().unlock();

        NeptusLog.pub().info("Vehicle " + vehicle + " is " + state.value);
    }

    /**
     * Confirms both that there are reliable communications
     * with the vehicle and that it is currently considered
     * as available.
     * */
    public boolean isVehicleAvailable(String vehicle) {
        RW_LOCK.readLock().lock();
        VEHICLE_STATE state = vehiclesState.get(vehicle);
        RW_LOCK.readLock().unlock();

        return state != null && state == VEHICLE_STATE.AVAILABLE;
    }

    /**
     * Checks if its possible to communicate with the vehicle
     * (is active) and if this communication is via TCP.
     * If the vehicle is in simulation mode it is considered
     * that there are reliable communications whether TCP is on
     * or not
     * */
    private boolean hasReliableComms(String vehicle) {
        ImcSystem sys = ImcSystemsHolder.getSystemWithName(vehicle);
        return sys.isActive() && (sys.isTCPOn() || sys.isSimulated());
    }
}
