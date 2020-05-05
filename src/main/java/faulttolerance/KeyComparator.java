package faulttolerance;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Comparator;

/**
 * Key comparator for DB keys
 */
class KeyComparator implements Comparator<byte[]>, Serializable {

    /**
     * Compares two DB keys.
     *
     * @param key1 first key
     * @param key2 second key
     *
     * @return comparison result
     */
    public int compare(byte[] key1, byte[] key2) {
        return new BigInteger(key1).compareTo(new BigInteger(key2));
    }
}
