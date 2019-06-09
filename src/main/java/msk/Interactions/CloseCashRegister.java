package msk.Interactions;

import lombok.*;
import msk.utils.Interaction;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
@ToString
public class CloseCashRegister extends InteractionRoot {
    protected int cashRegisterId;


    public static CloseCashRegister toInteractionObject(Interaction interaction) {
        return (CloseCashRegister) CloseCashRegister.toInteractionObject(interaction, new CloseCashRegister());
    }

}
