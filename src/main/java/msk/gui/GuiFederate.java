package msk.gui;

import hla.rti1516e.exceptions.*;
import msk.Interactions.CalculateStatistics;
import msk.basic.BaseFederate;
import msk.utils.Interaction;
import msk.utils.InteractionType;

public class GuiFederate extends BaseFederate {

    public static void main(String[] args) {
        GuiFederate guiFederate = new GuiFederate();
        GuiApplication guiApplication = new GuiApplication();
        new Thread(() -> {
            try {
                guiFederate.runFederate(new GuiAmbassador());
            } catch (Exception rtie) {
                rtie.printStackTrace();
            }
        }).start();
        guiApplication.run(args);
    }

    @Override
    protected void stepChange() {
//        System.out.println("GuiFederate - stepChange()");
    }

    @Override
    protected void publishAndSubscribe() throws RestoreInProgress, NameNotFound, InteractionClassNotDefined, SaveInProgress, FederateNotExecutionMember, RTIinternalError, FederateServiceInvocationsAreBeingReportedViaMOM, NotConnected, InvalidInteractionClassHandle {
        log("publishAndSubscribe()");
        subscribeInteraction(InteractionType.CALCULATE_STATISTICS);
    }

    @Override
    protected void handleInteraction(Interaction interaction) {
        System.out.println("handleInteraction()");
        if (InteractionType.CALCULATE_STATISTICS.equals(interaction.getName())) {
            onCalculateStatistics(interaction);
        }
    }

    private void onCalculateStatistics(Interaction interaction) {
        CalculateStatistics objectInteraction = CalculateStatistics.toInteractionObject(interaction);
        System.out.println("onCalculateStatistics() " + objectInteraction.toString());

        int seriesNo = objectInteraction.getCashRegisterId() - 1;
        GuiApplication.controller.chaneServedCustomersState(seriesNo, interaction.getTime(), objectInteraction.getNumberOfClients());
        GuiApplication.controller.chaneAvgQueueLengthState(seriesNo, interaction.getTime(), objectInteraction.getAvgQueueLength());
        GuiApplication.controller.chaneAvgWaitingTimeState(seriesNo, interaction.getTime(), objectInteraction.getAvgWaitingTime());
    }

}
