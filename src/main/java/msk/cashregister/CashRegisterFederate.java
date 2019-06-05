package msk.cashregister;

import hla.rti1516e.exceptions.*;
import msk.basic.BaseFederate2;
import msk.utils.InteractionType;

import java.util.Arrays;
import java.util.List;

public class CashRegisterFederate extends BaseFederate2 {

    public static void main(String[] args) {
        try {
            new CashRegisterFederate().runFederate();
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }

    @Override
    protected void onRun() throws RTIexception {
        System.out.println("CashRegisterFederate - OnRun()");
        sendInteraction2(InteractionType.FinishShopping.name(), Arrays.asList("clientId"));
    }

    @Override
    protected void publishAndSubscribe() throws RestoreInProgress, NameNotFound, InteractionClassNotDefined, SaveInProgress, FederateNotExecutionMember, RTIinternalError, NotConnected {
        log("publishAndSubscribe()");
        publishInteraction(InteractionType.FinishShopping.name());
    }

    @Override
    protected void handleInteraction(String interactionName) {
        System.out.println("handleInteraction()");
        if (interactionName.equals(InteractionType.FinishShopping.name())) {
            System.out.println("handle  -  FinishShopping");
        }
    }


}
