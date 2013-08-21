package org.meandre.core.system.components.ext;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Objects of this class just denotes a stream delimiter of
 * a stream sequence.
 *
 * @author Xavier Llor&agrave;
 *
 */
public abstract class StreamDelimiter {

    private final int _streamId;

    /** Properties that can be attached to the stream delimiter */
    private final Map<String,Object> mapValues= new ConcurrentHashMap<String,Object>();

    public StreamDelimiter(int streamId) {
        _streamId = streamId;
    }

    public int getStreamId() {
        return _streamId;
    }

    /** Assigns a value to with the given key.
     *
     * @param sKey The key
     * @param oObject The value
     */
    public void put( String sKey, Object oObject ) {
        mapValues.put(sKey, oObject);
    }

    /** Gets the value for a given Key.
     *
     * @param sKey The key
     * @return The value
     */
    public Object get( String sKey ) {
        return mapValues.get(sKey);
    }

    /** Returns the set of available keys in this delimiter.
     *
     * @return The key set
     */
    public Set<String> keySet() {
        return mapValues.keySet();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null || other.getClass() != getClass()) return false;

        StreamDelimiter sd = (StreamDelimiter) other;

        return _streamId == sd._streamId;
    }

    @Override
    public int hashCode() {
        return _streamId;
    }
}
