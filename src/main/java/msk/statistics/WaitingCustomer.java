package msk.statistics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class WaitingCustomer {
    private int clientId;
    private int cashRegisterId;
    private double startWaitingTime;

}
