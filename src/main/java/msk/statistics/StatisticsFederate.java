package msk.statistics;

import hla.rti1516e.exceptions.*;
import msk.Interactions.AssignToCashRegister;
import msk.Interactions.CalculateStatistics;
import msk.Interactions.StartCustomerService;
import msk.basic.BaseFederate;
import msk.utils.Interaction;
import msk.utils.InteractionType;

import java.util.HashMap;
import java.util.Map;

public class StatisticsFederate extends BaseFederate {
    private Map<Integer, WaitingCustomer> waitingCustomers = new HashMap<>();
    private Map<Integer, Double> avgWaitingTimes = new HashMap<>();
    private Map<Integer, Integer> queuesLength = new HashMap<>();
    private Map<Integer, Integer> servedCustomerCount = new HashMap<>();

    public static void main(String[] args) {
        try {
            new StatisticsFederate().runFederate(new StatisticsAmbassador());
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }

    @Override
    protected void stepChange() {
//        System.out.println("StatisticsFederate - stepChange()");
    }

    @Override
    protected void publishAndSubscribe() throws RestoreInProgress, NameNotFound, InteractionClassNotDefined, SaveInProgress, FederateNotExecutionMember, RTIinternalError, FederateServiceInvocationsAreBeingReportedViaMOM, NotConnected, InvalidInteractionClassHandle {
        log("publishAndSubscribe()");
        publishInteraction(InteractionType.CALCULATE_STATISTICS);
        subscribeInteraction(InteractionType.ASSIGN_TO_CASH_REGISTER);
        subscribeInteraction(InteractionType.START_CUSTOMER_SERVICE);
    }

    @Override
    protected void handleInteraction(Interaction interaction) throws RTIexception {
        System.out.println("handleInteraction()");
        switch (interaction.getName()) {
            case InteractionType.START_CUSTOMER_SERVICE:
                onStartCustomerService(interaction);
                break;
            case InteractionType.ASSIGN_TO_CASH_REGISTER:
                onAssignToCashRegister(interaction);
                break;
        }
    }

    private void onStartCustomerService(Interaction interaction) throws RTIexception {
        StartCustomerService objectInteraction = StartCustomerService.toInteractionObject(interaction);
        System.out.println("onStartCustomerService() " + objectInteraction.toString());

        decreaseQueueLength(objectInteraction.getCashRegisterId());
        increaseClientsServedCount(objectInteraction.getCashRegisterId());
        calculateAverageWaitingTime(objectInteraction.getClientId(), interaction.getTime());

        sendStatistics(objectInteraction.getCashRegisterId());
    }

    private void onAssignToCashRegister(Interaction interaction) throws RTIexception {
        AssignToCashRegister objectInteraction = AssignToCashRegister.toInteractionObject(interaction);
        System.out.println("onAssignToCashRegister() " + objectInteraction.toString());

        WaitingCustomer waitingCustomer = new WaitingCustomer(objectInteraction.getClientId(), objectInteraction.getCashRegisterId(), interaction.getTime());
        waitingCustomers.put(objectInteraction.getClientId(), waitingCustomer);

        increaseQueueLength(objectInteraction.getCashRegisterId());

        sendStatistics(objectInteraction.getCashRegisterId());
    }

    private void calculateAverageWaitingTime(int clientId, double stopWaitingTime) {
        WaitingCustomer waitingCustomer = waitingCustomers.get(clientId);
        double waitingTime = stopWaitingTime - waitingCustomer.getStartWaitingTime();

        if (!avgWaitingTimes.containsKey(waitingCustomer.getCashRegisterId())) {
            avgWaitingTimes.put(waitingCustomer.getCashRegisterId(), waitingTime);
        } else {
            Double avgWaitingTime = avgWaitingTimes.get(waitingCustomer.getCashRegisterId());
            int queueLength = servedCustomerCount.get(waitingCustomer.getCashRegisterId());
            double newAvgWaitingTime = (avgWaitingTime * (queueLength - 1) + waitingTime) / queueLength;
            avgWaitingTimes.put(waitingCustomer.getCashRegisterId(), newAvgWaitingTime);
        }

        waitingCustomers.remove(clientId);
    }

    private void increaseClientsServedCount(int cashRegisterId) {
        servedCustomerCount.merge(cashRegisterId, 1, Integer::sum);
    }

    private void increaseQueueLength(int cashRegisterId) {
        changeQueueLength(cashRegisterId, 1);
    }

    private void decreaseQueueLength(int cashRegisterId) {
        changeQueueLength(cashRegisterId, -1);
    }

    private void changeQueueLength(int cashRegisterId, int n) {
        queuesLength.merge(cashRegisterId, n, Integer::sum);
    }

    private void sendStatistics(int cashRegisterId) throws RTIexception {
        Double avgWaitingTime = avgWaitingTimes.getOrDefault(cashRegisterId, 0.0);
        System.out.println("avgWaitingTime = " + avgWaitingTime);
        Integer avgQueueLength = queuesLength.getOrDefault(cashRegisterId, 0);
        System.out.println("avgQueueLength = " + avgQueueLength);
        Integer servedCustomers = servedCustomerCount.getOrDefault(cashRegisterId, 0);
        System.out.println("servedCustomers = " + servedCustomers);


        Interaction calculateStatisticsInteraction = new CalculateStatistics(cashRegisterId, avgWaitingTime, avgQueueLength, servedCustomers).toInteraction();
        sendInteraction(calculateStatisticsInteraction);
    }

}
