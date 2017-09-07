package io.fotoapparat.parameter.selector;

import io.fotoapparat.parameter.Size;
import io.fotoapparat.util.CompareSizesByArea;
import java.util.Collection;
import java.util.Collections;

/**
 * Selector functions for {@link Size}.
 */
public class SizeSelectors {

  private static final CompareSizesByArea COMPARATOR_BY_AREA = new CompareSizesByArea();

  /**
   * @return {@link SelectorFunction} which always provides the biggest size.
   */
  public static SelectorFunction<Size> biggestSize() {
    return new SelectorFunction<Size>() {
      @Override public Size select(Collection<Size> items) {
        if (items.isEmpty()) {
          return null;
        }

        return Collections.max(items, COMPARATOR_BY_AREA);
      }
    };
  }

  /**
   * @return {@link SelectorFunction} which always provides the smallest size.
   */
  public static SelectorFunction<Size> smallestSize() {
    return new SelectorFunction<Size>() {
      @Override public Size select(Collection<Size> items) {
        if (items.isEmpty()) {
          return null;
        }

        return Collections.min(items, COMPARATOR_BY_AREA);
      }
    };
  }
}
