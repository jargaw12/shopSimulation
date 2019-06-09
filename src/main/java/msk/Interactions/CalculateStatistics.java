package msk.Interactions;

import lombok.*;
import msk.utils.Interaction;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
@ToString
public class CalculateStatistics extends InteractionRoot {
    protected int cashRegisterId;
    protected double avgWaitingTime;
    protected double avgQueueLength;
    protected int numberOfClients;


    public static CalculateStatistics toInteractionObject(Interaction interaction) {
        return (CalculateStatistics) CalculateStatistics.toInteractionObject(interaction, new CalculateStatistics());
    }
}
