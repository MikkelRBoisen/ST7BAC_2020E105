package com.example.st7bac_2020e105.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.st7bac_2020e105.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText username, password;
    Button loginButton;

    //TEST
    Button automatiskLoginKnap;

    private FirebaseAuth mAuth;

    //progress dialog
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        username = (EditText)findViewById(R.id.edt_Username);
        password = (EditText)findViewById(R.id.edt_Password);
        loginButton = (Button)findViewById(R.id.btn_Login);

        //TEST
        automatiskLoginKnap = (Button)findViewById(R.id.btn_AutomaticLogin);
        automatiskLoginKnap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username.setText("test@gmail.com");
                password.setText("123456789");
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String usernameLogin = username.getText().toString();
                String passwordLogin = password.getText().toString();

                if (!Patterns.EMAIL_ADDRESS.matcher(usernameLogin).matches()) {
                    //invalid email
                    username.setError("Invalid username");
                    username.setFocusable(true);
                }
                else{
                    //valid email
                    loginUser(usernameLogin,passwordLogin);
                }
            }
        });

        pd = new ProgressDialog(this);
        pd.setMessage("Logging in...");
    }

    private void loginUser(String email, String passw) {
        // Viser progress dialog
        pd.setMessage("Logging In...");
        pd.show();
        mAuth.signInWithEmailAndPassword(email, passw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            pd.dismiss();
                            // Sign in success, update UI
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(new Intent(LoginActivity.this, EmergencyVehicleLocationActivity.class));
                            finish();
                        } else {
                            //sign in fails
                            Toast.makeText(LoginActivity.this, getString(R.string.AuthenticationFailedToast),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //error message
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        });
    }

}