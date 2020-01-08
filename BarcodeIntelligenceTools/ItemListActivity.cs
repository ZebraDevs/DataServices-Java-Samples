using Android.Content;
using Android.OS;
using Android.Views;
using Android.Widget;
using AndroidX.AppCompat.App;
using AndroidX.RecyclerView.Widget;
using BarcodeIntelligenceTools.API;
using System;
using System.Collections.Generic;

namespace BarcodeIntelligenceTools
{
    /**
     * An activity representing a list of Items. This activity
     * has different presentations for handset and tablet-size devices. On
     * handsets, the activity presents a list of items, which when touched,
     * lead to a {@link ItemDetailActivity} representing
     * item details. On tablets, the activity presents the list of items and
     * item details side-by-side using two vertical panes.
     */
    public partial class ItemListActivity : AppCompatActivity, IScanReceiver
    {
        public static int density;
        APIContent content;
        /**
         * Whether or not the activity is in two-pane mode, i.e. running on a tablet
         * device.
         */
        private bool mTwoPane;
        //
        // After registering the broadcast receiver, the next step (below) is to define it.
        // Here it's done in the MainActivity.cs, but also can be handled by a separate class.
        // The logic of extracting the scanned data and displaying it on the screen
        // is executed in its own method (later in the code). Note the use of the
        // extra keys defined in the strings.xml file.
        //
        private BroadcastReceiver myBroadcastReceiver;

        public ItemListActivity()
        {
            myBroadcastReceiver = new SavannaBroadcastReceiver(this);
        }

        protected override void OnCreate(Bundle savedInstanceState)
        {
            base.OnCreate(savedInstanceState);
            SetContentView(Resource.Layout.activity_item_list);

            content = new APIContent(this);

            var toolbar = FindViewById<AndroidX.AppCompat.Widget.Toolbar>(Resource.Id.toolbar);
            SetSupportActionBar(toolbar);
            toolbar.Title = Title;

            if (FindViewById(Resource.Id.item_detail_container) != null)
            {
                // The detail container view will be present only in the
                // large-screen layouts (res/values-w900dp).
                // If this view is present, then the
                // activity should be in two-pane mode.
                mTwoPane = true;
            }

            View recyclerView = FindViewById(Resource.Id.item_list);
            SetupRecyclerView((RecyclerView)recyclerView);
            IntentFilter filter = new IntentFilter();
            filter.AddCategory(Intent.CategoryDefault);
            filter.AddAction(Resources.GetString(Resource.String.activity_intent_filter_action));
            RegisterReceiver(myBroadcastReceiver, filter);
            density = (int)Math.Ceiling(Resources.DisplayMetrics.Density);
        }

        public override bool OnOptionsItemSelected(IMenuItem item)
        {
            if (item.ItemId == Resource.Id.settings)
            {
                Intent intent = new Intent(this, typeof(SettingsActivity));
                StartActivity(intent);
                return true;
            }
            return base.OnOptionsItemSelected(item);
        }

        protected override void OnDestroy()
        {
            base.OnDestroy();
            UnregisterReceiver(myBroadcastReceiver);
        }

        //
        // The section below assumes that a UI exists in which to place the data. A production
        // application would be driving much of the behavior following a scan.
        //
        public void DisplayScanResult(Intent initiatingIntent)
        {
            if (!mTwoPane) return;
            string decodedData = initiatingIntent.GetStringExtra(Resources.GetString(Resource.String.datawedge_intent_key_data));
            string decodedLabelType = initiatingIntent.GetStringExtra(Resources.GetString(Resource.String.datawedge_intent_key_label_type)).ToLower();

            Console.WriteLine("decodedData: " + decodedData);
            Console.WriteLine("decodedLabelType: " + decodedLabelType);

            ItemDetailFragment.Instance.routeScanData(decodedData, decodedLabelType);
        }

        public override bool OnCreateOptionsMenu(IMenu menu)
        {
            MenuInflater inflater = MenuInflater;
            inflater.Inflate(Resource.Menu.main, menu);
            return true;
        }

        private void SetupRecyclerView(RecyclerView recyclerView)
        {
            recyclerView.SetAdapter(new SimpleItemRecyclerViewAdapter(this, APIContent.ITEMS, mTwoPane));
        }

        public class SimpleItemRecyclerViewAdapter :
             RecyclerView.Adapter, View.IOnClickListener
        {

            private readonly ItemListActivity mParentActivity;
            private readonly List<JavaObjectWrapper<ApiItem>> mValues;
            private readonly bool mTwoPane;

            public void OnClick(View view)
            {
                ApiItem item = ((JavaObjectWrapper<ApiItem>)view.Tag).Item;
                if (mTwoPane)
                {
                    Bundle arguments = new Bundle();
                    arguments.PutString(ItemDetailFragment.ARG_ITEM_ID, item.id);
                    ItemDetailFragment fragment = new ItemDetailFragment
                    {
                        Arguments = arguments
                    };
                    mParentActivity.SupportFragmentManager.BeginTransaction()
                            .Replace(Resource.Id.item_detail_container, fragment)
                            .Commit();
                }
                else
                {
                    Context context = view.Context;
                    Intent intent = new Intent(context, typeof(ItemDetailActivity));
                    intent.PutExtra(ItemDetailFragment.ARG_ITEM_ID, item.id);

                    context.StartActivity(intent);
                }
            }

            public SimpleItemRecyclerViewAdapter(ItemListActivity parent,
                                           List<JavaObjectWrapper<ApiItem>> items,
                                           bool twoPane)
            {
                mValues = items;
                mParentActivity = parent;
                mTwoPane = twoPane;
            }

            public override RecyclerView.ViewHolder OnCreateViewHolder(ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.From(parent.Context)
                    .Inflate(Resource.Layout.item_list_content, parent, false);
                return new ViewHolder(view);
            }

            public override void OnBindViewHolder(RecyclerView.ViewHolder holder, int position)
            {
                ((ViewHolder)holder).mIdView.Text = mValues[position].Item.id;
                ((ViewHolder)holder).mContentView.Text = mValues[position].Item.content;

                holder.ItemView.Tag = mValues[position];
                holder.ItemView.SetOnClickListener(this);
            }

            public override int ItemCount
            {
                get
                {
                    return mValues.Count;
                }
            }

            public class ViewHolder : RecyclerView.ViewHolder
            {
                public readonly TextView mIdView;
                public readonly TextView mContentView;

                public ViewHolder(View view) : base(view)
                {
                    mIdView = view.FindViewById<TextView>(Resource.Id.id_text);
                    mContentView = view.FindViewById<TextView>(Resource.Id.content);
                }
            }
        }
    }
}