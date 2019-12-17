package com.zebra.barcodeintellgencetools;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.fragment.app.Fragment;

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

import com.zebra.barcodeintellgencetools.api.APIContent;
import com.zebra.savanna.BaseAPI;
import com.zebra.savanna.Symbology;

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

    private static ViewGroup root;

    /**
     * The item content this fragment is presenting.
     */
    static APIContent.ApiItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    public static void onPostExecute(Object apiData) {
        if (apiData instanceof byte[]) {
            ImageView barcode = root.findViewById(R.id.barcode);
            byte[] data = (byte[]) apiData;
            Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
            barcode.setImageBitmap(b);
        } else {
            JSONObject json = (JSONObject) apiData;
            TextView results = root.findViewById(R.id.resultData);
            if (results != null) {
                if (results.getText().equals(""))
                    results.setText(json.toString());
                else
                    results.setText(results.getText() + "\n" + json.toString());
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_ITEM_ID)) {
            // Load the item content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = APIContent.ITEM_MAP.get(args.getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = activity == null ? null : (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.content);
            }
        }
    }

    static void routeScanData(String barcode, String symbology) {
        TextView results = root.findViewById(R.id.resultData);
        symbology = symbology.substring("label-type-".length());
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
                results.setText("");
                new RetrieveAPITask().execute("foodUpc", barcode);
                new RetrieveAPITask().execute("drugUpc", barcode);
                return;
            case "3":
                results.setText("");
                new RetrieveAPITask().execute("lookup", barcode);
                return;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.item_detail, container, false);

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        BaseAPI.APIKey = sharedPreferences.getString("apikey", "");
        System.out.println(BaseAPI.APIKey);
        // Show the item content as text in a TextView.
        if (mItem != null) {
            ((TextView) root.findViewById(R.id.item_detail)).setText(mItem.details);
        }

        switch (mItem.id) {
            case "1":
                View createView = inflater.inflate(R.layout.create_barcode, container, false);
                Spinner types = createView.findViewById(R.id.barcodeTypes);
                types.setAdapter(new ArrayAdapter(this.getContext(), android.R.layout.simple_spinner_dropdown_item, Symbology.values()));
                Button create = createView.findViewById(R.id.createBarcode);
                create.setOnClickListener(this);
                root.addView(createView);
                break;
            case "2":
                View recallView = inflater.inflate(R.layout.fda_recall, container, false);
                Button recalls = recallView.findViewById(R.id.fdaSearch);
                recalls.setOnClickListener(this);
                root.addView(recallView);
                break;
            case "3":
                View lookupView = inflater.inflate(R.layout.upc_lookup, container, false);
                TextView results = lookupView.findViewById(R.id.resultData);
                results.setText(R.string.scan);
                root.addView(lookupView);
                break;
        }

        return root;
    }

    @Override
    public void onClick(View v) {
        TextView results = root.findViewById(R.id.resultData);
        switch (mItem.id) {
            case "1":
                EditText barcodeText = root.findViewById(R.id.barcodeText);
                Spinner barcodeType = root.findViewById(R.id.barcodeTypes);

                new RetrieveAPITask().execute("create", barcodeText.getText().toString(), barcodeType.getSelectedItem().toString());
                return;
            case "2":
                results.setText("");
                EditText searchText = root.findViewById(R.id.fdaSearchTerm);
                new RetrieveAPITask().execute("foodSearch", searchText.getText().toString());
                new RetrieveAPITask().execute("drugSearch", searchText.getText().toString());
                return;
        }
    }
}
