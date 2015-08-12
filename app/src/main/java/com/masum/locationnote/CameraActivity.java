package com.masum.locationnote;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.masum.database.NoteTable;
import com.masum.locationlibrary.LocationApplication;
import com.masum.utils.Utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {

    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final String IMAGE = "image";
    public static final String VIDEO = "video";
    public static final String ACTIVITY_LAUNCH = "launch";
    private File capturedImage = null;
    private boolean activity_launch = true;


    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "LocationNote";

    private Uri fileUri; // file url to store image/video
    private ImageView imgPreview;
    private VideoView videoPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        // Checking camera availability
        if (!isDeviceSupportCamera()) {
            Utility.displayWarning(this, "No camera", "Sorry! Your device doesn't support camera");
            // will close the activity if the device does't have camera
            finish();
        }

        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        videoPreview = (VideoView) findViewById(R.id.videoPreview);
        if (savedInstanceState != null){
            activity_launch = savedInstanceState.getBoolean(ACTIVITY_LAUNCH);
        }
        if ( activity_launch) {
            Bundle extras = getIntent().getExtras();
            int mediaType = extras.getInt(NoteTable.COLUMN_IMAGE);
            initializeView(mediaType);
        }
    }

    public void initializeView(int mediaType) {

        if (mediaType == MEDIA_TYPE_IMAGE) {
            // capture picture
            captureImage();
        } else if (mediaType == MEDIA_TYPE_VIDEO) {
            // record video
            recordVideo();
        } else {
            Utility.displayWarning(this, "Unknown media type", "Please try again");
        }
    }

    /**
     * Checking device has camera hardware or not
     */
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /*
     * Capturing Camera Image will lauch camera app requrest image capture
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /*
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
        outState.putBoolean(ACTIVITY_LAUNCH, false);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
        Log.i(MapsActivity.TAG, "Got Uri: " + fileUri.getPath());
    }

    /*
     * Recording video
     */
    private void recordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

        // set video quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
        // name

        // start the video capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    /**
     * Receiving activity result method will be called after closing the camera
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                saveImage();
                //previewCapturedImage();
                // successfully captured the image
                // display it in image view
                // previewCapturedImage();
                /*if( saveImage() ){
                    Bundle bundle = new Bundle();
                    bundle.putString(NoteTable.COLUMN_IMAGE, capturedImage.getAbsolutePath());
                    Intent intent = new Intent(this, NoteEditor.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                }else {
                    Utility.displayWarning(this, "Photo failed", "Sorry fail to take photo.");
                }*/

            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture. Just return to previous activity.
                finish();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == 0) {
                // video successfully recorded
                // preview the recorded video
                previewVideo();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled recording
                Toast.makeText(getApplicationContext(),
                        "User cancelled video recording", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to record video
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void saveImage() {
        final Bitmap bitmap = timeStampImage(fileUri.getPath());
        if (bitmap != null) {
            LocationApplication locationApplication = (LocationApplication) getApplication();
            Bundle bundle = new Bundle();
            bundle.putFloat(NoteTable.COLUMN_LATITUDE, locationApplication.mLocationInfo.lastLat);
            bundle.putFloat(NoteTable.COLUMN_LONGITUDE, locationApplication.mLocationInfo.lastLong);
            if(locationApplication.mTotalAddress == null){
                locationApplication.mTotalAddress = "debug address";
            }
            bundle.putString(NoteTable.COLUMN_ADDRESS, locationApplication.mTotalAddress);
            bundle.putString(NoteTable.COLUMN_IMAGE, capturedImage.getAbsolutePath());
            Intent intent = new Intent(this, NoteEditor.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to save image!", Toast.LENGTH_LONG).show();
            ;
        }
    }

    /*
     * Display image from a path to ImageView
     */
  /*
    private void previewCapturedImage() {
        try {
            // hide video preview
            videoPreview.setVisibility(View.GONE);

            imgPreview.setVisibility(View.VISIBLE);

            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);
           // final Bitmap bitmap = timeStampImage(fileUri.getPath());

            imgPreview.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
*/

    Bitmap timeStampImage(String path) {
        // bitmap factory
        BitmapFactory.Options options = new BitmapFactory.Options();
        // downsizing image as it throws OutOfMemory Exception for larger images
        options.inSampleSize = 8;
        final Bitmap srcBitmap = BitmapFactory.decodeFile(path, options);
        Bitmap destBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String dateTime = sdf.format(Calendar.getInstance().getTime()); // reading local time in the system
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm",
                Locale.getDefault()).format(new Date());
        LocationApplication locationApplication = (LocationApplication) getApplication();
        String imgText = null;
        if(locationApplication.mTotalAddress != null){
             imgText = locationApplication.mTotalAddress+". "+dateTime;
        }else{
            imgText = dateTime;
        }


        Canvas cs = new Canvas(destBitmap);
        Paint tPaint = new Paint();
        tPaint.setTextSize(20);
        tPaint.setColor(Color.WHITE);
        tPaint.setStyle(Paint.Style.FILL);
        cs.drawBitmap(srcBitmap, 0f, 0f, null);

        float height = tPaint.measureText("yX") / 2;
        //cs.drawText(dateTime, 20f, height+15f, tPaint);
        cs.drawText(imgText, 0, destBitmap.getHeight() - height, tPaint);
        try {
            capturedImage = getOutputMediaFile(MEDIA_TYPE_IMAGE," ");
            destBitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(capturedImage));
            Utility.deleteFile(path);
            //imgPreview.setImageBitmap(destBitmap);
            return destBitmap;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            Log.e(MapsActivity.TAG, "ex: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    /*
     * Previewing recorded video
     */
    private void previewVideo() {
        try {
            // hide image preview
            imgPreview.setVisibility(View.GONE);

            videoPreview.setVisibility(View.VISIBLE);
            videoPreview.setVideoPath(fileUri.getPath());
            // start playing
            videoPreview.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ------------ Helper Methods ----------------------
     */

	/*
     * Creating file uri to store image/video
	 */
    public Uri getOutputMediaFileUri(int type) {

        return Uri.fromFile(getOutputMediaFile(type,""));
    }

    /*
     * returning image / video
     */
    private File getOutputMediaFile(int type, String fileNamePad) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Utility.displayWarning(this, "Directory error", "Directory creation filed!");
                Log.d(MapsActivity.TAG, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm",
                Locale.getDefault()).format(new Date())+fileNamePad;
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "LocationNoteImage_" + timeStamp + ".jpg");

        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "LocationNoteVideo_" + timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }
}
