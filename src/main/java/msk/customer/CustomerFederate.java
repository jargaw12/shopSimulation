package msk.customer;

import hla.rti1516e.exceptions.*;
import msk.basic.BaseFederate2;
import msk.utils.InteractionType;

public class CustomerFederate extends BaseFederate2 {

    public static void main(String[] args) {
        try {
            new CustomerFederate().runFederate();
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }

    @Override
    protected void onRun() {
        System.out.println("CustomerFederate - OnRun()");
    }

    @Override
    protected void publishAndSubscribe() throws RestoreInProgress, NameNotFound, InteractionClassNotDefined, SaveInProgress, FederateNotExecutionMember, RTIinternalError, FederateServiceInvocationsAreBeingReportedViaMOM, NotConnected, InvalidInteractionClassHandle {
        log("publishAndSubscribe()");
        subscribeInteraction(InteractionType.FinishShopping.name());
    }

    @Override
    protected void handleInteraction(String interactionName) {
        System.out.println("handleInteraction()");
        if (interactionName.equals(InteractionType.FinishShopping.name())) {
            System.out.println("handle  -  FinishShopping");
        }
    }

}
