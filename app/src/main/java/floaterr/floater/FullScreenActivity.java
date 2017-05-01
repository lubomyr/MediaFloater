package floaterr.floater;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import static floaterr.floater.NameKeys.KEY_IMAGE;
import static floaterr.floater.NameKeys.KEY_POSITION;
import static floaterr.floater.NameKeys.KEY_VIDEO;

public class FullScreenActivity extends Activity implements MediaPlayer.OnCompletionListener,
				     								  MediaPlayer.OnInfoListener {
    private MediaController mediaControls;
    private VideoView mVideoView;
	private Uri videoUri;
	private Uri imageUri;
	private Integer position;
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full);

		if (mediaControls == null)
			mediaControls = new MediaController(this);
			
		getData();
		if (imageUri != null)
			showImage();
		if (videoUri != null)
			startPlayer();
		bindPopUpButton();
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
		mVideoView.setMediaController(mediaControls);
		mediaControls.show();
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
}
