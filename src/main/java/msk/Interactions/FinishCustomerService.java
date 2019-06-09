package msk.Interactions;

import lombok.*;
import msk.utils.Interaction;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
@ToString
public class FinishCustomerService extends InteractionRoot {
    protected int clientId;
    protected int cashRegisterId;


    public static FinishCustomerService toInteractionObject(Interaction interaction) {
        return (FinishCustomerService) FinishCustomerService.toInteractionObject(interaction, new FinishCustomerService());
    }

}
