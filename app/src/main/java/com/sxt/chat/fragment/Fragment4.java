package com.sxt.chat.fragment;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.comm.util.AdError;
import com.sxt.chat.R;
import com.sxt.chat.base.BaseRecyclerAdapter;
import com.sxt.chat.base.LazyFragment;
import com.sxt.chat.utils.Constants;
import com.sxt.chat.view.CustomRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by 11837 on 2018/4/22.
 */

public class Fragment4 extends LazyFragment implements
        NativeExpressAD.NativeExpressADListener {

    private final String TAG = "AD_sxt";
    public static final int AD_COUNT = 10;    // 加载广告的条数，取值范围为[1, 10]

    private Handler handler = new Handler();
    private CustomRecyclerView mRecyclerView;
    private CustomAdapter mAdapter;
    private NativeExpressAD mADManager;
    private List<Object> mDataList = new ArrayList<>();
    private List<NativeExpressADView> mAdViewList;
    private HashMap<NativeExpressADView, Integer> mAdViewPositionMap = new HashMap();
    private SwipeRefreshLayout swipeRefreshLayout;
    private View preBannerView;

    @Override
    protected int getDisplayView(LayoutInflater inflater, ViewGroup container) {
        return R.layout.fragment_4;
    }

    @Override
    protected void initView() {
        mRecyclerView = (CustomRecyclerView) contentView.findViewById(R.id.recyclerView);
        swipeRefreshLayout = contentView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryDark, R.color.colorAccent, R.color.main_blue, R.color.main_green);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadAD();//刷新广告
            }
        });
        setAdapter();
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                loadAD();//刷新原生广告
                swipeRefreshLayout.setRefreshing(true);//第一次来 并不会调用onRefresh方法  android bug
            }
        });
    }

    private void setAdapter() {
        mDataList.clear();
//        mDataList.addAll(contentList);
        if (mAdapter == null) {
            mAdapter = new CustomAdapter(activity, mDataList);
            mRecyclerView.setEmptyView(LayoutInflater.from(activity).inflate(R.layout.item_no_data, null, false));
            mRecyclerView.setAdapter(new LinearLayoutManager(activity), mAdapter);
            mRecyclerView.getRecyclerView().setHasFixedSize(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        // 使用完了每一个NativeExpressADView之后都要释放掉资源。
        if (mAdViewList != null) {
            for (NativeExpressADView view : mAdViewList) {
                view.destroy();
            }
        }
    }

    /**
     * 如果选择支持视频的模版样式，请使用{@link Constants#NativeExpressSupportVideoPosID}
     */
    private void loadAD() {
        if (mADManager == null) {
            ADSize adSize = new ADSize(ADSize.FULL_WIDTH, ADSize.AUTO_HEIGHT); // 消息流中用AUTO_HEIGHT
            mADManager = new NativeExpressAD(activity, adSize, Constants.APPID, Constants.NativeExpressSupportVideoPosID, this);
//            mADManager = new NativeExpressAD(activity, adSize, "1101152570", "2000629911207832", this);
        }
        mADManager.loadAD(AD_COUNT);
    }

    @Override
    public void onNoAD(AdError adError) {
        swipeRefreshLayout.setRefreshing(false);
        mDataList.clear();
        mAdapter.notifyDataSetChanged();
        Log.i(TAG, String.format("onNoAD, error code: %d, error msg: %s", adError.getErrorCode(), adError.getErrorMsg()));
    }

    @Override
    public void onADLoaded(List<NativeExpressADView> adList) {
        Log.i(TAG, "onADLoaded: " + adList.size());
        mAdViewList = adList;
        setAdapter();
        for (int i = 0; i < mAdViewList.size(); i++) {
//            if (i % 2 == 0) {
            mAdViewPositionMap.put(mAdViewList.get(i), i); // 把每个广告在列表中位置记录下来
            mDataList.add(mAdViewList.get(i));
//            mAdapter.addADViewToPosition(i, mAdViewList.get(i));
//            }
        }
        mAdapter.notifyDataSetChanged();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 1000);
    }

    @Override
    public void onRenderFail(NativeExpressADView adView) {
        Log.i(TAG, "onRenderFail: " + adView.toString());
    }

    @Override
    public void onRenderSuccess(NativeExpressADView adView) {
        Log.i(TAG, "onRenderSuccess: " + adView.toString());
    }

    @Override
    public void onADExposure(NativeExpressADView adView) {
        Log.i(TAG, "onADExposure: " + adView.toString());
    }

    @Override
    public void onADClicked(NativeExpressADView adView) {
        Log.i(TAG, "onADClicked: " + adView.toString());
    }

    @Override
    public void onADClosed(NativeExpressADView adView) {
        Log.i(TAG, "onADClosed: " + adView.toString());
        if (mAdapter != null) {
            int removedPosition = mAdViewPositionMap.get(adView);
            mAdapter.removeADView(removedPosition, adView);
        }
    }

    @Override
    public void onADLeftApplication(NativeExpressADView adView) {
        Log.i(TAG, "onADLeftApplication: " + adView.toString());
    }

    @Override
    public void onADOpenOverlay(NativeExpressADView adView) {
        Log.i(TAG, "onADOpenOverlay: " + adView.toString());
    }

    @Override
    public void onADCloseOverlay(NativeExpressADView adView) {
        Log.i(TAG, "onADCloseOverlay");
    }

    /**
     * RecyclerView的Adapter
     */
    class CustomAdapter extends BaseRecyclerAdapter<Object> {

        static final int TYPE_DATA = 0;
        static final int TYPE_AD = 1;

        public CustomAdapter(Context context, List<Object> data) {
            super(context, data);
        }

        // 把返回的NativeExpressADView添加到数据集里面去
        public void addADViewToPosition(int position, NativeExpressADView adView) {
            if (position >= 0 && position < data.size() && adView != null) {
                data.add(position, adView);
            }
        }

        // 移除NativeExpressADView的时候是一条一条移除的
        public void removeADView(int position, NativeExpressADView adView) {
            data.remove(position);
            mAdapter.notifyItemRemoved(position); // position为adView在当前列表中的位置
            mAdapter.notifyItemRangeChanged(0, data.size());
        }

        @Override
        public int getItemViewType(int position) {
            return data.get(position) instanceof NativeExpressADView ? TYPE_AD : TYPE_DATA;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            CustomViewHolder customViewHolder = (CustomViewHolder) viewHolder;
            int type = getItemViewType(position);
            if (TYPE_AD == type) {
                final NativeExpressADView adView = (NativeExpressADView) data.get(position);
                mAdViewPositionMap.put(adView, position); // 广告在列表中的位置是可以被更新的
                if (customViewHolder.container.getChildCount() > 0
                        && customViewHolder.container.getChildAt(0) == adView) {
                    return;
                }

                if (customViewHolder.container.getChildCount() > 0) {
                    customViewHolder.container.removeAllViews();
                }

                if (adView.getParent() != null) {
                    ((ViewGroup) adView.getParent()).removeView(adView);
                }

                customViewHolder.container.addView(adView);
                adView.render(); // 调用render方法后sdk才会开始展示广告
            } else {
                customViewHolder.title.setText(String.valueOf(data.get(position)));
            }
        }

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            int layoutId = (viewType == TYPE_AD) ? R.layout.item_express_ad : R.layout.item_data;
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, null);
            return new CustomViewHolder(view);
        }

        class CustomViewHolder extends RecyclerView.ViewHolder {
            public TextView title;
            public ViewGroup container;

            public CustomViewHolder(View view) {
                super(view);
                title = (TextView) view.findViewById(R.id.title);
                container = (ViewGroup) view.findViewById(R.id.express_ad_container);
            }
        }

    }

}