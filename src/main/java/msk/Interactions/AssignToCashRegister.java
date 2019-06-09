package msk.Interactions;

import lombok.*;
import msk.utils.Interaction;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
@ToString
public class AssignToCashRegister extends InteractionRoot {
    protected int clientId;
    protected int cashRegisterId;

    public static AssignToCashRegister toInteractionObject(Interaction interaction) {
        return (AssignToCashRegister) AssignToCashRegister.toInteractionObject(interaction, new AssignToCashRegister());
    }
}
