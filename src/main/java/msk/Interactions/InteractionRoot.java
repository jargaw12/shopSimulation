package msk.Interactions;

import msk.utils.Encoder;
import msk.utils.Interaction;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class InteractionRoot {

    protected static InteractionRoot toInteractionObject(Interaction interaction, InteractionRoot interactionObject) {
        Field[] fields = interactionObject.getClass().getDeclaredFields();
        Arrays.stream(fields)
                .forEach(field -> {
                    String type = field.getType().getSimpleName();
                    byte[] value = interaction.getParameterValue(field.getName());
                    if (value != null) {
                        try {
                            switch (type) {
                                case "String":
                                    String stringValue = Encoder.decodeString(value);
                                    field.set(interactionObject, stringValue);
                                    break;
                                case "int":
                                    int intValue = Encoder.decodeInt(value);
                                    field.setInt(interactionObject, intValue);
                                    break;
                                case "double":
                                    double doubleValue = Encoder.decodeDouble(value);
                                    field.setDouble(interactionObject, doubleValue);
                                    break;
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                });

        return interactionObject;
    }

    public Interaction toInteraction() {
        Interaction interaction = new Interaction(getClass().getSimpleName());
        Field[] fields = getClass().getDeclaredFields();
        Arrays.stream(fields)
                .forEach(field -> {
                    String type = field.getType().getSimpleName();
                    byte[] value = new byte[0];
                    try {
                        switch (type) {
                            case "String":
                                value = Encoder.encodeString((String) field.get(this));
                                break;
                            case "int":
                                value = Encoder.encodeInt(field.getInt(this));
                                break;
                            case "double":
                                value = Encoder.encodeDouble(field.getDouble(this));
                                break;
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    interaction.addParameter(field.getName(), value);
                });
        return interaction;
    }

    public List<String> getParamsNameList() {
        return Arrays.stream(getClass().getFields()).map(Field::getName).collect(Collectors.toList());
    }
}
