package com.advance.trbs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class SendOTP extends AppCompatActivity {
    Button sendotp, signup;
    ProgressBar progressBar;
    EditText user_name;
    ImageButton backtologin;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_otp);

        // Initialize buttons
        sendotp = findViewById(R.id.btnsendOTP);
        backtologin = findViewById(R.id.btnBack);
        signup = findViewById(R.id.btnSignUp);
        user_name = findViewById(R.id.fpetuser_name);
        progressBar = findViewById(R.id.progress);
        // Set click listeners for buttons
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start SignUpChoices activity
                Intent intent = new Intent(SendOTP.this, SignUpChoices.class);
                startActivity(intent);
            }
        });

        // Set click listeners for buttons
        backtologin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start SignUpChoices activity
                Intent intent = new Intent(SendOTP.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Set click listeners for buttons
        sendotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "http://capstone-it4b.com/TailoringSystem/includes/otp.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                progressBar.setVisibility(View.GONE);
                                Log.d("Response", response); // Add logging statement for debugging
                                if (response.equals("success")){
                                    Intent intent = new Intent(getApplicationContext(), ForgotPass.class);
                                    intent.putExtra("user_name", user_name.getText().toString());
                                    startActivity(intent);
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        error.printStackTrace();
                    }
                }){
                    protected Map<String, String> getParams(){
                        Map<String, String> paramV = new HashMap<>();
                        paramV.put("user_name", user_name.getText().toString());
                        return paramV;
                    }
                };
                stringRequest.setRetryPolicy(new RetryPolicy() {
                    @Override
                    public int getCurrentTimeout() {
                        return 30000;
                    }

                    @Override
                    public int getCurrentRetryCount() {
                        return 30000;
                    }

                    @Override
                    public void retry(VolleyError error) throws VolleyError {

                    }
                });
                queue.add(stringRequest);
            }
        });



    }
}
