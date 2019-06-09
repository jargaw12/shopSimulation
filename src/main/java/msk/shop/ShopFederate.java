package msk.shop;

import hla.rti1516e.exceptions.*;
import msk.Interactions.AssignToCashRegister;
import msk.Interactions.CloseCashRegister;
import msk.Interactions.OpenNewCashRegister;
import msk.Interactions.StartCustomerService;
import msk.basic.BaseFederate;
import msk.utils.Interaction;
import msk.utils.InteractionType;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class ShopFederate extends BaseFederate {
    public final static int MIN_CASH_REGISTER_COUNT = 2;
    public final static int MAX_QUEUE_LENGTH = 5;
    public final static int MAX_CASH_REGISTER_COUNT = 5;
    public final static int DEFAULT_CASH_REGISTER_COUNT = 2;
    private Map<Integer, Integer> queuesLength = new HashMap<>();

    public ShopFederate() {
        IntStream.rangeClosed(1, ShopFederate.DEFAULT_CASH_REGISTER_COUNT)
                .forEach(cashRegisterId -> queuesLength.put(cashRegisterId, 0));
    }

    public static void main(String[] args) {
        try {
            new ShopFederate().runFederate(new ShopAmbassador());
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }

    @Override
    protected void stepChange() {
//        System.out.println("ShopFederate - stepChange()");
    }

    @Override
    protected void publishAndSubscribe() throws RestoreInProgress, NameNotFound, InteractionClassNotDefined, SaveInProgress, FederateNotExecutionMember, RTIinternalError, FederateServiceInvocationsAreBeingReportedViaMOM, NotConnected, InvalidInteractionClassHandle {
        log("publishAndSubscribe()");
        publishInteraction(InteractionType.OPEN_NEW_CASH_REGISTER);
        publishInteraction(InteractionType.CLOSE_CASH_REGISTER);
        subscribeInteraction(InteractionType.ASSIGN_TO_CASH_REGISTER);
        subscribeInteraction(InteractionType.START_CUSTOMER_SERVICE);
    }

    @Override
    protected void handleInteraction(Interaction interaction) throws RTIexception {
        System.out.println("handleInteraction()");
        switch (interaction.getName()) {
            case InteractionType.ASSIGN_TO_CASH_REGISTER:
                onAssignToCashRegister(interaction);
                break;
            case InteractionType.START_CUSTOMER_SERVICE:
                onStartCustomerService(interaction);
                break;
        }
    }

    private void onStartCustomerService(Interaction interaction) throws RTIexception {
        StartCustomerService objectInteraction = StartCustomerService.toInteractionObject(interaction);
        System.out.println("onStartCustomerService() " + objectInteraction.toString());

        decreaseQueueLength(objectInteraction.getCashRegisterId());
        if (needToCloseCashRegister(objectInteraction.getCashRegisterId()))
            closeCashRegister(objectInteraction.getCashRegisterId());
    }

    private void onAssignToCashRegister(Interaction interaction) throws RTIexception {
        AssignToCashRegister objectInteraction = AssignToCashRegister.toInteractionObject(interaction);
        System.out.println("onAssignToCashRegister() " + objectInteraction.toString());

        increaseQueueLength(objectInteraction.getCashRegisterId());
        if (needToOpenNewCashRegister()) openNewCashRegister();
    }

    private void increaseQueueLength(int cashRegisterId) {
        changeQueueLength(cashRegisterId, 1);
    }

    private void decreaseQueueLength(int cashRegisterId) {
        changeQueueLength(cashRegisterId, -1);
    }

    private void changeQueueLength(int cashRegisterId, int n) {
        if (queuesLength.containsKey(cashRegisterId))
            queuesLength.merge(cashRegisterId, n, Integer::sum);
        else System.out.println(" x x x - size(cashRegister=" + cashRegisterId + ")  + (" + n + ") - x x x ");
    }

    private boolean needToOpenNewCashRegister() {
        boolean allQueuesAreTooLong = queuesLength.values().stream().allMatch(count -> count >= MAX_QUEUE_LENGTH);
        boolean allCashRegisterAreOpen = queuesLength.size() == MAX_CASH_REGISTER_COUNT;
        return allQueuesAreTooLong && !allCashRegisterAreOpen;
    }

    private void openNewCashRegister() throws RTIexception {
        int freeCashRegisterId = IntStream.rangeClosed(1, ShopFederate.MAX_CASH_REGISTER_COUNT)
                .filter(id -> !queuesLength.containsKey(id))
                .findFirst()
                .orElse(ShopFederate.MAX_CASH_REGISTER_COUNT);

        System.out.println("open new cash register " + freeCashRegisterId + ",  ->  " + queuesLength.toString());
        Interaction openNewCashRegisterInteraction = new OpenNewCashRegister(freeCashRegisterId).toInteraction();
        sendInteraction(openNewCashRegisterInteraction);
        queuesLength.put(freeCashRegisterId, 0);
    }

    private boolean needToCloseCashRegister(int cashRegisterId) {
        boolean cashRegisterIsEmpty = queuesLength.get(cashRegisterId) == 0;
        boolean moreThenMinCashRegisterAreOpen = queuesLength.size() > MIN_CASH_REGISTER_COUNT;
        boolean otherQueuesNotHaveMaxLength = !queuesLength.entrySet().stream()
                .filter(entry -> entry.getKey() != cashRegisterId)
                .allMatch(entry -> entry.getValue() >= MAX_QUEUE_LENGTH);

        return cashRegisterIsEmpty && moreThenMinCashRegisterAreOpen && otherQueuesNotHaveMaxLength;
    }

    private void closeCashRegister(int cashRegisterId) throws RTIexception {
        System.out.println("close cash register " + cashRegisterId + ",  " + queuesLength.toString());
        Interaction closeCashRegisterInteraction = new CloseCashRegister(cashRegisterId).toInteraction();
        sendInteraction(closeCashRegisterInteraction);
        queuesLength.remove(cashRegisterId);
    }
}