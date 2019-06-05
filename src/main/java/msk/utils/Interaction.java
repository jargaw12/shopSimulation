package msk.utils;

import hla.rti1516e.encoding.EncoderFactory;

import java.util.HashMap;
import java.util.Optional;

public class Interaction implements Comparable<Interaction> {

    private String name;
    private double time;
    private HashMap<String, InteractionParam> parameters = new HashMap<>();

    public Interaction(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addParameter(String name, byte[] value, String type) {
        if (!parameters.containsKey(name)) {
            parameters.put(name, new InteractionParam(name, value, type));
        }
    }

    public int getParamSize() {
        return parameters.size();
    }

    public byte[] getParameterValue(String name) {
        return Optional.of(parameters.get(name).getValue())
                .orElse(null);
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getTime() {
        return this.time;
    }


    @Override
    public int compareTo(Interaction o) {
        return Double.compare(this.time, o.time);
    }
}
