package com.teamK.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class loginActivity extends AppCompatActivity {

    private EditText loginEmailText;
    private EditText loginPassText;
    private Button loginbtn;
    private Button signupbtn;
    private ProgressBar loginProgress;

    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmailText=(EditText) findViewById(R.id.login_email);
        loginPassText= (EditText) findViewById(R.id.login_password);
        loginbtn=(Button) findViewById(R.id.login_btn);
        signupbtn=(Button)findViewById(R.id.signup_btn);
        loginProgress=(ProgressBar) findViewById(R.id.login_progress);

        mAuth= FirebaseAuth.getInstance();

        //Login The user
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                String loginEmail= loginEmailText.getText().toString();
                String loginPassword=loginPassText.getText().toString();

                if(!loginEmail.isEmpty() && !loginPassword.isEmpty()){

                    loginProgress.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(loginEmail,loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                sentToMainActivity();

                            }else{
                                String errormessage=task.getException().getMessage();
                                Toast.makeText(loginActivity.this,"Error: "+errormessage,Toast.LENGTH_LONG).show();

                            }
                            loginProgress.setVisibility(View.INVISIBLE);
                        }
                    });


                }else{

                    Toast.makeText(loginActivity.this,"Please enter email and password!",Toast.LENGTH_LONG).show();

                }




            }
        });

        //take the user to signup page
        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 gotoRegisterpage();
            }
        });

    }







    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser= mAuth.getCurrentUser();

        if(currentUser!=null)
        {
             sentToMainActivity();
        }

    }

    private void sentToMainActivity() {

        Intent mainIntent= new Intent(loginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void gotoRegisterpage(){
        Intent registerIntent= new Intent(loginActivity.this,registerActivity.class);
        startActivity(registerIntent);
        finish();
    }
}




