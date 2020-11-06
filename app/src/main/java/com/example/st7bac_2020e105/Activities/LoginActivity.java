package com.example.st7bac_2020e105.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.st7bac_2020e105.Adapter.VehicleItemAdapter;
import com.example.st7bac_2020e105.Model.VehicleItem;
import com.example.st7bac_2020e105.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText username, password;
    Button loginButton;

    //TEST
    Button automatiskLoginKnap;

    //dropdown menu
    Spinner vehicleSpinner;
    ArrayList<VehicleItem> customList;
    int width = 150;
    private String VehicleChosen;

    private FirebaseAuth mAuth;

    //progress dialog
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");
        mAuth = FirebaseAuth.getInstance();
        username = (EditText)findViewById(R.id.edt_Username);
        password = (EditText)findViewById(R.id.edt_Password);
        loginButton = (Button)findViewById(R.id.btn_Login);



        //TEST
        automatiskLoginKnap = (Button)findViewById(R.id.btn_AutomaticLogin);
        automatiskLoginKnap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username.setText("test");
                password.setText("123456789");
            }
        });

        //https://www.youtube.com/watch?v=UUGipy7h2l8
        //inflate spinner
        vehicleSpinner = findViewById(R.id.spinner);
        customList = getCustomList();
        VehicleItemAdapter adapter = new VehicleItemAdapter(this, customList);
        //if (vehicleSpinner == null) {
            vehicleSpinner.setAdapter(adapter);
            vehicleSpinner.setOnItemSelectedListener(this);
            vehicleSpinner.setSelection(0,false);
        //}

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameLogin = username.getText().toString();
                String emaillogin = usernameLogin+"@gmail.com";
                String passwordLogin = password.getText().toString();

                if (!Patterns.EMAIL_ADDRESS.matcher(emaillogin).matches()) {
                    //invalid email
                    username.setError("Invalid username");
                    username.setFocusable(true);
                }
                else{
                    //valid email
                    loginUser(emaillogin,passwordLogin, VehicleChosen);
                }
            }
        });

        pd = new ProgressDialog(this);
        pd.setMessage("Logging in...");
    }

    private ArrayList<VehicleItem> getCustomList() {
    customList = new ArrayList<>();
        customList.add(new VehicleItem("Choose Emergency vehicle...",0));
        customList.add(new VehicleItem("Ambulance",R.drawable.ambulance_noemergency));
        customList.add(new VehicleItem("Firetruck",R.drawable.firetruck_noemergency));
        customList.add(new VehicleItem("Medical car",R.drawable.medicalcar_noemergency));
    return customList;
    }

    private void loginUser(String email, String passw, final String vehicle) {
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
                            Intent loginIntent = new Intent(getApplicationContext(), EmergencyVehicleLocationActivity.class);
                            loginIntent.putExtra("Vehicle",vehicle);
                            startActivity(loginIntent);
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


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        try {
            LinearLayout linearLayout = findViewById(R.id.customSpinnerItemLayout);
            width = linearLayout.getWidth();
        } catch (Exception e) {
        }
        vehicleSpinner.setDropDownWidth(width);

        if (position > 0) {
            VehicleItem item = (VehicleItem)adapterView.getSelectedItem();
            if(item.getSpinneritemsName().equals("Ambulance")){
                Toast.makeText(this, item.getSpinneritemsName(), Toast.LENGTH_SHORT).show();
            }
            if (item.getSpinneritemsName().equals("Firetruck")){
                Toast.makeText(this, item.getSpinneritemsName(), Toast.LENGTH_SHORT).show();
            }
            if (item.getSpinneritemsName().equals("Medical car")){
                Toast.makeText(this, item.getSpinneritemsName(), Toast.LENGTH_SHORT).show();
            }
            VehicleChosen = item.getSpinneritemsName();
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}