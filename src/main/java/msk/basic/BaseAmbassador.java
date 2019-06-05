package msk.basic;

import hla.rti.LogicalTime;
import hla.rti.jlc.NullFederateAmbassador;
import hla.rti1516e.InteractionClassHandle;
import msk.utils.Interaction;
import org.portico.impl.hla13.types.DoubleTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public abstract class BaseAmbassador extends NullFederateAmbassador {

    private BaseFederate fed;
    private ArrayList<Interaction> interactionList = new ArrayList<>();
    private HashMap<String, InteractionClassHandle> interactionHandles = new HashMap<>();
    private HashMap<InteractionClassHandle, ArrayList<String>> interactionParameters = new HashMap<>();

    public double federateTime = 0.0;
    public double federateLookahead = 1.0;

    protected boolean isRegulating = false;
    protected boolean isConstrained = false;
    protected boolean isAdvancing = false;

    protected boolean isAnnounced = false;
    protected boolean isReadyToRun = false;

    protected boolean running = true;


    private double convertTime(LogicalTime logicalTime) {
        // PORTICO SPECIFIC!!
        return ((DoubleTime) logicalTime).getTime();
    }

    private void log(String message) {
        System.out.println("FederateAmbassador: " + message);
    }

    public void synchronizationPointRegistrationFailed(String label) {
        log("Failed to register sync point: " + label);
    }

    public void synchronizationPointRegistrationSucceeded(String label) {
        log("Successfully registered sync point: " + label);
    }

    public void announceSynchronizationPoint(String label, byte[] tag) {
        log("Synchronization point announced: " + label);
        if (label.equals(BaseFederate.READY_TO_RUN))
            this.isAnnounced = true;
    }

    public void federationSynchronized(String label) {
        log("Federation Synchronized: " + label);
        if (label.equals(BaseFederate.READY_TO_RUN))
            this.isReadyToRun = true;
    }

    /**
     * The RTI has informed us that time regulation is now enabled.
     */
    public void timeRegulationEnabled(LogicalTime theFederateTime) {
        this.federateTime = convertTime(theFederateTime);
        this.isRegulating = true;
    }

    public void timeConstrainedEnabled(LogicalTime theFederateTime) {
        this.federateTime = convertTime(theFederateTime);
        this.isConstrained = true;
    }

    public void timeAdvanceGrant(LogicalTime theTime) {
        this.federateTime = convertTime(theTime);
        this.isAdvancing = false;
    }

    public void registerHandle(String name, InteractionClassHandle handle, String [] params) {
        if(!interactionHandles.containsKey(name)) {
            interactionHandles.put(name, handle);
        }

        if(!interactionParameters.containsKey(handle) && params != null) {
            interactionParameters.put(handle, new ArrayList<>(Arrays.asList(params)));
        }
    }

    public ArrayList<Interaction> getInteractions() {
        return interactionList;
    }

    public void clearInteractions() {
        interactionList.clear();
    }

}
