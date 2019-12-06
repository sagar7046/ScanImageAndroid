package com.example.scanimage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.concurrent.Executor;

import static androidx.camera.core.ImageCapture.CaptureMode;

public class FaceDetection extends AppCompatActivity {
    public Button btn_detect_face;
    public TextureView face_finder;
    public  ImageButton imageButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection);
        btn_detect_face=findViewById(R.id.btn_detect_face);
        face_finder=findViewById(R.id.face_finder);

        imageButton=findViewById(R.id.imgCapture);
        //Invoking camera preview method....
        cameraPreview();

        btn_detect_face.setOnClickListener(v -> {
            Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent,100);
        });
    }
    @SuppressLint("RestrictedApi")
    public  void cameraPreview()
    {
        CameraX.unbindAll();
//        Rational aspectRatio =new Rational(face_finder.getWidth(),face_finder.getHeight());
        Size screenSize = new Size(face_finder.getWidth(),face_finder.getHeight() );
        PreviewConfig.Builder pConfig = new PreviewConfig.Builder();
        Preview preview = new Preview(pConfig.setTargetResolution(screenSize).build());
        preview.setOnPreviewOutputUpdateListener(output -> {
            ViewGroup parent = (ViewGroup) face_finder.getParent();
            parent.removeView(face_finder);
            parent.addView(face_finder, 0);
            face_finder.setSurfaceTexture(output.getSurfaceTexture());
            updateTransform();
        });

        ImageCaptureConfig config =
                new ImageCaptureConfig.Builder()
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .build();
       final ImageCapture imgCap = new ImageCapture(config);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".jpg");
                imgCap.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        System.out.println("File Saved");
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.ImageCaptureError imageCaptureError, @NonNull String message, @Nullable Throwable cause) {
                        System.out.println("Error in saving file...");
                    }
                });
            }
        });
        CameraX.bindToLifecycle((LifecycleOwner)this, preview, imgCap);
    }

    public void updateTransform() {
        Matrix mx = new Matrix();
        float w = face_finder.getMeasuredWidth();
        float h = face_finder.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int)face_finder.getRotation();

        switch(rotation){
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float)rotationDgr, cX, cY);
        face_finder.setTransform(mx);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK) {
            Uri selectedImage = data.getData();

            // High-accuracy landmark detection and face classification
            FirebaseVisionFaceDetectorOptions options =
                    new FirebaseVisionFaceDetectorOptions.Builder()
                            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                            .build();


            //Select file from path to detect face....
            FirebaseVisionImage image;
            try {
                image = FirebaseVisionImage.fromFilePath(getApplicationContext(), selectedImage);
                FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(options);

                //Detect The image now using TaskList...

                Task<List<FirebaseVisionFace>> result =
                        detector.detectInImage(image)
                                .addOnSuccessListener(
                                        faces -> {
                                            // Task completed successfully
                                            // ...
                                            List<FirebaseVisionPoint> leftEyeContour=null;
                                            for (FirebaseVisionFace face : faces) {
                                                Rect bounds = face.getBoundingBox();
                                                float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                                float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

                                                // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                                // nose available):
                                                FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
                                                if (leftEar != null) {
                                                    FirebaseVisionPoint leftEarPos = leftEar.getPosition();
                                                    System.out.println("Lfet Ear"+leftEarPos.toString());
                                                }

                                                // If contour detection was enabled:
                                                leftEyeContour =
                                                        face.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints();

                                                List<FirebaseVisionPoint> upperLipBottomContour =
                                                        face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).getPoints();

                                                // If classification was enabled:
                                                if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                                    float smileProb = face.getSmilingProbability();
                                                    System.out.println("SmileProb"+smileProb);
                                                }
                                                if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                                    float rightEyeOpenProb = face.getRightEyeOpenProbability();
                                                }

                                                // If face tracking was enabled:
                                                if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
                                                    int id = face.getTrackingId();
                                                }
                                            }
                                            for (int i=0;i<leftEyeContour.size();i++)
                                            {
                                                System.out.println("Left eye Points:"+leftEyeContour.get(i));
                                            }

                                        })
                                .addOnFailureListener(
                                        e -> {
                                            // Task failed with an exception
                                            // ...
                                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
