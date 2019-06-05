package msk.basic;

import hla.rti1516e.*;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.*;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;
import msk.utils.Encoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

public abstract class BaseFederate2 {

    public static final int ITERATIONS = 20;
    public static final String READY_TO_RUN = "ReadyToRun";
    public static final String INTERACTION_ROOT_PREFIX = "InteractionRoot.";

    private RTIambassador rtiamb;
    private BaseAmbassador2 fedamb;
    private HLAfloat64TimeFactory timeFactory;
    protected EncoderFactory encoderFactory;

    protected abstract void onRun() throws RTIexception;

    protected abstract void publishAndSubscribe() throws RestoreInProgress, NameNotFound, InteractionClassNotDefined, SaveInProgress, FederateNotExecutionMember, RTIinternalError, NotConnected, FederateServiceInvocationsAreBeingReportedViaMOM, InvalidInteractionClassHandle;

    protected abstract void handleInteraction(String interactionName);

    protected void log(String message) {
        System.out.println("ExampleFederate   : " + message);
    }

    private void waitForUser() {
        log(" >>>>>>>>>> Press Enter to Continue <<<<<<<<<<");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            reader.readLine();
        } catch (Exception e) {
            log("Error while waiting for user input: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void runFederate() throws Exception {
        log("Creating RTIambassador");
        rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
        encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();

        // connect
        log("Connecting...");
        fedamb = new BaseAmbassador2(this);
        rtiamb.connect(fedamb, CallbackModel.HLA_EVOKED);

        log("Creating Federation...");
        try {
            URI uri = getClass().getResource("/shop.fed").toURI();
            URL[] foms = new URL[]{new File(uri).toURI().toURL()};
            rtiamb.createFederationExecution("ShopFederation", foms);
            log("Created Federation");
        } catch (FederationExecutionAlreadyExists exists) {
            log("Didn't create federation, it already existed");
        } catch (MalformedURLException urle) {
            log("Exception loading one of the FOM modules from disk: " + urle.getMessage());
            urle.printStackTrace();
            return;
        }

        rtiamb.joinFederationExecution(
                getClass().getSimpleName(),
                getClass().getSimpleName().replace("Federate", "Type"),
                "ShopFederation");

        log("Joined Federation as " + getClass().getSimpleName());

        this.timeFactory = (HLAfloat64TimeFactory) rtiamb.getTimeFactory();


        rtiamb.registerFederationSynchronizationPoint(READY_TO_RUN, null);
        while (!fedamb.isAnnounced) {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }

        waitForUser();

        rtiamb.synchronizationPointAchieved(READY_TO_RUN);
        log("Achieved sync point: " + READY_TO_RUN + ", waiting for federation...");
        while (!fedamb.isReadyToRun) {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }

        enableTimePolicy();
        log("Time Policy Enabled");

        publishAndSubscribe();
        log("Published and Subscribed");

//        ObjectInstanceHandle objectHandle = registerObject();
//        log("Registered Object, handle=" + objectHandle);

        for (int i = 0; i < ITERATIONS; i++) {
//            updateAttributeValues(objectHandle);

//            sendInteraction();

            advanceTime(1.0);
            log("Time Advanced to " + fedamb.federateTime);

//            //Sorting
//            fedamb.sortInteractions();
//            for (Interaction inter : fedamb.getInteractions()) {
//                handleInteraction(inter);
//            }
//            fedamb.clearInteractions();

            onRun();
        }

//        deleteObject(objectHandle);
//        log("Deleted Object, handle=" + objectHandle);

        rtiamb.resignFederationExecution(ResignAction.DELETE_OBJECTS);
        log("Resigned from Federation");

        try {
            rtiamb.destroyFederationExecution("ShopFederation");
            log("Destroyed Federation");
        } catch (FederationExecutionDoesNotExist dne) {
            log("No need to destroy federation, it doesn't exist");
        } catch (FederatesCurrentlyJoined fcj) {
            log("Didn't destroy federation, federates still joined");
        }
    }


    private void enableTimePolicy() throws Exception {
        HLAfloat64Interval lookahead = timeFactory.makeInterval(fedamb.federateLookahead);

        this.rtiamb.enableTimeRegulation(lookahead);

        // tick until we get the callback
        while (!fedamb.isRegulating) {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }

        this.rtiamb.enableTimeConstrained();

        while (!fedamb.isConstrained) {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    protected void publishInteraction(String interactionName) throws RestoreInProgress, InteractionClassNotDefined, SaveInProgress, FederateNotExecutionMember, RTIinternalError, NotConnected, NameNotFound {
        String iname = INTERACTION_ROOT_PREFIX + interactionName;
        InteractionClassHandle handle = rtiamb.getInteractionClassHandle(iname);
        rtiamb.publishInteractionClass(handle);
    }

    protected void subscribeInteraction(String interactionName) throws NameNotFound, NotConnected, RTIinternalError, FederateNotExecutionMember, InteractionClassNotDefined, RestoreInProgress, SaveInProgress, FederateServiceInvocationsAreBeingReportedViaMOM, InvalidInteractionClassHandle {
        subscribeInteraction(interactionName, Collections.EMPTY_LIST);
    }

    protected void subscribeInteraction(String interactionName, List<String> params) throws NameNotFound, NotConnected, RTIinternalError, FederateNotExecutionMember, InteractionClassNotDefined, RestoreInProgress, SaveInProgress, FederateServiceInvocationsAreBeingReportedViaMOM, InvalidInteractionClassHandle {
        String iname = INTERACTION_ROOT_PREFIX + interactionName;
        InteractionClassHandle handle = rtiamb.getInteractionClassHandle(iname);
        rtiamb.subscribeInteractionClass(handle);
        for (String param: params) {
            rtiamb.getParameterHandle(handle, param);
        }
        fedamb.registerClassHandle(interactionName, handle);
    }



//    private ObjectInstanceHandle registerObject() throws RTIexception {
//        return rtiamb.registerObjectInstance(sodaHandle);
//    }
//
//
//    private void updateAttributeValues(ObjectInstanceHandle objectHandle) throws RTIexception {
//        AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(2);
//
//        HLAinteger16BE cupsValue = encoderFactory.createHLAinteger16BE(getTimeAsShort());
//        attributes.put(cupsHandle, cupsValue.toByteArray());
//
//        int randomValue = 101 + new Random().nextInt(3);
//        HLAinteger32BE flavValue = encoderFactory.createHLAinteger32BE(randomValue);
//        attributes.put(flavHandle, flavValue.toByteArray());
//
//        rtiamb.updateAttributeValues(objectHandle, attributes, generateTag());
//
//        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + fedamb.federateLookahead);
//        rtiamb.updateAttributeValues(objectHandle, attributes, generateTag(), time);
//    }

    protected void sendInteraction(String interactionName) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(0);
        InteractionClassHandle handle = rtiamb.getInteractionClassHandle(INTERACTION_ROOT_PREFIX + interactionName);
        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + fedamb.federateLookahead);
        rtiamb.sendInteraction(handle, parameters, generateTag(), time);
    }

    protected void sendInteraction2(String interactionName, List<String> params) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(0);
        InteractionClassHandle handle = rtiamb.getInteractionClassHandle(INTERACTION_ROOT_PREFIX + interactionName);
        ParameterHandle param = rtiamb.getParameterHandle(handle, "clientId");
        parameters.put(param, Encoder.encodeInt(encoderFactory,1));
        fedamb.registerParameterHandle("clientId", param);

        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + fedamb.federateLookahead);
        rtiamb.sendInteraction(handle, parameters, generateTag(), time);
    }

    private void advanceTime(double timestep) throws RTIexception {
        fedamb.isAdvancing = true;
        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + timestep);
        rtiamb.timeAdvanceRequest(time);

        while (fedamb.isAdvancing) {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    private void deleteObject(ObjectInstanceHandle handle) throws RTIexception {
        rtiamb.deleteObjectInstance(handle, generateTag());
    }

    private short getTimeAsShort() {
        return (short) fedamb.federateTime;
    }

    private byte[] generateTag() {
        return ("(timestamp) " + System.currentTimeMillis()).getBytes();
    }

}