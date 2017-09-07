package io.fotoapparat.hardware.v2.surface;

import android.graphics.SurfaceTexture;
import android.view.TextureView;
import io.fotoapparat.view.SurfaceTextureAvailabilityListener;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * Returns a {@link SurfaceTexture} from the {@link TextureView}.
 */
class GetSurfaceTextureTask
    implements SurfaceTextureAvailabilityListener.Listener, Callable<SurfaceTexture> {

  private final TextureView textureView;
  private final CountDownLatch latch = new CountDownLatch(1);
  private SurfaceTexture surfaceTexture;

  GetSurfaceTextureTask(TextureView textureView) {
    this.textureView = textureView;
  }

  @Override public SurfaceTexture call() {

    surfaceTexture = textureView.getSurfaceTexture();

    if (surfaceTexture == null) {
      textureView.setSurfaceTextureListener(new SurfaceTextureAvailabilityListener(this));
      try {
        latch.await();
      } catch (InterruptedException e) {
        // Do nothing
      }
    }

    return surfaceTexture;
  }

  @Override public void onSurfaceAvailable(SurfaceTexture surface) {
    this.surfaceTexture = surface;
    latch.countDown();
  }
}
