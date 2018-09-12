package com.peersafe.base.core.coretypes;

import com.peersafe.base.core.coretypes.uint.UInt32;
import com.peersafe.base.core.serialized.BinaryParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

//public class RippleDate extends Date implements SerializedType {
public class RippleDate extends Date {
    public static long RIPPLE_EPOCH_SECONDS_OFFSET = 0x386D4380;
    static {
        /**
         * Magic constant tested and documented.
         *
         * Seconds since the unix epoch from unix time (accounting leap years etc)
         * at 1/January/2000 GMT
         */
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
        long computed = cal.getTimeInMillis() / 1000;
        assertEquals("1 Jan 2000 00:00:00 GMT", cal.getTime().toGMTString()); // TODO
        assertEquals(RippleDate.RIPPLE_EPOCH_SECONDS_OFFSET, computed);
    }

    private static void assertEquals(String s, String s1) {
        if (!s.equals(s1)) throw new AssertionError(String.format("%s != %s", s, s1));
    }
    private static void assertEquals(long a, long b) {
        if (a != b) throw new AssertionError(String.format("%s != %s", a, b));
    }

    private RippleDate() {
        super();
    }
    private RippleDate(long milliseconds) {
        super(milliseconds);
    }

    public long secondsSinceRippleEpoch() {
        return ((this.getTime() / 1000) - RIPPLE_EPOCH_SECONDS_OFFSET);
    }
    public static RippleDate fromSecondsSinceRippleEpoch(Number seconds) {
        return new RippleDate((seconds.longValue() + RIPPLE_EPOCH_SECONDS_OFFSET) * 1000);
    }
    public static RippleDate fromParser(BinaryParser parser) {
        UInt32 uInt32 = UInt32.translate.fromParser(parser);
        return fromSecondsSinceRippleEpoch(uInt32);
    }
    public static RippleDate now() {
        return new RippleDate();
    }
    //
	/**
     * local时间转换成UTC时间
     * @param localTime 格式："yyyy-MM-dd HH:mm:ss"
     * @return UTC milliseconds
     */
    public static long localToUTC(String localTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date localDate= null;
        try {
            localDate = sdf.parse(localTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long localTimeInMillis=localDate.getTime();
        /** long时间转换成Calendar */
        Calendar calendar= Calendar.getInstance();
        calendar.setTimeInMillis(localTimeInMillis);
        /** 取得时间偏移量 */
        int zoneOffset = calendar.get(java.util.Calendar.ZONE_OFFSET);
        /** 取得夏令时差 */
        int dstOffset = calendar.get(java.util.Calendar.DST_OFFSET);
        /** 从本地时间里扣除这些差量，即可以取得UTC时间*/
        calendar.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        /** 取得的时间就是UTC标准时间 */
        return (calendar.getTimeInMillis());
    }

	/**
     * local时间转换成UTC时间
     * @param UTC milliseconds
     * @return localTime 
     */
    public static String utcToLocal(long milliseconds)
    {
    	SimpleDateFormat localFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(TimeZone.getDefault());
        localFormater.setTimeZone(TimeZone.getDefault());
        String localTime = localFormater.format(milliseconds);
//        System.out.println(localTime);
        return localTime;
    }
    
    public static RippleDate iso8601ToChainsqlTime(String localTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date localDate= null;
        try {
            localDate = sdf.parse(localTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (new RippleDate(localDate.getTime()));
    }
    
    public static long secondsSinceRippleEpoch(String localTime)
    {
    	RippleDate date = iso8601ToChainsqlTime(localTime);
    	return date.secondsSinceRippleEpoch();
    }
    
    public static String localFromSecondsSinceRippleEpoch(long secondsSinceRippleEpoch)
    {
		RippleDate date = RippleDate.fromSecondsSinceRippleEpoch(secondsSinceRippleEpoch);
		String localTime = RippleDate.utcToLocal(date.getTime());
		return localTime;
    }

/*    @Override
    public Object toJSON() {
        return secondsSinceRippleEpoch();
    }

    @Override
    public byte[] toBytes() {
        return new UInt32(secondsSinceRippleEpoch()).toBytes();
    }

    @Override
    public String toHex() {
        return new UInt32(secondsSinceRippleEpoch()).toHex();
    }

    @Override
    public void toBytesSink(BytesSink to) {
        new UInt32(secondsSinceRippleEpoch()).toBytesSink(to);
    }*/
}
