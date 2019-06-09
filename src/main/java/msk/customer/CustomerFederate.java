package msk.customer;

import hla.rti1516e.exceptions.*;
import msk.Interactions.FinishShopping;
import msk.basic.BaseFederate;
import msk.utils.Interaction;
import msk.utils.InteractionType;

import java.util.Random;

public class CustomerFederate extends BaseFederate {
    private final int DEFAULT_STANDARD_DEVIATION = 6;
    private final int MIN_BASKET_SIZE = 1;
    private final int MAX_BASKET_SIZE = 8;
    private Random random = new Random();
    private int lastCreatedClientId = 0;
    private int currentStep = 0;

    public static void main(String[] args) {
        try {
            new CustomerFederate().runFederate(new CustomerAmbassador());
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }

    @Override
    protected void stepChange() throws RTIexception {
//        System.out.println("CustomerFederate - stepChange()");
        currentStep++;
        int standardDeviation = DEFAULT_STANDARD_DEVIATION;
        if (currentStep > BaseFederate.ITERATIONS / 4) standardDeviation = DEFAULT_STANDARD_DEVIATION - 1;
        if (currentStep > 2 * BaseFederate.ITERATIONS / 3) standardDeviation = DEFAULT_STANDARD_DEVIATION + 1;

        int nextGaussian = (int) Math.round(random.nextGaussian() * standardDeviation);
        if (nextGaussian >= -1 && nextGaussian <= 1) {
            generateNewClient();
        }
    }

    private void generateNewClient() throws RTIexception {
        int clientId = lastCreatedClientId++;
        int basketSize = MIN_BASKET_SIZE + random.nextInt(MAX_BASKET_SIZE - MIN_BASKET_SIZE);
        System.out.println("generateNewClient(); " + new FinishShopping(clientId, basketSize).toString());

        Interaction finishShoppingInteraction = new FinishShopping(clientId, basketSize).toInteraction();
        sendInteraction(finishShoppingInteraction);
    }

    @Override
    protected void publishAndSubscribe() throws RestoreInProgress, NameNotFound, InteractionClassNotDefined, SaveInProgress, FederateNotExecutionMember, RTIinternalError, NotConnected {
        log("publishAndSubscribe()");
        publishInteraction(InteractionType.FINISH_SHOPPING);
    }

    @Override
    protected void handleInteraction(Interaction interaction) {
    }

}
