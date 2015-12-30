package finalwaterfall;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.hawx.waterfalldemo.R;


import java.util.HashMap;

/**
 * Created by Administrator on 2015/12/29.
 */
public class WaterfallMainActivity extends AppCompatActivity {
    private int columsCount=3;
    private RecyclerView recyclerView;
    private int cacheSize;
    private int spanWidth;
    private LruCache<String,Bitmap> cache;
    private HashMap<String,ImageSize> sizeHashMap=new HashMap<String, ImageSize>();
    private int initpageSize=25;
    private int refreshSize=10;
    private int itemCount=0;
    private boolean allLoaded;
    private boolean noMore=false;
    private Context context=this;
    private WaterfallRecyclerAdapter adapter;
    private StaggeredGridLayoutManager layoutManager;
    private RequestQueue requestqueue;
    private int taskCount=0;
    private ProgressBar progressBar;
    private int progress;
    private int diskCacheSize=50*1024*1024;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waterfall_main_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        progressBar= (ProgressBar) findViewById(R.id.prograssbar);
        recyclerView.setHasFixedSize(true);
        //设置布局方式为StaggeredGridLayout，垂直排列列数为3
        layoutManager = new StaggeredGridLayoutManager(columsCount, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        //获取每列宽度
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        spanWidth = display.getWidth() / columsCount;
        //初始化Volley RequestQueue实例
        requestqueue=MyVolley.newRequestQueue(context,diskCacheSize);
        //设置内存缓存
        setLruCache();
        //初始化图片缓存
        refreshLruCache(initpageSize);
        adapter = new WaterfallRecyclerAdapter(context, itemCount,requestqueue,spanWidth);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new WaterfallRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent=new Intent(context,ImageDisplay.class);
                intent.putExtra("POSITION",position);
                startActivity(intent);
                Toast.makeText(context, "you clicked " + position, Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnItemLongClickListener(new WaterfallRecyclerAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View v, int position) {

            }
        });
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //判断是否停止滚动
                if(newState==RecyclerView.SCROLL_STATE_IDLE) {
                        //判断当前加载是否完成
                        if (allLoaded) {
                            //得到每一列最后一个可见的元素的Position
                            int[] lastvisibalItem = layoutManager.findLastVisibleItemPositions(null);
                            int lastposition;
                            if (columsCount != 1) {
                                lastposition = Math.max(lastvisibalItem[0], lastvisibalItem[1]);
                                for (int i = 2; i < columsCount; i++) {
                                    //获取整个视图可见元素中Position的最大值
                                    lastposition = Math.max(lastposition, lastvisibalItem[i]);
                                }
                            } else {
                                lastposition = lastvisibalItem[0];
                            }
                            if ((lastposition + 1) == itemCount) {
                                //当最后一个可见元素的Position与加载的元素总数相等时，判断滑到底部，更新缓存、加载更多
                                if ((lastposition + 11) <= ImageURLs.imageUrls.length) {
                                    //当还剩余十个以上元素待加载时，加载10个元素
                                    refreshLruCache(refreshSize);
                                    Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show();
                                } else {
                                    if(!noMore) {
                                        //当剩余元素不足十个时，加载剩余元素并提示
                                        int remaining = ImageURLs.imageUrls.length - lastposition - 1;
                                        refreshLruCache(remaining);
                                        Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show();
                                        //没有更多图片
                                        noMore = true;
                                    }else {
                                        //没有更多图片时提示
                                        Toast.makeText(context, "No more pictrues", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }
    private void refreshLruCache(final int refreshNum) {
        //加载状态设置为未完成
        Log.d("WaterFall","refresh start");
        allLoaded=false;
        progressBar.setVisibility(View.VISIBLE);
        if(refreshNum==0){
            allLoaded=true;
            progressBar.setVisibility(View.GONE);
        }
        for(int i=itemCount;i<itemCount+refreshNum;i++){
            final int finalI = i;
            requestqueue.add(new ImageRequest(ImageURLs.imageUrls[i], new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    //将返回的Bitmap加入内存缓存
                    cache.put(ImageURLs.imageUrls[finalI],response);
                    ImageSize imageSize=new ImageSize(response.getWidth(),response.getHeight());
                    sizeHashMap.put(ImageURLs.imageUrls[finalI],imageSize);
                    //所有任务完成后将缓存传入Adapter并更新视图
                    taskCount++;
                    progress= (int) ((float)taskCount/(refreshNum-1)*100);
                    progressBar.setProgress(progress);
                    if(taskCount==refreshNum) {
                        adapter.setLruCache(cache);
                        adapter.setSizeHashMap(sizeHashMap);
                        //更新元素个数
                        itemCount=itemCount+refreshNum;
                        adapter.setItemCount(itemCount);
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                        progress=0;
                        taskCount=0;
                        //加载状态设置为全部完成
                        allLoaded=true;
                        Log.d("WaterFall","refresh end");
                    }
                    Log.d("WaterFall","Task: "+finalI+" completed");
                    Log.d("WaterFall","remaining memorysize is "+(cacheSize-cache.size()));
                }
            },spanWidth,0, ImageView.ScaleType.CENTER_CROP,null,null));
        }

    }

    private void setLruCache() {
        //获取最大缓存大小，单位M
        int maxCacheSize= (int) (Runtime.getRuntime().maxMemory()/1024);
        cacheSize=maxCacheSize/8;
        cache=new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //重写LruCache中计算元素大小方法
                return value.getByteCount()/1024;
            }
        };
    }
}
