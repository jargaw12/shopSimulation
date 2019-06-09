package msk.utils;

import msk.Interactions.*;

public class InteractionHelper {

    public static Class<? extends InteractionRoot> getInteractionClass(String interactionName) {
        switch (interactionName) {
            case InteractionType.ASSIGN_TO_CASH_REGISTER:
                return AssignToCashRegister.class;
            case InteractionType.CALCULATE_STATISTICS:
                return CalculateStatistics.class;
            case InteractionType.FINISH_CUSTOMER_SERVICE:
                return FinishCustomerService.class;
            case InteractionType.FINISH_SHOPPING:
                return FinishShopping.class;
            case InteractionType.GENERATE_NEW_CLIENT:
                return GenerateNewClient.class;
            case InteractionType.OPEN_NEW_CASH_REGISTER:
                return OpenNewCashRegister.class;
            case InteractionType.CLOSE_CASH_REGISTER:
                return CloseCashRegister.class;
            case InteractionType.START_CUSTOMER_SERVICE:
                return StartCustomerService.class;
            default:
                return null;
        }
    }
}
