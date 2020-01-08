using Android.Content;
using System.Collections.Generic;

namespace BarcodeIntelligenceTools.API
{
    /**
     * Helper class for providing API content for user interfaces created by
     * Android template wizards.
     * <p>
     */
    public class APIContent
    {
        /// <summary>
        /// An array of API items.
        /// </summary>
        public static readonly List<JavaObjectWrapper<ApiItem>> ITEMS = new List<JavaObjectWrapper<ApiItem>>();
        /// <summary>
        /// A map of API items, by ID.
        /// </summary>
        public static readonly Dictionary<string, ApiItem> ITEM_MAP = new Dictionary<string, ApiItem>();

        public APIContent(Context context)
        {

            // Add some API items.
            ITEMS.Clear();
            addItem(new ApiItem("1", context.GetString(Resource.String.create_barcode), context.GetString(Resource.String.create_barcode_details)));
            addItem(new ApiItem("2", context.GetString(Resource.String.fda_recall), context.GetString(Resource.String.fda_recall_details)));
            addItem(new ApiItem("3", context.GetString(Resource.String.upc_lookup), context.GetString(Resource.String.upc_lookup_details)));
        }

        private static void addItem(ApiItem item)
        {
            ITEMS.Add(new JavaObjectWrapper<ApiItem> { Item = item, });
            ITEM_MAP[item.id] = item;
        }
    }

    /// <summary>
    /// An item representing a piece of API content.
    /// </summary>
    public class ApiItem
    {
        public readonly string id;
        public readonly string content;
        public readonly string details;

        public ApiItem(string id, string content, string details)
        {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        public override string ToString()
        {
            return content;
        }
    }
}