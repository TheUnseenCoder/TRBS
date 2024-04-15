package com.advance.trbs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ProductList extends AppCompatActivity implements ProductAdapter.OnProductClickListener {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> products;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product);
        EditText searchBar = findViewById(R.id.search_bar);
        Button loginButton = findViewById(R.id.login_button);
        // Initialize RecyclerView

        // Search functionality
        // Search functionality
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = searchBar.getText().toString().trim();
                    searchProducts(query);
                    return true;
                }
                return false;
            }
        });

        // Login button click listener
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle login button click
                // For example, start LoginActivity
                Intent intent = new Intent(ProductList.this, MainActivity.class);
                startActivity(intent);
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize data list
        products = new ArrayList<>();

        // Fetch data using Volley
        fetchProductsFromServer();
    }

    // Method to search products based on query using Volley
    private void searchProducts(String query) {
        // Check if the search query is empty
        if (TextUtils.isEmpty(query)) {
            // If empty, fetch all products again
            fetchProductsFromServer();
            return;
        }

        String url = "http://192.168.1.7/TailoringSystem/includes/search_product.php?query=" + query;

        // Create a request using Volley
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray productsArray = response.getJSONArray("products");
                            List<Product> searchResults = new ArrayList<>(); // Create a new list to store search results
                            for (int i = 0; i < productsArray.length(); i++) {
                                JSONObject jsonObject = productsArray.getJSONObject(i);
                                Product product = new Product();
                                product.setProductId(jsonObject.getString("product_id"));
                                product.setName(jsonObject.getString("name"));
                                product.setBasePrice(jsonObject.getDouble("base_price"));

                                String imageString = jsonObject.getString("image");
                                if (imageString.startsWith("[")) {
                                    // If the image string starts with '[', it's an array
                                    JSONArray imageArray = new JSONArray(imageString);
                                    List<String> images = new ArrayList<>();
                                    for (int j = 0; j < imageArray.length(); j++) {
                                        images.add(imageArray.getString(j));
                                    }
                                    product.setImage(TextUtils.join(",", images));
                                } else {
                                    // Otherwise, it's a single image URL
                                    product.setImage(imageString);
                                }

                                // Check if the product name contains the search query
                                if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                                    searchResults.add(product); // Add the product to the search results
                                }
                            }
                            // Update the dataset with the search results
                            products.clear();
                            products.addAll(searchResults);
                            // Notify the adapter of the dataset changes
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error
                Log.e("ProductList", "Error searching products: " + error.getMessage());
                Toast.makeText(ProductList.this, "Error searching products", Toast.LENGTH_SHORT).show();
            }
        });

        // Add the request to the RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }



    private void fetchProductsFromServer() {
        String url = "http://192.168.1.7/TailoringSystem/includes/products.php"; // Replace with your actual URL

        // Create a request using Volley
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Parse JSON response and populate products list
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);
                                Product product = new Product();
                                product.setProductId(jsonObject.getString("product_id"));
                                product.setName(jsonObject.getString("name"));
                                product.setBasePrice(jsonObject.getDouble("base_price"));
                                product.setImage(jsonObject.getString("image"));
                                products.add(product);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        // Create and set adapter
                        adapter = new ProductAdapter(ProductList.this, products, ProductList.this);
                        recyclerView.setAdapter(adapter);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error
                Toast.makeText(ProductList.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });

        // Add the request to the RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    @Override
    public void onProductClick(Product product) {
        // Handle product click here
        // For example, you can start the ProductDetailsActivity and pass the product_id
        Intent intent = new Intent(this, ProductDetailsActivity.class);
        intent.putExtra("PRODUCT_ID", product.getProductId());
        startActivity(intent);
    }
}
