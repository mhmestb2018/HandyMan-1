package com.example.locale_lite;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class ProfileServiceProvider extends AppCompatActivity implements View.OnClickListener  {



    private static final int PICK_IMAGE_REQUEST = 234;

    private Button buttonChoose1,buttonChoose2;
    private Button buttonUpload1,buttonUpload2;
    private Button next;
    private ImageView imageView1,imageView2;
    private String fname;
    private String uploadId;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    //a Uri object to store file path
    private Uri filePath1;
    static int count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_service_provider);

        storageReference= FirebaseStorage.getInstance().getReference("uploads");
        databaseReference = FirebaseDatabase.getInstance().getReference("Images");
        //getting buttons from layout
        buttonChoose1 =  findViewById(R.id.chooser1);
        buttonUpload1 =  findViewById(R.id.uploader1);
        buttonChoose2 =  findViewById(R.id.chooser2);
        buttonUpload2 =  findViewById(R.id.uploader2);
        next = findViewById(R.id.profilenext_button);

        imageView1 = findViewById(R.id.imageView1);

        //attaching listener
        buttonChoose1.setOnClickListener(this);
        buttonUpload1.setOnClickListener(this);
        buttonChoose2.setOnClickListener(this);
        buttonUpload2.setOnClickListener(this);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileServiceProvider.this, asklocation.class);
                startActivity(intent);
            }
        });
    }

    //method to show file chooser
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath1 = data.getData();
            try {
                Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath1);
                imageView1.setImageBitmap(bitmap1);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    private void uploadFile() {
        //if there is a file to upload
        if (filePath1 != null) {
            //displaying a progress dialog while upload is going on
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            final StorageReference profileRef = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(filePath1));
            profileRef.putFile(filePath1)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog

                            progressDialog.dismiss();

                            count++;
                            if(count==1)
                            {
                                buttonUpload1.setBackgroundColor(Color.GRAY);
                                buttonChoose1.setClickable(false);
                                buttonUpload1.setClickable(false);
                                imageView1.setImageBitmap(null);
                                fname = "ProfilePic";
                            }
                            else if(count==2)
                            {
                                buttonUpload2.setBackgroundColor(Color.GRAY);
                                buttonChoose2.setClickable(false);
                                buttonUpload2.setClickable(false);
                                imageView1.setImageBitmap(null);
                                fname = "IDProof";
                            }
                            //and displaying a success toast
                            Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                            UploadImage uploadImage = new UploadImage(fname, profileRef.getDownloadUrl().toString());
                            uploadId = databaseReference.push().getKey();
                            databaseReference.child(uploadId).setValue(uploadImage);
                        }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying error message
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            double progress = 100.0 * (taskSnapshot.getBytesTransferred()/ taskSnapshot.getTotalByteCount());

                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");

                        }

                    });
        }
        //if there is not any file
        else {
            //you can display an error toast
            Toast.makeText(getApplicationContext(), "No file selected", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onClick(View view) {
        //if the clicked button is choose
        if (view == buttonChoose1) {
            showFileChooser();
        }
        //if the clicked button is upload
        else if (view == buttonUpload1) {
            uploadFile();
        }
        if (view == buttonChoose2) {
            showFileChooser();
        }
        //if the clicked button is upload
        else if (view == buttonUpload2) {
            uploadFile();
        }

    }
}





