package basic;

import hla.rti.LogicalTime;
import hla.rti.LogicalTimeInterval;
import hla.rti.RTIexception;
import hla.rti13.java1.*;
import org.portico.impl.hla13.types.DoubleTime;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public abstract class BaseFederate {
    public static final String READY_TO_RUN = "ReadyToRun";
    public RTIambassador rtiamb;
    public BaseAmbasador fedamb;



    private void log( String message )
    {
        System.out.println( "ExampleFederate   : " + message );
    }

    private void waitForUser()
    {
        log( " >>>>>>>>>> Press Enter to Continue <<<<<<<<<<" );
        BufferedReader reader = new BufferedReader( new InputStreamReader(System.in) );
        try
        {
            reader.readLine();
        }
        catch( Exception e )
        {
            log( "Error while waiting for user input: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    private LogicalTime convertTime( double time )
    {
        return new DoubleTime( time );
    }

    public abstract void runFederate(String federateName);

    public abstract void publishAndSubscribe() throws RTIexception, NameNotFound, FederateNotExecutionMember, RTIinternalError, ObjectClassNotDefined, ConcurrentAccessAttempted, InteractionClassNotDefined, RestoreInProgress, SaveInProgress, FederateLoggingServiceCalls, AttributeNotDefined, ArrayIndexOutOfBounds, OwnershipAcquisitionPending;

    public abstract void sendInteraction();


}
