package ljw.comicviewer.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import ljw.comicviewer.ui.DetailsActivity;
import ljw.comicviewer.ui.Global;
import ljw.comicviewer.R;
import ljw.comicviewer.ui.adapter.PictureGridAdapter;
import ljw.comicviewer.util.DisplayUtil;
import ljw.comicviewer.bean.CallBackData;
import ljw.comicviewer.bean.Comic;
import ljw.comicviewer.http.ComicService;
import ljw.comicviewer.http.ComicFetcher;



/**
 * Created by ljw on 2017-08-23 023.
 */

public class ComicGridFragment extends Fragment
        implements AbsListView.OnScrollListener, ComicService.RequestCallback  {
    private String TAG = ComicGridFragment.class.getSimpleName()+"----";
    private Context context;
    private PictureGridAdapter pictureGridAdapter;
    private List<Comic> comicList = new ArrayList<>();
    private File myCache;
    private int loadedPage = 1;
    private boolean isLoadingNext = false;
    // map设定：键为null未加载 0加载中 1加载完毕
    private Map<Integer,Integer> imageState = new HashMap<>();
//    @BindView(R.id.swipe_refresh_list)
//    SwipeRefreshLayout fileListSwipe;
    @BindView(R.id.comic_info_pull_refresh_grid)
    PullToRefreshGridView pullToRefreshGridView;
//    @BindView(R.id.comic_info_view)
    GridView gridView;
    @BindView(R.id.grid_net_error)
    TextView txt_netError;
//    @BindView(R.id.grid_tips)
//    TextView txt_tips;
//    @BindView(R.id.grid_tips_view)
//    RelativeLayout tips_view;
    public ComicGridFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = this.getActivity();
        View rootView = inflater.inflate(R.layout.fragment_comic_grid,null);
        ButterKnife.bind(this,rootView);

        initPTRGridView(rootView);
        initGridView();
        myCache = context.getExternalCacheDir();

        getListItems(1);
        return rootView;
    }

    private void initPTRGridView(View view) {
        // 得到下拉刷新的GridView
        pullToRefreshGridView = (PullToRefreshGridView) view.findViewById(R.id.comic_info_pull_refresh_grid);
        // 设置监听器，这个监听器是可以监听双向滑动的，这样可以触发不同的事件
        pullToRefreshGridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
//                Toast.makeText(context, "下拉", Toast.LENGTH_SHORT).show();
                loadedPage = 1;
                comicList.clear();
                imageState.clear();
                // 获取对象，重新获取当前目录对象
                getListItems(loadedPage);
                //2秒刷新事件
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullToRefreshGridView.onRefreshComplete();
                    }
                }, 2000);
            }
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
//                Toast.makeText(context, "上拉", Toast.LENGTH_SHORT).show();
                isLoadingNext = true;
                getListItems(++loadedPage);
                Log.d(TAG,"is last; currentLoadingPage = "+loadedPage);
            }
        });
    }

    private void initGridView() {
        gridView = pullToRefreshGridView.getRefreshableView();

        //根据屏幕宽度设置列数
        gridView.setColumnWidth(GridView.AUTO_FIT);
//        int columns = DisplayUtil.getGridNumColumns(context,120);
//        gridView.setNumColumns(columns);

        pictureGridAdapter = new PictureGridAdapter(context,comicList);
        gridView.setAdapter(pictureGridAdapter);
        gridView.setOnScrollListener(this);
        gridView.setOnItemClickListener(new ItemClickListener());

    }

    //获得漫画列表对象并存入comicList
    private void getListItems(int page){
        ComicService.get().getListItems(this,page);
    }


    //TODO:滑动事件--------
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView absListView, int firstItem, int visibleItem, int totalItem) {
        //firstItem 为第一个可见对象的下标, visibleItem可见对象的数量, totalItem 可见对象的总数

    }


    //TODO:网络请求，更新UI
    @Override
    public void onFinish(Object data, String what) {
        switch (what){
            case Global.REQUEST_COMICS_LIST:
                comicList.addAll(ComicFetcher.getComicList(data.toString()));
//                Toast.makeText(context,"获得数据"+comicList.size(),Toast.LENGTH_LONG).show();
                pictureGridAdapter.notifyDataSetChanged();
                //得到数据立刻取消刷新状态
                pullToRefreshGridView.onRefreshComplete();
                txt_netError.setVisibility(View.GONE);
                pullToRefreshGridView.setMode(PullToRefreshBase.Mode.BOTH);
                isLoadingNext = false;
                break;
            case Global.REQUEST_COMICS_IMAGE:
                CallBackData callBackData = (CallBackData) data;
                int position = (int) callBackData.getArg1();
                comicList.get(position).setCover((Bitmap) callBackData.getObj());
                imageState.put(position,1);
                pictureGridAdapter.notifyDataSetChanged();
                Log.d(TAG,callBackData.getMsg());
                break;
        }
    }

    @Override
    public void onError(String msg ,String what) {
        switch (what){
            case Global.REQUEST_COMICS_LIST:
                pullToRefreshGridView.onRefreshComplete();
                if(isLoadingNext) {
                    Toast.makeText(context, R.string.gird_tips_loading_next_page_fail, Toast.LENGTH_LONG).show();
                }else{
                    pullToRefreshGridView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                    txt_netError.setVisibility(View.VISIBLE);
                }
                isLoadingNext = false;
                break;
        }
        Log.e(TAG, "Error: " + msg);

    }

    //-------------------

    //TODO:网格对象点击事件
    class  ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Comic comic = comicList.get(position);
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("id",comic.getId());
            intent.putExtra("score",comic.getScore());
            intent.putExtra("title",comic.getName());
            startActivity(intent);
        }
    }



}