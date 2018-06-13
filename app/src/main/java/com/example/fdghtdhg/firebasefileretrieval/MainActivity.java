package com.example.fdghtdhg.firebasefileretrieval;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class MainActivity extends AppCompatActivity {


    Button fileSelect, fileUpload,fileFetch;
    TextView textView;

    FirebaseStorage firebaseStorage; //uploading files
    FirebaseDatabase firebaseDatabase; //saving urls

    Uri pdfuri;
    ProgressDialog pd;
    String fileName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileSelect = (Button) findViewById(R.id.fileSelect);
        fileUpload = (Button) findViewById(R.id.fileUpload);
        fileFetch = (Button) findViewById(R.id.fileFetch);


        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        fileSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPDF();
            }
        });

        fileUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pdfuri != null)
                {
                    uploadPDF(pdfuri);
                }
            }
        });

        fileFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,fetch.class));
            }
        });


    }

    private void selectPDF()
    {
        Intent i = new Intent();
        i.setType("application/pdf");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i,11);
}

    private void uploadPDF(Uri pdfuri)
    {
        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setTitle("Uploading..");
        pd.setProgress(0);
        pd.show();

        fileName= new File(pdfuri.getPath()).getName();
        StorageReference storageReference = firebaseStorage.getReference(); //returns root path
        storageReference.child("Pune University").child("Computer Engineering").child(fileName).putFile(pdfuri)
        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                @SuppressWarnings("VisibleForTests")
                String url = taskSnapshot.getDownloadUrl().toString();
                DatabaseReference databaseReference = firebaseDatabase.getReference();
                databaseReference.child("file URLS").child("Pune University").child("Computer Engineering").child(fileName).setValue(url)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(getBaseContext(),"Upload successful",Toast.LENGTH_SHORT).show();
                            pd.cancel();
                        }
                        else
                        {
                            Toast.makeText(getBaseContext(),"Upload unsuccessful",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        })
        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                @SuppressWarnings("VisibleForTests")
                int currentProgress = (int) ((taskSnapshot.getBytesTransferred() * 100) / taskSnapshot.getTotalByteCount());
                pd.setProgress(currentProgress);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 11 && resultCode == RESULT_OK && data != null)
        {
            pdfuri = data.getData();
            Toast.makeText(MainActivity.this,"File successfully selected",Toast.LENGTH_SHORT).show();
            textView = (TextView) findViewById(R.id.textView);
            textView.setText(""+data.getData().getLastPathSegment());
        }
        else
        {
            Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_SHORT).show();
        }
    }
}
