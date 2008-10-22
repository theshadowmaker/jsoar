/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.35
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package sml;

public enum smlSystemEventId {
  smlEVENT_BEFORE_SHUTDOWN(1),
  smlEVENT_AFTER_CONNECTION,
  smlEVENT_SYSTEM_START,
  smlEVENT_BEFORE_AGENTS_RUN_STEP,
  smlEVENT_SYSTEM_STOP,
  smlEVENT_INTERRUPT_CHECK,
  smlEVENT_SYSTEM_PROPERTY_CHANGED,
  smlEVENT_LAST_SYSTEM_EVENT(smlEVENT_SYSTEM_PROPERTY_CHANGED);

  public final int swigValue() {
    return swigValue;
  }

  public static smlSystemEventId swigToEnum(int swigValue) {
    smlSystemEventId[] swigValues = smlSystemEventId.class.getEnumConstants();
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (smlSystemEventId swigEnum : swigValues)
      if (swigEnum.swigValue == swigValue)
        return swigEnum;
    throw new IllegalArgumentException("No enum " + smlSystemEventId.class + " with value " + swigValue);
  }

  @SuppressWarnings("unused")
  private smlSystemEventId() {
    this.swigValue = SwigNext.next++;
  }

  @SuppressWarnings("unused")
  private smlSystemEventId(int swigValue) {
    this.swigValue = swigValue;
    SwigNext.next = swigValue+1;
  }

  @SuppressWarnings("unused")
  private smlSystemEventId(smlSystemEventId swigEnum) {
    this.swigValue = swigEnum.swigValue;
    SwigNext.next = this.swigValue+1;
  }

  private final int swigValue;

  private static class SwigNext {
    private static int next = 0;
  }
}

