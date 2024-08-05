package com.example.feproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Camera_Detect extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording = null;
    int cameraFacing = CameraSelector.LENS_FACING_BACK;
    private static final String TAG = "Camera_Detect";
    SQLiteDatabase database;
    int backward = 0;
    int forward = 0;
    int bending = 0;
    int correct = 0;
    long startTime = 0;
    long endTime = 0;


    private boolean isRecording = false;

    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private ExecutorService cameraExecutor;
    private long startTime1 = 0;
    private int check = 0;


    TextToSpeech textToSpeech;
    PreviewView previewView;
    ImageButton btnCapture, btnVideo,  btnChange;
    TextView timerTextView, PredictTextView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera_detect);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnCapture = findViewById(R.id.capture_btn);
        btnVideo = findViewById(R.id.video_btn);
        btnChange = findViewById(R.id.change_cam_btn);
        timerTextView = findViewById(R.id.video_time);
        previewView = findViewById(R.id.preview_view);
        PredictTextView = findViewById(R.id.predict_text);

        if (allPermissionsGranted()) {
            cameraExecutor = Executors.newSingleThreadExecutor();
            startCameraX(cameraFacing);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
            }, REQUEST_CAMERA_PERMISSION);
        }

        database = openOrCreateDatabase("statistics", MODE_PRIVATE, null);
        try {
            database.execSQL("CREATE TABLE IF NOT EXISTS statistics (day VARCHAR ,correct REAL, forward REAL, backward REAL, bending REAL)");
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });

        textToSpeech = new TextToSpeech(this, this);

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraFacing = cameraFacing == CameraSelector.LENS_FACING_FRONT ? CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT;
                startCameraX(cameraFacing);
            }
        });

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime1;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                timerTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

                timerHandler.postDelayed(this, 500);
            }
        };


        startCameraX(cameraFacing);
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void startCameraX(int cameraFacing) {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing)
                        .build();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture, videoCapture);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));


    }


    private void processImage(ImageProxy image) {
        // Chuyển đổi ImageProxy thành byte array hoặc base64 string
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        // Chuyển đổi thành base64
        String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);
        APIs.checkPoseAsync(base64Image, new APIs.Callback() {
            @Override
            public void onResult(String result) {
                String pred = result.split(":")[1];
                pred = pred.substring(1, pred.length() - 2);
                // Update UI with the result
                String finalPred = pred;
                String []list_pred = pred.split(",");
                for (String x: list_pred) {
                    if (x.equals("Correct Sitting")) {
                        correct ++;
                        if (check != 0) {
                            textToSpeech.speak(pred, TextToSpeech.QUEUE_FLUSH, null, null);
                            check = 0;
                        }
                    }
                    if (x.equals("Neck bending")) {
                        bending ++;
                    }
                    if (x.equals("Leaning forward")) {
                        forward ++;
                        if (check != 1) {
                            textToSpeech.speak(pred, TextToSpeech.QUEUE_FLUSH, null, null);
                            check = 1;
                        }
                    }
                    else if (x.equals("Leaning backward")) {
                        backward ++;
                        if (check != 2) {
                            textToSpeech.speak(pred, TextToSpeech.QUEUE_FLUSH, null, null);
                            check = 2;
                        }
                    }
                }
                runOnUiThread(() -> {
                    if (finalPred.equals("Correct Sitting")) {
                        PredictTextView.setBackgroundColor(getResources().getColor(R.color.green));
                        PredictTextView.setText(finalPred);
                    } else {
                        PredictTextView.setText(finalPred);
                        PredictTextView.setBackgroundColor(getResources().getColor(R.color.red));
                    }
                    Log.d("RESULTS", finalPred);
                });
            }
        });
    }

    private void takePhoto() {
        // Tạo file để lưu ảnh
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File photoFile = new File(storageDir, "my_photo_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis()) + ".jpg");

        // Thiết lập đầu ra cho file
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                // Ảnh đã được lưu
                // Bạn có thể thông báo cho người dùng biết
                Toast.makeText(Camera_Detect.this, "Image saved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                // Xử lý lỗi khi lưu ảnh
                Toast.makeText(Camera_Detect.this, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                exception.printStackTrace();
            }
        });
    }

    private void saveVideo() {
        Recording record1 = recording;
        if (record1 != null) {
            stopTimer();
            record1.stop();
            recording = null;
            return;
        }

        String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        MediaStoreOutputOptions outputOptions = new MediaStoreOutputOptions.Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            String msg = "Error: Please get audio permission";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            return;
        }
        recording = videoCapture.getOutput().prepareRecording(Camera_Detect.this, outputOptions).withAudioEnabled().start(ContextCompat.getMainExecutor(Camera_Detect.this), videoRecordEvent -> {
            if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                btnVideo.setEnabled(true);
            } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                    String msg = "Video capture succeeded: ";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                } else {
                    recording.close();
                    recording = null;
                    String msg = "Error: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getError();
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startRecording() {
        saveVideo();
        if (!isRecording) {
            isRecording = true;
            btnVideo.setImageResource(R.drawable.on_record);
            startTimer();
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(captureImageRunnable, 0);
        } else {
            isRecording = false;
            btnVideo.setImageResource(R.drawable.videocam);
            stopTimer();
            endTime = System.currentTimeMillis();
            timerHandler.removeCallbacks(captureImageRunnable);
            saveToDatabse();
        }
    }

    // Runnable for capturing images at regular intervals
    private Runnable captureImageRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRecording) {
                captureImageForPrediction();
                timerHandler.postDelayed(this, 1000); // Capture image every 0.5 seconds
            }
        }
    };

    // Capture image and process for prediction
    private void captureImageForPrediction() {
        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                processImage(image);
                image.close();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(Camera_Detect.this, "Capture failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void startTimer() {
        startTime1 = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
        timerTextView.setText("00:00");
        PredictTextView.setText("");
    }

    private void saveToDatabse() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());

        int bending_val = bending;
        int forward_val = forward;
        int correct_val = correct;
        int backward_val = backward;

        database.execSQL("INSERT INTO statistics(day, correct, forward, backward, bending) VALUES('" + currentDate + "', '" + correct_val + "', '" + forward_val + "', '" + backward_val + "', '" + bending_val + "')");
        bending = 0;
        correct = 0;
        forward = 0;
        backward = 0;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int langResult = textToSpeech.setLanguage(Locale.US); // Chọn ngôn ngữ, ví dụ: Locale.US

            if (langResult == TextToSpeech.LANG_MISSING_DATA ||
                    langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Ngôn ngữ không được hỗ trợ hoặc dữ liệu ngôn ngữ bị thiếu.");
            } else {
                // Đọc văn bản thành tiếng
                String textToRead = "Hello, welcome to the Sitting Posture Corrector.";
                textToSpeech.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        } else {
            Log.e(TAG, "Khởi tạo Text-to-Speech thất bại.");
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

}