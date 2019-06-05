package msk.basic;

import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import msk.utils.Interaction;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Random;

public abstract class BaseFederate {

    public static final String READY_TO_RUN = "ReadyToRun";
    protected final double timeStep = 10.0;
    protected RTIambassador rtiamb;
    protected BaseAmbassador fedamb;

    protected abstract void onRun() throws hla.rti1516e.exceptions.RTIexception;

    protected abstract void publishAndSubscribe() throws hla.rti1516e.exceptions.RTIexception, RTIexception;
    protected abstract void firstStep();

    protected abstract void handleInteraction(Interaction i) throws hla.rti1516e.exceptions.RTIexception;

    public void runFederate(BaseAmbassador ambassador) throws RTIexception, hla.rti1516e.exceptions.RTIexception {
        rtiamb = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();

        try {
            URI uri = getClass().getResource("/shop.fed").toURI();
            File fom = new File(uri);
            rtiamb.createFederationExecution("ShopFederation", fom.toURI().toURL());
            log("Created Federation");
        } catch (FederationExecutionAlreadyExists exists) {
            log("Didn't create federation, it already existed");
        } catch (MalformedURLException urle) {
            log("Exception processing fom: " + urle.getMessage());
            urle.printStackTrace();
            return;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        fedamb = ambassador;
        rtiamb.joinFederationExecution(getClass().getSimpleName(), "ShopFederation", fedamb);
        log("Joined Federation as " + getClass().getSimpleName());

        rtiamb.registerFederationSynchronizationPoint(READY_TO_RUN, null);

        while (!fedamb.isAnnounced) {
            rtiamb.tick();
        }

        waitForUser();

        rtiamb.synchronizationPointAchieved(READY_TO_RUN);
        log("Achieved sync point: " + READY_TO_RUN + ", waiting for federation...");

        while (!fedamb.isReadyToRun) {
            rtiamb.tick();
        }

        enableTimePolicy();

        publishAndSubscribe();

        firstStep();

        while (fedamb.running) {
            for(Interaction inter : fedamb.getInteractions()) {
                handleInteraction(inter);
            }

            fedamb.clearInteractions();

            advanceTime(randomTime());
            onRun();
            rtiamb.tick();
        }

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

    private void enableTimePolicy() throws RTIexception {
        LogicalTime currentTime = convertTime(fedamb.federateTime);
        LogicalTimeInterval lookahead = convertInterval(fedamb.federateLookahead);

        this.rtiamb.enableTimeRegulation(currentTime, lookahead);

        while (!fedamb.isRegulating) {
            rtiamb.tick();
        }

        this.rtiamb.enableTimeConstrained();

        while (!fedamb.isConstrained) {
            rtiamb.tick();
        }
    }

    protected void sendInteraction(double timeStep, String interactionName) throws RTIexception {
        log("sendInteraction()");
        SuppliedParameters parameters =
                RtiFactoryFactory.getRtiFactory().createSuppliedParameters();
        Random random = new Random();
        byte[] quantity = EncodingHelpers.encodeInt(random.nextInt(10) + 1);

        int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot." + interactionName);
        int quantityHandle = rtiamb.getParameterHandle("clientId", interactionHandle);

        parameters.add(quantityHandle, quantity);

        LogicalTime time = convertTime(timeStep);
        rtiamb.sendInteraction(interactionHandle, parameters, "tag".getBytes(), time);
    }

    private void advanceTime(double timestep) throws RTIexception {
        log("requesting time advance for: " + timestep);
        // request the advance
        fedamb.isAdvancing = true;
        LogicalTime newTime = convertTime(fedamb.federateTime + timestep);
        rtiamb.timeAdvanceRequest(newTime);
        while (fedamb.isAdvancing) {
            rtiamb.tick();
        }
    }

    private double randomTime() {
        Random r = new Random();
        return 1 + (9 * r.nextDouble());
    }

    private LogicalTime convertTime(double time) {
        // PORTICO SPECIFIC!!
        return new DoubleTime(time);
    }

    /**
     * Same as for {@link #convertTime(double)}
     */
    private LogicalTimeInterval convertInterval(double time) {
        // PORTICO SPECIFIC!!
        return new DoubleTimeInterval(time);
    }

    protected void log(String message) {
        System.out.println("StorageFederate   : " + message);
    }

    protected void publishInteraction(String name) throws NameNotFound, FederateNotExecutionMember, RTIinternalError, ConcurrentAccessAttempted, InteractionClassNotDefined, RestoreInProgress, SaveInProgress {
        log("publishInteraction()");
        int servedHandle = rtiamb.getInteractionClassHandle("InteractionRoot." + name);
        rtiamb.publishInteractionClass(servedHandle);
    }

    protected void subscribeInteraction(String name) throws NameNotFound, FederateNotExecutionMember, RTIinternalError, FederateLoggingServiceCalls, ConcurrentAccessAttempted, InteractionClassNotDefined, RestoreInProgress, SaveInProgress {
        log("subscribeInteraction()");
        int servedHandle = rtiamb.getInteractionClassHandle("InteractionRoot." + name);
        rtiamb.subscribeInteractionClass(servedHandle);
//        fedamb.registerClassHandle(name, servedHandle, strings);

    }

}
