package msk.basic;

import hla.rti1516e.*;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.HLAinteger16BE;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.time.HLAfloat64Time;
import msk.utils.Interaction;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;


public abstract class BaseAmbassador extends NullFederateAmbassador {
    protected double federateTime = 0.0;
    protected double federateLookahead = 1.0;
    protected boolean isRegulating = false;
    protected boolean isConstrained = false;
    protected boolean isAdvancing = false;
    protected boolean isAnnounced = false;
    protected boolean isReadyToRun = false;
    protected Map<InteractionClassHandle, String> interactionClassHandles = new HashMap<>();
    protected Map<ParameterHandle, String> parameterHandles = new HashMap<>();
    protected PriorityQueue<Interaction> interactions = new PriorityQueue<>();
    private BaseFederate federate;


    public BaseAmbassador() {
    }

    private void log(String message) {
        System.out.println("FederateAmbassador: " + message);
    }


    private short decodeNumCups(byte[] bytes) {
        HLAinteger16BE value = federate.encoderFactory.createHLAinteger16BE();
        // decode
        try {
            value.decode(bytes);
            return value.getValue();
        } catch (DecoderException de) {
            de.printStackTrace();
            return 0;
        }
    }

    @Override
    public void synchronizationPointRegistrationFailed(String label,
                                                       SynchronizationPointFailureReason reason) {
        log("Failed to register sync point: " + label + ", reason=" + reason);
    }

    @Override
    public void synchronizationPointRegistrationSucceeded(String label) {
        log("Successfully registered sync point: " + label);
    }

    @Override
    public void announceSynchronizationPoint(String label, byte[] tag) {
        log("Synchronization point announced: " + label);
        if (label.equals(BaseFederate.READY_TO_RUN))
            this.isAnnounced = true;
    }

    @Override
    public void federationSynchronized(String label, FederateHandleSet failed) {
        log("Federation Synchronized: " + label);
        if (label.equals(BaseFederate.READY_TO_RUN))
            this.isReadyToRun = true;
    }

    /**
     * The RTI has informed us that time regulation is now enabled.
     */
    @Override
    public void timeRegulationEnabled(LogicalTime time) {
        this.federateTime = ((HLAfloat64Time) time).getValue();
        this.isRegulating = true;
    }

    @Override
    public void timeConstrainedEnabled(LogicalTime time) {
        this.federateTime = ((HLAfloat64Time) time).getValue();
        this.isConstrained = true;
    }

    @Override
    public void timeAdvanceGrant(LogicalTime time) {
        this.federateTime = ((HLAfloat64Time) time).getValue();
        this.isAdvancing = false;
    }


    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass,
                                   ParameterHandleValueMap theParameters,
                                   byte[] tag,
                                   OrderType sentOrdering,
                                   TransportationTypeHandle theTransport,
                                   LogicalTime time,
                                   OrderType receivedOrdering,
                                   SupplementalReceiveInfo receiveInfo) {
        Interaction interaction = parseInteraction(interactionClass, theParameters, time);
        if (interaction != null) {
            System.out.println(" *  receiveInteraction " + interaction.toString());
            interactions.add(interaction);
        }
    }

    private Interaction parseInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, LogicalTime time) {
        String interactionName = getInteractionName(interactionClass);
        Interaction interaction = new Interaction(interactionName);
        interaction.setTime(((HLAfloat64Time) time).getValue());
        theParameters.forEach((key, value) -> {
            String paramName = getParamName(key);
            if (paramName != null)
                interaction.addParameter(paramName, value);
        });
        return interaction;
    }

    private String getInteractionName(InteractionClassHandle code) {
        return interactionClassHandles.get(code);
    }

    private String getParamName(ParameterHandle code) {
        return parameterHandles.get(code);
    }


    @Override
    public void removeObjectInstance(ObjectInstanceHandle theObject,
                                     byte[] tag,
                                     OrderType sentOrdering,
                                     SupplementalRemoveInfo removeInfo)
            throws FederateInternalError {
        log("Object Removed: handle=" + theObject);
    }

    public void registerClassHandle(String name, InteractionClassHandle handle) {
        interactionClassHandles.put(handle, name);
    }

    public void registerParameterHandle(String name, ParameterHandle handle) {
        parameterHandles.put(handle, name);
    }

    public PriorityQueue<Interaction> getInteractions() {
        return interactions;
    }

    public BaseAmbassador setFederate(BaseFederate federate) {
        this.federate = federate;
        return this;
    }
}
