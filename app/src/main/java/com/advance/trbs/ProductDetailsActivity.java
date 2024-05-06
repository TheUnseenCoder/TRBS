package com.advance.trbs;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.util.Log;


public class ProductDetailsActivity extends AppCompatActivity {

    private List<String> sizesList = new ArrayList<>();
    private RadioGroup variantsContainer; // Added RadioGroup for variants

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // Initialize variants container
        variantsContainer = findViewById(R.id.variants_container);

        FloatingActionButton fab = findViewById(R.id.fab);
        String email = getIntent().getStringExtra("email");
        if (email == null || email.isEmpty()) {
            fab.setVisibility(View.GONE); // Hide the FAB
        } else {
            fab.setVisibility(View.VISIBLE); // Show the FAB
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExpandedMenuFragment expandedMenuFragment = new ExpandedMenuFragment();
                    expandedMenuFragment.show(getSupportFragmentManager(), "ExpandedMenuFragment");
                }
            });
        }

        String storedEmail = SharedPreferencesUtils.getStoredEmail(this);
        if (storedEmail == null || storedEmail.isEmpty()) {
            fab.setVisibility(View.GONE); // Hide the FAB
        } else {
            fab.setVisibility(View.VISIBLE); // Show the FAB
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExpandedMenuFragment expandedMenuFragment = new ExpandedMenuFragment();
                    expandedMenuFragment.show(getSupportFragmentManager(), "ExpandedMenuFragment");
                }
            });
        }

        ImageView addToCart = findViewById(R.id.addToCart);
        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle login button click
                // For example, start LoginActivity
                Intent intent = new Intent(ProductDetailsActivity.this, AddToCart.class);
                startActivity(intent);
            }
        });

        String productIdString = getIntent().getStringExtra("PRODUCT_ID");

        // Check if productIdString is not nullProdu
        if (productIdString != null) {
            try {
                // Parse the productIdString to an int
                int productId = Integer.parseInt(productIdString);

                // Fetch product details, sizes, and variants
                fetchProductDetails(productId);
                fetchProductSizes(productId);
                fetchProductVariants(productId);
                fetchCurrentQuantity(productId);
                fetchAndDisplayMeasurements(productId);
            } catch (NumberFormatException e) {
                // Handle the case where productIdString cannot be parsed to an int
                Toast.makeText(this, "Invalid product ID format", Toast.LENGTH_SHORT).show();
                // Optionally, close the activity or take appropriate action
                finish();
            }
        } else {
            // Handle the case where productIdString is null
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            // Optionally, close the activity or take appropriate action
            finish();
        }

        Button addToCartButton = findViewById(R.id.buttonAddToCart);
        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle add to cart button click
                addToCart();
            }
        });

        Button BuyNow = findViewById(R.id.buttonBuyNow);
        BuyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle add to cart button click
                BuyNow();
            }
        });

    }

    private void fetchProductDetails(int productId) {
        String url = "http://192.168.1.11/tailoringSystem/includes/product_details.php?product_id=" + productId;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject productDetails = response.getJSONObject("product_details");

                            // Display product name and description
                            TextView productNameTextView = findViewById(R.id.textViewProductName);
                            TextView productDescriptionTextView = findViewById(R.id.textViewProductDescription);
                            TextView productPrice = findViewById(R.id.baseprice);
                            productNameTextView.setText(productDetails.getString("product_name"));
                            productDescriptionTextView.setText(productDetails.getString("product_description"));
                            productPrice.setText("₱" + productDetails.getString("base_price"));

                            // Display images
                            displayImages(productDetails.getJSONArray("images"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void fetchProductSizes(int productId) {
        String url = "http://192.168.1.11/tailoringSystem/includes/product_sizes.php?product_id=" + productId;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Update sizes list
                            sizesList.clear();
                            for (int i = 0; i < response.length(); i++) {
                                sizesList.add(response.getString(i));
                            }

                            // Display sizes
                            displaySizes();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        Volley.newRequestQueue(this).add(jsonArrayRequest);
    }

    private void fetchProductVariants(int productId) {
        String url = "http://192.168.1.11/tailoringSystem/includes/product_variants.php?product_id=" + productId;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Clear existing radio buttons
                            variantsContainer.removeAllViews();

                            // Create RadioButtons for each variant
                            for (int i = 0; i < response.length(); i++) {
                                String variant = response.getString(i);
                                // Check if variant contains comma
                                if (variant.contains(",")) {
                                    // Split variant by comma and add each part as a separate RadioButton
                                    String[] variants = variant.split(", ");
                                    for (String v : variants) {
                                        addRadioButton(v);
                                    }
                                } else {
                                    // Add single RadioButton for the variant
                                    addRadioButton(variant);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        Volley.newRequestQueue(this).add(jsonArrayRequest);
    }

    private void addRadioButton(String variant) {
        // Create RadioButton for the variant
        RadioButton radioButton = new RadioButton(ProductDetailsActivity.this);
        radioButton.setText(variant);
        radioButton.setTextColor(getResources().getColor(android.R.color.white));

        // Add RadioButton to the RadioGroup
        variantsContainer.addView(radioButton);
    }

    private void displayImages(JSONArray imagesArray) {
        try {
            LinearLayout imagesContainer = findViewById(R.id.images_container);

            // Check if imagesArray is empty
            if (imagesArray.length() == 0) {
                // If no images available, add default image
                ImageView defaultImageView = new ImageView(ProductDetailsActivity.this);
                LinearLayout.LayoutParams defaultLayoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                defaultImageView.setLayoutParams(defaultLayoutParams);
                defaultImageView.setImageResource(R.drawable.default_product_image);
                defaultImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imagesContainer.addView(defaultImageView);
                return; // Exit the method
            }

            // Calculate the desired width for the ImageView
            int desiredWidth = 900; // Adjust as needed

            // Iterate through each image URL
            for (int i = 0; i < imagesArray.length(); i++) {
                String imageUrl = imagesArray.getString(i);

                // Create a new ImageView with fixed dimensions
                ImageView imageView = new ImageView(ProductDetailsActivity.this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        desiredWidth,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                layoutParams.setMargins(0, 0, 3, 0);
                imageView.setLayoutParams(layoutParams);
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                imageView.setTag(imageUrl);


                // Use Volley to load the image asynchronously
                ImageRequest imageRequest = new ImageRequest(imageUrl,
                        new Response.Listener<android.graphics.Bitmap>() {
                            @Override
                            public void onResponse(android.graphics.Bitmap response) {
                                // Set the loaded image to the ImageView
                                imageView.setImageBitmap(response);
                            }
                        }, 0, 0, null,
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // Handle error loading image
                                Log.e("Image Loading", "Error loading image: " + error.getMessage());
                            }
                        });

                // Add the ImageRequest to the Volley RequestQueue
                Volley.newRequestQueue(ProductDetailsActivity.this).add(imageRequest);

                // Add the ImageView to the imagesContainer
                imagesContainer.addView(imageView);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displaySizes() {
        try {
            RadioGroup sizesContainer = findViewById(R.id.sizes_container);
            sizesContainer.clearCheck();
            sizesContainer.removeAllViews();
            int textColor = getResources().getColor(android.R.color.white);

            // Create RadioButtons for each size
            for (String size : sizesList) {
                RadioButton radioButton = new RadioButton(ProductDetailsActivity.this);
                radioButton.setText(size);
                radioButton.setTextColor(textColor);

                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Uncheck all other RadioButtons
                        for (int i = 0; i < sizesContainer.getChildCount(); i++) {
                            View child = sizesContainer.getChildAt(i);
                            if (child instanceof RadioButton && child != view) {
                                ((RadioButton) child).setChecked(false);
                            }
                        }
                    }
                });

                // Add RadioButton to the RadioGroup
                sizesContainer.addView(radioButton);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchCurrentQuantity(int productId) {
        String url = "http://192.168.1.11/tailoringSystem/includes/product_quantity.php?product_id=" + productId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Parse the response JSON to get the current quantity
                            int currentQuantity = response.getInt("current_quantity");

                            // Update the TextView with the current quantity
                            TextView tvCurrentQty = findViewById(R.id.tvCurrentQty);
                            tvCurrentQty.setText("Current Qty: " + currentQuantity);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle errors
                        error.printStackTrace();
                    }
                });

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
    private void fetchAndDisplayMeasurements(int productId) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String FETCH_URL = "http://192.168.1.11/tailoringSystem/includes/fetch_matrix.php?product_id=%d";
        String url = String.format(FETCH_URL, productId);

        // Request a JSON response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        populateMeasurementsTable(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }


    private void populateMeasurementsTable(JSONObject measurements) {
        TableLayout measurementsTable = findViewById(R.id.measurementsTable);

        try {
            // Add header row
            TableRow headerRow = new TableRow(this);
            headerRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

            TextView sizeTextView = createTextView("Size");
            headerRow.addView(sizeTextView);

            // Extract measurement names dynamically from the first size
            JSONObject firstSizeObject = measurements.getJSONObject(measurements.keys().next());
            for (Iterator<String> measurementIterator = firstSizeObject.keys(); measurementIterator.hasNext();) {
                String measurementName = measurementIterator.next();
                TextView measurementNameTextView = createTextView(measurementName);
                headerRow.addView(measurementNameTextView);
            }

            // Add header for "Additional"
            TextView additionalHeader = createTextView("ADDITIONAL");
            headerRow.addView(additionalHeader);

            measurementsTable.addView(headerRow);

            // Iterate over each size in the measurements object
            for (Iterator<String> sizeIterator = measurements.keys(); sizeIterator.hasNext();) {
                String size = sizeIterator.next();
                JSONObject sizeObject = measurements.getJSONObject(size);

                // Create a new row for each size
                TableRow row = new TableRow(this);
                row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

                // Add size TextView to the row
                TextView sizeNameTextView = createTextView(size);
                row.addView(sizeNameTextView);

                double totalAdditional = 0.0; // Initialize total additional for the current row

                // Iterate over each measurement for the current size
                for (Iterator<String> measurementIterator = sizeObject.keys(); measurementIterator.hasNext();) {
                    String measurementName = measurementIterator.next();
                    JSONObject measurementObject = sizeObject.getJSONObject(measurementName);
                    String measurementSize = measurementObject.getString("measurement_size");
                    String additional = measurementObject.getString("additional");

                    // Create TextView for the measurement size and additional, then add to the row
                    TextView measurementSizeTextView = createTextView(measurementSize);
                    row.addView(measurementSizeTextView);

                    // Update total additional
                    totalAdditional += Double.parseDouble(additional);
                }

                // Add total additional to the row
                TextView totalAdditionalTextView = createTextView(String.valueOf(totalAdditional));
                row.addView(totalAdditionalTextView);

                // Add the row to the table
                measurementsTable.addView(row);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(16);
        textView.setTextColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER); // Set text alignment to center
        }
        textView.setPadding(40, 40, 40, 40);
        return textView;
    }

    // Method to handle adding the selected product to the cart
// Method to handle adding the selected product to the cart
    private void addToCart() {
        // Get the selected size
        RadioGroup sizesContainer = findViewById(R.id.sizes_container);
        int selectedSizeId = sizesContainer.getCheckedRadioButtonId();
        RadioButton selectedSizeRadioButton = findViewById(selectedSizeId);
        String selectedSize = selectedSizeRadioButton != null ? selectedSizeRadioButton.getText().toString() : "";

        // Get the selected variant
        int selectedVariantId = variantsContainer.getCheckedRadioButtonId();
        RadioButton selectedVariantRadioButton = findViewById(selectedVariantId);
        String selectedVariant = selectedVariantRadioButton != null ? selectedVariantRadioButton.getText().toString() : "";

        // Get the selected quantity
        EditText qtyEditText = findViewById(R.id.ETqty);
        String qtyString = qtyEditText.getText().toString();
        int qty = qtyString.isEmpty() ? 0 : Integer.parseInt(qtyString);

        // Get the product ID
        String productIdString = getIntent().getStringExtra("PRODUCT_ID");
        int productId = productIdString != null ? Integer.parseInt(productIdString) : 0;
        String email = SharedPreferencesUtils.getStoredEmail(this);
        // Check if required fields are selected
        if (selectedSize.isEmpty() || selectedVariant.isEmpty() || qty == 0 || productId == 0) {
            // Notify the user to select all required options
            Toast.makeText(this, "Please select size, variant, and enter quantity", Toast.LENGTH_SHORT).show();
            return;
        }
        // Check if email is empty or null
        if (email == null || email.isEmpty()) {
            // Notify the user to login first
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // If all required fields are selected, proceed with adding to cart
        // You can perform the HTTP request here to add the selected product to the cart
        // Construct the request URL with all the required parameters (size, variant, quantity, product ID)
        String addToCartUrl = "http://192.168.1.11/tailoringSystem/includes/addtocart.php?" +
                "product_id=" + productId +
                "&email=" + email +
                "&size=" + selectedSize +
                "&variant=" + selectedVariant +
                "&quantity=" + qty;

        // Create a request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create a string request to send the data to the server
        StringRequest stringRequest = new StringRequest(Request.Method.GET, addToCartUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the response from the server
                        // If the response contains "success", show success message
                        // Otherwise, show error message
                        if (response.contains("success")) {
                            Toast.makeText(ProductDetailsActivity.this, "Product added to cart successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ProductDetailsActivity.this, AddToCart.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(ProductDetailsActivity.this, response, Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle errors that occur during the request
                        Toast.makeText(ProductDetailsActivity.this, "Error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                });

        // Add the request to the request queue
        queue.add(stringRequest);
    }

    private void BuyNow() {
        // Get the selected size
        RadioGroup sizesContainer = findViewById(R.id.sizes_container);
        int selectedSizeId = sizesContainer.getCheckedRadioButtonId();
        RadioButton selectedSizeRadioButton = findViewById(selectedSizeId);
        String selectedSize = selectedSizeRadioButton != null ? selectedSizeRadioButton.getText().toString() : "";

        // Get the selected variant
        int selectedVariantId = variantsContainer.getCheckedRadioButtonId();
        RadioButton selectedVariantRadioButton = findViewById(selectedVariantId);
        String selectedVariant = selectedVariantRadioButton != null ? selectedVariantRadioButton.getText().toString() : "";

        // Get the selected quantity
        EditText qtyEditText = findViewById(R.id.ETqty);
        String qtyString = qtyEditText.getText().toString();
        int qty = qtyString.isEmpty() ? 0 : Integer.parseInt(qtyString);

        // Get other product details
        String productName = ((TextView) findViewById(R.id.textViewProductName)).getText().toString();
        String productDescription = ((TextView) findViewById(R.id.textViewProductDescription)).getText().toString();
        String basePrice = ((TextView) findViewById(R.id.baseprice)).getText().toString();
        // Get the product ID

        String productIdString = getIntent().getStringExtra("PRODUCT_ID");
        int productId = productIdString != null ? Integer.parseInt(productIdString) : 0;
        String email = SharedPreferencesUtils.getStoredEmail(this);

        List<String> imagesList = new ArrayList<>();
        LinearLayout imagesContainer = findViewById(R.id.images_container);

        if (imagesContainer.getChildCount() > 0) {
            ImageView firstImageView = (ImageView) imagesContainer.getChildAt(0);
            Object tag = firstImageView.getTag();
            String firstImageUrl = (tag != null) ? tag.toString() : "";
            imagesList.add(firstImageUrl);
        }



        // Check if required fields are selected
        if (selectedSize.isEmpty() || selectedVariant.isEmpty() || qty == 0 || qtyString.isEmpty()) {
            // Notify the user to select all required options
            Toast.makeText(this, "Please select size, variant, and enter quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        String addToCartUrl = "http://192.168.1.11/tailoringSystem/includes/seeqty.php?" +
                "product_id=" + productId +
                "&email=" + email +
                "&quantity=" + qty;

        // Create a request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create a string request to send the data to the server
        StringRequest stringRequest = new StringRequest(Request.Method.GET, addToCartUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the response from the server
                        // If the response contains "success", show success message
                        // Otherwise, show error message
                        if (response.contains("success")) {
                            Intent intent = new Intent(ProductDetailsActivity.this, PlaceOrder.class);

                            // Pass the product details as extras
                            intent.putExtra("EMAIL", email);
                            intent.putExtra("PRODUCT_ID", productId);
                            intent.putExtra("PRODUCT_NAME", productName);
                            intent.putExtra("PRODUCT_DESCRIPTION", productDescription);
                            intent.putExtra("BASE_PRICE", basePrice);
                            intent.putExtra("SELECTED_SIZE", selectedSize);
                            intent.putExtra("SELECTED_VARIANT", selectedVariant);
                            intent.putExtra("QUANTITY", qty);

                            // Serialize the imagesList and pass it as an ArrayList

                            intent.putStringArrayListExtra("IMAGES", new ArrayList<>(imagesList));


                            startActivity(intent);

                        } else {
                            Toast.makeText(ProductDetailsActivity.this, response, Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle errors that occur during the request
                        Toast.makeText(ProductDetailsActivity.this, "Error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                });

        // Add the request to the request queue
        queue.add(stringRequest);

    }

}

