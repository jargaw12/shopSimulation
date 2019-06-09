package msk.Interactions;

import lombok.*;
import msk.utils.Interaction;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
@ToString
public class StartCustomerService extends InteractionRoot {
    protected int clientId;
    protected int cashRegisterId;


    public static StartCustomerService toInteractionObject(Interaction interaction) {
        return (StartCustomerService) StartCustomerService.toInteractionObject(interaction, new StartCustomerService());
    }

}
