/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.35
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package sml;

public enum smlStopLocationFlags {
  sml_STOP_AFTER_SMALLEST_STEP(1 << 0),
  sml_STOP_AFTER_PHASE(1 << 1),
  sml_STOP_AFTER_DECISION_CYCLE(1 << 2);

  public final int swigValue() {
    return swigValue;
  }

  public static smlStopLocationFlags swigToEnum(int swigValue) {
    smlStopLocationFlags[] swigValues = smlStopLocationFlags.class.getEnumConstants();
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (smlStopLocationFlags swigEnum : swigValues)
      if (swigEnum.swigValue == swigValue)
        return swigEnum;
    throw new IllegalArgumentException("No enum " + smlStopLocationFlags.class + " with value " + swigValue);
  }

  private smlStopLocationFlags() {
    this.swigValue = SwigNext.next++;
  }

  private smlStopLocationFlags(int swigValue) {
    this.swigValue = swigValue;
    SwigNext.next = swigValue+1;
  }

  private smlStopLocationFlags(smlStopLocationFlags swigEnum) {
    this.swigValue = swigEnum.swigValue;
    SwigNext.next = this.swigValue+1;
  }

  private final int swigValue;

  private static class SwigNext {
    private static int next = 0;
  }
}

