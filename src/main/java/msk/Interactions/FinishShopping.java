package msk.Interactions;

import lombok.*;
import msk.utils.Interaction;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
@ToString
public class FinishShopping extends InteractionRoot {
    protected int clientId;
    protected int basketSize;


    public static FinishShopping toInteractionObject(Interaction interaction) {
        return (FinishShopping) FinishShopping.toInteractionObject(interaction, new FinishShopping());
    }

}
