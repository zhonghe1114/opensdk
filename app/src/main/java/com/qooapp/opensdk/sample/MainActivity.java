package com.qooapp.opensdk.sample;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qooapp.opensdk.QooAppOpenSDK;
import com.qooapp.opensdk.common.PaymentCallback;
import com.qooapp.opensdk.common.QooAppCallback;
import com.qooapp.opensdk.sample.model.Product;
import com.qooapp.opensdk.sample.util.CacheUtil;
import com.qooapp.opensdk.sample.util.SharedPrefsUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple that how to use the sdk of QooAppOpenSDK
 *
 * devel@qoo-app.com
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    //Your app id
    private String mAppId = "Your app id";
    public final static String KEY_APP_ID = "app_id";
    public static final String KEY_USERID = "user_id";
    private List<String> mDataList;
    //The QooAppOpenSDK created a singleton
    private QooAppOpenSDK mQooAppOpenSDK;
    private View mLayoutInit;
    private View mLayoutCheckLicense;
    private View mLayoutLogin;
    private View mLayoutProducts;
    private String mUserId;
    private ListView mListView;
    private List<Product> mProductsList = new ArrayList<>();
    private ProductAdapter mAdapter;
    private EditText mEdtAppId;
    private Button mBtnSelect;
    private Button mBtnProducts;
    private Button mBtnPurchased;
    private ProgressDialog progressDialog;
    private int mSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("QooAppOpenSDK: Initialize");

        mEdtAppId = this.findViewById(R.id.edt_appId);
        Button btnInit = this.findViewById(R.id.btn_init);
        mBtnSelect = this.findViewById(R.id.btn_select);
        mBtnProducts = this.findViewById(R.id.btn_products);
        mLayoutInit = this.findViewById(R.id.layout_init);
        mLayoutCheckLicense = this.findViewById(R.id.layout_verify);
        mLayoutLogin = this.findViewById(R.id.layout_login);
        mLayoutProducts = this.findViewById(R.id.layout_products);
        mBtnPurchased = this.findViewById(R.id.btn_purchased);
        mListView = this.findViewById(R.id.list_view);
        initLocalData();
        btnInit.setOnClickListener(v -> {
            mAppId = mEdtAppId.getText().toString();
            SharedPrefsUtils.setStringPreference(MainActivity.this, KEY_APP_ID, mAppId);
            CacheUtil.saveChannel(MainActivity.this, mAppId);
            initQooAppOpenSDK();
            showCheckLicense();

        });

        mBtnSelect.setOnClickListener(v -> showSelectChannel());
    }

    /**
     * 初始化QooAppOpenSDK
     */
    protected void initQooAppOpenSDK() {
        if (mAppId != null && !"".equals(mAppId)) {
            //Create a QooAppOpenSDK, you can use this way.
            mQooAppOpenSDK = QooAppOpenSDK.initialize(MainActivity.this, mAppId);
        } else {
            //Create a QooAppOpenSDK, you can use this way too.
            //Create a QooAppOpenSDK, you must provide params in AndroidManifest.xml
            mQooAppOpenSDK = QooAppOpenSDK.initialize(MainActivity.this);
        }
    }

    protected void initLocalData() {
        mDataList = CacheUtil.getChannels(MainActivity.this);
        mBtnSelect.setVisibility(mDataList.size() > 0 ? View.VISIBLE : View.GONE);
        mAppId = SharedPrefsUtils.getStringPreference(MainActivity.this, KEY_APP_ID);
//        mAppId = "BETA75d5fae60b5e11ea8d1b061ac4f87ae8";
//        mAppId = "BETAfad266b20b5411ea8b28061ac4f87ae8";
        mEdtAppId.setText(mAppId);
        mBtnSelect.setText(mAppId + "   ▼");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSelectChannel() {
        final String[] channels = new String[mDataList.size()];
        for (int i = 0; i < mDataList.size(); i++) {
            String info = mDataList.get(i);
            channels[i] = info;
        }
        new AlertDialog.Builder(this)
                .setTitle("Options")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setSingleChoiceItems(channels, mSelected,
                        (dialog, which) -> {
                            mSelected = which;
                            dialog.dismiss();
                            String channel = mDataList.get(which);
                            mEdtAppId.setText(channel);
                            mBtnSelect.setText(channels[which] + "▼");
                        }
                )
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void consumePurchase(String token, String purchase_id) {
        mQooAppOpenSDK.consume(new QooAppCallback() {
            @Override
            public void onSuccess(String response) {
                showToast(MainActivity.this, "Consumption successful!");
                Log.d("mQooAppOpenSDK", "response = "+response);
                mQooAppOpenSDK.closePaymentUI();
            }

            @Override
            public void onError(String error) {
                Log.e("mQooAppOpenSDK", "error = "+error);
                showToast(MainActivity.this, "Consuming error:" + error);
            }
        }, purchase_id, token);
    }

    private void showToast(Context context, CharSequence text) {
        if (context == null || text == null) return;
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void login(View view) {
        mQooAppOpenSDK.login(new QooAppCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("mQooAppOpenSDK", "response = "+response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    mUserId = jsonObject.getJSONObject("data").getString("user_id");
                    SharedPrefsUtils.setStringPreference(MainActivity.this, KEY_USERID, mUserId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                displayResult(response);
            }

            @Override
            public void onError(String error) {
                Log.e("mQooAppOpenSDK", "error = "+error);
                displayResult(error);
            }
        });
    }

    /**
     * checkLicense
     */
    public void checkLicense(View view) {
        mQooAppOpenSDK.checkLicense(new QooAppCallback() {

            @Override
            public void onSuccess(String response) {
                // verification succeed
                Log.d("mQooAppOpenSDK", "response = "+response);
                displayResult(response);
            }

            @Override
            public void onError(String error) {
                Log.e("mQooAppOpenSDK", "error = "+error);
                // For unknown reason, verification cannot be done.
                // Please disallow access for proper protection.
                displayResult(error);
            }
        });
    }

    /**
     * 獲取商品列表
     */
    public void queryProducts(View view) {
        showProgress();
        mQooAppOpenSDK.queryProducts(new QooAppCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("mQooAppOpenSDK", "response = "+response);
                dismissProgress();
                displayResult(response);
            }

            @Override
            public void onError(String error) {
                Log.e("mQooAppOpenSDK", "error = "+error);
                dismissProgress();
                displayResult(error);
            }
        });
    }

    /**
     * restorePurchases
     */
    public void getPurchasedRecords(View view) {
        showProgress();
        mQooAppOpenSDK.restorePurchases(new QooAppCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("mQooAppOpenSDK", "response = "+response);
                dismissProgress();
                displayResult(true, response);
            }

            @Override
            public void onError(String error) {
                Log.e("mQooAppOpenSDK", "error = "+error);
                dismissProgress();
                displayResult(error);
            }
        });
    }

    private void displayResult(final String result) {
        displayResult(false, result);
    }

    private void displayResult(boolean isGetRecords, final String result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(result);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mLayoutProducts.getVisibility() == View.VISIBLE) {
                    if (isGetRecords) {
                        parseRecords(result);
                    } else {
                        parseProducts(result);
                    }
                } else if (mLayoutLogin.getVisibility() == View.VISIBLE) {
                    showProducts();
                } else if (mLayoutCheckLicense.getVisibility() == View.VISIBLE) {
                    showLogin();
                }
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showCheckLicense() {
        mLayoutCheckLicense.setVisibility(View.VISIBLE);
        mLayoutLogin.setVisibility(View.GONE);
        mLayoutProducts.setVisibility(View.GONE);
        mLayoutInit.setVisibility(View.GONE);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("CheckLicense");
        TextView tvSkip = findViewById(R.id.btn_skip);
        tvSkip.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        tvSkip.getPaint().setAntiAlias(true);
        tvSkip.setOnClickListener(v -> showLogin());
    }

    private void showLogin() {
        mLayoutCheckLicense.setVisibility(View.GONE);
        mLayoutLogin.setVisibility(View.VISIBLE);
        mLayoutProducts.setVisibility(View.GONE);
        mLayoutInit.setVisibility(View.GONE);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Login");
        final EditText editUid = findViewById(R.id.edt_uid);
        findViewById(R.id.btn_bind).setOnClickListener(v -> {
            mUserId = editUid.getText().toString();
            if (!TextUtils.isEmpty(mUserId)) {
                SharedPrefsUtils.setStringPreference(MainActivity.this, KEY_USERID, mUserId);
                mQooAppOpenSDK.bindUserId(mUserId);
                showProducts();
            }
        });
    }

    private void parseProducts(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray dataArray = jsonObject.getJSONArray("data");
            if (dataArray != null) {
                mProductsList.clear();
                for (int i = 0, l = dataArray.length(); i < l; i++) {
                    JSONObject productObject = dataArray.getJSONObject(i);
                    Product product = new Product();


                    product.setProductId(productObject.optString("product_id"));
                    product.setName(productObject.optString("name"));
                    JSONObject priceObj = productObject.getJSONObject("price");
                    JSONObject paypalObj = priceObj.getJSONObject("paypal");

                    product.setAmount(paypalObj.optDouble("amount"));
                    product.setCurrency(paypalObj.optString("currency"));

                    mProductsList.add(product);
                }
                mAdapter = new ProductAdapter(this, mProductsList);
                mListView.setVisibility(View.VISIBLE);
                mListView.setAdapter(mAdapter);
                mListView.setOnItemClickListener((parent, view, position, id) -> {
                    final Product product = mProductsList.get(position);
                    mQooAppOpenSDK.purchase(new PaymentCallback() {

                        @Override
                        public void onComplete(String response) {
                            Log.d("mQooAppOpenSDK", "response = "+response);
                            //Handle success case
                            try {
                                JSONObject obj = new JSONObject(response);
                                JSONObject jsonObject = obj.getJSONObject("data");
                                //We have product. Consuming it.
                                String purchase_id = jsonObject.optString("purchase_id");
                                final String token = jsonObject.optString("token");
                                consumePurchase(token, purchase_id);
                                String product_id = jsonObject.optString("product_id");
                                mQooAppOpenSDK.closePaymentUI();
                                showToast(MainActivity.this, "Purchasing successful，Consuming...[purchase_id:" + purchase_id + ",product_id:" + product_id + ",token:" + token);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("mQooAppOpenSDK", "error = "+error);
                            //Handle error case
                            showToast(MainActivity.this, "Error:" + error);
                        }

                        @Override
                        public void onCancel() {
                            Log.e("mQooAppOpenSDK", "onCancel ");
                            //Handle the dialog hided case
                            showToast(MainActivity.this, "Be canceled");
                        }
                    }, MainActivity.this, product.getProductId());
                });
                mBtnProducts.setVisibility(View.GONE);
                mBtnPurchased.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            showToast(this, e.getMessage());
        }
    }

    private void parseRecords(String result) {
        try {

            JSONObject obj = new JSONObject(result);
            JSONArray dataArray = obj.getJSONArray("data");

            if (dataArray != null) {
                mProductsList.clear();
                for (int i = 0, l = dataArray.length(); i < l; i++) {
                    JSONObject productObject = dataArray.getJSONObject(i);
                    Product product = new Product();
                    product.setProductId(productObject.optString("product_id"));
                    product.setPurchase_id(productObject.optString("purchase_id"));
                    product.setToken(productObject.optString("token"));
                    mProductsList.add(product);
                }
                mAdapter = new ProductAdapter(this, mProductsList);
                mListView.setVisibility(View.VISIBLE);
                mListView.setAdapter(mAdapter);
                mListView.setOnItemClickListener((parent, view, position, id) -> {
                    final Product product = mProductsList.get(position);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Consume the product?");
                    builder.setPositiveButton("Ok", (dialog, which) -> consumePurchase(product.getToken(), product.getPurchase_id()));
                    AlertDialog dialog = builder.create();
                    dialog.show();
                });
            }
            mBtnProducts.setVisibility(View.GONE);
            mBtnPurchased.setVisibility(View.GONE);
        } catch (JSONException e) {
            e.printStackTrace();
            showToast(this, e.getMessage());
        }
    }

    private void showProducts() {
        mLayoutInit.setVisibility(View.GONE);
        mLayoutCheckLicense.setVisibility(View.GONE);
        mLayoutLogin.setVisibility(View.GONE);
        mLayoutProducts.setVisibility(View.VISIBLE);
        mBtnProducts.setVisibility(View.VISIBLE);
        mBtnPurchased.setVisibility(View.VISIBLE);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Products");
    }

    private void showInit() {
        mLayoutInit.setVisibility(View.VISIBLE);
        mLayoutCheckLicense.setVisibility(View.GONE);
        mLayoutLogin.setVisibility(View.GONE);
        mLayoutProducts.setVisibility(View.GONE);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setTitle("QooAppOpenSDK: Initialize");
    }

    private void showProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Please wait...");
        }
        progressDialog.show();
    }

    private void dismissProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (mAdapter != null) {
            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
        }
        if (mLayoutProducts.getVisibility() == View.VISIBLE) {
            if (mListView.getVisibility() == View.VISIBLE) {
                mListView.setVisibility(View.GONE);
                showProducts();
            } else {
                showLogin();
            }
        } else if (mLayoutLogin.getVisibility() == View.VISIBLE) {
            showCheckLicense();
        } else if (mLayoutCheckLicense.getVisibility() == View.VISIBLE) {
            showInit();
        } else {
            finish();
        }
    }
}
