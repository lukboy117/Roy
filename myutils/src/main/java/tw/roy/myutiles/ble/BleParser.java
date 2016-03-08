package tw.roy.myutiles.ble;

import java.util.Arrays;

class BleParser {

    private static final String TAG = "BleParser";
    public String uuid;
    public int major;
    public int minor;

    public BleParser(String uuid, int major, int minor) {
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
    }

    public static BleParser parse(byte[] scanData) {

        /**
         * Calculate values of major & minor.
         */
        int major, minor;
        int startByte = 0;
        @SuppressWarnings("unused")
        boolean patternFound = false;
        while (startByte <= 5) {
            if (((int) scanData[startByte] & 0xff) == 0x4c
                    && ((int) scanData[startByte + 1] & 0xff) == 0x00
                    && ((int) scanData[startByte + 2] & 0xff) == 0x02
                    && ((int) scanData[startByte + 3] & 0xff) == 0x15) {
                // yes! This is an iBeacon
                patternFound = true;
                break;
            } else if (((int) scanData[startByte] & 0xff) == 0x2d
                    && ((int) scanData[startByte + 1] & 0xff) == 0x24
                    && ((int) scanData[startByte + 2] & 0xff) == 0xbf
                    && ((int) scanData[startByte + 3] & 0xff) == 0x16) {
                // this is an Estimote beacon
                major = 0;
                minor = 0;
            }
            startByte++;
        }

        major = (scanData[startByte + 20] & 0xff) * 0x100
                + (scanData[startByte + 21] & 0xff);
        minor = (scanData[startByte + 22] & 0xff) * 0x100
                + (scanData[startByte + 23] & 0xff);

        /**
         * Calculate value of UUID.
         */
        String uuid;
        int uuidIndex = 9;
        if (scanData[0] == 26) {
            uuidIndex = 6;
        }
        byte uuidArr[] = Arrays.copyOfRange(scanData, uuidIndex,
                uuidIndex + 16);
        uuid = String
                .format("%02X%02X%02X%02X-%02X%02X-%02X%02X-%02X%02X-%02X%02X%02X%02X%02X%02X",
                        new Object[]{Byte.valueOf(uuidArr[0]),
                                Byte.valueOf(uuidArr[1]),
                                Byte.valueOf(uuidArr[2]),
                                Byte.valueOf(uuidArr[3]),
                                Byte.valueOf(uuidArr[4]),
                                Byte.valueOf(uuidArr[5]),
                                Byte.valueOf(uuidArr[6]),
                                Byte.valueOf(uuidArr[7]),
                                Byte.valueOf(uuidArr[8]),
                                Byte.valueOf(uuidArr[9]),
                                Byte.valueOf(uuidArr[10]),
                                Byte.valueOf(uuidArr[11]),
                                Byte.valueOf(uuidArr[12]),
                                Byte.valueOf(uuidArr[13]),
                                Byte.valueOf(uuidArr[14]),
                                Byte.valueOf(uuidArr[15])});

//        MyLog.e(BleParser.TAG, "major=" + major + " , minor=" + minor);

        return new BleParser(uuid, major, minor);
    }
}
