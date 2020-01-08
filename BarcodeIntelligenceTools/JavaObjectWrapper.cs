namespace BarcodeIntelligenceTools
{
    public class JavaObjectWrapper<T> : Java.Lang.Object
    {
        public T Item { get; set; }
    }
}