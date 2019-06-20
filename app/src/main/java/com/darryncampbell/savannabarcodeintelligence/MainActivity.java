package com.darryncampbell.savannabarcodeintelligence;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

//  Used the following tutorial for RxJava and Retrofit: https://android.jlelse.eu/rest-api-on-android-made-simple-or-how-i-learned-to-stop-worrying-and-love-the-rxjava-b3c2c949cad4
public class MainActivity extends AppCompatActivity implements View.OnTouchListener  {

    public static final String YOUR_API_KEY = "";

    private ApiService apiService;
    private TextView txtTitle;
    private TextView txtEan;
    private TextView txtDescription;
    private TextView txtFirstOffer;
    private ImageView imageView;
    private List<String> currentReturnedImages = null;
    private static final String LOG_TAG = "SAVANNA_BI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtTitle = findViewById(R.id.idTitle);
        txtEan = findViewById(R.id.idEan);
        txtDescription = findViewById(R.id.idDescription);
        txtFirstOffer = findViewById(R.id.idFirstOffer);
        imageView = findViewById(R.id.imageView);

        if (YOUR_API_KEY.equals(""))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please visit the Zebra developer portal to obtain an API key for the Barcode Lookup API")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.setTitle("No API Key");
            alert.show();
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnTouchListener(this);
        DataWedgeUtilities.CreateProfiles(getApplicationContext());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.zebra.com/v2/tools/barcode/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        //  Create an instance of the ApiService
        apiService = retrofit.create(ApiService.class);

        clearUI("");
    }

    @Override
    protected void onNewIntent(Intent newIntent)
    {
        //  New scan Intent received by DataWedge
        if (newIntent != null)
        {
            String action = newIntent.getAction();
            if (action.equalsIgnoreCase(getPackageName() + ".ACTION"))
            {
                //  Received a barcode through StartActivity
                displayScanResult(newIntent);
            }
        }
    }

    private void displayScanResult(Intent initiatingIntent) {
        //  Parses the received scan before passing it on to be displayed
        String decodedSource = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_source));
        String decodedData = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));
        String decodedLabelType = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_label_type));
        Log.i(LOG_TAG, "Received Scan: " + decodedData);
        showDefaultImage();
        lookupUpc(decodedData);
    }

    private void lookupUpc(String upc)
    {
        //  Invoke the Savanna Barcode UPC Lookup API
        //  Uses RXJava and Retrofit to simplify API call and serialization
        Single<Product> product = apiService.getProductData(upc,
                YOUR_API_KEY);
        product.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Product>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(LOG_TAG, "onSubscribe");
                    }

                    @Override
                    public void onSuccess(Product product) {
                        Log.d(LOG_TAG, "onSuccess");
                        List<Item> items = product.getItems();
                        //  Some UPCs return multiple results but we just display the first one
                        if (items.size() > 0)
                        {
                            final Item scannedItem = items.get(0);
                            txtTitle.setText(scannedItem.getTitle());
                            txtEan.setText("Barcode: " + scannedItem.getEan());
                            txtDescription.setText(scannedItem.getDescription());
                            List<Offer> offers = scannedItem.getOffers();
                            //  Each UPC will have multiple prices but only the first one is displayed
                            if (offers.size() > 0)
                            {
                                Offer firstOffer = offers.get(0);
                                String currency = "$";
                                if (!firstOffer.getCurrency().equals(""))
                                    currency = firstOffer.getCurrency();
                                txtFirstOffer.setText("Available for sale at: " +
                                        firstOffer.getMerchant() + " for " +
                                        currency +
                                        String.format("%10.2f", firstOffer.getPrice()));
                            }
                            else {
                                txtFirstOffer.setText("");
                            }

                            //  Download and display the first image associated with the product
                            //  There is room for improvement here, often the first image returned is quite small
                            //  Obviously this needs to be done in a separate thread
                            currentReturnedImages = scannedItem.getImages();
                            if (currentReturnedImages.size() > 0)
                            {
                                new DownloadImageTask((ImageView) findViewById(R.id.imageView))
                                        .execute(currentReturnedImages);
                            }
                            else
                            {
                                showDefaultImage();
                            }
                        }
                        else
                        {
                            clearUI("Item could not be found");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG_TAG, "onError" + e.getMessage());
                        clearUI("Error searching for item: " + e.getMessage());
                    }
                });
    }

    private void showDefaultImage()
    {
        //  Catch all logic to ensure we don't show an old image
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imageView.setImageDrawable(getDrawable(R.drawable.default_product_image));
                }
            }
        });
    }

    private void clearUI(final String titleText){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtTitle.setText(titleText);
                txtEan.setText("");
                txtDescription.setText("");
                txtFirstOffer.setText("");
                showDefaultImage();
            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //  Handler for the Floating action button
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
        {
            //  Tests - rather than use the hardware scanner you can invoke lookupUpc here
            //lookupUpc("309974700016");
            //lookupUpc("9780099558453");

            //  Scan button pressed
            DataWedgeUtilities.SoftScanTrigger(getApplicationContext(), true);
        }
        else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
        {
            //  Scan button released
            DataWedgeUtilities.SoftScanTrigger(getApplicationContext(), false);
        }
        return true;
    }

    private class DownloadImageTask extends AsyncTask<List<String>, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(List<String>... urls) {
            List<String> imageUrls = urls[0];
            //  Multiple image URLs are returned, "iterate" over the URLs starting with the first one
            String urlToDisplay = imageUrls.get(0);
            Bitmap downloadedImage = null;
            try {
                InputStream in = new java.net.URL(urlToDisplay).openStream();
                BufferedInputStream buf = new BufferedInputStream(in, 1024);
                downloadedImage = BitmapFactory.decodeStream(buf);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return downloadedImage;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null)
                bmImage.setImageBitmap(result);
            else
            {
                //  Bit of a hack but if the first image in the returned array could not be resolved
                //  then try the next image.  Obviously there is a possibility of race conditions if
                //  you scan rapidly but this is a proof of concept(!)
                if (currentReturnedImages.size() > 1)
                {
                    currentReturnedImages.remove(0);
                    new DownloadImageTask((ImageView) findViewById(R.id.imageView)).execute(currentReturnedImages);
                }
                else
                {
                    showDefaultImage();
                }
            }
        }
    }
}
