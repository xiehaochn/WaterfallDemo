package finalwaterfall;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.hawx.waterfalldemo.R;


/**
 * Created by Administrator on 2015/12/30.
 */
public class ImageDisplay extends AppCompatActivity {
    private ImageView imageView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imagedisplay_layout);
        imageView= (ImageView) findViewById(R.id.displayview);
        int p=getIntent().getIntExtra("POSITION",0);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(new ImageRequest(ImageURLs.imageUrls[p], new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                imageView.setImageBitmap(response);
            }
        },0,0, ImageView.ScaleType.CENTER_CROP,null,null));
    }
}
