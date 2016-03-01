package tw.roy.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;

/**
 * Created by Roy on 2015/12/24.
 */
public class Utils {

    public static String getDeviceID(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Build.SERIAL;
        } else {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }

    }

    public static boolean isConnected(Context context) {
        ConnectivityManager CM = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = CM.getActiveNetworkInfo();
        if (info != null) {
            return info.isConnectedOrConnecting();
        }
        return false;
    }

    public static boolean isEmptyString(String string) {

        if (string == null || string.length() == 0 || string.equals("null")) {
            return true;
        }
        return false;
    }

    public static class mProgressDialog {

        private static ProgressDialog progressDialog;


        public static void show(Context context, CharSequence title, CharSequence message, boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            progressDialog = android.app.ProgressDialog.show(context, title, message, true, cancelable, cancelListener);
        }

        /**
         * @param context
         * @param title
         * @param message
         * @param style   ProgressDialog.STYLE_HORIZONTAL 直條
         *                ProgressDialog.STYLE_SPINNER 圓圈
         */
        public static void showWithProgress(Context context, CharSequence title, CharSequence message, int style, int max, boolean cancelable) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle(title);
            progressDialog.setMessage(message);
            progressDialog.setIndeterminate(true);
            progressDialog.setProgressStyle(style);
            progressDialog.setMax(max);
            progressDialog.setCancelable(cancelable);
            progressDialog.show();
        }

        public static void setProgress(int progress) {

            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.setIndeterminate(false);
                progressDialog.setProgress(progress);
            }

        }

        public static void dismiss() {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }

        public static void destroy() {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            progressDialog = null;
        }
    }
}
