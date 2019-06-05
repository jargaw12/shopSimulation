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


public class BaseAmbassador2 extends NullFederateAmbassador {
    private BaseFederate2 federate;

    protected double federateTime = 0.0;
    protected double federateLookahead = 1.0;

    protected boolean isRegulating = false;
    protected boolean isConstrained = false;
    protected boolean isAdvancing = false;

    protected boolean isAnnounced = false;
    protected boolean isReadyToRun = false;

    protected Map<String, InteractionClassHandle> interactionClassHandles = new HashMap<>();
    protected Map<String, ParameterHandle> parameterHandles = new HashMap<>();
    protected PriorityQueue<Interaction> interactions = new PriorityQueue<>();


    public BaseAmbassador2(BaseFederate2 federate) {
        this.federate = federate;
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
        if (label.equals(BaseFederate2.READY_TO_RUN))
            this.isAnnounced = true;
    }

    @Override
    public void federationSynchronized(String label, FederateHandleSet failed) {
        log("Federation Synchronized: " + label);
        if (label.equals(BaseFederate2.READY_TO_RUN))
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
        interactions.add(interaction);
    }

    private Interaction parseInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, LogicalTime time) {
        String interactionName = getInteractionName(interactionClass);
        theParameters
                .keySet()
                .forEach(paramCode -> {
            String paramName = getParamName(paramCode);
        });
        return null;
    }

    private String getInteractionName(InteractionClassHandle code) {
        return interactionClassHandles.entrySet().stream()
                .filter(entry -> entry.getValue().equals(code))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private String getParamName(ParameterHandle code) {
        return parameterHandles.entrySet().stream()
                .filter(entry -> entry.getValue().equals(code))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
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
        interactionClassHandles.put(name, handle);
    }

    public void registerParameterHandle(String name, ParameterHandle handle) {
        parameterHandles.put(name, handle);
    }
}
