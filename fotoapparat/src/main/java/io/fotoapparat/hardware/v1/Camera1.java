package io.fotoapparat.hardware.v1;

import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.view.SurfaceView;
import android.view.TextureView;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import io.fotoapparat.hardware.CameraDevice;
import io.fotoapparat.hardware.CameraException;
import io.fotoapparat.hardware.Capabilities;
import io.fotoapparat.hardware.orientation.OrientationUtils;
import io.fotoapparat.hardware.provider.AvailableLensPositionsProvider;
import io.fotoapparat.hardware.provider.V1AvailableLensPositionProvider;
import io.fotoapparat.hardware.v1.capabilities.CapabilitiesFactory;
import io.fotoapparat.lens.FocusResult;
import io.fotoapparat.log.Logger;
import io.fotoapparat.parameter.LensPosition;
import io.fotoapparat.parameter.Parameters;
import io.fotoapparat.parameter.RendererParameters;
import io.fotoapparat.parameter.Size;
import io.fotoapparat.photo.Photo;
import io.fotoapparat.preview.PreviewStream;

/**
 * Camera hardware driver for v1 {@link Camera} API.
 */
@SuppressWarnings("deprecation")
public class Camera1 implements CameraDevice {

    private final CapabilitiesFactory capabilitiesFactory;
    private final ParametersConverter parametersConverter;
    private final AvailableLensPositionsProvider availableLensPositionsProvider;
    private final Logger logger;

    private Camera camera;
    private int cameraId = -1;
    private PreviewStream1 previewStream;

    private Throwable lastStacktrace;
    private int imageRotation;

    public Camera1(Logger logger) {
        this.capabilitiesFactory = new CapabilitiesFactory();
        this.parametersConverter = new ParametersConverter();
        this.availableLensPositionsProvider = new V1AvailableLensPositionProvider();
        this.logger = logger;
    }

    private static void throwOnFailSetDisplaySurface(Object displaySurface, IOException e) {
        throw new CameraException("Unable to set display surface: " + displaySurface, e);
    }

    private static void throwOnFailSetParameters(Parameters parameters, Exception e) {
        throw new CameraException("Failed to set parameters: " + parameters, e);
    }

    @Override
    public void open(LensPosition lensPosition) {
        recordMethod();

        try {
            cameraId = cameraIdForLensPosition(lensPosition);
            camera = Camera.open(cameraId);
            previewStream = new PreviewStream1(camera);
        } catch (RuntimeException e) {
            throwOnFailedToOpenCamera(lensPosition, e);
        }

        camera.setErrorCallback(new Camera.ErrorCallback() {
            @Override
            public void onError(int error, Camera camera) {
                if (lastStacktrace != null) {
                    lastStacktrace.printStackTrace();
                }

                throw new IllegalStateException("Camera error code: " + error);
            }
        });
    }

    private void throwOnFailedToOpenCamera(LensPosition lensPosition, RuntimeException e) {
        throw new CameraException(
                "Failed to open camera with lens position: " + lensPosition + " and id: " + cameraId,
                e
        );
    }

    private int cameraIdForLensPosition(LensPosition lensPosition) {
        int numberOfCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = getCameraInfo(i);

            if (info.facing == facingForLensPosition(lensPosition)) {
                return i;
            }
        }

        return 0;
    }

    private int facingForLensPosition(LensPosition lensPosition) {
        switch (lensPosition) {
            case FRONT:
                return Camera.CameraInfo.CAMERA_FACING_FRONT;
            case BACK:
                return Camera.CameraInfo.CAMERA_FACING_BACK;
            default:
                throw new IllegalArgumentException("Camera is not supported: " + lensPosition);
        }
    }

    @Override
    public void close() {
        recordMethod();

        if (camera != null) {
            camera.release();
        }
    }

    @Override
    public void startPreview() {
        recordMethod();

        camera.startPreview();
    }

    @Override
    public void stopPreview() {
        recordMethod();

        camera.stopPreview();
    }

    @Override
    public void setDisplaySurface(Object displaySurface) {
        recordMethod();

        try {
            trySetDisplaySurface(displaySurface);
        } catch (IOException e) {
            throwOnFailSetDisplaySurface(displaySurface, e);
        }
    }

    @Override
    public void setDisplayOrientation(int degrees) {
        recordMethod();

        Camera.CameraInfo info = getCameraInfo(cameraId);

        imageRotation = computeImageOrientation(degrees, info);

        camera.setDisplayOrientation(
                computeDisplayOrientation(degrees, info)
        );
        previewStream.setFrameOrientation(imageRotation);
    }

    private int computeDisplayOrientation(int screenRotationDegrees,
                                          Camera.CameraInfo info) {
        return OrientationUtils.computeDisplayOrientation(
                screenRotationDegrees,
                info.orientation,
                info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT
        );
    }

    private int computeImageOrientation(int screenRotationDegrees,
                                        Camera.CameraInfo info) {
        return OrientationUtils.computeImageOrientation(
                screenRotationDegrees,
                info.orientation,
                info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT
        );
    }

    @Override
    public void updateParameters(Parameters parameters) {
        recordMethod();

        Camera.Parameters cameraParameters = parametersConverter.convert(
                parameters,
                camera.getParameters()
        );

        try {
            camera.setParameters(cameraParameters);
        } catch (Exception e) {
            throwOnFailSetParameters(parameters, e);
        }
    }

    @Override
    public Capabilities getCapabilities() {
        recordMethod();

        return capabilitiesFactory.fromParameters(
                camera.getParameters()
        );
    }

    private void trySetDisplaySurface(Object displaySurface) throws IOException {
        if (displaySurface instanceof TextureView) {
            camera.setPreviewTexture(((TextureView) displaySurface).getSurfaceTexture());
        } else if (displaySurface instanceof SurfaceView) {
            camera.setPreviewDisplay(((SurfaceView) displaySurface).getHolder());
        } else {
            throw new IllegalArgumentException("Unsupported display surface: " + displaySurface);
        }
    }

    @Override
    public Photo takePicture() {
        recordMethod();

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Photo> photoReference = new AtomicReference<>();

        camera.takePicture(
                null,
                null,
                null,
                new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        photoReference.set(
                                new Photo(data, imageRotation)
                        );

                        latch.countDown();
                    }
                }
        );

        try {
            latch.await();
        } catch (InterruptedException e) {
            // Do nothing
        }

        return photoReference.get();
    }

    @Override
    public PreviewStream getPreviewStream() {
        recordMethod();
        ensurePreviewStreamAvailable();

        return previewStream;
    }

    private void ensurePreviewStreamAvailable() {
        if (previewStream == null) {
            throw new IllegalStateException("Preview stream is null. Make sure camera is opened.");
        }
    }

    @Override
    public RendererParameters getRendererParameters() {
        recordMethod();

        return new RendererParameters(
                previewSize(),
                imageRotation
        );
    }

    @Override
    public FocusResult autoFocus() {
        recordMethod();

        final CountDownLatch latch = new CountDownLatch(1);

        try {
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    latch.countDown();
                }
            });
        } catch (Exception e) {
            throwOnFailAutoFocus(e);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            // Do nothing
        }

        return FocusResult.successNoMeasurement();
    }

    private void throwOnFailAutoFocus(Exception e) {
        throw new CameraException("Failed to perform autofocus using device " + cameraId, e);
    }

    @Override
    public void measureExposure() {
        // Do nothing. Not supported by Camera1.
    }

    @Override
    public List<LensPosition> getAvailableLensPositions() {
        return availableLensPositionsProvider.getAvailableLensPositions();
    }

    private Size previewSize() {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();

        return new Size(
                previewSize.width,
                previewSize.height
        );
    }

    @NonNull
    private Camera.CameraInfo getCameraInfo(int id) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(id, info);
        return info;
    }

    private void recordMethod() {
        lastStacktrace = new Exception();

        logger.log(
                lastStacktrace.getStackTrace()[1].getMethodName()
        );
    }

}
