package io.fotoapparat.hardware.operators;

import android.support.annotation.NonNull;
import io.fotoapparat.lens.FocusResult;
import io.fotoapparat.parameter.FocusArea;

/**
 * Performs auto focus.
 */
public interface AutoFocusOperator {

  /**
   * Performs auto focus. This is a blocking operation which returns the result of the operation
   * when auto focus completes.
   */
  FocusResult autoFocus();

  /**
   * Performs auto focus in given coordinates, works only w/ Camera1
   */
  FocusResult autoFocus(@NonNull FocusArea area);
}
