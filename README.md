# WaterfallDemo
    瀑布流照片墙demo，基于RecyclerView和Volley设计
    1、实现了memory和disk二级缓存，解决了RecyclerView加载大量图片时的卡顿现象。
    2、通过设置OnScrollListener实现触底加载更多。
    3、当前图片不在内存缓存时使用默认图片占位。
    
    不足：内存缓存设计不够完善，在上滑返回过多之后会出现轻微卡顿。
          由于RecyclerView的自带动画，用来写瀑布流感觉不如用其他自定义控件方便...
