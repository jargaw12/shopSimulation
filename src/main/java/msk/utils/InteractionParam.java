package msk.utils;

public class InteractionParam {
    private String name;
    private byte[] value;
    private String type;

    public InteractionParam(String name, byte[] value, String type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public byte[] getValue() {
        return value;
    }

    public String getType() {
        return type;
    }
}
