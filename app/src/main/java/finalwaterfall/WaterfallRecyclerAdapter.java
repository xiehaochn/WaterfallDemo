package finalwaterfall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.hawx.waterfalldemo.R;

import java.util.HashMap;

/**
 * Created by Administrator on 2015/12/29.
 */
public class WaterfallRecyclerAdapter extends RecyclerView.Adapter<WaterfallRecyclerAdapter.WaterFallVH> {
    private LruCache<String,Bitmap> lruCache;
    private HashMap<String,ImageSize> sizeHashMap;
    private Context context;
    private int itemCount;
    private RequestQueue requestQueue;
    private int spanWidth;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public WaterfallRecyclerAdapter(Context context,int itemCount, RequestQueue requestQueue,int spanWidth) {
        super();
        this.context=context;
        this.itemCount=itemCount;
        this.requestQueue=requestQueue;
        this.spanWidth=spanWidth;
    }

    @Override
    public WaterFallVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.waterfall_viewholder,parent,false);
        WaterFallVH waterFallVH=new WaterFallVH(view);
        return waterFallVH;
    }

    @Override
    public void onBindViewHolder(final WaterFallVH holder, int position) {
        if(lruCache.get(ImageURLs.imageUrls[position])!=null){
            //内存缓存存在直接加载
            holder.imageView.setImageBitmap(lruCache.get(ImageURLs.imageUrls[position]));
        }else{
            //若尺寸信息缓存存在，则获取图片大小信息
            if(sizeHashMap.get(ImageURLs.imageUrls[position])!=null) {
                int[] size = getLruSize(position);
                Bitmap bitmap=resizedImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.loading),size[0],size[1]);
                //加载占位图片
                holder.imageView.setImageBitmap(bitmap);
            }
            //内存缓存中不存在该元素，发送Volley请求
            sendVolleyRequest(holder,position);
        }
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener!=null){
                    onItemClickListener.onItemClick(holder.imageView,holder.getLayoutPosition());
                }
            }
        });
        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(onItemLongClickListener!=null){
                    onItemLongClickListener.onItemLongClick(holder.imageView,holder.getLayoutPosition());
                }
                return true;
            }
        });
    }

    private int[] getLruSize(int position) {
        ImageSize imageSize=sizeHashMap.get(ImageURLs.imageUrls[position]);
        int[] size=new int[2];
        size[0]=imageSize.getImageWidth();
        size[1]=imageSize.getImageHeight();
        return size;
    }

    private void sendVolleyRequest(final WaterFallVH holder, final int position) {
        requestQueue.add(new ImageRequest(ImageURLs.imageUrls[position], new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                holder.imageView.setImageBitmap(response);
                //再次加入内存缓存
                lruCache.put(ImageURLs.imageUrls[position],response);
                Log.d("WaterFall","Task: "+position+" completed again");
            }
        },spanWidth,0, ImageView.ScaleType.CENTER_CROP,null,null));
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    public class WaterFallVH extends RecyclerView.ViewHolder{
        private ImageView imageView;
        public WaterFallVH(View itemView) {
            super(itemView);
            imageView= (ImageView) itemView.findViewById(R.id.iamgeview);
        }
    }
    public void setOnItemClickListener(OnItemClickListener listener){
        onItemClickListener=listener;
    }
    public void setOnItemLongClickListener(OnItemLongClickListener listener){
        onItemLongClickListener=listener;
    }
    public interface OnItemClickListener{
        void onItemClick(View v, int position);
    }
    public interface OnItemLongClickListener{
        void onItemLongClick(View v, int position);
    }
    public void setItemCount(int itemCount){
        this.itemCount=itemCount;
    }
    public void setLruCache(LruCache<String,Bitmap> lruCache){
        this.lruCache=lruCache;
    }
    public void setSizeHashMap(HashMap<String,ImageSize> hashMap){
        this.sizeHashMap=hashMap;
    }
    private Bitmap resizedImage(Bitmap bitmap,int desiredWidth,int desiredHeight) {
        Bitmap tempbitmap=Bitmap.createScaledBitmap(bitmap,desiredWidth,desiredHeight,true);
        return tempbitmap;
    }
}
