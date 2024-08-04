/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.feproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import androidx.annotation.VisibleForTesting;
import androidx.camera.core.ImageProxy;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PoseLandmarkerHelper {


    public static class ResultBundle {
        private final List<PoseLandmarkerResult> results;
        private final long inferenceTime;
        private final int inputImageHeight;
        private final int inputImageWidth;

        public ResultBundle(List<PoseLandmarkerResult> results, long inferenceTime, int inputImageHeight, int inputImageWidth) {
            this.results = results;
            this.inferenceTime = inferenceTime;
            this.inputImageHeight = inputImageHeight;
            this.inputImageWidth = inputImageWidth;
        }

        public List<PoseLandmarkerResult> getResults() {
            return results;
        }

        public long getInferenceTime() {
            return inferenceTime;
        }

        public int getInputImageHeight() {
            return inputImageHeight;
        }

        public int getInputImageWidth() {
            return inputImageWidth;
        }
    }




    private static final String TAG = "PoseLandmarkerHelper";

    public static final int DELEGATE_CPU = 0;
    public static final int DELEGATE_GPU = 1;
    public static final float DEFAULT_POSE_DETECTION_CONFIDENCE = 0.5F;
    public static final float DEFAULT_POSE_TRACKING_CONFIDENCE = 0.5F;
    public static final float DEFAULT_POSE_PRESENCE_CONFIDENCE = 0.5F;
    public static final int DEFAULT_NUM_POSES = 1;
    public static final int OTHER_ERROR = 0;
    public static final int GPU_ERROR = 1;
    public static final int MODEL_POSE_LANDMARKER_FULL = 0;
    public static final int MODEL_POSE_LANDMARKER_LITE = 1;
    public static final int MODEL_POSE_LANDMARKER_HEAVY = 2;

    private float minPoseDetectionConfidence = DEFAULT_POSE_DETECTION_CONFIDENCE;
    private float minPoseTrackingConfidence = DEFAULT_POSE_TRACKING_CONFIDENCE;
    private float minPosePresenceConfidence = DEFAULT_POSE_PRESENCE_CONFIDENCE;
    private int currentModel = MODEL_POSE_LANDMARKER_FULL;
    private int currentDelegate = DELEGATE_CPU;
    private RunningMode runningMode = RunningMode.IMAGE;
    private Context context;
    private LandmarkerListener poseLandmarkerHelperListener;
    private PoseLandmarker poseLandmarker;

    public PoseLandmarkerHelper(float minPoseDetectionConfidence, float minPoseTrackingConfidence,
                                float minPosePresenceConfidence, int currentModel, int currentDelegate,
                                RunningMode runningMode, Context context,
                                LandmarkerListener poseLandmarkerHelperListener) {
        this.minPoseDetectionConfidence = minPoseDetectionConfidence;
        this.minPoseTrackingConfidence = minPoseTrackingConfidence;
        this.minPosePresenceConfidence = minPosePresenceConfidence;
        this.currentModel = currentModel;
        this.currentDelegate = currentDelegate;
        this.runningMode = runningMode;
        this.context = context;
        this.poseLandmarkerHelperListener = poseLandmarkerHelperListener;
        setupPoseLandmarker();
    }

    public PoseLandmarkerHelper(Context context, LandmarkerListener poseLandmarkerHelperListener) {
        this.context = context;
        this.poseLandmarkerHelperListener = poseLandmarkerHelperListener;
        setupPoseLandmarker();
    }

    public void clearPoseLandmarker() {
        if (poseLandmarker != null) {
            poseLandmarker.close();
            poseLandmarker = null;
        }
    }

    public boolean isClose() {
        return poseLandmarker == null;
    }

    public void setupPoseLandmarker() {
        BaseOptions.Builder baseOptionBuilder = BaseOptions.builder();

        // Use the specified hardware for running the model. Default to CPU
        if (currentDelegate == DELEGATE_CPU) {
            baseOptionBuilder.setDelegate(Delegate.CPU);
        } else if (currentDelegate == DELEGATE_GPU) {
            baseOptionBuilder.setDelegate(Delegate.GPU);
        }

        String modelName;
        switch (currentModel) {
            case MODEL_POSE_LANDMARKER_LITE:
                modelName = "pose_landmarker_lite.task";
                break;
            case MODEL_POSE_LANDMARKER_HEAVY:
                modelName = "pose_landmarker_heavy.task";
                break;
            default:
                modelName = "pose_landmarker_full.task";
                break;
        }

        baseOptionBuilder.setModelAssetPath(modelName);

        // Check if runningMode is consistent with poseLandmarkerHelperListener
        if (runningMode == RunningMode.LIVE_STREAM && poseLandmarkerHelperListener == null) {
            throw new IllegalStateException(
                    "poseLandmarkerHelperListener must be set when runningMode is LIVE_STREAM."
            );
        }

        try {
            BaseOptions baseOptions = baseOptionBuilder.build();
            PoseLandmarker.PoseLandmarkerOptions.Builder optionsBuilder =
                    PoseLandmarker.PoseLandmarkerOptions.builder()
                            .setBaseOptions(baseOptions)
                            .setMinPoseDetectionConfidence(minPoseDetectionConfidence)
                            .setMinTrackingConfidence(minPoseTrackingConfidence)
                            .setMinPosePresenceConfidence(minPosePresenceConfidence)
                            .setRunningMode(runningMode);

            // The ResultListener and ErrorListener are only used for LIVE_STREAM mode.
            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder
                        .setResultListener(this::returnLivestreamResult)
                        .setErrorListener(this::returnLivestreamError);
            }

            PoseLandmarker.PoseLandmarkerOptions options = optionsBuilder.build();
            poseLandmarker = PoseLandmarker.createFromOptions(context, options);
        } catch (IllegalStateException e) {
            if (poseLandmarkerHelperListener != null) {
                poseLandmarkerHelperListener.onError(
                        "Pose Landmarker failed to initialize. See error logs for details"
                );
            }
            Log.e(TAG, "MediaPipe failed to load the task with error: " + e.getMessage());
        } catch (RuntimeException e) {
            // This occurs if the model being used does not support GPU
            if (poseLandmarkerHelperListener != null) {
                poseLandmarkerHelperListener.onError(
                        "Pose Landmarker failed to initialize. See error logs for details", GPU_ERROR
                );
            }
            Log.e(TAG, "Image classifier failed to load model with error: " + e.getMessage());
        }
    }

    public void detectLiveStream(ImageProxy imageProxy, boolean isFrontCamera) {
        if (runningMode != RunningMode.LIVE_STREAM) {
            throw new IllegalArgumentException(
                    "Attempting to call detectLiveStream while not using RunningMode.LIVE_STREAM"
            );
        }
        long frameTime = SystemClock.uptimeMillis();

        Bitmap bitmapBuffer = Bitmap.createBitmap(
                imageProxy.getWidth(),
                imageProxy.getHeight(),
                Bitmap.Config.ARGB_8888
        );

//        imageProxy.use(() -> bitmapBuffer.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer()));
//        imageProxy.close();
        bitmapBuffer.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer());

        Matrix matrix = new Matrix();
        matrix.postRotate((float) imageProxy.getImageInfo().getRotationDegrees());

        // Flip image if user is using front camera
        if (isFrontCamera) {
            matrix.postScale(-1f, 1f, (float) imageProxy.getWidth(), (float) imageProxy.getHeight());
        }

        Bitmap rotatedBitmap = Bitmap.createBitmap(
                bitmapBuffer, 0, 0, bitmapBuffer.getWidth(), bitmapBuffer.getHeight(),
                matrix, true
        );

        MPImage mpImage = new BitmapImageBuilder(rotatedBitmap).build();
        detectAsync(mpImage, frameTime);
    }

    @VisibleForTesting
    public void detectAsync(MPImage mpImage, long frameTime) {
        if (poseLandmarker != null) {
            poseLandmarker.detectAsync(mpImage, frameTime);
        }
    }

//    public ResultBundle detectVideoFile(Uri videoUri, long inferenceIntervalMs) throws IOException {
//        if (runningMode != RunningMode.VIDEO) {
//            throw new IllegalArgumentException(
//                    "Attempting to call detectVideoFile while not using RunningMode.VIDEO"
//            );
//        }
//
//        long startTime = SystemClock.uptimeMillis();
//        boolean didErrorOccurred = false;
//
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        retriever.setDataSource(context, videoUri);
//        Long videoLengthMs = null;
//
//        String metadata = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//        if (metadata != null) {
//            videoLengthMs = Long.parseLong(metadata);
//        }
//
//        Bitmap firstFrame = retriever.getFrameAtTime(0);
//        Integer width = firstFrame != null ? firstFrame.getWidth() : null;
//        Integer height = firstFrame != null ? firstFrame.getHeight() : null;
//
//        if (videoLengthMs == null || width == null || height == null) return null;
//
//        List<PoseLandmarkerResult> resultList = new ArrayList<>();
//        long numberOfFramesToRead = videoLengthMs / inferenceIntervalMs;
//        long frameTime = 0L;
//
//        for (long i = 0; i < numberOfFramesToRead; i++) {
//            if (didErrorOccurred) break;
//            Bitmap videoFrame = retriever.getFrameAtTime(frameTime * 1_000L);
//            frameTime += inferenceIntervalMs;
//
//            if (videoFrame != null) {
//                MPImage mpImage = new BitmapImageBuilder(videoFrame).build();
//                try {
//                    if (poseLandmarker != null) {
//                        PoseLandmarkerResult result = poseLandmarker.detectForVideo(mpImage, frameTime);
//                        resultList.add(result);
//                    }
//                } catch (RuntimeException e) {
//                    Log.e(TAG, "An error occurred while getting the video frames: " + e.getMessage());
//                    didErrorOccurred = true;
//                }
//            }
//        }
//
//        retriever.release();
//        long inferenceTimePerFrameMs = (SystemClock.uptimeMillis() - startTime) / resultList.size();
//        return new ResultBundle(resultList, inferenceTimePerFrameMs, height, width);
//    }
//
//    public ResultBundle detectImageFile(Bitmap bitmap) {
//        if (runningMode != RunningMode.IMAGE) {
//            throw new IllegalArgumentException(
//                    "Attempting to call detectImageFile while not using RunningMode.IMAGE"
//            );
//        }
//        long startTime = SystemClock.uptimeMillis();
//
//        MPImage mpImage = new BitmapImageBuilder(bitmap).build();
//        List<PoseLandmarkerResult> resultList = new ArrayList<>();
//
//        if (poseLandmarker != null) {
//            resultList.add(poseLandmarker.detect(mpImage));
//        }
//
//        long inferenceTimePerImageMs = (SystemClock.uptimeMillis() - startTime) / resultList.size();
//        return new ResultBundle(resultList, inferenceTimePerImageMs, bitmap.getHeight(), bitmap.getWidth());
//    }

    private void returnLivestreamError(RuntimeException error) {
        if (poseLandmarkerHelperListener != null) {
            poseLandmarkerHelperListener.onError(error.getMessage());
        }
    }

    private void returnLivestreamResult(PoseLandmarkerResult result, MPImage input) {
        if (poseLandmarkerHelperListener != null) {
            poseLandmarkerHelperListener.onResults(result, input);
        }
    }

    public float getMinPoseDetectionConfidence() {
        return minPoseDetectionConfidence;
    }

    public float getMinPoseTrackingConfidence() {
        return minPoseTrackingConfidence;
    }

    public float getMinPosePresenceConfidence() {
        return minPosePresenceConfidence;
    }

    public int getCurrentModel() {
        return currentModel;
    }

    public int getCurrentDelegate() {
        return currentDelegate;
    }

    public RunningMode getRunningMode() {
        return runningMode;
    }

    public interface LandmarkerListener {
        void onError(String error);

        void onError(String error, int errorCode);

        void onResults(PoseLandmarkerResult result, MPImage input);
    }
}