package msk.basic;

import hla.rti1516e.*;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.*;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;
import msk.Interactions.InteractionRoot;
import msk.utils.Interaction;
import msk.utils.InteractionHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public abstract class BaseFederate {

    public static final int ITERATIONS = 400;
    public static final String READY_TO_RUN = "ReadyToRun";
    public static final String INTERACTION_ROOT_PREFIX = "InteractionRoot.";
    protected EncoderFactory encoderFactory;
    private RTIambassador rtiamb;
    private BaseAmbassador fedamb;
    private HLAfloat64TimeFactory timeFactory;

    protected abstract void stepChange() throws RTIexception;

    protected abstract void publishAndSubscribe() throws RestoreInProgress, NameNotFound, InteractionClassNotDefined, SaveInProgress, FederateNotExecutionMember, RTIinternalError, NotConnected, FederateServiceInvocationsAreBeingReportedViaMOM, InvalidInteractionClassHandle;

    protected abstract void handleInteraction(Interaction interaction) throws RTIexception;

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


    public void runFederate(BaseAmbassador ambassador) throws Exception {
        log("Creating RTIambassador");
        rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
        encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();

        // connect
        log("Connecting...");
        fedamb = ambassador.setFederate(this);
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


        for (int i = 0; i < ITERATIONS; i++) {

            advanceTime(1.0);
            log("Time Advanced to " + fedamb.federateTime);

            for (Interaction inter : fedamb.getInteractions()) {
                handleInteraction(inter);
            }

            fedamb.getInteractions().clear();
            stepChange();
        }


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

    protected void subscribeInteraction(String interactionName) throws NameNotFound, InvalidInteractionClassHandle, NotConnected, RTIinternalError, FederateNotExecutionMember, FederateServiceInvocationsAreBeingReportedViaMOM, InteractionClassNotDefined, RestoreInProgress, SaveInProgress {
        String iname = INTERACTION_ROOT_PREFIX + interactionName;
        InteractionClassHandle handle = rtiamb.getInteractionClassHandle(iname);
        rtiamb.subscribeInteractionClass(handle);
        Class<? extends InteractionRoot> interactionClass = InteractionHelper.getInteractionClass(interactionName);
        if (interactionClass != null) {
            for (Field param : interactionClass.getDeclaredFields()) {
                ParameterHandle paramHandle = rtiamb.getParameterHandle(handle, param.getName());
                fedamb.registerParameterHandle(param.getName(), paramHandle);
            }
        }
        fedamb.registerClassHandle(interactionName, handle);
    }


    protected void sendInteraction(Interaction interaction) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(0);
        InteractionClassHandle handle = rtiamb.getInteractionClassHandle(INTERACTION_ROOT_PREFIX + interaction.getName());

        interaction.getParameters().forEach((key, value) -> {
            ParameterHandle param = null;
            System.out.println("sendInteraction - " + interaction.getName());

            try {
                System.out.println("sendInteraction - param: " + key + " : " + handle.toString());
                param = rtiamb.getParameterHandle(handle, key);
            } catch (NameNotFound | InvalidInteractionClassHandle | FederateNotExecutionMember | NotConnected | RTIinternalError nameNotFound) {
                nameNotFound.printStackTrace();
            }
            parameters.put(param, value.getValue());
        });

        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + fedamb.federateLookahead);
        rtiamb.sendInteraction(handle, parameters, generateTag(), time);
    }

    protected void advanceTime(double timestep) throws RTIexception {
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