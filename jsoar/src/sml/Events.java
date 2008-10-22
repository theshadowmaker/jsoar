/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.35
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package sml;

public class Events {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected Events(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(Events obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if(swigCPtr != 0 && swigCMemOwn) {
      swigCMemOwn = false;
      smlJNI.delete_Events(swigCPtr);
    }
    swigCPtr = 0;
  }

  public Events() {
    this(smlJNI.new_Events(), true);
  }

  public int ConvertToEvent(String pStr) {
    return smlJNI.Events_ConvertToEvent(swigCPtr, this, pStr);
  }

  public String ConvertToString(int id) {
    return smlJNI.Events_ConvertToString(swigCPtr, this, id);
  }

}
