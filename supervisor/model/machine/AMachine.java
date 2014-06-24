/**
  * This file is part of VoteBox.
  * 
  * VoteBox is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3 as published by
  * the Free Software Foundation.
  * 
  * You should have received a copy of the GNU General Public License
  * along with VoteBox, found in the root of any distribution or
  * repository containing all or part of VoteBox.
  * 
  * THIS SOFTWARE IS PROVIDED BY WILLIAM MARSH RICE UNIVERSITY, HOUSTON,
  * TX AND IS PROVIDED 'AS IS' AND WITHOUT ANY EXPRESS, IMPLIED OR
  * STATUTORY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, WARRANTIES OF
  * ACCURACY, COMPLETENESS, AND NONINFRINGEMENT.  THE SOFTWARE USER SHALL
  * INDEMNIFY, DEFEND AND HOLD HARMLESS RICE UNIVERSITY AND ITS FACULTY,
  * STAFF AND STUDENTS FROM ANY AND ALL CLAIMS, ACTIONS, DAMAGES, LOSSES,
  * LIABILITIES, COSTS AND EXPENSES, INCLUDING ATTORNEYS' FEES AND COURT
  * COSTS, DIRECTLY OR INDIRECTLY ARISING OUR OF OR IN CONNECTION WITH
  * ACCESS OR USE OF THE SOFTWARE.
 */

package supervisor.model.machine;

import supervisor.model.ObservableEvent;

import java.util.Observer;

/**
 * Abstract notion of a machine on the VoteBox network.
 * This is a mini-model in the machine MVC, which will be contained entirely within the
 * larger Supervisor MVC.
 *
 * @author Corey Shaw
 */
public abstract class AMachine implements Comparable {

    /** The serial number that this machine uses in its network communications */
    private int serial;

    /** A status field, can be set to ACTIVE, INACTIVE, and other machine-dependent status codes */
    private int status;

    /** The Supervisor's label for this machine, which is displayed in the GUIs on both machines */
    private int label;

    /** Whether this machine is connected to and sending messages across the network */
    private boolean online;

    /** An observer that will allow the mini-MVC to update its state and reflect the changes on the mini-vew */
    protected ObservableEvent obs;

    /**
     * Constructs a new machine with the given serial number
     *
     * @param serial the serial number the machine uses to communicate on the network
     */
    public AMachine(int serial) {
        this.serial = serial;

        /*
         * If a machine is being constructed, it must have sent a "Joined"
         * message across the network and therefore must be connected
         */
        online = true;

        /* Initialize our event dispatcher */
        obs = new ObservableEvent();
    }

    /**
     * Adds an observer that will be notified when this machine is changed.
     * This enables an event-driven update model.
     *
     * @param o the observer
     * @see java.util.Observable#addObserver(java.util.Observer)
     */
    public void addObserver(Observer o) {
        obs.addObserver(o);
    }

    /**
     * Compares this machine to another machine, by their serial numbers.
     * If the serials are the same, this returns 0. If this machine's number is
     * greater than the other one, the return value will be greater than zero. If
     * the other machine's serial is greater, this will return a value less than zero.
     *
     * @see java.lang.Comparable#compareTo(Object)
     */
    public int compareTo(Object o) {
        AMachine rhs = (AMachine) o;
        return serial - rhs.serial;
    }

    /**
     * @return this machine's label
     */
    public int getLabel() {
        return label;
    }

    /**
     * @return this machine's serial number
     */
    public int getSerial() {
        return serial;
    }

    /**
     * @return this machine's status
     */
    public int getStatus() {
        return status;
    }

    /**
     * @return whether this machine is online (that is, if there exists a direct
     *         link between this machine and the current machine, managed by the Supervisor)
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * Sets this machine's label, and
     * update the state of the machine to reflect the change.
     *
     * @param label the label to set
     */
    public void setLabel(int label) {
        this.label = label;
        obs.notifyObservers();
    }

    /**
     * Sets whether this machine is online, and
     * update the state of the machine to reflect the change.
     *
     * @param online the online to set
     */
    public void setOnline(boolean online) {
        this.online = online;
        obs.notifyObservers();
    }

    /**
     * Sets this machine's status, and
     * update the state of the machine to reflect the change.
     *
     * @param status the status to set
     */
    public void setStatus(int status) {
        this.status = status;
        obs.notifyObservers();
    }

    /**
     * Overrides the default implementation of toString to produce a meaningful representation of this machine
     *
     * @return String representation of this machine in form "[MachineType]{serial = [machineSerial] status = [machineStatus]}"
     */
    public String toString(){
        String res = "";
        if(this instanceof SupervisorMachine){
            res = "SupervisorMachine{serial =  " + serial + " status = " + ((status==4)?"ACTIVE":"INACTIVE") + "}";
        } else if(this instanceof VoteBoxBooth){
            res = "VoteBoxBooth{serial = " + serial + " status = " + ((status==1)? "IN-USE": "READY") + "}";
        } else if(this instanceof BallotScannerMachine){
            res = "ballotScannerMachine{serial = " + serial + " status = " + ((status == 6)?"ACTIVE":"INACTIVE") + "}";
        }
        return res;
    }
}
