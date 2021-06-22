package com.a_track_it.fitdata.common.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.a_track_it.fitdata.common.Constants;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.a_track_it.fitdata.common.Constants.KEY_FIT_VALUE;
/*
 * Extracts {@link android.graphics.Bitmap} data from the
 * {@link com.google.android.gms.wearable.Asset}
 */

public class LoadBitmapAsyncTask extends AsyncTask<Asset, Void, Bitmap> {
    private static final String LOG_TAG = LoadBitmapAsyncTask.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private Context mContext;

    public LoadBitmapAsyncTask(Context context) {
        super();
        mContext = context;
    }

    @Override
    protected Bitmap doInBackground(Asset... params) {

        if (params.length > 0) {
            Asset asset = params[0];
            if (asset == null)
                return null;

            Task<DataClient.GetFdForAssetResponse> getFdForAssetResponseTask =
                    Wearable.getDataClient(mContext).getFdForAsset(asset);

            try {
                // Block on a task and get the result synchronously. This is generally done
                // when executing a task inside a separately managed background thread. Doing
                // this on the main (UI) thread can cause your application to become
                // unresponsive.
                DataClient.GetFdForAssetResponse getFdForAssetResponse =
                        Tasks.await(getFdForAssetResponseTask);

                InputStream assetInputStream = getFdForAssetResponse.getInputStream();

                if (assetInputStream != null) {
                    Bitmap bp = BitmapFactory.decodeStream(assetInputStream);
                    assetInputStream.close();
                    return bp;

                } else {
                    Log.w(LOG_TAG, "Requested an unknown Asset.");
                    return null;
                }

            } catch (ExecutionException | IOException exception) {
                Log.e(LOG_TAG, "Failed retrieving asset, Task failed: " + exception);
                return null;

            } catch (InterruptedException exception) {
                Log.e(LOG_TAG, "Failed retrieving asset, interrupt occurred: " + exception);
                return null;
            }

        } else {
            Log.e(LOG_TAG, "Asset must be non-null");
            return null;
        }
    }
    /**
     * Writes bitmap to a temporary file and returns the Uri for the file
     * @param applicationContext Application context
     * @param bitmap Bitmap to write to temp file
     * @return Uri for temp file with bitmap
     * @throws FileNotFoundException Throws if bitmap file cannot be found
     */
    private Uri writeBitmapToFile(
            @NonNull Context applicationContext,
            @NonNull Bitmap bitmap) throws FileNotFoundException {

        String name = String.format("atrackit-%s.png", UUID.randomUUID().toString());

        File outputDir = new File(applicationContext.getFilesDir(), Constants.OUTPUT_PATH);
        if (!outputDir.exists()) {
            outputDir.mkdirs(); // should succeed
        }
        File outputFile = new File(outputDir, name);
        FileOutputStream out = null;
        try {
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

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if ((bitmap != null) && (mContext != null)){
            //int imageAssetItemIndex = mCustomRecyclerAdapter.setImageAsset(bitmap);
            try {
                Uri outputUri =  writeBitmapToFile(mContext, bitmap);
                if (outputUri != null){
                    Intent refreshIntent = new Intent(Constants.INTENT_HOME_REFRESH);
                    refreshIntent.putExtra(KEY_FIT_VALUE, outputUri.toString());
                    refreshIntent.setPackage(Constants.ATRACKIT_ATRACKIT_CLASS);
                    mContext.sendBroadcast(refreshIntent);
                }
            }catch (FileNotFoundException fnf){
                Log.e(LOG_TAG, "writeBitmapToFile error " + fnf.getMessage());
            }
            mContext = null;
        }
    }
}