package com.a_track_it.fitdata.common.workers;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.a_track_it.fitdata.common.Constants;
import com.a_track_it.fitdata.common.Utilities;
import com.a_track_it.fitdata.common.model.UserPreferences;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import static androidx.core.content.FileProvider.getUriForFile;

public class ImageWorker extends Worker {
    public ImageWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }
    private static final String TAG = ImageWorker.class.getSimpleName();

    @NonNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();
        String resourceUri = getInputData().getString(Constants.KEY_IMAGE_URI);
        int resourceType = getInputData().getInt(Constants.KEY_FIT_TYPE, 0);
        String sUserID = getInputData().getString(Constants.KEY_FIT_USER);
        if (sUserID == null) return Result.failure();
        UserPreferences userPrefs = UserPreferences.getPreferences(applicationContext, sUserID);
        Uri outputUri = null;
        try {
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input uri");
                throw new IllegalArgumentException("Invalid input uri");
            }
            File path = new File(applicationContext.getExternalCacheDir(), ((resourceType == 1) ? "camera" : "images"));
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            // html or external source - require INTERNET permissions - decodeStream
            if (resourceType == 0) {
                URL url = new URL(resourceUri);
                // Initialize a new http url connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                // Connect the http url connection
                connection.connect();
                InputStream inputStream = null;
                // Get the input stream from http url connection
                try {
                    inputStream = connection.getInputStream();
                }catch (IOException ioe){
                    Log.e("ImageWorker", ioe.getMessage());
                    if (inputStream != null) inputStream.close();
                    return Result.failure();
                }
                // Initialize a new BufferedInputStream from InputStream
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                // Create a bitmap
                Bitmap picture = BitmapFactory.decodeStream(bufferedInputStream);
                inputStream.close();
                bufferedInputStream.close();
                // Save bitmap
                ContentResolver resolver = applicationContext.getContentResolver();
                String name = String.format("atrackit-%s.jpg", timeStamp);
                //Bitmap output = WorkerUtils.blurBitmap(picture, applicationContext);
                // Write bitmap to a temp file
                try {
                    outputUri = Utilities.saveImageToStorage(resolver, picture, name);
                }catch (Exception e){
                    e.printStackTrace();
                    outputUri = writeBitmapToFile(Uri.fromFile(path), picture, name);
                }
                //outputUri = writeBitmapToFile(Uri.fromFile(path), picture, name);
                userPrefs.setPrefStringByLabel(Constants.LABEL_INT_FILE, outputUri.toString());
            }
            // file path variety
            //  - require EXTERNAL LOCATION permissions - decodeFile
            if (resourceType == 1) {
                String name = String.format("img-%s.png", timeStamp);
                FileOutputStream outStream = null;
                InputStream inputStream = null;
                File outputFile = null;
                try {
                        ContentResolver resolver = applicationContext.getContentResolver();
                        Uri uriFile = Uri.parse(resourceUri);
                        Log.e(ImageWorker.class.getSimpleName(), "opening " + uriFile.toString());
                        ContentProviderClient providerClient = resolver.acquireContentProviderClient(uriFile);
                        ParcelFileDescriptor fileDescriptor;
                        if (providerClient != null)
                             fileDescriptor = providerClient.openFile(uriFile, "r", null);
                        else
                            fileDescriptor = resolver.openFileDescriptor(uriFile,"r",null);
                        Log.e(ImageWorker.class.getSimpleName(), "fileDescriptor  " + fileDescriptor.toString());
                        FileDescriptor fd = fileDescriptor.getFileDescriptor();
                        Log.e(ImageWorker.class.getSimpleName(), "fd  " + fd.toString());
                        Bitmap b = BitmapFactory.decodeFileDescriptor(fd);
                        Log.e(ImageWorker.class.getSimpleName(), "bitmapFactory bp  ok " + (b != null));
                        outputUri = writeBitmapToFile(Uri.fromFile(path), b, name);
                        Log.e(ImageWorker.class.getSimpleName(), "writeBitmapToFile outputUri ok " + outputUri.toString());
                        userPrefs.setPrefStringByLabel(Constants.LABEL_EXT_FILE, outputUri.toString());
                        providerClient.close();
                        fileDescriptor.close();
                       // inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
                        // work out how much scaling from original - then do it
                       /* BitmapFactory.Options o = new BitmapFactory.Options();
                        o.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(resourceUri, o);
                        int width_tmp = o.outWidth, height_tmp = o.outHeight;
                        int scale = 1;

                        while (true) {
                            if (width_tmp / 2 < 148 || height_tmp / 2 < 148)
                                break;
                            width_tmp /= 2;
                            height_tmp /= 2;
                            scale *= 2;
                        }

                        BitmapFactory.Options o2 = new BitmapFactory.Options();
                        o2.inSampleSize = scale;*/
/*                       try{
                            outputFile = new File(path, name);
                            outStream = new FileOutputStream(outputFile);

                            byte[] buf = new byte[10240];
                            int len;
                            while ((len = inputStream.read(buf)) > 0) {
                                outStream.write(buf, 0, len);
                            }
                        } catch (Exception e) {
                           Log.e(ImageWorker.class.getSimpleName(), e.getMessage());
                        }*/
                   /* Bitmap bitmap = BitmapFactory.decodeFile(resourceUri, o2);
                    if (bitmap != null) bitmap.compress(Bitmap.CompressFormat.PNG, 0 , outStream);*/
                }catch(Exception ee){
                    Log.e(ImageWorker.class.getSimpleName() + " 1 ", ee.getMessage());
                    FirebaseCrashlytics.getInstance().recordException(ee);
                    return Result.failure();
                } finally {
/*                    if (outStream != null) {
                        try {
                            if (inputStream != null) inputStream.close();
                            outStream.close();
                            outputUri = Uri.fromFile(outputFile);
                            userPrefs.setPrefStringByLabel(Constants.LABEL_EXT_FILE, outputUri.toString());
                            userPrefs.setPrefStringByLabel(Constants.LABEL_LOGO_SOURCE, Constants.LABEL_EXT_FILE);
                        } catch (IOException ignore) {
                            Log.e(ImageWorker.class.getSimpleName(),ignore.getMessage());
                        }
                    }*/
                }
            }
            // If there were no errors, return SUCCESS
            Data outputData = new Data.Builder()
                    .putString(Constants.KEY_IMAGE_URI, (outputUri != null) ? outputUri.toString() : Constants.ATRACKIT_EMPTY)
                    .putString(Constants.KEY_FIT_USER, sUserID)
                    .putInt(Constants.KEY_FIT_TYPE, resourceType)
                    .putString(Constants.KEY_FIT_VALUE, ImageWorker.class.getSimpleName())
                    .build();
            return Result.success(outputData);
        } catch (Throwable throwable) {

            // Technically WorkManager will return Result.failure()
            // but it's best to be explicit about it.
            // Thus if there were errors, we're return FAILURE
            Log.e(TAG, "Error downloading image", throwable);
            return Result.failure();
        }
    }
    private Uri getCacheImagePath(Context context, String fileName, int type) {
        File path = new File(context.getExternalCacheDir(), ((type == 0) ? "camera" : "images"));
        if (!path.exists()) path.mkdirs();
        File image = new File(path, fileName);
        return getUriForFile(context, "com.a_track_it.fitdata.provider", image);
    }



    /**
     * Writes bitmap to a temporary file and returns the Uri for the file
     * @param uriPic Uri to write too
     * @param bitmap Bitmap to write to temp file
     * @param name Filename
     * @return Uri for temp file with bitmap
     * @throws FileNotFoundException Throws if bitmap file cannot be found
     */
    private Uri writeBitmapToFile(
            @NonNull Uri uriPic,
            @NonNull Bitmap bitmap, String name) throws FileNotFoundException {


        FileOutputStream out = null;
        File outputFile;
        try {
            File outputDir = new File(uriPic.getPath());
            if (!outputDir.exists()) {
                outputDir.mkdirs(); // should succeed
            }
            outputFile = new File(outputDir, name);

            out = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /* ignored for PNG */, out);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignore) {
                }
            }
        }
        return Uri.fromFile(outputFile);
    }
}
