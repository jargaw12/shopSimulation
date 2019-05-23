package customer;

import basic.BaseFederate;
import hla.rti.RTIexception;
import hla.rti.jlc.RtiFactoryFactory;
import hla.rti13.java1.*;

public class CustomerFederate extends BaseFederate {
    public void runFederate(String federateName) {

    }

    public void publishAndSubscribe() throws RTIexception, NameNotFound, FederateNotExecutionMember, RTIinternalError, ObjectClassNotDefined, ConcurrentAccessAttempted, InteractionClassNotDefined, RestoreInProgress, SaveInProgress, FederateLoggingServiceCalls, AttributeNotDefined, ArrayIndexOutOfBounds, OwnershipAcquisitionPending {
        int classHandle = rtiamb.getObjectClassHandle("ObjectRoot.A");
        int aaHandle = rtiamb.getAttributeHandle("aa", classHandle);
        int abHandle = rtiamb.getAttributeHandle("ab", classHandle);
        int acHandle = rtiamb.getAttributeHandle("ac", classHandle);


        AttributeHandleSet attributes =
                (AttributeHandleSet) RtiFactoryFactory.getRtiFactory().createAttributeHandleSet();
        attributes.add(aaHandle);
        attributes.add(abHandle);
        attributes.add(acHandle);

        rtiamb.publishObjectClass(classHandle, attributes);

        rtiamb.subscribeObjectClassAttributes(classHandle, attributes);

        int interactionHandle = rtiamb.getInteractionClassHandle("InteractionRoot.X");

        rtiamb.publishInteractionClass(interactionHandle);

        rtiamb.subscribeInteractionClass(interactionHandle);
    }

    public void sendInteraction() {

    }
}
