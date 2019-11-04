package com.example.scanimage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.net.URI;

public class ScanActivity extends AppCompatActivity {
    Button btn_scan_text;
    public  String text="";
    CoordinatorLayout coordinatorLayout;
    View v;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        btn_scan_text=findViewById(R.id.btn_scan_text);
        btn_scan_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,100);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
        {
            Uri selectedImage=data.getData();
            FirebaseVisionImage firebaseVisionImage;
            try {


                firebaseVisionImage=FirebaseVisionImage.fromFilePath(getApplicationContext(), selectedImage);
                FirebaseVisionTextRecognizer detector= FirebaseVision.getInstance().getOnDeviceTextRecognizer();
                Task<FirebaseVisionText> result =
                        detector.processImage(firebaseVisionImage)
                                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                    @Override
                                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                        // Task completed successfully
                                        // ...

                                        for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                                            text = text+ block.getText()+"\n";
                                            Log.d("VISION",text);
                                            v=findViewById(R.id.btn_scan_text);
                                           // Snackbar.make(v,"Recognized Text:"+text,Snackbar.LENGTH_LONG).show();
                                        }
                                        Intent intent=new Intent(getApplicationContext(),Main3Activity.class);
                                        intent.putExtra("Data",text);
                                        startActivity(intent);
                                    }});
                text="";
            }
            catch(Exception ex)
            {
                System.out.println("Error in reading" +ex.getMessage());
            }

            Log.d("PATH",selectedImage.getPath());
        }
    }
}
