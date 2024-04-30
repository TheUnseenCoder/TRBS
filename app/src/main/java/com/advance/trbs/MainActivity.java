package com.advance.trbs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    ImageView google;
    EditText PasswordEt, UsernameEt;
    Button signUpButton, forgotPasswordButton;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UsernameEt = findViewById(R.id.etuser_name);
        PasswordEt = findViewById(R.id.etuser_pass);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);
        google = findViewById(R.id.btngoogle);

        // Initialize buttons
        signUpButton = findViewById(R.id.etsignup);
        forgotPasswordButton = findViewById(R.id.etforgotpass);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // Check if email is already stored
        String storedEmail = SharedPreferencesUtils.getStoredEmail(this);
        if (!storedEmail.isEmpty()) {
            // If email is stored, automatically populate the email field
            UsernameEt.setText(storedEmail);
            OnLogin(null);
        }

        // Set click listeners for buttons
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start SignUpChoices activity
                Intent intent = new Intent(MainActivity.this, SignUpChoices.class);
                startActivity(intent);
            }
        });
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start ForgotPass activity
                Intent intent = new Intent(MainActivity.this, SendOTP.class);
                startActivity(intent);
            }
        });
    }

    public void OnLogin(View view){
        Log.d("MainActivity", "OnLogin method called"); // Add logging statement
        String user_name = UsernameEt.getText().toString();
        String user_pass = PasswordEt.getText().toString();
        String type = "login";
        SharedPreferencesUtils.saveEmail(this, user_name);

        BackgroundWorker backgroundWorker = new BackgroundWorker(this);
        backgroundWorker.execute(type, user_name, user_pass);
    }

    void signIn() {
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    String email = account.getEmail();
                    String fullName = account.getDisplayName();

                    SharedPreferencesUtils.saveEmail(this, email);

                    // Call method to send data to server
                    sendDataToServer(email, fullName);
                }
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void sendDataToServer(final String email, final String fullName) {
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://capstone-it4b.com/TailoringSystem/includes/googlechecker.php";

        // Request a String response from the provided URL
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle response
                        if (response.equals("success")) {
                            // Redirect to MainActivity
                            Intent intent = new Intent(MainActivity.this, ProductList.class);
                            intent.putExtra("email", email);
                            intent.putExtra("fullname", fullName);
                            startActivity(intent);
                            finish(); // Close current activity
                        } else {
                            Toast.makeText(getApplicationContext(), "Error: " + response, Toast.LENGTH_LONG).show();
                            Log.d("Error", "Error: " + response);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error
                Toast.makeText(getApplicationContext(), "Error sending data", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("fullname", fullName);
                // Add other parameters as needed
                return params;
            }
        };

        // Add the request to the RequestQueue
        queue.add(stringRequest);
    }

}
