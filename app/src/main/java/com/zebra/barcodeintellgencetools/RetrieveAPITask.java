package com.zebra.barcodeintellgencetools;


import android.os.AsyncTask;

import com.zebra.savanna.CreateBarcode;
import com.zebra.savanna.FDARecall;
import com.zebra.savanna.Rotation;
import com.zebra.savanna.Symbology;
import com.zebra.savanna.UPCLookup;

import org.json.JSONException;
import org.json.JSONObject;

class RetrieveAPITask extends AsyncTask<String, Void, Object> {
    private Exception exception;

    protected Object doInBackground(String... args) {
        try {
            switch (args[0]) {
                case "create":
                    return CreateBarcode.create(Symbology.fromString(args[2]), args[1], ItemListActivity.density, Rotation.Normal,true);
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
            try {
                ItemDetailFragment.onPostExecute(new JSONObject(exception.getMessage()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            ItemDetailFragment.onPostExecute(apiData);
        }
    }
}
