package com.advance.trbs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.paypal.checkout.approve.Approval;
import com.paypal.checkout.approve.ApprovalData;
import com.paypal.checkout.approve.OnApprove;
import com.paypal.checkout.createorder.CreateOrder;
import com.paypal.checkout.createorder.CreateOrderActions;
import com.paypal.checkout.createorder.CurrencyCode;
import com.paypal.checkout.createorder.OrderIntent;
import com.paypal.checkout.createorder.UserAction;
import com.paypal.checkout.order.Amount;
import com.paypal.checkout.order.AppContext;
import com.paypal.checkout.order.CaptureOrderResult;
import com.paypal.checkout.order.OnCaptureComplete;
import com.paypal.checkout.order.OrderRequest;
import com.paypal.checkout.order.PurchaseUnit;
import com.paypal.checkout.paymentbutton.PaymentButtonContainer;
import com.squareup.picasso.Picasso;
import com.android.volley.NetworkResponse;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderDetailsActivity extends AppCompatActivity {
    private double totalAdditional = 0.0;
    private double totalAmount = 0.0;
    private double amount_paid = 0.0;
    private double remaining_balance = 0.0;
    private int orderId;
    private static final int PAYPAL_REQUEST_CODE = 123;
    private String buyerEmail;
    private int productId;
    private int quantity;
    private String size;
    private String variant;
    private double basePrice;
    double PercentageValue = 0.7; // Default to 70%

    private static final String TAG = "MyTag";
    PaymentButtonContainer paymentButtonContainer100, paymentButtonContainer70;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

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
                Intent intent = new Intent(OrderDetailsActivity.this, AddToCart.class);
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        orderId = intent.getIntExtra("ORDER_ID", 0);
        productId = intent.getIntExtra("PRODUCT_ID", 0);
        buyerEmail = intent.getStringExtra("EMAIL");
        quantity = intent.getIntExtra("QUANTITY", 0);
        amount_paid = intent.getDoubleExtra("AMOUNT_PAID", 0.00);
        remaining_balance = intent.getDoubleExtra("REMAINING_BALANCE", 0.00);
        String sizeWithPrice = intent.getStringExtra("SELECTED_SIZE");
        size = sizeWithPrice.split("\\(")[0].trim(); // Splitting the string and getting the first part
        variant = intent.getStringExtra("SELECTED_VARIANT");
        totalAdditional = intent.getIntExtra("TOTAL_ADDITIONAL", 0);
        String productName = intent.getStringExtra("PRODUCT_NAME");
        String productDescription = intent.getStringExtra("PRODUCT_DESCRIPTION");
        String basePriceString = intent.getStringExtra("BASE_PRICE"); // Retrieve as String
        String order_status = intent.getStringExtra("ORDER_STATUS"); // Retrieve as String
        ArrayList<String> imagesList = intent.getStringArrayListExtra("IMAGES");
        String payment_status = intent.getStringExtra("PAYMENT_STATUS");

        TextView productNameTextView = findViewById(R.id.textViewProductName);
        TextView productDescriptionTextView = findViewById(R.id.textViewProductDescription);
        TextView basePriceTextView = findViewById(R.id.baseprice);
        TextView variantTextView = findViewById(R.id.variant);
        TextView qtyTextView = findViewById(R.id.qty);
        TextView sizeTextView = findViewById(R.id.size);
        ImageView imageView = findViewById(R.id.images_container);


        // Load the first image from the imagesList using Picasso
        if (imagesList != null && !imagesList.isEmpty()) {
            Picasso.get().load(imagesList.get(0)).into(imageView);
        }

        // Convert base price from string to double
        basePrice = 0.0;
        try {
            basePrice = Double.parseDouble(basePriceString.replace("₱", ""));
        } catch (NumberFormatException e) {
            e.printStackTrace(); // Handle parse error if necessary
        }

        // Set the extracted values to the corresponding views
        productNameTextView.setText(productName);
        productDescriptionTextView.setText(productDescription);
        basePriceTextView.setText("Base Price: " + String.format("₱%.2f", basePrice));
        variantTextView.setText(productName);
        sizeTextView.setText("Size: " + size);
        variantTextView.setText("Variant: " + variant);
        qtyTextView.setText("Ordered Quantity: " + quantity);

        TextView OrderStatusTextView = findViewById(R.id.order_status);
        OrderStatusTextView.setText("Order Status: " + order_status);
        TextView PaymentStatusTextView = findViewById(R.id.payment_status);
        PaymentStatusTextView.setText("Payment Status: " + payment_status);
        Button orderReceived = findViewById(R.id.order_received);
        orderReceived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle login button click
                // For example, start LoginActivity
                updateOrderStatus(orderId);
            }
        });

        if (order_status.equals("Packing Order")) {
            OrderStatusTextView.setTextColor(Color.parseColor("#FE9705"));
        } else if (order_status.equals("Ready for pick up")) {
            OrderStatusTextView.setTextColor(Color.parseColor("#0569FF"));
        } else {
            OrderStatusTextView.setTextColor(Color.parseColor("#3AC430"));
        }
        if (payment_status.equals("Partially Paid")) {
            PaymentStatusTextView.setTextColor(Color.parseColor("#FE9705"));
            orderReceived.setVisibility(View.GONE);
        }else if(payment_status.equals("Fully Paid") && order_status.equals("Packing Order")){
            PaymentStatusTextView.setTextColor(Color.parseColor("#3AC430"));
            orderReceived.setVisibility(View.GONE);
        }else {
            PaymentStatusTextView.setTextColor(Color.parseColor("#3AC430"));
            orderReceived.setVisibility(View.VISIBLE);
        }

        TextView paid_amount = findViewById(R.id.amountpaid);
        paid_amount.setText("Amount Paid: ₱" + amount_paid);
        TextView balance_remaining = findViewById(R.id.balance);
        balance_remaining.setText("Remaining Balance: ₱" + remaining_balance);


        fetchTotalAdditional(productId, quantity, size, basePrice);
        if(remaining_balance == 0.00 || order_status.equals("Packing Order")){
            LinearLayout percent70Layout = findViewById(R.id.percent70Layout);
            percent70Layout.setVisibility(View.GONE);
        }else{
            LinearLayout percent70Layout = findViewById(R.id.percent70Layout);
            percent70Layout.setVisibility(View.VISIBLE);
        }

        paymentButtonContainer70 = findViewById(R.id.payment_button_container_70);
        paymentButtonContainer70.setup(
                new CreateOrder() {
                    @Override
                    public void create(@NotNull CreateOrderActions createOrderActions) {
                        Log.d(TAG, "create: ");
                        ArrayList<PurchaseUnit> purchaseUnits = new ArrayList<>();
                        purchaseUnits.add(
                                new PurchaseUnit.Builder()
                                        .amount(
                                                new Amount.Builder()
                                                        .currencyCode(CurrencyCode.USD)
                                                        .value(String.valueOf(remaining_balance))
                                                        .build()
                                        )
                                        .build()
                        );
                        OrderRequest order = new OrderRequest(
                                OrderIntent.CAPTURE,
                                new AppContext.Builder()
                                        .userAction(UserAction.PAY_NOW)
                                        .build(),
                                purchaseUnits
                        );
                        createOrderActions.create(order, (CreateOrderActions.OnOrderCreated) null);
                    }
                },
                new OnApprove() {
                    @Override
                    public void onApprove(@NotNull Approval approval) {
                        handleApproval(approval);

                    }
                }
        );
    }
    // Method to handle approval based on payment percent
    private void handleApproval(@NotNull Approval approval) {
        approval.getOrderActions().capture(new OnCaptureComplete() {
            @Override
            public void onCaptureComplete(@NotNull CaptureOrderResult result) {
                // Handle capture completion
                // Accessing the transaction ID (payment_id) directly from the captures array
                String resultString = String.format("CaptureOrderResult: %s", result);
                // Define a regular expression pattern to match the id
                Pattern pattern = Pattern.compile("Capture\\(id=(\\w+),");
                // Create a matcher with the input string
                Matcher matcher = pattern.matcher(resultString);
                if (matcher.find()) {
                    String transactionId = matcher.group(1);
                    // Further processing with the transaction ID
                    Toast.makeText(OrderDetailsActivity.this, "Successfully Paid", Toast.LENGTH_SHORT).show();

                    Log.e("Transaction ID: ", transactionId);
                    String payerId = String.format("%s", approval.getData().getPayerId());
                    String payerPaymentEmail1 = String.format("%s", approval.getData().getPayer().getEmail());
                    String payerGivenName = String.format("%s", approval.getData().getPayer().getName().getGivenName());
                    String payerFamilyName = String.format("%s", approval.getData().getPayer().getName().getFamilyName());
                    String payerPaymentEmail = payerPaymentEmail1.substring(payerPaymentEmail1.indexOf("=") + 1, payerPaymentEmail1.indexOf(",")).trim();

                    // Send order details to server based on payment percent
                    sendOrderDetailsToServer(buyerEmail, PercentageValue, productId, quantity, size, variant, basePrice, totalAdditional, orderId, payerId, transactionId, payerPaymentEmail, payerGivenName, payerFamilyName, remaining_balance);
                } else {
                    // Handle case when no match is found
                    Log.e("Transaction ID: ", "No match found");
                    Toast.makeText(OrderDetailsActivity.this, "Failed to process transaction ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchTotalAdditional(int productId, int quantity, String size, double basePrice) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://192.168.1.11/tailoringsystem/includes/buynow_getadditional.php?product_id=" + productId + "&quantity=" + quantity + "&size=" + size;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Check if the response array is not empty
                            if (response.length() > 0) {
                                // Get the first object from the array
                                JSONObject jsonObject = response.getJSONObject(0);
                                // Extract the total_additional value
                                totalAdditional = jsonObject.getDouble("total_additional"); // Update class-level variable
                                double Additional = jsonObject.getDouble("additional");

                                // Update the TextView with the retrieved value
                                TextView additional = findViewById(R.id.additional);
                                additional.setText("Additional for Size: ₱" + Additional);

                                // Update the TextView with the retrieved value
                                TextView total_additional = findViewById(R.id.total_additional);
                                total_additional.setText("Total Additional for Size: ₱" + totalAdditional);

                                // Calculate the total price after fetching additional data
                                TextView totalPrice = findViewById(R.id.totalprice);
                                double totalprice = (double) (basePrice * quantity);
                                totalPrice.setText("Total Price: ₱" + totalprice);

                                // Calculate the total amount after fetching additional data
                                TextView totalamount = findViewById(R.id.totalamount);
                                totalAmount = (double) ((Additional * quantity) + (basePrice * quantity));
                                totalamount.setText("Total Amount: ₱" + totalAmount);
                            } else {
                                // Handle empty response
                                Toast.makeText(OrderDetailsActivity.this, "No additional data found", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(OrderDetailsActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            Log.e("Volley Response", "Error parsing response: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(OrderDetailsActivity.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("Volley Error", "Error fetching data: " + error.getMessage());
                        Log.e("Product ID", String.valueOf(productId));
                        Log.e("Quantity", String.valueOf(quantity));
                        Log.e("Size", size);
                    }
                });

        // Add the request to the RequestQueue
        queue.add(request);
    }

    private void sendOrderDetailsToServer(String buyer_email, double paymentPercent, int product_id, int quantity, String size, String variant, double basePrice, double totalAdditional, int orderId, String payerId, String transactionId, String payerPaymentEmail, String payerGivenName, String payerFamilyName, double remaining_balance) {
        // Example:
        String url = "http://192.168.1.11/tailoringSystem/includes/pay_again.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle server response
                        Log.e("Response from server", response); // Log the response

                        if (response.trim().equalsIgnoreCase("success")) {
                            // Order placed successfully
                            Toast.makeText(OrderDetailsActivity.this, "Order is successfully paid", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(OrderDetailsActivity.this, OrderDetailsActivity.class);
                            startActivity(intent);
                            Log.e("Email", buyer_email);
                            Log.e("Product ID", String.valueOf(product_id));
                            Log.e("Quantity", String.valueOf(quantity));
                            Log.e("Size", size);
                            Log.e("Variant", variant);
                            Log.e("Base Price", String.valueOf(basePrice));
                            Log.e("Total Additional", String.valueOf(totalAdditional));
                            Log.e("Payment Percent", String.valueOf(paymentPercent));
                            Log.e("Order ID", String.valueOf(orderId));
                            Log.e("Transaction ID", transactionId);
                            Log.e("Payer ID", payerId);
                            Log.e("Payer Email", payerPaymentEmail);
                            Log.e("Payer FullName", payerGivenName + " " + payerFamilyName);
                        } else {
                            // Order placement failed
                            Toast.makeText(OrderDetailsActivity.this, response, Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        Toast.makeText(OrderDetailsActivity.this, "Failed to place order. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Add parameters for the order details
                params.put("email", buyer_email);
                params.put("product_id", String.valueOf(product_id));
                params.put("quantity", String.valueOf(quantity));
                params.put("size", size);
                params.put("variant", variant);
                params.put("base_price", String.valueOf(basePrice));
                params.put("total_additional", String.valueOf(totalAdditional));
                params.put("payment_percent", String.valueOf(paymentPercent));
                params.put("order_id", String.valueOf(orderId));
                params.put("transaction_id", transactionId);
                params.put("payer_id", payerId);
                params.put("payer_givenname", payerGivenName);
                params.put("payer_familyname", payerFamilyName);
                params.put("payer_email", payerPaymentEmail);
                params.put("remaining_balance", String.valueOf(remaining_balance));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void updateOrderStatus(int orderId) {
        // Example:
        String url = "http://192.168.1.11/tailoringSystem/includes/update_orderstatus.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle server response
                        Log.e("Response from server", response); // Log the response

                        if (response.trim().equalsIgnoreCase("success")) {
                            // Order placed successfully
                            Toast.makeText(OrderDetailsActivity.this, "Order status successfully updated!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(OrderDetailsActivity.this, Orders.class);
                            startActivity(intent);
                            Log.e("Order ID", String.valueOf(orderId));
                        } else {
                            // Order placement failed
                            Toast.makeText(OrderDetailsActivity.this, response, Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        Toast.makeText(OrderDetailsActivity.this, "Failed to place order. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("order_id", String.valueOf(orderId));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
