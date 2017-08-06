package io.fotoapparat.routine.focus;

import android.support.annotation.NonNull;
import io.fotoapparat.hardware.CameraDevice;
import io.fotoapparat.parameter.FocusArea;
import io.fotoapparat.result.FocusResult;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Tries to perform auto focus and returns result as {@link FocusResult}.
 */
public class AutoFocusTask extends FutureTask<FocusResult> {

  public AutoFocusTask(final CameraDevice cameraDevice) {
    super(new Callable<FocusResult>() {
      @Override public FocusResult call() throws Exception {
        return performFocus(cameraDevice);
      }
    });
  }

  public AutoFocusTask(final CameraDevice cameraDevice, final FocusArea area) {
    super(new Callable<FocusResult>() {
      @Override public FocusResult call() throws Exception {
        return performFocus(cameraDevice, area);
      }
    });
  }

  @NonNull private static FocusResult performFocus(CameraDevice cameraDevice) {
    return cameraDevice.autoFocus().succeeded ? FocusResult.FOCUSED : FocusResult.UNABLE_TO_FOCUS;
  }

  @NonNull private static FocusResult performFocus(CameraDevice cameraDevice, FocusArea area) {
    return cameraDevice.autoFocus(area).succeeded ? FocusResult.FOCUSED
        : FocusResult.UNABLE_TO_FOCUS;
  }
}
