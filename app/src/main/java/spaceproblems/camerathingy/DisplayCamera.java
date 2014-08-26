package spaceproblems.camerathingy;


import spaceproblems.camerathingy.utils.SystemUiHider;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class DisplayCamera extends ListActivity implements SurfaceHolder
        .Callback
{
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = false;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 1500;

    private static final String TAG = "DisplayCamera";

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;
    private SurfaceView mSurfaceView;
    private Camera.PictureCallback mPicture;
    private Timer timer;
    private File pictureFile;
    private Camera mCamera;
    private boolean capturing = false;
    private ImageAdapter mImageAdapter;
    private BitmapDrawable[] thumbNails;
    private String[] paths;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_camera);
        setupActionBar();

        loadThumbNails();

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.mySurfaceView);
        final ListView picturesView = getListView();
        mImageAdapter = new ImageAdapter(this, R.layout.listview_item_row,
                thumbNails, paths);
        picturesView.setAdapter(mImageAdapter);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener()
                {

                    @Override
                    @TargetApi(Build.VERSION_CODES.KITKAT)
                    public void onVisibilityChange(boolean visible)
                    {

                        controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        picturesView.setVisibility(visible ? View.VISIBLE : View.GONE);

                        if (visible && AUTO_HIDE)
                        {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (TOGGLE_ON_CLICK)
                {
                    mSystemUiHider.toggle();
                } else
                {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        findViewById(R.id.startCapturing).setOnTouchListener(mDelayHideTouchListener);

        mSurfaceView = (SurfaceView) findViewById(R.id.mySurfaceView);
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(this);

        mPicture = new Camera.PictureCallback()
        {

            @Override
            public void onPictureTaken(byte[] data, Camera camera)
            {

                pictureFile = getOutputMediaFile(1);
                if (pictureFile == null)
                {
                    Log.d(TAG, "Error creating media file, check storage permissions: ");
                    return;
                }

                try
                {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    data = greyScaledImage(data);
                    fos.write(data);
                    fos.close();
                    displayToast(pictureFile);
                } catch (FileNotFoundException e)
                {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e)
                {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }
            }
        };

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height)
    {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {

        if (safeCameraOpen())
        {
            mCamera.setDisplayOrientation(90);
            Camera.Parameters parameters = mCamera.getParameters();

            try
            {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e)
            {
                mCamera.release();
            }

            parameters.set("orientation", "portrait");
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {

        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null)
        {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();
            releaseCameraAndPreview();
        }
    }

    private boolean safeCameraOpen()
    {
        boolean qOpened = false;

        try
        {
            releaseCameraAndPreview();
            mCamera = Camera.open();
            qOpened = (mCamera != null);
        } catch (Exception e)
        {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;
    }

    private void releaseCameraAndPreview()
    {

        if (mCamera != null)
        {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setupActionBar()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener()
    {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent)
        {
            if (AUTO_HIDE)
            {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis)
    {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void startCapturing(View view)
    {

        if (capturing == true)
        {
            capturing = false;
            timer.cancel();
            return;
        } else if (capturing == false)
        {
            capturing = true;
            timer = new Timer();
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    checkDirectorySizeAndPurge();
                    mCamera.startPreview();
                    mCamera.takePicture(null, null, mPicture);
                }
            }, 0, 2000);
        }
    }

    private void displayToast(File picture)
    {

        Context context = getApplicationContext();
        long fileSize = picture.length();
        CharSequence text = "Size of File Saved " + fileSize;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void purgeOldestFile(File directory)
    {

        File[] files = directory.listFiles();

        Arrays.sort(files, new Comparator<File>()
        {
            public int compare(File f1, File f2)
            {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });
        Log.d(TAG, "deleting file " + files[0].getName());
        files[0].delete();

    }

    private void checkDirectorySizeAndPurge()
    {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraAppThingy");
        long size = getFolderSize(mediaStorageDir);
        while (size > 20000000)
        {
            Log.d(TAG, "PURGING DIRECTORY");
            purgeOldestFile(mediaStorageDir);
            size = getFolderSize(mediaStorageDir);
        }
    }

    public long getFolderSize(File dir)
    {
        long size = 0;
        for (File file : dir.listFiles())
        {
            size += file.length();
        }
        Log.d(TAG, "Folder size " + size);
        return size;
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type)
    {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraAppThingy");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists())
        {
            if (!mediaStorageDir.mkdirs())
            {
                Log.d("MyCameraAppThingy", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == 1)
        {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        }
        else
        {
            return null;
        }

        return mediaFile;
    }

    private void loadThumbNails()
    {

        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraAppThingy");
        int size = dir.listFiles().length;
        BitmapDrawable[] images = new BitmapDrawable[size];
        String[] localPaths = new String[size];
        int i = 0;
        thumbNails = null;
        paths = null;
        for (File file : dir.listFiles())
        {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inDither = true;
            Bitmap image = ThumbnailUtils.extractThumbnail(BitmapFactory
                            .decodeFile(file.getAbsolutePath(),options),
                            192, 192);
            images[i] = new BitmapDrawable(getResources(), image);
            localPaths[i] = file.getAbsolutePath();
            i++;
        }
        thumbNails = images;
        paths = localPaths;
    }

    private byte[] greyScaledImage(byte[] data)
    {

        return data;

    }

    @Override
    public void onListItemClick(ListView lv, View v, int position, long id)
    {

        thumbNails = null;
        Log.d(TAG, "List item clicked: " + position);
        Intent intent = new Intent(this, InteractWithImage.class);
        Bundle b = new Bundle();
        b.putString("image", paths[position]);
        intent.putExtras(b);
        startActivity(intent);

    }

//    @Override
//    protected void onResume()
//    {
//
//        loadThumbNails();
//        mImageAdapter.notifyDataSetChanged();
//        super.onResume();
//
//    }
}
