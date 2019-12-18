package com.zebra.barcodeintellgencetools;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.zebra.barcodeintellgencetools.api.APIContent;
import com.zebra.barcodeintellgencetools.api.RetrieveAPITask;
import com.zebra.savanna.BaseAPI;
import com.zebra.savanna.Symbology;

import org.json.JSONException;
import org.json.JSONObject;

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
    private static APIContent.ApiItem mItem;
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
        if (apiData instanceof byte[]) {
            ImageView barcode = root.findViewById(R.id.barcode);
            byte[] data = (byte[]) apiData;
            barcodeImage = BitmapFactory.decodeByteArray(data, 0, data.length);
            barcode.setImageBitmap(barcodeImage);
        } else {
            JSONObject json = (JSONObject) apiData;
            TextView results = root.findViewById(R.id.resultData);
            if (results != null) {
                try {
                    if (details.equals("")) {
                        details = json.toString(2);
                    } else
                        details += "\n" + json.toString(2);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                results.setText(details);
            }
        }
    }

    void routeScanData(String barcode, String symbology) {
        ViewGroup root = (ViewGroup) getView();
        if (root == null) return;
        TextView results = root.findViewById(R.id.resultData);
        symbology = symbology.substring("label-type-".length());
        if (symbology.equals("upce0")){
            symbology = "upce";
            if (barcode.length() == 6)
                barcode = "0" + barcode + "0";
        }
        switch (mItem.id) {
            case "1":
                EditText barcodeText = root.findViewById(R.id.barcodeText);
                barcodeText.setText(barcode);

                Spinner barcodeType = root.findViewById(R.id.barcodeTypes);
                int index = Arrays.asList(Symbology.values()).indexOf(Symbology.fromString(symbology));
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
            mItem = APIContent.ITEM_MAP.get(key);

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = activity == null ? null : (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.content);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.item_detail, container, false);

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        BaseAPI.APIKey = sharedPreferences.getString("apikey", "");
        System.out.println(BaseAPI.APIKey);
        // Show the item content as text in a TextView.
        if (mItem != null) {
            ((TextView) root.findViewById(R.id.item_detail)).setText(mItem.details);

            switch (mItem.id) {
                case "1":
                    View createView = inflater.inflate(R.layout.create_barcode, container, false);
                    Spinner types = createView.findViewById(R.id.barcodeTypes);
                    Context context = getContext();
                    if (context != null)
                        types.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, Symbology.values()));
                    Button create = createView.findViewById(R.id.createBarcode);
                    create.setOnClickListener(this);
                    if (barcodeImage != null) {
                        ImageView barcode = createView.findViewById(R.id.barcode);
                        barcode.setImageBitmap(barcodeImage);
                    }
                    root.addView(createView);
                    break;
                case "2":
                    View recallView = inflater.inflate(R.layout.fda_recall, container, false);
                    Button recalls = recallView.findViewById(R.id.fdaSearch);
                    recalls.setOnClickListener(this);
                    TextView recallResults = recallView.findViewById(R.id.resultData);
                    if (details.equals(""))
                        recallResults.setText(R.string.fda_scan);
                    else
                        recallResults.setText(details);
                    root.addView(recallView);
                    break;
                case "3":
                    View lookupView = inflater.inflate(R.layout.upc_lookup, container, false);
                    TextView results = lookupView.findViewById(R.id.resultData);
                    if (details.equals(""))
                        results.setText(R.string.scan);
                    else
                        results.setText(details);
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
        switch (mItem.id) {
            case "1":
                EditText barcodeText = root.findViewById(R.id.barcodeText);
                Spinner barcodeType = root.findViewById(R.id.barcodeTypes);

                new RetrieveAPITask().execute("create", barcodeText.getText().toString(), barcodeType.getSelectedItem().toString());
                return;
            case "2":
                details = "";
                results.setText(details);
                EditText searchText = root.findViewById(R.id.fdaSearchTerm);
                new RetrieveAPITask().execute("deviceSearch", searchText.getText().toString());
                new RetrieveAPITask().execute("drugSearch", searchText.getText().toString());
        }
    }
}
