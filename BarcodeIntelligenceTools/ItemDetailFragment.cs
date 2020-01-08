using Android.Content;
using Android.Graphics;
using Android.OS;
using Android.Preferences;
using Android.Views;
using Android.Widget;
using AndroidX.Fragment.App;
using BarcodeIntelligenceTools.API;
using Google.Android.Material.AppBar;
using Savanna;
using System;
using System.Linq;

namespace BarcodeIntelligenceTools
{
    /**
     * A fragment representing a single Item detail screen.
     * This fragment is either contained in a {@link ItemListActivity}
     * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
     * on handsets.
     */
    public class ItemDetailFragment : Fragment, View.IOnClickListener
    {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        public const string ARG_ITEM_ID = "item_id";
        public static ItemDetailFragment Instance;
        /**
         * The item content this fragment is presenting.
         */
        private static ApiItem mItem;
        private static string details = "";
        private static Bitmap barcodeImage;

        /**
         * Mandatory empty constructor for the fragment manager to instantiate the
         * fragment (e.g. upon screen orientation changes).
         */
        public ItemDetailFragment()
        {
        }

        public void onPostExecute(object apiData)
        {
            ViewGroup root = (ViewGroup)View;
            if (root == null) return;
            ImageView barcode = root.FindViewById<ImageView>(Resource.Id.barcode);
            TextView results = root.FindViewById<TextView>(Resource.Id.resultData);
            if (apiData is byte[])
            {
                byte[] data = (byte[])apiData;
                barcodeImage = BitmapFactory.DecodeByteArray(data, 0, data.Length);
                barcode.SetImageBitmap(barcodeImage);
                barcode.Visibility = ViewStates.Visible;
                results.Visibility = ViewStates.Gone;
            }
            else
            {
                string json = (string)apiData;
                results.Visibility = ViewStates.Visible;
                if (details.Equals(""))
                {
                    details = json;
                }
                else
                    details += "\n" + json;
                results.Text = details;
                if (barcode != null)
                    barcode.Visibility = ViewStates.Gone;
            }
        }

        public void routeScanData(string barcode, string symbology)
        {
            ViewGroup root = (ViewGroup)View;
            if (root == null) return;
            TextView results = root.FindViewById<TextView>(Resource.Id.resultData);
            symbology = symbology.Substring("label-type-".Length);
            if (symbology.Equals("upce0"))
            {
                symbology = "upce";
                if (barcode.Length == 6)
                    barcode = "0" + barcode + "0";
            }
            switch (mItem.id)
            {
                case "1":
                    EditText barcodeText = root.FindViewById<EditText>(Resource.Id.barcodeText);
                    barcodeText.Text = barcode;

                    Spinner barcodeType = root.FindViewById<Spinner>(Resource.Id.barcodeTypes);
                    int index = Array.IndexOf(Enum.GetValues(typeof(Symbology)), Enum.Parse<Symbology>(symbology.Replace('-', '_')));
                    if (index > -1)
                        barcodeType.SetSelection(index);
                    return;
                case "2":
                    details = "";
                    results.Text = details;
                    new RetrieveAPITask().Execute("foodUpc", barcode);
                    new RetrieveAPITask().Execute("drugUpc", barcode);
                    return;
                case "3":
                    details = "";
                    results.Text = details;
                    EditText upc = root.FindViewById<EditText>(Resource.Id.upc);
                    upc.Text = barcode;
                    new RetrieveAPITask().Execute("lookup", barcode);
                    return;
            }
        }


        public override void OnCreate(Bundle savedInstanceState)
        {
            base.OnCreate(savedInstanceState);
            Instance = this;
            Bundle args = Arguments;
            if (args != null && args.ContainsKey(ARG_ITEM_ID))
            {
                string key = args.GetString(ARG_ITEM_ID);

                if (mItem != null && key != null && !key.Equals(mItem.id))
                {
                    barcodeImage = null;
                    details = "";
                }

                // Load the item content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                mItem = APIContent.ITEM_MAP[key];

                var activity = Activity;
                var appBarLayout = activity?.FindViewById<CollapsingToolbarLayout>(Resource.Id.toolbar_layout);
                if (appBarLayout != null)
                {
                    appBarLayout.SetTitle(mItem.content);
                }
            }
        }

        public override View OnCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            ViewGroup root = (ViewGroup)inflater.Inflate(Resource.Layout.item_detail, container, false);

            var sharedPreferences =
                    PreferenceManager.GetDefaultSharedPreferences(Context);
            BaseAPI.APIKey = sharedPreferences.GetString("apikey", "");
            Console.WriteLine(BaseAPI.APIKey);
            // Show the item content as text in a TextView.
            if (mItem != null)
            {
                root.FindViewById<TextView>(Resource.Id.item_detail).Text = mItem.details;

                switch (mItem.id)
                {
                    case "1":
                        View createView = inflater.Inflate(Resource.Layout.create_barcode, container, false);

                        TextView createResults = createView.FindViewById<TextView>(Resource.Id.resultData);
                        createResults.Text = details;

                        Button create = createView.FindViewById<Button>(Resource.Id.createBarcode);
                        create.SetOnClickListener(this);

                        Spinner types = createView.FindViewById<Spinner>(Resource.Id.barcodeTypes);
                        Context context = Context;
                        if (context != null)
                        {
                            Array syms = Enum.GetValues(typeof(Symbology));
                            Symbology[] values = new Symbology[syms.Length];
                            syms.CopyTo(values, 0);
                            types.Adapter = new ArrayAdapter(context, Android.Resource.Layout.SimpleSpinnerDropDownItem, values.Select(s => s.ToString().Replace('_', '-')).ToList());
                        }
                        ImageView barcode = createView.FindViewById<ImageView>(Resource.Id.barcode);
                        if (barcodeImage == null && details.Equals(""))
                        {
                            barcode.Visibility = ViewStates.Gone;
                            createResults.Visibility = ViewStates.Visible;
                        }
                        else
                        {
                            barcode.SetImageBitmap(barcodeImage);
                            barcode.Visibility = ViewStates.Visible;
                            createResults.Visibility = ViewStates.Gone;
                        }
                        root.AddView(createView);
                        break;
                    case "2":
                        View recallView = inflater.Inflate(Resource.Layout.fda_recall, container, false);
                        Button recalls = recallView.FindViewById<Button>(Resource.Id.fdaSearch);
                        recalls.SetOnClickListener(this);
                        TextView recallResults = recallView.FindViewById<TextView>(Resource.Id.resultData);
                        recallResults.Text = details;
                        root.AddView(recallView);
                        break;
                    case "3":
                        View lookupView = inflater.Inflate(Resource.Layout.upc_lookup, container, false);
                        TextView results = lookupView.FindViewById<TextView>(Resource.Id.resultData);
                        results.Text = details;

                        Button lookup = lookupView.FindViewById<Button>(Resource.Id.upc_lookup);
                        lookup.SetOnClickListener(this);

                        root.AddView(lookupView);
                        break;
                }
            }

            return root;
        }

        public void OnClick(View v)
        {
            ViewGroup root = (ViewGroup)this.View;
            if (root == null) return;

            TextView results = root.FindViewById<TextView>(Resource.Id.resultData);
            details = "";
            results.Text = details;
            switch (mItem.id)
            {
                case "1":
                    EditText barcodeText = root.FindViewById<EditText>(Resource.Id.barcodeText);
                    Spinner barcodeType = root.FindViewById<Spinner>(Resource.Id.barcodeTypes);
                    new RetrieveAPITask().Execute("create", barcodeText.Text, barcodeType.SelectedItem);
                    return;
                case "2":
                    EditText searchText = root.FindViewById<EditText>(Resource.Id.fdaSearchTerm);
                    new RetrieveAPITask().Execute("deviceSearch", searchText.Text);
                    new RetrieveAPITask().Execute("drugSearch", searchText.Text);
                    return;
                case "3":
                    EditText lookupText = root.FindViewById<EditText>(Resource.Id.upc);
                    new RetrieveAPITask().Execute("lookup", lookupText.Text);
                    return;
            }
        }
    }
}