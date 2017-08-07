package io.fotoapparat.routine.picture;

import android.support.annotation.NonNull;
import io.fotoapparat.hardware.CameraDevice;
import io.fotoapparat.result.PhotoResult;
import java.util.concurrent.Executor;

/**
 * Takes picture.
 */
public class TakePictureRoutine {

  private final CameraDevice cameraDevice;
  private final Executor cameraExecutor;

  public TakePictureRoutine(CameraDevice cameraDevice, Executor cameraExecutor) {
    this.cameraDevice = cameraDevice;
    this.cameraExecutor = cameraExecutor;
  }

  /**
   * Takes picture, returns immediately.
   *
   * @return {@link PhotoResult} which will deliver result asynchronously.
   */
  public PhotoResult takePicture() {
    return execute(new TakePictureTask(cameraDevice));
  }

  public PhotoResult takePicture(boolean shouldFocus) {
    return execute(new TakePictureTask(cameraDevice, shouldFocus));
  }

  private PhotoResult execute(@NonNull TakePictureTask task) {
    cameraExecutor.execute(task);
    return PhotoResult.fromFuture(task);
  }
}
