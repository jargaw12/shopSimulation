package basic;

import hla.rti13.java1.NullFederateAmbassador;

public class BaseAmbasador extends NullFederateAmbassador {
    protected double federateTime        = 0.0;
    protected double federateLookahead   = 1.0;

    protected boolean isRegulating       = false;
    protected boolean isConstrained      = false;
    protected boolean isAdvancing        = false;

    protected boolean isAnnounced        = false;
    protected boolean isReadyToRun       = false;

    protected boolean running 			 = true;
    protected int finishHandle;

    public BaseAmbasador() {
    }
}
