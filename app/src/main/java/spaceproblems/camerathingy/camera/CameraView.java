//package spaceproblems.camerathingy.camera;
//
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.Rect;
//import android.hardware.Camera;
//import android.view.Surface;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//
//import java.io.IOException;
//
//// extending SurfaceView to render the camera images
//public class CameraView extends SurfaceView implements SurfaceHolder
//{
//    private SurfaceHolder mHolder;
//    private Camera mCamera;
//
//    public CameraView(Context context, Camera mCamera) {
//        super(context);
//
//        this.mCamera = mCamera;
//        mHolder = this.getHolder();
//
//        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//
//        setFocusable(true);
//
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width,
//                               int height) {
//    }
//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//
//        try {
//            mCamera.setPreviewDisplay(mHolder);
//        } catch (IOException e) {
//            mCamera.release();
//        }
//        mCamera.startPreview();
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//
//        mCamera.stopPreview();
//        mCamera.release();
//
//    }
//
//    @Override
//    public void addCallback(Callback callback) {
//
//    }
//
//    @Override
//    public void removeCallback(Callback callback) {
//
//    }
//
//    @Override
//    public boolean isCreating() {
//        return false;
//    }
//
//    @Override
//    public void setType(int type) {
//
//    }
//
//    @Override
//    public void setFixedSize(int width, int height) {
//
//    }
//
//    @Override
//    public void setSizeFromLayout() {
//
//    }
//
//    @Override
//    public void setFormat(int format) {
//
//    }
//
//    @Override
//    public Canvas lockCanvas() {
//        return null;
//    }
//
//    @Override
//    public Canvas lockCanvas(Rect dirty) {
//        return null;
//    }
//
//    @Override
//    public void unlockCanvasAndPost(Canvas canvas) {
//
//    }
//
//    @Override
//    public Rect getSurfaceFrame() {
//        return null;
//    }
//
//    @Override
//    public Surface getSurface() {
//        return null;
//    }
//}