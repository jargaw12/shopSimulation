package msk.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class InteractionParameter {
    private String name;
    private byte[] value;
}
