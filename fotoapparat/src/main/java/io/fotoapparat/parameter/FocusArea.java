package io.fotoapparat.parameter;

import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import io.fotoapparat.util.Dimensions;

/**
 * Created by alex
 */

public final class FocusArea {
  private static final float FOCUS_COEFFICIENT = 1F;
  private static final float METERING_COEFFICIENT = 1.5F;
  private static final float DEFAULT_AREA_SIZE = 72F;

  private final float x;
  private final float y;
  private final int previewWidth;
  private final int previewHeight;

  public FocusArea(float x, float y, int previewWidth, int previewHeight) {
    this.x = x;
    this.y = y;
    this.previewWidth = previewWidth;
    this.previewHeight = previewHeight;
  }

  @NonNull public static FocusArea of(float x, float y, int previewWidth, int previewHeight) {
    return new FocusArea(x, y, previewWidth, previewHeight);
  }

  @NonNull public Rect asFocusRect() {
    return asRect(FOCUS_COEFFICIENT);
  }

  @NonNull public Rect asMeteringRect() {
    return asRect(METERING_COEFFICIENT);
  }

  @NonNull private Rect asRect(float coef) {
    final int size = Float.valueOf(Dimensions.dp(DEFAULT_AREA_SIZE) * coef).intValue();
    final int left = clamp((int) x - size / 2, 0, previewWidth - size);
    final int top = clamp((int) y - size / 2, 0, previewHeight - size);
    final RectF touch = new RectF(left, top, left + size, top + size);
    final float xScale = 2000F / previewWidth;
    final float yScale = 2000F / previewHeight;

    return new Rect(Math.round(touch.left * xScale - 1000), Math.round(touch.top * yScale - 1000),
        Math.round(touch.right * xScale - 1000), Math.round(touch.bottom * yScale - 1000));
  }

  private int clamp(int x, int min, int max) {
    return Math.max(min, Math.min(x, max));
  }
}
