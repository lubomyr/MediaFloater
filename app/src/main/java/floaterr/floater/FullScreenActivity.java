package floaterr.floater;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

public class FullScreenActivity extends Activity implements MediaPlayer.OnCompletionListener,
				     								  MediaPlayer.OnInfoListener {
    private MediaController mediaControls;
    private VideoView mVideoView;
	private Uri uri;
	private Integer position;
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full);

		if (mediaControls == null)
			mediaControls = new MediaController(this);
			
		getData();
		startPlayer();
    }

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt("Position", mVideoView.getCurrentPosition());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		int position = savedInstanceState.getInt("Position");
		mVideoView.seekTo(position);
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
		position = intent.getIntExtra("Position", 0);
		uri = intent.getParcelableExtra("video");
	}

	private void startPlayer()
	{
		mVideoView = (VideoView) findViewById(R.id.video);
		ImageButton popupBtn = (ImageButton) findViewById(R.id.popup);
		mVideoView.setVideoURI(uri);
		mVideoView.setMediaController(mediaControls);
		mediaControls.show();
		mVideoView.start();
		if ((position != null) && (position != 0))
			mVideoView.seekTo(position);
		
		mVideoView.setOnCompletionListener(this);
		mVideoView.setOnInfoListener(this);
		
		popupBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
					Intent intent = new Intent(FullScreenActivity.this, FloatingWindow.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					int pos = mVideoView.getCurrentPosition();
					intent.putExtra("video", uri);
					intent.putExtra("Position", pos);
					startService(intent);
				}
			});
	}
}
