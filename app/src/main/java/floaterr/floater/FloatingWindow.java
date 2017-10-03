package floaterr.floater;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.VideoView;

import floaterr.floater.view.VideoControllerView;

import static floaterr.floater.NameKeys.KEY_IMAGE;
import static floaterr.floater.NameKeys.KEY_POSITION;
import static floaterr.floater.NameKeys.KEY_VIDEO;

public class FloatingWindow extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, VideoControllerView.MediaPlayerControl {
    private WindowManager wm;
    private FrameLayout mVideoLayout;
    private ImageView mImageView;
    private VideoView mVideoView;
    private VideoControllerView controller;
    private ImageButton closeBtn;
    private ImageButton fullScrButton;
    private WindowManager.LayoutParams parameters;
    private int currentImageWidth;
    private int currentImageHeight;
    private int realVideoWidth;
    private int realVideoHeight;
    private Uri videoUri;
    private Uri imageUri;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        //getDefaultDimension();
        currentImageWidth = UserPreferences.getWindowWidth();
        currentImageHeight = UserPreferences.getWindowHeight();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startup();
        getData(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    private void startup() {
        parameters = new WindowManager.LayoutParams(
                currentImageWidth, currentImageHeight, WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        parameters.gravity = Gravity.NO_GRAVITY;

        LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mVideoLayout = (FrameLayout) li.inflate(R.layout.floating_layout, null);
        closeBtn = (ImageButton) mVideoLayout.findViewById(R.id.ib_close);
        fullScrButton = (ImageButton) mVideoLayout.findViewById(R.id.popup);
        ImageButton resize = (ImageButton) mVideoLayout.findViewById(R.id.ib_resize);
        mImageView = (ImageView) mVideoLayout.findViewById(R.id.myImage);
        mVideoView = (VideoView) mVideoLayout.findViewById(R.id.myVideo);
        //controller = (VideoControllerView) mVideoLayout.findViewById(R.id.media_controller);
        wm.addView(mVideoLayout, parameters);

        mVideoLayout.setOnTouchListener(new View.OnTouchListener() {
            WindowManager.LayoutParams updatedParameters = parameters;
            double x;
            double y;
            double pressedX;
            double pressedY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = updatedParameters.x;
                        y = updatedParameters.y;
                        pressedX = event.getRawX();
                        pressedY = event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        updatedParameters.x = (int) (x + (event.getRawX() - pressedX));
                        updatedParameters.y = (int) (y + (event.getRawY() - pressedY));
                        wm.updateViewLayout(mVideoLayout, updatedParameters);
                        break;

                    case MotionEvent.ACTION_UP:
                        break;

                    default:
                        break;
                }

                return false;
            }
        });


        resize.setOnTouchListener(new View.OnTouchListener() {
            WindowManager.LayoutParams updatedParameters = parameters;
            double width;
            double height;
            double pressedX;
            double pressedY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                closeBtn.setVisibility(View.INVISIBLE);
                fullScrButton.setVisibility(View.INVISIBLE);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        width = updatedParameters.width;
                        height = updatedParameters.height;
                        pressedX = event.getRawX();
                        pressedY = event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        currentImageWidth = (int) (width + (event.getRawX() - pressedX));
                        currentImageHeight = (int) (height + (event.getRawY() - pressedY));
                        updatedParameters.width = (int) (width + (event.getRawX() - pressedX));
                        updatedParameters.height = (int) (height + (event.getRawY() - pressedY));
                        updatedParameters.gravity = Gravity.NO_GRAVITY;
                        wm.updateViewLayout(mVideoLayout, updatedParameters);
                        break;

                    case MotionEvent.ACTION_UP:
                        if (mImageView.getVisibility() == View.VISIBLE)
                            syncImageSize(updatedParameters);
                        if (mVideoView.getVisibility() == View.VISIBLE)
                            syncVideoSize(updatedParameters, realVideoWidth, realVideoHeight);
                        break;
                    default:
                        break;
                }

                return false;
            }

        });

        fullScrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserPreferences.saveWindowSize(currentImageWidth, currentImageHeight);
                wm.removeView(mVideoLayout);
                stopSelf();
                Intent intent = new Intent(FloatingWindow.this, FullScreenActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (videoUri != null) {
                    int pos = mVideoView.getCurrentPosition();
                    intent.putExtra(KEY_VIDEO, videoUri);
                    intent.putExtra(KEY_POSITION, pos);
                } else if (imageUri != null)
                    intent.putExtra(KEY_IMAGE, imageUri);
                startActivity(intent);
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserPreferences.saveWindowSize(currentImageWidth, currentImageHeight);
                wm.removeView(mVideoLayout);
                stopSelf();
                System.exit(0);
            }
        });

        mVideoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (controller != null) {
                    controller.show();
                }
            }
        });
    }

    private void getData(Intent intent) {
        imageUri = intent.getParcelableExtra(KEY_IMAGE);
        videoUri = intent.getParcelableExtra(KEY_VIDEO);
        int position = intent.getIntExtra(KEY_POSITION, 0);
        if (imageUri != null) {
            mVideoView.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setImageURI(imageUri);
            syncImageSize(parameters);
        } else if (videoUri != null) {
            mImageView.setVisibility(View.GONE);
            mVideoView.setVisibility(View.VISIBLE);
            fullScrButton.setVisibility(View.VISIBLE);
            mVideoView.setVideoURI(videoUri);
            controller = new VideoControllerView(this);
            controller.setMediaPlayer(this);
            controller.setAnchorView(mVideoLayout);
            mVideoView.start();
            mVideoView.seekTo(position);
            mVideoView.setOnCompletionListener(this);
            mVideoView.setOnPreparedListener(this);
        } else {
            mVideoView.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);
        }
    }

    private void syncImageSize(WindowManager.LayoutParams updatedParameters) {
        final int minWidth = 200;
        final int minHeight = 200;
        int realImageWidth = mImageView.getDrawable().getIntrinsicWidth();
        int realImageHeight = mImageView.getDrawable().getIntrinsicHeight();
        double scale;
        int imageWidth;
        int imageHeight;
        if (mImageView.getWidth() != 0)
            currentImageWidth = mImageView.getWidth();
        if (mImageView.getHeight() != 0)
            currentImageHeight = mImageView.getHeight();
        if (currentImageWidth < minWidth)
            currentImageWidth = minWidth;
        if (currentImageHeight < minHeight)
            currentImageHeight = minHeight;
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        if (currentImageWidth > size.x)
            currentImageWidth = size.x;
        if (currentImageHeight > size.y)
            currentImageHeight = size.y;
        if (realImageWidth > realImageHeight) {
            scale = realImageWidth / (double) realImageHeight;
            imageWidth = currentImageWidth;
            imageHeight = (int) (currentImageWidth / scale);
        } else {
            scale = realImageHeight / (double) realImageWidth;
            imageWidth = (int) (currentImageHeight / scale);
            imageHeight = currentImageHeight;
        }
        updatedParameters.width = imageWidth;
        updatedParameters.height = imageHeight;
        wm.updateViewLayout(mVideoLayout, updatedParameters);
        closeBtn.setVisibility(View.VISIBLE);
        fullScrButton.setVisibility(View.VISIBLE);
    }

    private void syncVideoSize(WindowManager.LayoutParams updatedParameters,
                               int realVideoWidth, int realVideoHeight) {
        double scale;
        int imageWidth;
        int imageHeight;
        if (mVideoView.getWidth() != 0)
            currentImageWidth = mVideoView.getWidth();
        if (mVideoView.getHeight() != 0)
            currentImageHeight = mVideoView.getHeight();
        if (realVideoWidth > realVideoHeight) {
            scale = realVideoWidth / (double) realVideoHeight;
            imageWidth = currentImageWidth;
            imageHeight = (int) (currentImageWidth / scale);
        } else {
            scale = realVideoHeight / (double) realVideoWidth;
            imageWidth = (int) (currentImageHeight / scale);
            imageHeight = currentImageHeight;
        }
        updatedParameters.width = imageWidth;
        updatedParameters.height = imageHeight;
        wm.updateViewLayout(mVideoLayout, updatedParameters);
        closeBtn.setVisibility(View.VISIBLE);
        fullScrButton.setVisibility(View.VISIBLE);
    }

    private void getDefaultDimension() {
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        currentImageWidth = width / 2;
        currentImageHeight = height / 2;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (!mediaPlayer.isPlaying()) {
            mVideoView.setVideoURI(videoUri);
            mVideoView.start();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        realVideoWidth = mediaPlayer.getVideoWidth();
        realVideoHeight = mediaPlayer.getVideoHeight();
        syncVideoSize(parameters, realVideoWidth, realVideoHeight);
    }

    // Implement VideoMediaController.MediaPlayerControl
    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return mVideoView.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return mVideoView.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return mVideoView.isPlaying();
    }

    @Override
    public void pause() {
        mVideoView.pause();
    }

    @Override
    public void seekTo(int i) {
        mVideoView.seekTo(i);
    }

    @Override
    public void start() {
        mVideoView.start();
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }
// End VideoMediaController.MediaPlayerControl

}
