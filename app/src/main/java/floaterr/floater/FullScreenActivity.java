package floaterr.floater;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import floaterr.floater.view.VideoControllerView;

import static floaterr.floater.NameKeys.KEY_IMAGE;
import static floaterr.floater.NameKeys.KEY_POSITION;
import static floaterr.floater.NameKeys.KEY_VIDEO;

public class FullScreenActivity extends Activity implements MediaPlayer.OnCompletionListener,
		MediaPlayer.OnInfoListener, VideoControllerView.MediaPlayerControl {
	private MediaController mediaControls;
    private VideoView mVideoView;
	private Uri videoUri;
	private Uri imageUri;
	private Integer position;
	private VideoControllerView controller;
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.fullscreen_layout);

		if (mediaControls == null)
			mediaControls = new MediaController(this);
			
		getData();
		if (imageUri != null)
			showImage();
		if (videoUri != null)
			startPlayer();
		bindPopUpButton();
		bindLayoutClick();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if (videoUri != null)
			outState.putInt("Position", mVideoView.getCurrentPosition());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		if (videoUri != null) {
			int position = savedInstanceState.getInt("Position");
			mVideoView.seekTo(position);
		}
	}
	
	@Override
	public boolean onInfo(MediaPlayer p1, int p2, int p3)
	{
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer p1)
	{
		if (!p1.isPlaying()) {
			startPlayer();
		}
	}

	private void getData() {
		Intent intent = getIntent();
		imageUri = intent.getParcelableExtra(KEY_IMAGE);
		videoUri = intent.getParcelableExtra(KEY_VIDEO);
		position = intent.getIntExtra(KEY_POSITION, 0);
	}

	private void startPlayer()
	{
		mVideoView = (VideoView) findViewById(R.id.video);
		mVideoView.setVideoURI(videoUri);
		controller = new VideoControllerView(this);
		controller.setMediaPlayer(this);
		controller.setAnchorView((FrameLayout) findViewById(R.id.rootLayout));
		mVideoView.start();
		if ((position != null) && (position != 0))
			mVideoView.seekTo(position);
		
		mVideoView.setOnCompletionListener(this);
		mVideoView.setOnInfoListener(this);
	}

	private void showImage() {
		ImageView imageView = (ImageView) findViewById(R.id.image);
		imageView.setImageURI(imageUri);
	}

	private void bindPopUpButton() {
		ImageButton popupBtn = (ImageButton) findViewById(R.id.popup);
		popupBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				Intent intent = new Intent(FullScreenActivity.this, FloatingWindow.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				if (imageUri != null) {
					intent.putExtra(KEY_IMAGE, imageUri);
				} else if (videoUri != null) {
					int pos = mVideoView.getCurrentPosition();
					intent.putExtra(KEY_VIDEO, videoUri);
					intent.putExtra(KEY_POSITION, pos);
				}
				startService(intent);
			}
		});
	}

	private void bindLayoutClick() {
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.rootLayout);
		frameLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (controller != null) {
					controller.show();
				}
			}
		});
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
