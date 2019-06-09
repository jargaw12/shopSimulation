package msk.cashregister;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class Client {
    private int clientId;
    private double serviceTime;

    public void scanProduct() {
        serviceTime--;
    }

    public boolean basketIsEmpty() {
        return serviceTime <= 0;
    }


}
