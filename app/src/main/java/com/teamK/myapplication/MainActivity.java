package com.teamK.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;



public class MainActivity extends AppCompatActivity {

    //variables
    private Toolbar mainToolbar;
    private FirebaseAuth mAuth;
    private FloatingActionButton btnAddPost;
    private FirebaseFirestore firestore;

    private String currentUserId;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        //initializations
        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        mAuth = FirebaseAuth.getInstance();
        btnAddPost= findViewById(R.id.btn_add_new_post);
        firestore= FirebaseFirestore.getInstance();

        //Set ActionBar
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Blogging App");

        //onclick Listeners


        //goto write post activity
        btnAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  writeNewPost();
            }
        });




    }


    //goes to write a new post activity
    private void writeNewPost() {
        Intent writePostIntent= new Intent(MainActivity.this,writePost.class);
        startActivity(writePostIntent);

    }


    //checks if any user is logged in or not
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {

            sendToLoginPage();
        }
        else{
            currentUserId= mAuth.getCurrentUser().getUid();

            firestore.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if(task.isSuccessful())
                    {
                        if(!task.getResult().exists())
                        {
                            accountSettings();
                        }

                    }
                    else
                    {
                        String error= task.getException().getMessage();
                        Toast.makeText(MainActivity.this,error,Toast.LENGTH_LONG).show();
                    }

                }
            });


        }

    }




    //Create menu items and inflate them in toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }



    //tasks to do when we select menu option items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_logout_btn:
                logOut();
                return true;
            case R.id.action_account_settings_btn:
                accountSettings();
                return true;
            default:
                return false;

        }


    }


    //go to settings page
    private void accountSettings() {

            Intent settingsIntent= new Intent(MainActivity.this,accountSetup.class);
            startActivity(settingsIntent);

    }


    //logs out the user
    private void logOut() {
        mAuth.signOut();
        sendToLoginPage();
    }


    //sends user to login page if not logged in
    private void sendToLoginPage() {
        Intent i = new Intent(MainActivity.this, loginActivity.class);
        startActivity(i);
        finish();
    }
}
