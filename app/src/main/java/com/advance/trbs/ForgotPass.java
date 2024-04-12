package com.advance.trbs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class ForgotPass extends AppCompatActivity {
    EditText password, confirm_password, otp;
    Button changepassword;
    ProgressBar progressBar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotpass);
        String user_name = getIntent().getExtras().getString("user_name");
        password = findViewById(R.id.fpetpassword);
        confirm_password = findViewById(R.id.fpetconfirm_password);
        otp = findViewById(R.id.etotp);
        changepassword = findViewById(R.id.btnchange_password);
        progressBar = findViewById(R.id.progress);

        // Set click listeners for buttons
        changepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "http://capstone-it4b.com/TailoringSystem/includes/reset-password.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                progressBar.setVisibility(View.GONE);
                                if (response.equals("success")){
                                    Toast.makeText(getApplicationContext(), "New Password Created Successfully!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
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
                        paramV.put("user_name", user_name);
                        paramV.put("password", password.getText().toString());
                        paramV.put("confirm_password", confirm_password.getText().toString());
                        paramV.put("otp", otp.getText().toString());
                        return paramV;
                    }
                };
                queue.add(stringRequest);
            }
        });
    }
}
