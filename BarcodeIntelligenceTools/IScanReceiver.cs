using Android.Content;

namespace BarcodeIntelligenceTools
{
    public interface IScanReceiver
    {
        void DisplayScanResult(Intent intent);
    }
}