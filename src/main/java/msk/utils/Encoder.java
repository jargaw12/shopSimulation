package msk.utils;

import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.*;
import hla.rti1516e.exceptions.RTIinternalError;

public class Encoder {
    private static EncoderFactory factory;

    static {
        try {
            factory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
        } catch (RTIinternalError rtIinternalError) {
            rtIinternalError.printStackTrace();
        }
    }


    public Encoder() throws RTIinternalError {
    }

    public static byte[] encodeString(String string) {
        HLAASCIIstring value = factory.createHLAASCIIstring(string);
        return value.toByteArray();
    }

    public static byte[] encodeInt(int integer) {
        HLAinteger32BE value = factory.createHLAinteger32BE(integer);
        return value.toByteArray();
    }

    public static byte[] encodeDouble(double val) {
        HLAfloat64BE value = factory.createHLAfloat64BE(val);
        return value.toByteArray();
    }

    public static String decodeString(byte[] bytes) {
        HLAASCIIstring value = factory.createHLAASCIIstring();
        try {
            value.decode(bytes);
        } catch (DecoderException de) {
            return "";
        }
        return value.getValue();
    }

    public static int decodeInt(byte[] bytes) {
        HLAinteger32BE value = factory.createHLAinteger32BE();
        try {
            value.decode(bytes);
        } catch (DecoderException de) {
            return 0;
        }
        return value.getValue();
    }

    public static double decodeDouble(byte[] bytes) {
        HLAfloat64BE value = factory.createHLAfloat64BE();
        try {
            value.decode(bytes);
        } catch (DecoderException de) {
            return 0.0;
        }
        return value.getValue();
    }
}
