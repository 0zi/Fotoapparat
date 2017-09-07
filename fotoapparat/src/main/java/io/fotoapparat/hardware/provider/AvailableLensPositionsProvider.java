package io.fotoapparat.hardware.provider;

import io.fotoapparat.parameter.LensPosition;
import java.util.List;

/**
 * Provides list of {@link LensPosition} which are available on the device.
 */
public interface AvailableLensPositionsProvider {

  /**
   * @return list of {@link LensPosition} which are available on the device.
   */
  List<LensPosition> getAvailableLensPositions();
}
