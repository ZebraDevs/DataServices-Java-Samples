package com.zebra.barcodeintelligencetools.api;


import android.os.AsyncTask;

import com.zebra.barcodeintelligencetools.ItemDetailFragment;
import com.zebra.savanna.CreateBarcode;
import com.zebra.savanna.FDARecall;
import com.zebra.savanna.Models.Errors.Error;
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
                    return CreateBarcode.create(Symbology.fromValue(args[2]), args[1], Integer.parseInt(args[3]), Rotation.Normal, args.length > 4 ? Boolean.valueOf(args[4]) : true);
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
        if (exception != null) {
            ItemDetailFragment._showResultsLabel = false;
            String message = exception instanceof Error ? ((Error) exception).getMessageFormatted() : exception.getMessage();
            ItemDetailFragment.Instance.onPostExecute(message);

        } else {
            ItemDetailFragment._showResultsLabel = true;
            if (apiData instanceof String) {
                try {
                    apiData = new JSONObject((String) apiData).toString(2);
                } catch (JSONException e) {
                    ItemDetailFragment._showResultsLabel = false;
                    ItemDetailFragment.Instance.onPostExecute("Could not connect to service.");
                }
            }
            ItemDetailFragment.Instance.onPostExecute(apiData);
        }
    }
}