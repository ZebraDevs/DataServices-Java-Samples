using System.Collections.Generic;

namespace BarcodeIntelligenceTools.API
{
    /// <summary>
    /// Helper class for providing API content for user interfaces.
    /// </summary>
    public static class APIContent
    {
        /// <summary>
        /// A map of API items, by ID.
        /// </summary>
        public static readonly Dictionary<string, JavaObjectWrapper<ApiItem>> Items = new Dictionary<string, JavaObjectWrapper<ApiItem>>();

        public static void AddItem(ApiItem item)
        {
            Items[item.id] = new JavaObjectWrapper<ApiItem> { Item = item, };
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