package com.teamK.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class accountSetup extends AppCompatActivity {

    private Toolbar setuptoolbar;
    private CircleImageView profileImageSetup;
    private Uri mainImageUri = null;
    private String user_id; //current user id

    private EditText userNameText;
    private Button saveUserInfobtn;
    private ProgressBar settingsProgressbar;

    //Firebase Variables
    private FirebaseAuth mAuth;
    private StorageReference firebasestorageref;
    private FirebaseFirestore firestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);

        //initializations
        setuptoolbar = findViewById(R.id.account_setup_toolbar);
        setSupportActionBar(setuptoolbar);
        getSupportActionBar().setTitle("Account Setup");


        profileImageSetup = (CircleImageView) findViewById(R.id.profile_image_setup);
        userNameText = findViewById(R.id.user_name_text);
        saveUserInfobtn = findViewById(R.id.save_user_settings_btn);
        settingsProgressbar = findViewById(R.id.settings_progressbar);
        mAuth = FirebaseAuth.getInstance();
        user_id=mAuth.getCurrentUser().getUid();
        firebasestorageref = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();


        //show current userInfo

        getCurrentUserInfoToShow();




        //onclick Listeners

        //save username and Image to Firebase
        saveUserInfobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = userNameText.getText().toString();


                //check if the user has input his name and has a Profile pic uploaded
                if (!username.isEmpty() && mainImageUri != null) {

                    settingsProgressbar.setVisibility(View.VISIBLE);


                    final StorageReference image_path = firebasestorageref.child("profile_images").child(user_id + ".jpg");



                    //uploading the image to firebase storage and getting the download url
                    image_path.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(accountSetup.this, "Image Uploaded!", Toast.LENGTH_SHORT).show();


                            }else{
                                String errorUpload= task.getException().getMessage();
                                Toast.makeText(accountSetup.this, "Image Error: "+errorUpload, Toast.LENGTH_SHORT).show();
                            }


                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            image_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String profImageDownloadUrl= uri.toString();
                                    String username= userNameText.getText().toString();


                                    //creates a hashmap to store userdat in an object to set in the collection below
                                    Map<String,String> userInfoMap= new HashMap<>();
                                    userInfoMap.put("name",username);
                                    userInfoMap.put("image",profImageDownloadUrl);




                                    //adds a collection named Users and adds a document under the Users collection
                                    //takes user_id as user id to store individual data in the cloud firestore
                                    firestore.collection("Users").document(user_id).set(userInfoMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(accountSetup.this, "Updated the user Info!", Toast.LENGTH_SHORT).show();
                                                gotoHomePage();

                                            }
                                            else
                                            {
                                                String errorUpload= task.getException().getMessage();
                                                Toast.makeText(accountSetup.this, "Firestore Error: "+errorUpload, Toast.LENGTH_SHORT).show();

                                            }
                                            settingsProgressbar.setVisibility(View.INVISIBLE);
                                        }
                                    });


                                }
                            });
                        }
                    });


                } else {
                    Toast.makeText(accountSetup.this, "Please Enter a Profile pic and a Username", Toast.LENGTH_LONG).show();

                }

            }
        });


        //set up profile Image
        profileImageSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //For Marshmellow and upper users you need to take permissions to access the storage
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //checking if the user is permitted the access of storage
                    if (ContextCompat.checkSelfPermission(accountSetup.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        //Toast.makeText(accountSetup.this, "Permission Denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(accountSetup.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {

                        //using the crop Image library
                        startImagePicker();


                    }


                } else {

                    startImagePicker();
                }

            }
        });

    } //oncreate Method ends here



   //After Profiel update the user needs to get redirected to the main blog page
    private void gotoHomePage() {

               Intent goHome= new Intent(accountSetup.this,MainActivity.class);
               startActivity(goHome);
    }


    //current User Info retriever
    private void getCurrentUserInfoToShow() {
        settingsProgressbar.setVisibility(View.VISIBLE);
        firestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        String username= task.getResult().getString("name");
                        String imageUrl= task.getResult().getString("image");

                        userNameText.setText(username);
                        Glide.with(accountSetup.this).load(imageUrl).into(profileImageSetup);


                    }

                }else{
                    String errorUpload= task.getException().getMessage();
                    Toast.makeText(accountSetup.this, "Error getting User"+errorUpload, Toast.LENGTH_SHORT).show();

                }
                settingsProgressbar.setVisibility(View.INVISIBLE);

            }
        });
    }


    //Image Picker Library in action
    private void startImagePicker() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(accountSetup.this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();
                profileImageSetup.setImageURI(mainImageUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }

        }

    } //End of on activity results method


}
