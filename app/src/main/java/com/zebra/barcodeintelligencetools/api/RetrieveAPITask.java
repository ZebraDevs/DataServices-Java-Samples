package com.zebra.barcodeintelligencetools.api;


import android.os.AsyncTask;

import com.zebra.barcodeintelligencetools.ItemDetailFragment;
import com.zebra.savanna.CreateBarcode;
import com.zebra.savanna.FDARecall;
import com.zebra.savanna.Rotation;
import com.zebra.savanna.Symbology;
import com.zebra.savanna.UPCLookup;

import org.json.JSONException;
import org.json.JSONObject;

public class RetrieveAPITask extends AsyncTask<String, Void, Object> {
    private Exception exception;

    protected Object doInBackground(String... args) {
        try {
            switch (args[0]) {
                case "create":
                    return CreateBarcode.create(Symbology.fromValue(args[2]), args[1], Integer.parseInt(args[3]), Rotation.Normal, args.length > 3 ? Boolean.valueOf(args[4]) : true);
                case "lookup":
                    return UPCLookup.lookup(args[1]);
                case "deviceSearch":
                    return FDARecall.deviceSearch(args[1]);
                case "drugSearch":
                    return FDARecall.drugSearch(args[1]);
                case "drugUpc":
                    return FDARecall.drugUpc(args[1]);
                case "foodUpc":
                    return FDARecall.foodUpc(args[1]);
            }
        } catch (Exception e) {
            this.exception = e;
        }
        return null;
    }

    protected void onPostExecute(Object apiData) {
        try {
            if (exception != null) {
                ItemDetailFragment. _showResultsLabel = false;
                ItemDetailFragment.Instance.onPostExecute(new JSONObject(exception.getMessage()).toString(2));

            } else {
                ItemDetailFragment. _showResultsLabel = true;
                ItemDetailFragment.Instance.onPostExecute(apiData);
            }
        } catch (JSONException e) {
            ItemDetailFragment. _showResultsLabel = false;
            ItemDetailFragment.Instance.onPostExecute("Could not connect to service.");
        }
    }
}