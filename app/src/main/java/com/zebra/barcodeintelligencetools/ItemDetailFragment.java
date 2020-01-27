package com.zebra.barcodeintelligencetools;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.zebra.barcodeintelligencetools.api.ApiItem;
import com.zebra.barcodeintelligencetools.api.RetrieveAPITask;
import com.zebra.savanna.SavannaAPI;
import com.zebra.savanna.Symbology;

import java.util.Arrays;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment implements View.OnClickListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    static final String ARG_ITEM_ID = "item_id";
    public static ItemDetailFragment Instance;
    /**
     * The item content this fragment is presenting.
     */
    private static ApiItem mItem;
    private static String details = "";
    private static Bitmap barcodeImage;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    public void onPostExecute(Object apiData) {
        ViewGroup root = (ViewGroup) getView();
        if (root == null) return;
        ImageView barcode = root.findViewById(R.id.barcode);
        TextView results = root.findViewById(R.id.resultData);
        if (apiData instanceof byte[]) {
            byte[] data = (byte[]) apiData;
            barcodeImage = BitmapFactory.decodeByteArray(data, 0, data.length);
            barcode.setImageBitmap(barcodeImage);
            barcode.setVisibility(View.VISIBLE);
            results.setVisibility(View.GONE);
        } else {
            String json = (String) apiData;
            results.setVisibility(View.VISIBLE);
            if (details.equals("")) {
                details = json;
            } else
                details += "\n" + json;
            results.setText(details);
            if (barcode != null)
                barcode.setVisibility(View.GONE);
        }
    }

    void routeScanData(String barcode, String symbology) {
        ViewGroup root = (ViewGroup) getView();
        if (root == null) return;
        TextView results = root.findViewById(R.id.resultData);
        symbology = symbology.substring("label-type-".length());
        if (symbology.equals("upce0")) {
            symbology = "upce";
            if (barcode.length() == 6)
                barcode = "0" + barcode + "0";
        }
        switch (mItem.id) {
            case "1":
                EditText barcodeText = root.findViewById(R.id.barcodeText);
                barcodeText.setText(barcode);

                Spinner barcodeType = root.findViewById(R.id.barcodeTypes);
                int index = Arrays.asList(Symbology.values()).indexOf(Symbology.fromValue(symbology));
                if (index > -1)
                    barcodeType.setSelection(index);
                return;
            case "2":
                details = "";
                results.setText(details);
                new RetrieveAPITask().execute("foodUpc", barcode);
                new RetrieveAPITask().execute("drugUpc", barcode);
                return;
            case "3":
                details = "";
                results.setText(details);
                EditText upc = root.findViewById(R.id.upc);
                upc.setText(barcode);
                new RetrieveAPITask().execute("lookup", barcode);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Instance = this;
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_ITEM_ID)) {
            String key = args.getString(ARG_ITEM_ID);

            if (mItem != null && key != null && !key.equals(mItem.id)) {
                barcodeImage = null;
                details = "";
            }

            // Load the item content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = ItemListActivity.content.get(key);
        }

        Activity activity = getActivity();
        if (activity == null) return;
        Toolbar toolbar = activity.findViewById(R.id.detail_toolbar);
        CollapsingToolbarLayout toolbarLayout = activity.findViewById(R.id.toolbar_layout);
        int color;
        switch (mItem.id) {
            case "1":
                activity.getWindow().setStatusBarColor(getResources().getColor(R.color.colorCreateBarcodeDark));
                color = getResources().getColor(R.color.colorCreateBarcode);
                toolbar.setBackgroundColor(color);
                toolbarLayout.setContentScrimColor(color);
                toolbarLayout.setBackgroundColor(color);
                break;
            case "2":
                activity.getWindow().setStatusBarColor(getResources().getColor(R.color.colorFdaRecallDark));
                color = getResources().getColor(R.color.colorFdaRecall);
                toolbar.setBackgroundColor(color);
                toolbarLayout.setContentScrimColor(color);
                toolbarLayout.setBackgroundColor(color);
                break;
            case "3":
                activity.getWindow().setStatusBarColor(getResources().getColor(R.color.colorUpcLookupDark));
                color = getResources().getColor(R.color.colorUpcLookup);
                toolbar.setBackgroundColor(color);
                toolbarLayout.setContentScrimColor(color);
                toolbarLayout.setBackgroundColor(color);
                break;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = activity == null ? null : (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(mItem.content);
        }

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.item_detail, container, false);

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        SavannaAPI.setAPIKey(sharedPreferences.getString("apikey", ""));
        // Show the item content as text in a TextView.
        if (mItem != null) {
            ((TextView) root.findViewById(R.id.item_detail)).setText(mItem.details);

            switch (mItem.id) {
                case "1":
                    View createView = inflater.inflate(R.layout.create_barcode, container, false);

                    TextView createResults = createView.findViewById(R.id.resultData);
                    createResults.setText(details);

                    Button create = createView.findViewById(R.id.createBarcode);
                    create.setOnClickListener(this);

                    Spinner types = createView.findViewById(R.id.barcodeTypes);
                    Context context = getContext();
                    if (context != null)
                        types.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, Symbology.values()));

                    ImageView barcode = createView.findViewById(R.id.barcode);
                    if (barcodeImage == null && details.equals("")) {
                        barcode.setVisibility(View.GONE);
                        createResults.setVisibility(View.VISIBLE);
                    } else {
                        barcode.setImageBitmap(barcodeImage);
                        barcode.setVisibility(View.VISIBLE);
                        createResults.setVisibility(View.GONE);
                    }
                    root.addView(createView);
                    break;
                case "2":
                    View recallView = inflater.inflate(R.layout.fda_recall, container, false);
                    Button recalls = recallView.findViewById(R.id.fdaSearch);
                    recalls.setOnClickListener(this);
                    TextView recallResults = recallView.findViewById(R.id.resultData);
                    recallResults.setText(details);
                    root.addView(recallView);
                    break;
                case "3":
                    View lookupView = inflater.inflate(R.layout.upc_lookup, container, false);
                    TextView results = lookupView.findViewById(R.id.resultData);
                    results.setText(details);

                    Button lookup = lookupView.findViewById(R.id.upc_lookup);
                    lookup.setOnClickListener(this);

                    root.addView(lookupView);
                    break;
            }
        }

        return root;
    }

    @Override
    public void onClick(View v) {
        ViewGroup root = (ViewGroup) this.getView();
        if (root == null) return;

        TextView results = root.findViewById(R.id.resultData);
        details = "";
        results.setText(details);
        switch (mItem.id) {
            case "1":
                EditText barcodeText = root.findViewById(R.id.barcodeText);
                Spinner barcodeType = root.findViewById(R.id.barcodeTypes);
                CheckBox includeText = root.findViewById(R.id.includeText);
                new RetrieveAPITask().execute("create", barcodeText.getText().toString(), barcodeType.getSelectedItem().toString(), Boolean.toString(includeText.isChecked()));
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
}
