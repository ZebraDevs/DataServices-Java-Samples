using Android.OS;
using Savanna;
using System;

namespace BarcodeIntelligenceTools.API
{
    public class RetrieveAPITask : AsyncTask<string, Java.Lang.Void, object>
    {
        private Exception exception;

        protected override object RunInBackground(params string[] args)
        {
            try
            {
                switch (args[0])
                {
                    case "create":
                        return CreateBarcode.CreateAsync(Enum.Parse<Symbology>(args[2].Replace('-', '_')), args[1], ItemListActivity.density, Rotation.Normal, true);
                    case "lookup":
                        return UPCLookup.LookupAsync(args[1]);
                    case "deviceSearch":
                        return FDARecall.DeviceSearchAsync(args[1]);
                    case "drugSearch":
                        return FDARecall.DrugSearchAsync(args[1]);
                    case "drugUpc":
                        return FDARecall.DrugUpcAsync(args[1]);
                    case "foodUpc":
                        return FDARecall.FoodUpcAsync(args[1]);
                }
            }
            catch (Exception e)
            {
                this.exception = e;
            }
            return null;
        }

        protected override void OnPostExecute(object apiData)
        {
            if (exception != null)
            {
                ItemDetailFragment.Instance.onPostExecute(exception.Message);
            }
            else
            {
                ItemDetailFragment.Instance.onPostExecute(apiData);
            }
        }
    }
}