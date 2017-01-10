package floaterr.floater.view;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import floaterr.floater.R;

public class FloaterView extends RelativeLayout {
    private ImageView myImage;
    private VideoView myVideo;
    private ImageButton closeBtn;

    public FloaterView(Context context) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.custom_layout, this, true);

        closeBtn = (ImageButton) findViewById(R.id.ib_close);
        ImageButton resize = (ImageButton) findViewById(R.id.ib_resize);
        myImage = (ImageView) findViewById(R.id.myImage);
        myVideo = (VideoView) findViewById(R.id.myVideo);
    }

    public void setImage(Uri uri) {
        myVideo.setVisibility(View.GONE);
        myImage.setVisibility(View.VISIBLE);
        myImage.setImageURI(uri);
    }

    public void setVideo(Uri uri) {
        myImage.setVisibility(View.GONE);
        myVideo.setVisibility(View.VISIBLE);
        myVideo.setVideoURI(uri);
        myVideo.start();
        //myVideo.setOnCompletionListener(this);
        //myVideo.setOnPreparedListener(this);
    }

}
