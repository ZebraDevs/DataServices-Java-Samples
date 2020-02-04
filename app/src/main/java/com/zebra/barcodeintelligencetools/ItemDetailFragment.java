package com.zebra.barcodeintelligencetools;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.zebra.barcodeintelligencetools.api.ApiItem;
import com.zebra.barcodeintelligencetools.api.RetrieveAPITask;
import com.zebra.savanna.Models.Errors.Error;
import com.zebra.savanna.SavannaAPI;
import com.zebra.savanna.Symbology;

import java.util.Arrays;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment implements View.OnClickListener, TextWatcher, AdapterView.OnItemSelectedListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    static final String ArgItemId = "item_id";
    public static ItemDetailFragment Instance;
    /**
     * The item content this fragment is presenting.
     */
    private static ApiItem mItem;
    private static String details = "";
    private static Bitmap barcodeImage;
    public static boolean _showResultsLabel;
    private int _density;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    /**
     * Handle a response from Zebra Savanna APIs.
     *
     * @param apiData An object representing a json string, png byte array, or exception.
     */
    public void onPostExecute(Object apiData) {
        ViewGroup root = (ViewGroup) getView();
        if (root == null) return;
        TextView barcodeLabel = root.findViewById(R.id.resultLabel);
        barcodeLabel.setVisibility(_showResultsLabel ? View.VISIBLE : View.GONE);
        ImageView barcode = root.findViewById(R.id.barcode);
        TextView results = root.findViewById(R.id.resultData);
        if (apiData instanceof byte[]) {
            byte[] data = (byte[]) apiData;
            barcodeImage = BitmapFactory.decodeByteArray(data, 0, data.length);
            barcode.setImageBitmap(barcodeImage);
            _showResultsLabel = true;
            barcode.setVisibility(View.VISIBLE);
            results.setVisibility(View.GONE);
        } else if (apiData instanceof Error) {
            Error e = (Error) apiData;
            _showResultsLabel = false;
            onPostExecute(e.getMessageFormatted());
        } else if (apiData instanceof Exception) {
            Exception ex = (Exception) apiData;
            _showResultsLabel = false;
            onPostExecute(ex.getMessage());
        } else {
            String noResults = getResources().getString(R.string.noResults);
            String json = (String) apiData;
            if (json.equals("{}")) {
                json = "";
            }
            results.setVisibility(View.VISIBLE);
            if (details.length() == 0 || details.equals(noResults)) {
                details = json;
            } else if (!details.equals(json)) {
                details += "\n" + json;
            }
            if (details.length() == 0) {
                details = noResults;
            }
            results.setText(details);
            if (barcode != null) {
                barcode.setVisibility(View.GONE);
            }
        }
    }

    void routeScanData(String barcode, String symbology) {
        ViewGroup root = (ViewGroup) getView();
        if (root == null) return;
        closeKeyboard();
        TextView results = root.findViewById(R.id.resultData);
        symbology = symbology.substring("label-type-".length());
        String upcA = null;
        if (symbology.startsWith("upce")) {
            symbology = "upce";
            try {
                // Calculate UPC-A code for product lookup
                upcA = ean8ToUPCA(barcode);
            } catch (Exception e) {
                Log.i(getClass().getSimpleName(),"Invalid EAN8: " + barcode);
            }
        }
        TextView resultLabel;
        switch (mItem.id) {
            case "1":
                EditText barcodeText = root.findViewById(R.id.barcodeText);
                barcodeText.setText(barcode);

                Spinner barcodeType = root.findViewById(R.id.barcodeTypes);
                int index = Arrays.asList(Symbology.values()).indexOf(Symbology.fromValue(symbology));
                if (index > -1)
                    barcodeType.setSelection(index + 1);
                return;
            case "2":
                details = "";
                results.setText(details);
                resultLabel = root.findViewById(R.id.resultLabel);
                resultLabel.setVisibility(View.GONE);
                _showResultsLabel = false;
                new RetrieveAPITask().execute("foodUpc", barcode);
                new RetrieveAPITask().execute("drugUpc", barcode);
                return;
            case "3":
                details = "";
                results.setText(details);
                resultLabel = root.findViewById(R.id.resultLabel);
                resultLabel.setVisibility(View.GONE);
                _showResultsLabel = false;
                EditText upc = root.findViewById(R.id.upc);
                upc.setText(upcA == null ? barcode : upcA);
                new RetrieveAPITask().execute("lookup", barcode);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _density = (int) Math.ceil(getResources().getDisplayMetrics().density);
        Instance = this;
        Bundle args = getArguments();
        if (args != null && args.containsKey(ArgItemId)) {
            String key = args.getString(ArgItemId);

            if (mItem != null && key != null && !key.equals(mItem.id)) {
                barcodeImage = null;
                details = "";
                _showResultsLabel = false;
            }

            // Load the item content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = ItemListActivity.content.get(key);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Activity activity = getActivity();
        if(activity == null) return null;
        Toolbar toolbar = activity.findViewById(R.id.detail_toolbar);
        CollapsingToolbarLayout toolbarLayout = activity.findViewById(R.id.toolbar_layout);
        int bgColor;
        Resources r = getResources();
        ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
        switch (mItem.id) {
            case "1":
                activity.getWindow().setStatusBarColor(r.getColor(R.color.colorCreateBarcodeDark));
                bgColor = r.getColor(R.color.colorCreateBarcode);
                if (toolbar != null) {
                    toolbar.setBackgroundResource(R.color.colorCreateBarcode);
                    toolbarLayout.setContentScrimColor(bgColor);
                    toolbarLayout.setBackgroundResource(R.color.colorCreateBarcode);
                    toolbarLayout.setTitle(mItem.content);
                }
                if(actionBar != null)
                    actionBar.setBackgroundDrawable(new ColorDrawable(bgColor));
                break;
            case "2":
                activity.getWindow().setStatusBarColor(r.getColor(R.color.colorFdaRecallDark));
                bgColor = r.getColor(R.color.colorFdaRecall);
                if (toolbar != null) {
                    toolbar.setBackgroundResource(R.color.colorFdaRecall);
                    toolbarLayout.setContentScrimColor(bgColor);
                    toolbarLayout.setBackgroundResource(R.color.colorFdaRecall);
                    toolbarLayout.setTitle(mItem.content);
                }
                if(actionBar != null)
                    actionBar.setBackgroundDrawable(new ColorDrawable(bgColor));
                break;
            case "3":
                activity.getWindow().setStatusBarColor(r.getColor(R.color.colorUpcLookupDark));
                bgColor = r.getColor(R.color.colorUpcLookup);
                if (toolbar != null) {
                    toolbar.setBackgroundResource(R.color.colorUpcLookup);
                    toolbarLayout.setContentScrimColor(bgColor);
                    toolbarLayout.setBackgroundResource(R.color.colorUpcLookup);
                    toolbarLayout.setTitle(mItem.content);
                }
                if(actionBar != null)
                    actionBar.setBackgroundDrawable(new ColorDrawable(bgColor));
                break;
        }

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.item_detail, container, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        // Set Zebra Savanna API key
        SavannaAPI.setAPIKey(sharedPreferences.getString("apikey", ""));

        // Show the item content as text in a TextView.
        if (mItem != null) {
            ((TextView) root.findViewById(R.id.item_detail)).setText(mItem.details);
            TextView barcodeLabel;

            switch (mItem.id) {
                case "1":
                    View createView = inflater.inflate(R.layout.create_barcode, container, false);

                    TextView createResults = createView.findViewById(R.id.resultData);
                    createResults.setText(details);

                    Button create = createView.findViewById(R.id.createBarcode);
                    create.setOnClickListener(this);

                    Spinner types = createView.findViewById(R.id.barcodeTypes);
                    types.setOnItemSelectedListener(this);
                    Context context = getContext();
                    if (context != null) {
                        Symbology[] symbologies = Symbology.values();
                        Object[] values = new Object[symbologies.length + 1];
                        values[0] = getResources().getString(R.string.barcode_type);
                        System.arraycopy(symbologies, 0, values, 1, symbologies.length);
                        types.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, values));
                    }

                    ImageView barcode = createView.findViewById(R.id.barcode);
                    barcodeLabel = createView.findViewById(R.id.resultLabel);
                    if (barcodeImage == null && details.equals("")) {
                        barcode.setVisibility(View.GONE);
                        barcodeLabel.setVisibility(View.GONE);
                        createResults.setVisibility(View.VISIBLE);
                    } else {
                        barcode.setImageBitmap(barcodeImage);
                        barcode.setVisibility(View.VISIBLE);
                        barcodeLabel.setVisibility(barcodeImage != null ? View.VISIBLE : View.GONE);
                    }
                    root.addView(createView);
                    break;
                case "2":
                    View recallView = inflater.inflate(R.layout.fda_recall, container, false);
                    Button recalls = recallView.findViewById(R.id.fdaSearch);
                    recalls.setOnClickListener(this);
                    EditText searchText = recallView.findViewById(R.id.fdaSearchTerm);
                    searchText.addTextChangedListener(this);

                    TextView recallResults = recallView.findViewById(R.id.resultData);
                    recallResults.setText(details);

                    barcodeLabel = recallView.findViewById(R.id.resultLabel);
                    barcodeLabel.setVisibility(_showResultsLabel ? View.VISIBLE : View.GONE);
                    root.addView(recallView);
                    break;
                case "3":
                    View lookupView = inflater.inflate(R.layout.upc_lookup, container, false);
                    TextView results = lookupView.findViewById(R.id.resultData);
                    results.setText(details);

                    EditText lookupText = lookupView.findViewById(R.id.upc);
                    lookupText.addTextChangedListener(this);

                    Button lookup = lookupView.findViewById(R.id.upc_lookup);
                    lookup.setOnClickListener(this);

                    barcodeLabel = lookupView.findViewById(R.id.resultLabel);
                    barcodeLabel.setVisibility(_showResultsLabel ? View.VISIBLE : View.GONE);
                    root.addView(lookupView);
                    break;
            }
        }

        return root;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ViewGroup root = (ViewGroup) getView();
        if (root == null) return;
        EditText barcodeText = root.findViewById(R.id.barcodeText);
        Button generate = root.findViewById(R.id.createBarcode);
        barcodeText.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
        generate.setEnabled(position > 0);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        ViewGroup root = (ViewGroup) getView();
        if (root == null) return;
        int buttonId;
        switch (mItem.id) {
            case "2":
                buttonId = R.id.fdaSearch;
                break;
            case "3":
                buttonId = R.id.upc_lookup;
                break;
            default:
                return;
        }
        Button button = root.findViewById(buttonId);
        button.setEnabled(s.toString().trim().length() > 0);
    }

    @Override
    public void onClick(View v) {
        ViewGroup root = (ViewGroup) getView();
        if (root == null) return;
        closeKeyboard();
        TextView results = root.findViewById(R.id.resultData);
        details = "";
        barcodeImage = null;
        _showResultsLabel = false;
        results.setText(details);
        TextView resultLabel = root.findViewById(R.id.resultLabel);
        resultLabel.setVisibility(View.GONE);
        switch (mItem.id) {
            case "1":
                ImageView barcode = root.findViewById(R.id.barcode);
                barcode.setImageBitmap(barcodeImage);

                EditText barcodeText = root.findViewById(R.id.barcodeText);
                Spinner barcodeType = root.findViewById(R.id.barcodeTypes);
                CheckBox includeText = root.findViewById(R.id.includeText);
                new RetrieveAPITask().execute("create", barcodeText.getText().toString(), barcodeType.getSelectedItem().toString(), Integer.toString(_density * 3), Boolean.toString(includeText.isChecked()));
                return;
            case "2":
                EditText searchText = root.findViewById(R.id.fdaSearchTerm);
                new RetrieveAPITask().execute("deviceSearch", searchText.getText().toString());
                new RetrieveAPITask().execute("drugSearch", searchText.getText().toString());
                return;
            case "3":
                EditText lookupText = root.findViewById(R.id.upc);
                new RetrieveAPITask().execute("lookup", lookupText.getText().toString());
        }
    }

    private String ean8ToUPCA(String ean8) throws Exception {
        if ("012".contains(Character.toString(ean8.charAt(6)))) {
            return ean8.substring(0, 3) + ean8.charAt(6) + "0000" + ean8.substring(3, 6) + ean8.charAt(7);
        }
        if (ean8.charAt(6) == '3') {
            return ean8.substring(0, 4) + "00000" + ean8.substring(4, 6) + ean8.charAt(7);
        }
        if (ean8.charAt(6) == '4') {
            return ean8.substring(0, 5) + "00000" + ean8.charAt(5) + ean8.charAt(7);
        }
        if ("56789".contains(Character.toString(ean8.charAt(6)))) {
            return ean8.substring(0, 6) + "0000" + ean8.substring(6);
        }
        throw new Exception("Invalid EAN8 barcode.");
    }

    private void closeKeyboard() {
        // Hide keyboard
        Context c = getContext();
        View v = getView();
        if (c == null || v == null) return;
        InputMethodManager imm = (InputMethodManager)c.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}