package com.example.imgs.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.imgs.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginPage extends AppCompatActivity {
    private static final String TAG = LoginPage.class.getSimpleName();

    private EditText ETemail, ETpw;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        ETemail = findViewById(R.id.EmailET);
        ETpw = findViewById(R.id.PWET);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser checkuser = mAuth.getCurrentUser();

        if (checkuser != null){
            Log.d(TAG, "User is logged in");
            Intent profileintent = new Intent(LoginPage.this, Profile.class);
            startActivity(profileintent);
        }
        else{
            Log.d(TAG, "User not logged in");
        }
    }

    public void doSignIn(View view) {
        String s_email = ETemail.getText().toString();
        String s_pw = ETpw.getText().toString();

        Log.d(TAG, "doSignIn: "+s_email+"\t"+s_pw);

        if (s_email.equals("")){
            ETemail.setError("Enter Email");
        } else if (s_pw.equals("")){
            ETpw.setError("Enter PW");
        }else {
            mAuth.signInWithEmailAndPassword(s_email, s_pw)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: Signin Email Success");

                                Toast.makeText(LoginPage.this, "sign in success", Toast.LENGTH_SHORT).show();
                                Intent profileintent = new Intent(LoginPage.this, Profile.class);

                                startActivity(profileintent);
                            } else {
                                Toast.makeText(LoginPage.this, "Sign in failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void gotoSignUp(View view) {
        Intent SignUpIntent = new Intent(this, SignUp.class);
        startActivity(SignUpIntent);
    }
}