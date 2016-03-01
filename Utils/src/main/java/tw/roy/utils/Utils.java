package tw.roy.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import java.util.regex.Pattern;

public class Utils {


    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]+");
        return pattern.matcher(str).matches();
    }

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
        return info != null && info.isConnectedOrConnecting();
    }

    public static boolean isEmptyString(String string) {

        if (string == null || string.length() == 0 || string.equals("null")) {
            return true;
        }
        return false;
    }

    public static void notification(Context context, int id, int icon, String title,
                                    String message, Intent intent) {

        try {
            NotificationManager notifiM = (NotificationManager) context
                    .getSystemService(Service.NOTIFICATION_SERVICE);

            PendingIntent penIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setSmallIcon(icon)
                            .setTicker(title)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setContentIntent(penIntent)
                            .setAutoCancel(true);

//            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                builder.setColor(context.getResources().getColor(R.color.orange));
//            }

            Notification notifi = builder.build();
            notifiM.notify(id, notifi);

        } catch (SecurityException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
         * @param context context
         * @param title   title
         * @param message message
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
