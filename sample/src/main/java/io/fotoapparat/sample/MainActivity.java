package io.fotoapparat.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.error.CameraErrorCallback;
import io.fotoapparat.hardware.CameraException;
import io.fotoapparat.hardware.provider.CameraProviders;
import io.fotoapparat.parameter.FocusArea;
import io.fotoapparat.parameter.LensPosition;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.photo.Photo;
import io.fotoapparat.result.PendingResult;
import io.fotoapparat.view.CameraView;

import static io.fotoapparat.log.Loggers.logcat;
import static io.fotoapparat.parameter.selector.AspectRatioSelectors.standardRatio;
import static io.fotoapparat.parameter.selector.FlashSelectors.autoFlash;
import static io.fotoapparat.parameter.selector.FlashSelectors.autoRedEye;
import static io.fotoapparat.parameter.selector.FlashSelectors.off;
import static io.fotoapparat.parameter.selector.FlashSelectors.torch;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.autoFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.fixed;
import static io.fotoapparat.parameter.selector.LensPositionSelectors.lensPosition;
import static io.fotoapparat.parameter.selector.Selectors.firstAvailable;
import static io.fotoapparat.parameter.selector.SizeSelectors.biggestSize;

public class MainActivity extends AppCompatActivity {

  private Fotoapparat camera;
  private CameraView cameraView;
  private Button snap;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    cameraView = (CameraView) findViewById(R.id.camera_view);
    snap = (Button) findViewById(R.id.camera_snap);

    focusOnTouch(cameraView);
    snapOnClick(snap);

    setupCamera();
  }

  private void setupCamera() {
    camera = createFotoapparat(LensPosition.BACK);
  }

  private void focusOnTouch(@NonNull View view) {
    view.setOnTouchListener(new View.OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        camera.focus(FocusArea.of(event.getX(), event.getY(), v.getWidth(), v.getHeight()));
        return false;
      }
    });
  }

  private void snapOnClick(@NonNull View view) {
    view.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        camera.takePictureInstant().toPendingResult().whenDone(new PendingResult.Callback<Photo>() {
          @Override public void onResult(Photo result) {
            Toast.makeText(getApplicationContext(), "Photo's captured", Toast.LENGTH_LONG).show();
          }
        });
      }
    });
  }

  private Fotoapparat createFotoapparat(LensPosition position) {
    return Fotoapparat.with(this)
        .into(cameraView)
        .previewScaleType(ScaleType.CENTER_CROP)
        .photoSize(standardRatio(biggestSize()))
        .lensPosition(lensPosition(position))
        .focusMode(firstAvailable(autoFocus(), fixed()))
        .flash(firstAvailable(autoRedEye(), autoFlash(), torch(), off()))
        .logger(logcat())
        .cameraProvider(CameraProviders.v1())
        .cameraErrorCallback(new CameraErrorCallback() {
          @Override public void onError(CameraException e) {
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
          }
        })
        .build();
  }

  @Override protected void onStart() {
    super.onStart();
    camera.start();
  }

  @Override protected void onStop() {
    super.onStop();
    camera.stop();
  }
}
