package com.advance.trbs;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class ProductDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // Get the product_id from the intent
        String productId = getIntent().getStringExtra("PRODUCT_ID");

        // You can use the product_id to fetch the details of the selected product and display them
        // For now, let's just set the product_id to a TextView to demonstrate
        TextView productIdTextView = findViewById(R.id.product_id);
        productIdTextView.setText(productId);
    }
}
