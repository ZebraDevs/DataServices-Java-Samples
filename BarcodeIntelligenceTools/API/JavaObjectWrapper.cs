namespace BarcodeIntelligenceTools.API
{
    public class JavaObjectWrapper<T> : Java.Lang.Object
    {
        public T Item { get; set; }
    }
}