package com.zebra.barcodeintelligencetools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.CollapsingToolbarLayout;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ItemListActivity}.
 */
public class ItemDetailActivity extends AppCompatActivity {

    //
    // After registering the broadcast receiver, the next step (below) is to define it.
    // Here it's done in the MainActivity.java, but also can be handled by a separate class.
    // The logic of extracting the scanned data and displaying it on the screen
    // is executed in its own method (later in the code). Note the use of the
    // extra keys defined in the strings.xml file.
    //
    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //Bundle b = intent.getExtras();

            //  This is useful for debugging to verify the format of received intents from DataWedge
            //for (String key : b.keySet())
            //{
            //    Log.v(LOG_TAG, key);
            //}

            if (action != null && action.equals(getResources().getString(R.string.activity_intent_filter_action))) {
                //  Received a barcode scan
                try {
                    displayScanResult(intent);
                } catch (Exception e) {
                    //  Catch if the UI does not exist when we receive the broadcast
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        CollapsingToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        String itemId = getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_ID);
        int color;
        if (itemId != null) {
            switch (itemId) {
                case "1":
                    getWindow().setStatusBarColor(getResources().getColor(R.color.colorCreateBarcodeDark));
                    color = getResources().getColor(R.color.colorCreateBarcode);
                    toolbar.setBackgroundColor(color);
                    toolbarLayout.setContentScrimColor(color);
                    toolbarLayout.setBackgroundColor(color);
                    break;
                case "2":
                    getWindow().setStatusBarColor(getResources().getColor(R.color.colorFdaRecallDark));
                    color = getResources().getColor(R.color.colorFdaRecall);
                    toolbar.setBackgroundColor(color);
                    toolbarLayout.setContentScrimColor(color);
                    toolbarLayout.setBackgroundColor(color);
                    break;
                case "3":
                    getWindow().setStatusBarColor(getResources().getColor(R.color.colorUpcLookupDark));
                    color = getResources().getColor(R.color.colorUpcLookup);
                    toolbar.setBackgroundColor(color);
                    toolbarLayout.setContentScrimColor(color);
                    toolbarLayout.setBackgroundColor(color);
                    break;
            }
        }
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ItemDetailFragment.ARG_ITEM_ID,
                    itemId);
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }
        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(getResources().getString(R.string.activity_intent_filter_action));
        registerReceiver(myBroadcastReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
    }

    //
    // The section below assumes that a UI exists in which to place the data. A production
    // application would be driving much of the behavior following a scan.
    //
    private void displayScanResult(Intent initiatingIntent) {
        String decodedData = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));
        String decodedLabelType = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_label_type));
        if (decodedLabelType != null) {
            decodedLabelType = decodedLabelType.toLowerCase();
        }

        System.out.println("decodedData: " + decodedData);
        System.out.println("decodedLabelType: " + decodedLabelType);

        ItemDetailFragment.Instance.routeScanData(decodedData, decodedLabelType);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, ItemListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}