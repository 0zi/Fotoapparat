package io.fotoapparat.util;

import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

/**
 * Created by alex
 */

public final class Dimensions {
  private static DisplayMetrics sMetrics;

  public static void init(@NonNull DisplayMetrics metrics) {
    sMetrics = metrics;
  }

  public static int dp(float dp) {
    return (int) (dp * sMetrics.density);
  }
}