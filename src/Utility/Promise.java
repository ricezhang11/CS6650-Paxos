package Utility;

import java.io.Serializable;

// acceptors will return a Promise object back to the proposer
public class Promise implements Serializable {
    long sequenceNumber;
    String value;

    public Promise(long sn, String v) {
        this.sequenceNumber = sn;
        this.value = v;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public String getValue() {
        return value;
    }
}
