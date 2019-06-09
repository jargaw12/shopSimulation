package msk.Interactions;

import lombok.*;
import msk.utils.Interaction;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
@ToString
public class OpenNewCashRegister extends InteractionRoot {
    protected int cashRegisterId;


    public static OpenNewCashRegister toInteractionObject(Interaction interaction) {
        return (OpenNewCashRegister) OpenNewCashRegister.toInteractionObject(interaction, new OpenNewCashRegister());
    }

}
