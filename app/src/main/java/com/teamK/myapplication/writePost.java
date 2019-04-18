package com.teamK.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class writePost extends AppCompatActivity {


    private static final int MAX_LENGTH =100;
    //Variables
    private Toolbar toolbarWritePost;
    private EditText txt_postDesc;
    private ImageView img_postImage;
    private Button btn_postButton;
    private Uri postImageUri=null;
    private ProgressBar progress_post;
    private String currentUserid;
    private Bitmap compressedImageFile;

    //Firebase Variables
    private FirebaseFirestore firestore;
    private StorageReference storageRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        //initializations
        toolbarWritePost=findViewById(R.id.toolbar_writePost);
        setSupportActionBar(toolbarWritePost);
        getSupportActionBar().setTitle("Write a new post!");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storageRef= FirebaseStorage.getInstance().getReference();
        firestore= FirebaseFirestore.getInstance();
        mAuth= FirebaseAuth.getInstance();
        currentUserid= mAuth.getCurrentUser().getUid();
        txt_postDesc= findViewById(R.id.text_blog_post);
        btn_postButton= findViewById(R.id.btn_post);
        img_postImage= findViewById(R.id.img_selectPostImage);
        progress_post= findViewById(R.id.progress_post);

        //onclick Listeners
        img_postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startImagePicker();

            }
        });

        btn_postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String description= txt_postDesc.getText().toString();
                if(postImageUri==null && description.isEmpty())
                {
                    showMessage("Please Enter an Image and description");

                }else
                {
                    progress_post.setVisibility(View.VISIBLE);
                    btn_postButton.setVisibility(View.INVISIBLE);

                    //getting a random ImageName for each Images user uploads
                    final String imagename= UUID.randomUUID().toString();
                    final StorageReference imagePath= storageRef.child("post_images").child(imagename+".jpg");

                    imagePath.putFile(postImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imagePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {

                                    if(task.isSuccessful())
                                    {
                                        //for image thumbnail
                                        File newImagefile= new File(postImageUri.getPath());

                                        try {
                                            compressedImageFile= new Compressor(writePost.this).compressToBitmap(newImagefile);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        ByteArrayOutputStream baos= new ByteArrayOutputStream();
                                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                        byte[] thumbData= baos.toByteArray();



                                        UploadTask thumbnailImagePath= storageRef.child("post_images/thumbnails").child(imagename+".jpg").putBytes(thumbData);
                                        thumbnailImagePath.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });


                                        //uploading actual data
                                        String postImageDownloadUri= task.getResult().toString();
                                        Map<String,Object> postMap= new HashMap<>();

                                        postMap.put("image_url",postImageDownloadUri);
                                        postMap.put("desc",description);
                                        postMap.put("user_id",currentUserid);
                                        postMap.put("timestamp",FieldValue.serverTimestamp());



                                        firestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                if(task.isSuccessful())
                                                {
                                                    showMessage("Post sucessfully added");
                                                    goToMainActivity();

                                                }else{

                                                     showMessage(task.getException().getMessage());

                                                }
                                                progress_post.setVisibility(View.INVISIBLE);
                                                btn_postButton.setVisibility(View.VISIBLE);
                                            }
                                        });

                                    }else {

                                        progress_post.setVisibility(View.INVISIBLE);
                                        btn_postButton.setVisibility(View.VISIBLE);
                                        showMessage(task.getException().getMessage());

                                    }


                                }
                            });

                        }
                    });

                }


            }
        });

    }



    //send user to main activity after a successful post upload
    private void goToMainActivity() {

        Intent intent= new Intent(writePost.this, MainActivity.class);
        startActivity(intent);
        finish();

    }


    //prompt a message
    private void showMessage(String s) {
        Toast.makeText(writePost.this,s, Toast.LENGTH_SHORT).show();
    }


    //Image Picker Library in action
    private void startImagePicker() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512,512)
                .setAspectRatio(1, 1)
                .start(writePost.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                postImageUri=result.getUri();
                img_postImage.setImageURI(postImageUri);



            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }

        }

    } //End of on activity results method


    //Random String generator for image names
    //NOT USED IN THIS CODE
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

}
