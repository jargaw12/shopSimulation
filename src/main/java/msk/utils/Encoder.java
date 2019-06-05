package msk.utils;

import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAfloat64BE;
import hla.rti1516e.encoding.HLAinteger32BE;

public class Encoder {
	
	public static byte[] encodeInt(EncoderFactory factory, int integer) {
		HLAinteger32BE value = factory.createHLAinteger32BE(integer);
		return value.toByteArray();
	}
	
	public static byte[] encodeDouble(EncoderFactory factory, double val) {
		HLAfloat64BE value = factory.createHLAfloat64BE(val);
		return value.toByteArray();
	}
	
	
	public static int decodeInt(EncoderFactory factory, byte[] bytes) {
		HLAinteger32BE value = factory.createHLAinteger32BE();
		try
		{
			value.decode( bytes );
		}
		catch( DecoderException de )
		{
			return 0;
		}
		return value.getValue();
	}
	
	public static double decodeDouble(EncoderFactory factory, byte[] bytes) {
		HLAfloat64BE value = factory.createHLAfloat64BE();
		try
		{
			value.decode( bytes );
		}
		catch( DecoderException de )
		{
			return 0;
		}
		return value.getValue();
	}
}
