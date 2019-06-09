package msk.Interactions;

import lombok.*;
import msk.utils.Interaction;

@AllArgsConstructor
@Getter
@ToString
public class GenerateNewClient extends InteractionRoot {

    public static GenerateNewClient toInteractionObject(Interaction interaction) {
        return (GenerateNewClient) GenerateNewClient.toInteractionObject(interaction, new GenerateNewClient());
    }


}
