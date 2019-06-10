package msk.cashregister;

import com.google.common.collect.Lists;
import hla.rti1516e.exceptions.*;
import msk.Interactions.*;
import msk.basic.BaseFederate;
import msk.shop.ShopFederate;
import msk.utils.Interaction;
import msk.utils.InteractionType;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CashRegisterFederate extends BaseFederate {
    private final double PRODUCT_SCANNING_TIME = 4.0;

    private Map<Integer, List<Client>> cashRegisterQueues = new HashMap<>();
    private Map<Integer, Client> cashRegisterStatuses = new HashMap<>();

    public CashRegisterFederate() {
        IntStream.rangeClosed(1, ShopFederate.DEFAULT_CASH_REGISTER_COUNT)
                .forEach(cashRegisterId -> {
                    cashRegisterQueues.put(cashRegisterId, Lists.newArrayList());
                    cashRegisterStatuses.put(cashRegisterId, null);
                });
    }

    public static void main(String[] args) {
        try {
            new CashRegisterFederate().runFederate(new CashRegisterAmbassador());
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }

    @Override
    protected void stepChange() throws RTIexception {
        cashRegisterStatuses.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .peek(entry -> entry.getValue().scanProduct())
                .filter(entry -> entry.getValue().basketIsEmpty())
                .forEach(entry -> finishServeCustomer(entry.getKey(), entry.getValue()));
    }

    @Override
    protected void publishAndSubscribe() throws RestoreInProgress, NameNotFound, InteractionClassNotDefined, SaveInProgress, FederateNotExecutionMember, RTIinternalError, NotConnected, FederateServiceInvocationsAreBeingReportedViaMOM, InvalidInteractionClassHandle {
        publishInteraction(InteractionType.ASSIGN_TO_CASH_REGISTER);
        publishInteraction(InteractionType.START_CUSTOMER_SERVICE);
        publishInteraction(InteractionType.FINISH_CUSTOMER_SERVICE);

        subscribeInteraction(InteractionType.FINISH_SHOPPING);
        subscribeInteraction(InteractionType.OPEN_NEW_CASH_REGISTER);
        subscribeInteraction(InteractionType.CLOSE_CASH_REGISTER);
    }

    @Override
    protected void handleInteraction(Interaction interaction) throws RTIexception {
        System.out.println("handleInteraction()");
        switch (interaction.getName()) {
            case InteractionType.FINISH_SHOPPING:
                onFinishShopping(interaction);
                break;
            case InteractionType.OPEN_NEW_CASH_REGISTER:
                onOpenNewCashRegister(interaction);
                break;
            case InteractionType.CLOSE_CASH_REGISTER:
                onCloseCashRegister(interaction);
                break;
        }
    }

    private void onOpenNewCashRegister(Interaction interaction) {
        OpenNewCashRegister objectInteraction = OpenNewCashRegister.toInteractionObject(interaction);
        System.out.println("onOpenNewCashRegister() " + objectInteraction.toString());

        cashRegisterQueues.put(objectInteraction.getCashRegisterId(), Lists.newArrayList());
        cashRegisterStatuses.put(objectInteraction.getCashRegisterId(), null);
        System.out.println("open new cash register: " + objectInteraction.getCashRegisterId());
    }

    private void onCloseCashRegister(Interaction interaction) {
        CloseCashRegister objectInteraction = CloseCashRegister.toInteractionObject(interaction);
        System.out.println("onCloseCashRegister() " + objectInteraction.toString());
        System.out.println("close cash register: " + objectInteraction.getCashRegisterId());

        cashRegisterQueues.remove(objectInteraction.getCashRegisterId());
        cashRegisterStatuses.remove(objectInteraction.getCashRegisterId());
    }

    private void onFinishShopping(Interaction interaction) throws RTIexception {
        FinishShopping objectInteraction = FinishShopping.toInteractionObject(interaction);
        System.out.println("onFinishShopping() " + objectInteraction.toString());

        double serviceTime = objectInteraction.getBasketSize() * PRODUCT_SCANNING_TIME;
        assignToTheShortestQueue(new Client(objectInteraction.getClientId(), serviceTime));
    }

    private void assignToTheShortestQueue(Client client) throws RTIexception {
        Integer shortestCashRegisterId = cashRegisterQueues.entrySet().stream()
                .min(Comparator.comparingInt(entry -> entry.getValue().size()))
                .map(Map.Entry::getKey)
                .orElse(1);

        Interaction assignToCashRegisterInteraction = new AssignToCashRegister(client.getClientId(), shortestCashRegisterId).toInteraction();
        sendInteraction(assignToCashRegisterInteraction);
        System.out.println("number of cashRegister" + cashRegisterQueues.size() + "  ->  cashRegisters: " + cashRegisterToString());
        System.out.println("shortestQueue nr: " + shortestCashRegisterId + "  ->  size: " + cashRegisterQueues.get(shortestCashRegisterId).size());

        if (cashRegisterIsFree(shortestCashRegisterId)) {
            startService(shortestCashRegisterId, client);
        } else cashRegisterQueues.get(shortestCashRegisterId).add(client);
    }

    private void startService(Integer cashRegisterId, Client client) {
        System.out.println("startService(cash, client) " + cashRegisterId + ",  " + client.toString());
        cashRegisterQueues.get(cashRegisterId).remove(client);
        cashRegisterStatuses.put(cashRegisterId, client);

        Interaction startCustomerServiceInteraction = new StartCustomerService(client.getClientId(), cashRegisterId).toInteraction();
        try {
            sendInteraction(startCustomerServiceInteraction);
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }

    private void finishServeCustomer(Integer cashRegisterId, Client client) {
        System.out.println("finishServeCustomer -> " + cashRegisterId + "  --  " + client.toString());
        Interaction finishCustomerServiceInteraction = new FinishCustomerService(client.getClientId(), cashRegisterId).toInteraction();
        try {
            sendInteraction(finishCustomerServiceInteraction);
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }

        if (!cashRegisterQueues.get(cashRegisterId).isEmpty()) {
            Client nextClient = cashRegisterQueues.get(cashRegisterId).get(0);
            startService(cashRegisterId, nextClient);
        } else cashRegisterStatuses.put(cashRegisterId, null);
    }

    private boolean cashRegisterIsFree(int cashRegisterId) {
        return cashRegisterQueues.get(cashRegisterId).isEmpty() && cashRegisterStatuses.get(cashRegisterId) == null;
    }

    private String cashRegisterToString() {
        return cashRegisterQueues.entrySet().stream()
                .map((entry) -> "[" + entry.getKey() + ": size=" + entry.getValue().size() + "]")
                .collect(Collectors.joining(", ", "{ ", " }"));
    }

}
