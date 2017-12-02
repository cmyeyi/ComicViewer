package ljw.comicviewer.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ljw.comicviewer.Global;
import ljw.comicviewer.R;
import ljw.comicviewer.bean.Comic;
import ljw.comicviewer.http.ComicFetcher;
import ljw.comicviewer.http.ComicService;
import ljw.comicviewer.ui.DetailsActivity;
import ljw.comicviewer.ui.adapter.PictureGridAdapter;
import ljw.comicviewer.util.SnackbarUtil;


/**
 * Created by ljw on 2017-08-23 023.
 */

public class NewAddFragment extends BaseFragment
        implements AbsListView.OnScrollListener, ComicService.RequestCallback {
    private String TAG = NewAddFragment.class.getSimpleName()+"----";
    private Context context;
    protected PictureGridAdapter pictureGridAdapter;
    List<Comic> comicList = new ArrayList<>();
    private File myCache;
    private int loadedPage = 1;
    private boolean isLoadingNext = false;
    @BindView(R.id.comic_info_pull_refresh_grid)
    PullToRefreshGridView pullToRefreshGridView;
    GridView gridView;
    @BindView(R.id.grid_net_error)
    TextView txt_netError;
    @BindView(R.id.grid_loading)
    RelativeLayout loading;
    @BindView(R.id.comic_grid_coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    public NewAddFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = this.getActivity();
        View rootView = inflater.inflate(R.layout.fragment_newadd,null);
        ButterKnife.bind(this,rootView);
        initView();
        return rootView;
    }

    @Override
    public void initView() {
        //禁用上拉下拉
        pullToRefreshGridView.setMode(PullToRefreshBase.Mode.DISABLED);
        initPTRGridView();
        initGridView();
        myCache = context.getExternalCacheDir();

        initLoad();
    }

    public void initPTRGridView() {
        // 设置监听器，这个监听器是可以监听双向滑动的，这样可以触发不同的事件
        pullToRefreshGridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
                //下拉
                loadedPage = 1;
                comicList.clear();
                pictureGridAdapter.notifyDataSetChanged();
                // 获取对象，重新获取当前目录对象
                getListItems(loadedPage);
            }
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
                //上拉
                Glide.get(context).clearMemory();
                isLoadingNext = true;
                getListItems(++loadedPage);
                Log.d(TAG,"load next page; currentLoadingPage = "+loadedPage);
            }
        });
    }

    public void initGridView() {
        gridView = pullToRefreshGridView.getRefreshableView();

        //根据屏幕宽度设置列数
//        int columns = DisplayUtil.getGridNumColumns(context,120);
//        gridView.setNumColumns(columns);

        pictureGridAdapter = new PictureGridAdapter(context,comicList);
        gridView.setAdapter(pictureGridAdapter);
        gridView.setOnScrollListener(this);
        gridView.setOnItemClickListener(new ItemClickListener());
        pictureGridAdapter.notifyDataSetChanged();
    }

    @Override
    public void initLoad(){
        getListItems(1);
    }


    //获得漫画列表对象并存入comicList
    public void getListItems(int page){
        ComicService.get().getListItems(this,page);
    }


    //TODO:滑动事件--------
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState){
            case SCROLL_STATE_TOUCH_SCROLL:
                //手指接触状态
                break;
            case SCROLL_STATE_FLING:
                //屏幕处于滑动状态
                break;
            case SCROLL_STATE_IDLE:
                //停止滑动状态
                clearAndLoadImage();
                break;
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int firstItem, int visibleItem, int totalItem) {
        //firstItem 为第一个可见对象的下标, visibleItem可见对象的数量, totalItem 可见对象的总数
//        Log.d(TAG, "onScroll: "+firstItem+","+visibleItem+","+totalItem);
    }

    //延迟刷新适配器，防止第一次加载不显示封面
    public void delayedFlushAdapter(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pictureGridAdapter.notifyDataSetChanged();
                clearAndLoadImage();
            }
        },200);
    }

    //清理不可见是item,加载可见的item。如果是第一次就加载这加载整页
    public void clearAndLoadImage(){
        int firstVisiblePosition= gridView.getFirstVisiblePosition();
        int lastVisiblePosition = gridView.getLastVisiblePosition();
        if (lastVisiblePosition==-1){
            delayedFlushAdapter();
            return;
        }
        for(int i = 0; i < gridView.getCount();i++){
            View view = pictureGridAdapter.getViewMap().get(i);
            if (view!=null){
                ImageView image = (ImageView) view.findViewById(R.id.comic_img);
                ImageView EndTag = (ImageView) view.findViewById(R.id.comic_status);
                if(i<firstVisiblePosition || i>lastVisiblePosition){
                    //Log.d(TAG, "clearImage: "+i);
                    image.setImageBitmap(null);
                    image.setImageDrawable(null);
                    EndTag.setImageResource(0);
                }else{
                    //Log.d(TAG, "loadImage: "+i);
                    pictureGridAdapter.loadCover(i,view);
                }
            }
        }
        System.gc();
        pictureGridAdapter.notifyDataSetChanged();
    }

    @Override
    public Object myDoInBackground(String TAG,Object data) {
        switch (TAG) {
            case Global.REQUEST_COMICS_LIST:
                List<Comic> tempList = ComicFetcher.getComicList(data.toString());
                if(tempList.size()>0) comicList.addAll(tempList);
                return tempList.size();
        }
        return null;
    }

    @Override
    public void myOnPostExecute(String TAG,Object resultObj) {
        switch (TAG){
            case Global.REQUEST_COMICS_LIST:
                if (resultObj!=null && (Integer)resultObj > 0){
                    pictureGridAdapter.notifyDataSetChanged();
                    //得到数据立刻取消刷新状态
                    pullToRefreshGridView.onRefreshComplete();
                    txt_netError.setVisibility(View.GONE);
                    loading.setVisibility(View.GONE);
                    pullToRefreshGridView.setMode(PullToRefreshBase.Mode.BOTH);
                    isLoadingNext = false;
                    clearAndLoadImage();
                }else{
                    netErrorTo();
                }
                break;
        }
    }

    //网络请求，更新UI
    @Override
    public void onFinish(Object data, String what) {
        switch (what){
            case Global.REQUEST_COMICS_LIST:
                UIUpdateTask UIUpdateTask = new UIUpdateTask(what,data);
                UIUpdateTask.execute();
                break;
        }
    }

    @Override
    public void onError(String msg ,String what) {
        switch (what){
            case Global.REQUEST_COMICS_LATEST:
            case Global.REQUEST_COMICS_LIST:
                pullToRefreshGridView.onRefreshComplete();
                if(isLoadingNext) {
                    SnackbarUtil.newAddImageColorfulSnackar(
                            coordinatorLayout, getString(R.string.gird_tips_loading_next_page_fail),
                            R.drawable.icon_error,
                            ContextCompat.getColor(context,R.color.star_yellow)).show();
                }else{
                    netErrorTo();
                }
                break;
        }
        Log.e(TAG,what + " Error: " + msg);
    }

    public void netErrorTo(){
        if (pullToRefreshGridView.isRefreshing()) pullToRefreshGridView.onRefreshComplete();
        SnackbarUtil.newAddImageColorfulSnackar(
                coordinatorLayout, getString(R.string.data_load_fail),
                R.drawable.icon_error,
                ContextCompat.getColor(context,R.color.star_yellow)).show();
        if (!isLoadingNext){
            pullToRefreshGridView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            loading.setVisibility(View.GONE);
            txt_netError.setVisibility(View.VISIBLE);
        }
        isLoadingNext = false;
    }

    //-------------------

    //网格对象点击事件
    class  ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Comic comic = comicList.get(position);
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("id",comic.getComicId());
            intent.putExtra("score",comic.getScore());
            intent.putExtra("title",comic.getName());
            startActivity(intent);
        }
    }

}