// ITaskCallback.aidl
package com.footprint.androidaircraftcarrier;

// Declare any non-default types here with import statements

interface ITaskCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    void actionCallback(int actionId);
}
