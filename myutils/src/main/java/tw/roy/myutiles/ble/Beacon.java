package tw.roy.myutiles.ble;

public class Beacon {
    private String UUID;
    private int majorID;
    private int minorID;
    private float rssi;

    public int getMajorID() {
        return majorID;
    }

    public int getMinorID() {
        return minorID;
    }

    public String getUUID() {
        return UUID;
    }

    public float getRssi() {
        return rssi;
    }

    public void setMajorID(int majorID) {
        this.majorID = majorID;
    }

    public void setMinorID(int minorID) {
        this.minorID = minorID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public void setRssi(float rssi) {
        this.rssi = rssi;
    }
}
