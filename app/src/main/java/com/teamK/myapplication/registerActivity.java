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

public class registerActivity extends AppCompatActivity {

    private EditText regEmail;
    private EditText regPassword;
    private EditText regConfirmPass;
    private Button   regBtn;
    private Button   gotologinBtn;
    private ProgressBar regProgress;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Initialize the views
        regEmail=(EditText)findViewById(R.id.reg_email);
        regPassword=(EditText)findViewById(R.id.reg_pass);
        regConfirmPass=(EditText)findViewById(R.id.reg_confirm_pass);
        regBtn=(Button)findViewById(R.id.reg_user_btn);
        gotologinBtn=(Button)findViewById(R.id.already_have_account_btn);
        regProgress=(ProgressBar)findViewById(R.id.reg_progress);
        mAuth= FirebaseAuth.getInstance();


        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=regEmail.getText().toString();
                String pass=regPassword.getText().toString();
                String confirmPass=regConfirmPass.getText().toString();

                if(!email.isEmpty()&& !pass.isEmpty() && !confirmPass.isEmpty())
                {
                    regProgress.setVisibility(View.VISIBLE);
                    if(!pass.equals(confirmPass)){
                        Toast.makeText(registerActivity.this,"Please Enter the same Password",Toast.LENGTH_LONG).show();
                    }else
                    {


                         mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                             @Override
                             public void onComplete(@NonNull Task<AuthResult> task) {
                                 if(task.isSuccessful()){
                                     //sets up account information fo newly created user
                                     gotoSetupPage();

                                 }else
                                 {
                                     String regerrormessage=task.getException().getMessage();
                                     Toast.makeText(registerActivity.this,regerrormessage,Toast.LENGTH_LONG).show();

                                 }
                                 regProgress.setVisibility(View.INVISIBLE);

                             }
                         });

                    }



                }else{
                    Toast.makeText(registerActivity.this,"Please Enter Emaill and pass! ",Toast.LENGTH_LONG).show();

                }


            }
        });


        //go to loginPage
        gotologinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  gotoLoginPage();
            }
        });



    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null)
        {
            sendToMainActivity();
        }
    }



    private void sendToMainActivity() {
        Intent mainIntent= new Intent(registerActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void gotoLoginPage() {

        Intent loginIntent= new Intent(registerActivity.this,loginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void gotoSetupPage() {
        Intent setupPage=  new Intent(registerActivity.this,accountSetup.class);
        startActivity(setupPage);
        finish();

    }

}
