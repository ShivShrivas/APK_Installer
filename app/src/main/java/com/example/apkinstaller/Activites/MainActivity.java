package com.example.apkinstaller.Activites;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.apkinstaller.BuildConfig;
import com.example.apkinstaller.R;
import com.example.apkinstaller.Setup;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    Button downloadFile;
    DownloadManager manager;
    ArrayList<String> permissionsList;
    String[] permissionsStr = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int PERMISSION_REQUEST_CODE = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean isAppInstalled = appInstalledOrNot(this,"com.plcoding.backgroundlocationtracking");
        Toast.makeText(this, ""+isAppInstalled, Toast.LENGTH_SHORT).show();
        if(isAppInstalled) {
            //This intent will help you to launch if the package is already installed
//            Intent LaunchIntent = getPackageManager()
//                    .getLaunchIntentForPackage("com.plcoding.backgroundlocationtracking");
//            startActivity(LaunchIntent);
            Toast.makeText(this, "Installed", Toast.LENGTH_SHORT).show();
            Log.i("SampleLog", "Application is already installed.");
        } else {
            // Do whatever we want to do if application not installed
            // For example, Redirect to play store
            Toast.makeText(this, "Not Installed", Toast.LENGTH_SHORT).show();

            Log.i("SampleLog", "Application is not currently installed.");
        }





        permissionsList = new ArrayList<>();
        permissionsList.addAll(Arrays.asList(permissionsStr));
        askForPermissions(permissionsList);
        downloadFile=findViewById(R.id.downloadFile);
        downloadFile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {


//                Thread thread = new Thread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        try  {
//                            downloadFile("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",new File( Environment.getExternalStoragePublicDirectory("Download") + "/aisehi.pdf"));
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//
//                thread.start();
                Setup setup=new Setup(MainActivity.this);
                setup.run();
//                    UpdateApp atualizaApp = new UpdateApp();
//                    atualizaApp.setContext(MainActivity.this);
//                    atualizaApp.execute("https://drive.google.com/file/d/1yxoqxVLhLd6-NgMxjJZvRQxr-5kTFDjr");
//


            }
        });
    }

    public static boolean appInstalledOrNot(@NonNull final Context context, @NonNull final String targetPackage) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> pkgAppsList =
                context.getPackageManager().queryIntentActivities( mainIntent, 0);
        for (ResolveInfo resolveInfo : pkgAppsList) {
            Log.d("TAG", "__<>"+resolveInfo.activityInfo.packageName);
            if (resolveInfo.activityInfo.packageName != null
                    && resolveInfo.activityInfo.packageName.equals(targetPackage)) {
                return true;
            }
        }
        return false;
    }





    private void downloadFile(String url, File outputFile) {
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            int contentLength = conn.getContentLength();

            DataInputStream stream = new DataInputStream(u.openStream());

            byte[] buffer = new byte[contentLength];
            stream.readFully(buffer);
            stream.close();

            DataOutputStream fos = new DataOutputStream(new FileOutputStream(outputFile));
            fos.write(buffer);
            fos.flush();
            fos.close();
            Log.d("TAG", "downloadFile: done h guru");
        } catch(FileNotFoundException e) {
            return; // swallow a 404
        } catch (IOException e) {
            return; // swallow a 404
        }
    }
    private void askForPermissions(ArrayList<String> permissionsList) {
        String[] newPermissionStr = new String[permissionsList.size()];
        for (int i = 0; i < newPermissionStr.length; i++) {
            newPermissionStr[i] = permissionsList.get(i);
        }
        if (newPermissionStr.length > 0) {
            Toast.makeText(this, "\"Asking for permissions\"", Toast.LENGTH_SHORT).show();
            permissionsLauncher.launch(newPermissionStr);
        } else {
            /* User has pressed 'Deny & Don't ask again' so we have to show the enable permissions dialog
            which will lead them to app details page to enable permissions from there. */
            showPermissionDialog();
        }
    }
    int permissionsCount = 0;

    ActivityResultLauncher<String[]> permissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    new ActivityResultCallback<Map<String, Boolean>>() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onActivityResult(Map<String, Boolean> result) {
                            ArrayList<Boolean> list = new ArrayList<>(result.values());
                            permissionsList = new ArrayList<>();
                            permissionsCount = 0;
                            for (int i = 0; i < list.size(); i++) {
                                if (shouldShowRequestPermissionRationale(permissionsStr[i])) {
                                    permissionsList.add(permissionsStr[i]);
                                } else if (!hasPermission(MainActivity.this, permissionsStr[i])) {
                                    permissionsCount++;
                                }
                            }
                            if (permissionsList.size() > 0) {
                                //Some permissions are denied and can be asked again.
                                askForPermissions(permissionsList);
                            } else if (permissionsCount > 0) {
                                //Show alert dialog
                                showPermissionDialog();
                            } else {
                                //All permissions granted. Do your stuff 🤞
                                Toast.makeText(MainActivity.this, "\"All permissions are granted!\"", Toast.LENGTH_SHORT).show();                            }
                        }
                    });

    private boolean hasPermission(Context context, String permissionStr) {
        return ContextCompat.checkSelfPermission(context, permissionStr) == PackageManager.PERMISSION_GRANTED;
    }

    AlertDialog alertDialog;

    private void showPermissionDialog() {
        Toast.makeText(this, "\"Showing settings dialog\"", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission required")
                .setMessage("Some permissions are needed to be allowed to use this app without any problems.")
                .setPositiveButton("Ok", (dialog, which) -> {
                    dialog.dismiss();
                });
        if (alertDialog == null) {
            alertDialog = builder.create();
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted && cameraAccepted) {
                        UpdateApp updateApp = new UpdateApp();
                        updateApp.setContext(MainActivity.this);
                        updateApp.execute("https://drive.google.com/file/d/1yxoqxVLhLd6-NgMxjJZvRQxr-5kTFDjr");
                    }
                }
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        private boolean checkPermission() {
            int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);

            return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        private void requestPermission() {
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }

        public class UpdateApp extends AsyncTask<String, Integer, String> {
            private ProgressDialog mPDialog;
            private Context mContext;

            void setContext(Activity context) {
                mContext = context;
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPDialog = new ProgressDialog(mContext);
                        mPDialog.setMessage("Please wait...");
                        mPDialog.setIndeterminate(true);
                        mPDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        mPDialog.setCancelable(false);
                        mPDialog.show();
                    }
                });
            }

            @Override
            protected String doInBackground(String... sUrl) {
                InputStream input = null;
                OutputStream output = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(sUrl[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    // expect HTTP 200 OK, so we don't mistakenly save error report
                    // instead of the file
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        return "Server returned HTTP " + connection.getResponseCode()
                                + " " + connection.getResponseMessage();
                    }

                    // this will be useful to display download percentage
                    // might be -1: server did not report the length
                    int fileLength = connection.getContentLength();

                    // download the file
                    input = connection.getInputStream();
                    output = new FileOutputStream(Environment.getExternalStoragePublicDirectory("Download")+"/myapk.apk");

                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        // allow canceling with back button
                        if (isCancelled()) {
                            input.close();
                            return null;
                        }
                        total += count;
                        // publishing the progress....
                        if (fileLength > 0) // only if total length is known
                            publishProgress((int) (total * 100 / fileLength));
                        output.write(data, 0, count);

                        Toast.makeText(mContext, "Done this thing", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.d("TAG", "doInBackground: "+e.getMessage());
                    return e.toString();
                } finally {
                    try {
                        if (output != null)
                            output.close();
                        if (input != null)
                            input.close();
                    } catch (IOException ignored) {
                    }

                    if (connection != null)
                        connection.disconnect();
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (mPDialog != null)
                    mPDialog.show();

            }


            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                if (mPDialog != null) {
                    mPDialog.setIndeterminate(false);
                    mPDialog.setMax(100);
                    mPDialog.setProgress(values[0]);
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (mPDialog != null)
                    mPDialog.dismiss();
                if (result != null)
                    Toast.makeText(mContext, "Download error: " + result, Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(mContext, "File Downloaded"+result, Toast.LENGTH_SHORT).show();
            }


            private void installApk() {
                try {
                    String PATH = Objects.requireNonNull(mContext.getExternalFilesDir(null)).getAbsolutePath();

                    File file = new File(PATH + "/my_apk.apk");
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    if (Build.VERSION.SDK_INT >= 24) {
                        Uri downloaded_apk = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", file);
                        intent.setDataAndType(downloaded_apk, "application/vnd.android.package-archive");
                        List<ResolveInfo> resInfoList = mContext.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                        for (ResolveInfo resolveInfo : resInfoList) {
                            mContext.grantUriPermission(mContext.getApplicationContext().getPackageName() + ".provider", downloaded_apk, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);
                    } else {
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }




            public void downloadUpdate() {
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
                String fileName = "my_apk.apk";
                destination += fileName;
                final Uri uri = Uri.parse("file://" + destination);

                File file = new File(destination);
                if (file.exists())
                    file.delete();

                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(getIntent().getStringExtra("url")));
                request.setDestinationUri(uri);
                dm.enqueue(request);

                final String finalDestination = destination;
                final BroadcastReceiver onComplete = new BroadcastReceiver() {
                    public void onReceive(Context ctxt, Intent intent) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Uri contentUri = FileProvider.getUriForFile(ctxt, BuildConfig.APPLICATION_ID + ".provider", new File(finalDestination));
                            Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
                            openFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            openFileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            openFileIntent.setData(contentUri);
                            startActivity(openFileIntent);
                            unregisterReceiver(this);
                            finish();
                        } else {
                            Intent install = new Intent(Intent.ACTION_VIEW);
                            install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            install.setDataAndType(uri,
                                    "application/vnd.android.package-archive");
                            startActivity(install);
                            unregisterReceiver(this);
                            finish();
                        }
                    }
                };
                registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            }
        }
    }