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
    private FrameLayout myView;
    private ImageView myImage;
    private VideoView myVideo;
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

        currentImageWidth = 400;
        currentImageHeight = 400;
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
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        parameters = new WindowManager.LayoutParams(
                currentImageWidth, currentImageHeight, WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        parameters.gravity = Gravity.NO_GRAVITY;

        LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        myView = (FrameLayout) li.inflate(R.layout.floating_layout, null);
        closeBtn = (ImageButton) myView.findViewById(R.id.ib_close);
        fullScrButton = (ImageButton) myView.findViewById(R.id.popup);
        ImageButton resize = (ImageButton) myView.findViewById(R.id.ib_resize);
        myImage = (ImageView) myView.findViewById(R.id.myImage);
        myVideo = (VideoView) myView.findViewById(R.id.myVideo);
        wm.addView(myView, parameters);

        myView.setOnTouchListener(new View.OnTouchListener() {
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
                        wm.updateViewLayout(myView, updatedParameters);
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
                        wm.updateViewLayout(myView, updatedParameters);
                        break;

                    case MotionEvent.ACTION_UP:
                        if (myImage.getVisibility() == View.VISIBLE)
                            syncImageSize(updatedParameters);
                        if (myVideo.getVisibility() == View.VISIBLE)
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
                wm.removeView(myView);
                stopSelf();
                Intent intent = new Intent(FloatingWindow.this, FullScreenActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (videoUri != null) {
                    int pos = myVideo.getCurrentPosition();
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
                wm.removeView(myView);
                stopSelf();
                System.exit(0);
            }
        });

        myView.setOnClickListener(new View.OnClickListener() {
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
            myVideo.setVisibility(View.GONE);
            myImage.setVisibility(View.VISIBLE);
            myImage.setImageURI(imageUri);
            syncImageSize(parameters);
        } else if (videoUri != null) {
            myImage.setVisibility(View.GONE);
            myVideo.setVisibility(View.VISIBLE);
            fullScrButton.setVisibility(View.VISIBLE);
            myVideo.setVideoURI(videoUri);
            controller = new VideoControllerView(this);
            controller.setMediaPlayer(this);
            controller.setAnchorView((FrameLayout) myView.findViewById(R.id.rl_custom_layout));
            myVideo.start();
            myVideo.seekTo(position);
            myVideo.setOnCompletionListener(this);
            myVideo.setOnPreparedListener(this);
        } else {
            myVideo.setVisibility(View.GONE);
            myImage.setVisibility(View.VISIBLE);
        }
    }

    private void syncImageSize(WindowManager.LayoutParams updatedParameters) {
        final int minWidth = 200;
        final int minHeight = 200;
        int realImageWidth = myImage.getDrawable().getIntrinsicWidth();
        int realImageHeight = myImage.getDrawable().getIntrinsicHeight();
        double scale;
        int imageWidth;
        int imageHeight;
        if (myImage.getWidth() != 0)
            currentImageWidth = myImage.getWidth();
        if (myImage.getHeight() != 0)
            currentImageHeight = myImage.getHeight();
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
        wm.updateViewLayout(myView, updatedParameters);
        closeBtn.setVisibility(View.VISIBLE);
        fullScrButton.setVisibility(View.VISIBLE);
    }

    private void syncVideoSize(WindowManager.LayoutParams updatedParameters,
                               int realVideoWidth, int realVideoHeight) {
        double scale;
        int imageWidth;
        int imageHeight;
        if (myVideo.getWidth() != 0)
            currentImageWidth = myVideo.getWidth();
        if (myVideo.getHeight() != 0)
            currentImageHeight = myVideo.getHeight();
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
        wm.updateViewLayout(myView, updatedParameters);
        closeBtn.setVisibility(View.VISIBLE);
        fullScrButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (!mediaPlayer.isPlaying()) {
            myVideo.setVideoURI(videoUri);
            myVideo.start();
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
        return myVideo.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return myVideo.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return myVideo.isPlaying();
    }

    @Override
    public void pause() {
        myVideo.pause();
    }

    @Override
    public void seekTo(int i) {
        myVideo.seekTo(i);
    }

    @Override
    public void start() {
        myVideo.start();
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
