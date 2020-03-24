package com.qooapp.opensdk.sample;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

    private final String TAG = "MainActivity";

    //Your app id
    private String mAppId = "Your app id";
    public final static String KEY_APP_ID = "app_id";
    public static final String KEY_USERID = "user_id";
    private List<String> mDataList;
    //The QooAppOpenSDK created a singleton
    private View mLayoutInit;
    private View mLayoutCheckLicense;
    private View mLayoutProducts;
    private String mUserId;
    private ListView mListView;
    private List<Product> mProductsList = new ArrayList<>();
    private ProductAdapter mAdapter;
    private CheckBox mCbUseCache;
    private EditText mEdtAppId;
    private Button mBtnSelect;
    private Button mBtnProducts;
    private Button mBtnPurchased;
    private ProgressDialog progressDialog;
    private int mSelected;

    private final int TYPE_ERROR = 0;

    private final int TYPE_LOGIN = 1;

    private final int TYPE_VERIFY = 2;

    private final int TYPE_QUERY_PRODUCT = 3;

    private final int TYPE_QUERY_RECORD = 4;

    private QooAppCallback mInitCallback = new QooAppCallback() {
        @Override
        public void onSuccess(String response) {

            try {
                JSONObject jsonObject = new JSONObject(response);
                mUserId = jsonObject.getJSONObject("data").getString("user_id");
                SharedPrefsUtils.setStringPreference(MainActivity.this, KEY_USERID, mUserId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            displayResult(TYPE_LOGIN, response);
            hideProgress();
        }

        @Override
        public void onError(String error) {
            displayResult(TYPE_ERROR, error);
            hideProgress();
        }
    };

    private PaymentCallback mPaymentCallback = new PaymentCallback() {
        @Override
        public void onComplete(String json) {

            //Handle success case
            try {
                JSONObject obj = new JSONObject(json);
                JSONObject jsonObject = obj.getJSONObject("data");
                //We have product. Consuming it.
                String purchase_id = jsonObject.optString("purchase_id");
                final String token = jsonObject.optString("token");
                consumePurchase(token, purchase_id);
                String product_id = jsonObject.optString("product_id");
                QooAppOpenSDK.getInstance().closePaymentUI();
                showToast(MainActivity.this, "Purchasing successful，Consuming...[purchase_id:" + purchase_id + ",product_id:" + product_id + ",token:" + token);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(String error) {
            showToast(MainActivity.this, "Error:" + error);
        }

        @Override
        public void onCancel() {
            showToast(MainActivity.this, "Be canceled");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("QooAppOpenSDK: Initialize  v"+BuildConfig.VERSION_NAME);

        initView();
        initLocalData();
    }

    private void initView() {
        mCbUseCache = this.findViewById(R.id.cb_use_cache);
        mEdtAppId = this.findViewById(R.id.edt_appId);
        Button btnInit = this.findViewById(R.id.btn_init);
        mBtnSelect = this.findViewById(R.id.btn_select);
        mBtnProducts = this.findViewById(R.id.btn_products);
        mLayoutInit = this.findViewById(R.id.layout_init);
        mLayoutCheckLicense = this.findViewById(R.id.layout_verify);
        mLayoutProducts = this.findViewById(R.id.layout_products);
        mBtnPurchased = this.findViewById(R.id.btn_purchased);
        mListView = this.findViewById(R.id.list_view);
        btnInit.setOnClickListener(v -> {
            mAppId = mEdtAppId.getText().toString();
            SharedPrefsUtils.setStringPreference(MainActivity.this, KEY_APP_ID, mAppId);
            CacheUtil.saveChannel(MainActivity.this, mAppId);
            initQooAppOpenSDK();

        });


        mBtnSelect.setOnClickListener(v -> showSelectChannel());

        findViewById(R.id.btn_verify).setOnClickListener(v -> {
            QooAppOpenSDK.getInstance().checkLicense(new QooAppCallback() {

                @Override
                public void onSuccess(String info) {
                    // verification succeed
                    displayResult(TYPE_VERIFY, info);
                }

                @Override
                public void onError(String error) {
                    // For unknown reason, verification cannot be done.
                    // Please disallow access for proper protection.
                    displayResult(TYPE_VERIFY, error);
                }
            });
        });

        findViewById(R.id.btn_products).setOnClickListener(v -> {
            showProgress();
            QooAppOpenSDK.getInstance().queryProducts(new QooAppCallback() {
                @Override
                public void onSuccess(String result) {
                    hideProgress();
                    displayResult(TYPE_QUERY_PRODUCT, result);
                }

                @Override
                public void onError(String error) {
                    displayResult(TYPE_ERROR, error);
                    hideProgress();
                }
            });
        });

        findViewById(R.id.btn_purchased).setOnClickListener(v -> {
            showProgress();
            QooAppOpenSDK.getInstance().restorePurchases(new QooAppCallback() {
                @Override
                public void onSuccess(String result) {
                    hideProgress();
                    displayResult(TYPE_QUERY_RECORD, result);
                }

                @Override
                public void onError(String error) {
                    hideProgress();
                    displayResult(TYPE_ERROR, error);
                }
            });
        });
    }

    /**
     * init QooAppOpenSDK
     */
    protected void initQooAppOpenSDK() {
        showProgress();
        if (mAppId != null) {
            //Create a QooAppOpenSDK, you can use this way, not recommend.
            QooAppOpenSDK.initialize(mInitCallback, MainActivity.this, mAppId, mCbUseCache.isChecked(), false);
        } else {
            //Create a QooAppOpenSDK, you can use this way too.
            //Create a QooAppOpenSDK, you must provide params in AndroidManifest.xml
            QooAppOpenSDK.initialize(mInitCallback, MainActivity.this, mCbUseCache.isChecked(), false);
        }
    }

    protected void initLocalData() {
        mDataList = CacheUtil.getChannels(MainActivity.this);
        mBtnSelect.setVisibility(mDataList.size() > 0 ? View.VISIBLE : View.GONE);
        mAppId = SharedPrefsUtils.getStringPreference(MainActivity.this, KEY_APP_ID);
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
        QooAppOpenSDK.getInstance().consume(new QooAppCallback() {
            @Override
            public void onSuccess(String response) {
                showToast(MainActivity.this, "Consumption successful!");
                Log.d("mQooAppOpenSDK", "response = "+response);
                QooAppOpenSDK.getInstance().closePaymentUI();
            }

            @Override
            public void onError(String error) {
                Log.e("mQooAppOpenSDK", "error = "+error);
                showToast(MainActivity.this, "Consuming error:" + error);
            }
        }, purchase_id, token);
    }

    /**
     * show toast info
     * @param context
     * @param text
     */
    private void showToast(Context context, CharSequence text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * @param type which type info
     * @param result
     */
    private void displayResult(int type, final String result) {
        Log.d(TAG,"type = "+type+", result = "+result);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(result);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (type) {
                    case TYPE_LOGIN:
                        showVerifyView();
                        break;
                    case TYPE_VERIFY:
                        showProductsView();
                        break;
                    case TYPE_QUERY_PRODUCT:
                        parseProducts(result);
                        break;
                    case TYPE_QUERY_RECORD:
                        parseRecords(result);
                        break;
                }
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * parse products list info
     * @param result
     */
    private void parseProducts(String result) {
        Log.d(TAG, "result："+result);
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
                    QooAppOpenSDK.getInstance().purchase(mPaymentCallback, MainActivity.this, product.getProductId(), "cporderid----", "dev-0110");
                });
                mBtnProducts.setVisibility(View.GONE);
                mBtnPurchased.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            showToast(this, e.getMessage());
        }
    }

    /**
     * parse records list
     * @param result
     */
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

    private void showInitView() {
        mLayoutInit.setVisibility(View.VISIBLE);
        mLayoutCheckLicense.setVisibility(View.GONE);
        mLayoutProducts.setVisibility(View.GONE);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setTitle("QooAppOpenSDK: Initialize");
    }

    private void showVerifyView() {
        mLayoutCheckLicense.setVisibility(View.VISIBLE);
        mLayoutProducts.setVisibility(View.GONE);
        mLayoutInit.setVisibility(View.GONE);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Verify");
        TextView tvSkip = findViewById(R.id.btn_skip);
        tvSkip.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        tvSkip.getPaint().setAntiAlias(true);
        tvSkip.setOnClickListener(v -> showProductsView());
    }

    private void showProductsView() {
        mLayoutInit.setVisibility(View.GONE);
        mLayoutCheckLicense.setVisibility(View.GONE);
        mLayoutProducts.setVisibility(View.VISIBLE);
        mBtnProducts.setVisibility(View.VISIBLE);
        mBtnPurchased.setVisibility(View.VISIBLE);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Products");
    }

    private void showProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Please wait...");
        }
        progressDialog.show();
    }

    private void hideProgress() {
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
                showProductsView();
            } else {
                showVerifyView();
            }
        } else if (mLayoutCheckLicense.getVisibility() == View.VISIBLE) {
            showInitView();
        } else {
            finish();
        }
    }
}
