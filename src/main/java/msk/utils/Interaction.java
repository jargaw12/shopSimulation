package msk.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public class Interaction implements Comparable<Interaction> {

    private String name;
    private double time;
    private HashMap<String, InteractionParameter> parameters = new HashMap<>();

    public Interaction(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addParameter(String name, byte[] value) {
        if (!parameters.containsKey(name)) {
            parameters.put(name, new InteractionParameter(name, value));
        }
    }

    public byte[] getParameterValue(String name) {
        return Optional.of(parameters.get(name).getValue())
                .orElse(null);
    }

    public HashMap<String, InteractionParameter> getParameters() {
        return parameters;
    }

    public double getTime() {
        return this.time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    @Override
    public int compareTo(Interaction o) {
        return Double.compare(this.time, o.time);
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ ");
        builder.append("[name=");
        builder.append(name);
        builder.append("], [time=");
        builder.append(time);
        builder.append("], params=");

        String parmas = parameters.values().stream()
                .map(entry -> entry.getName() + " : " + Arrays.toString(entry.getValue()))
                .collect(Collectors.joining(" | ", "{ ", " }"));

        builder.append(parmas);

        return builder.toString();
    }
}
