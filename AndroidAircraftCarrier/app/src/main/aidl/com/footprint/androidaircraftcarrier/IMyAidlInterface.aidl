// IMyAidlInterface.aidl
package com.footprint.androidaircraftcarrier;

// Declare any non-default types here with import statements
import com.footprint.androidaircraftcarrier.ITaskCallback;

interface IMyAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);


    /** Request the process ID of this service, to do evil things with it. */
    String getMsg();

    void registerCallback(ITaskCallback cb);
    void unregisterCallback(ITaskCallback cb);
}
