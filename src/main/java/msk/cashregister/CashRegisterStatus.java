package msk.cashregister;

public enum CashRegisterStatus {
    FREE(true),
    OCCUPIED(false);

    private boolean status;

    CashRegisterStatus(boolean status) {
        this.status = status;
    }
}
