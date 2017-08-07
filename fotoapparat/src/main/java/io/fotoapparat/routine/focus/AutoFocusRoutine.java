package io.fotoapparat.routine.focus;

import android.support.annotation.NonNull;
import io.fotoapparat.hardware.CameraDevice;
import io.fotoapparat.parameter.FocusArea;
import io.fotoapparat.result.FocusResult;
import io.fotoapparat.result.PendingResult;
import java.util.concurrent.Executor;

/**
 * Performs auto focus.
 */
public class AutoFocusRoutine {

  private final CameraDevice cameraDevice;
  private final Executor cameraExecutor;

  public AutoFocusRoutine(CameraDevice cameraDevice, Executor cameraExecutor) {
    this.cameraDevice = cameraDevice;
    this.cameraExecutor = cameraExecutor;
  }

  /**
   * Perform auto focus asynchronously.
   */
  public PendingResult<FocusResult> autoFocus() {
    return execute(new AutoFocusTask(cameraDevice));
  }

  public PendingResult<FocusResult> autoFocus(@NonNull FocusArea area) {
    return execute(new AutoFocusTask(cameraDevice, area));
  }

  private PendingResult<FocusResult> execute(@NonNull AutoFocusTask task) {
    cameraExecutor.execute(task);
    return PendingResult.fromFuture(task);
  }
}
