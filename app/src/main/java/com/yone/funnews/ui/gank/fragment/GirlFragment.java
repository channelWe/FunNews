package com.yone.funnews.ui.gank.fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.victor.loading.rotate.RotateLoading;
import com.yone.funnews.R;
import com.yone.funnews.base.BaseFragment;
import com.yone.funnews.model.been.GankItemBean;
import com.yone.funnews.presenter.GirlPresenter;
import com.yone.funnews.presenter.contract.GirlContract;
import com.yone.funnews.ui.gank.activity.GirlDetailActivity;
import com.yone.funnews.ui.gank.adapter.GirlAdapter;
import com.yone.funnews.util.SnackbarUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Yoe on 2016/10/21.
 */

public class GirlFragment extends BaseFragment<GirlPresenter> implements GirlContract.View {


    @BindView(R.id.rv_girl_content)
    RecyclerView mRvGirlContent;
    @BindView(R.id.view_loading)
    RotateLoading mViewLoading;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;

    private static final int SPAN_COUNT = 2;

    GirlAdapter mAdapter;
    List<GankItemBean> mList;
    StaggeredGridLayoutManager mStaggeredGridLayoutManager;
    private boolean isLoadingMore = false;

    @Override
    protected void initInject() {
       getFragmentComponent().inject(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_girl;
    }

    @Override
    protected void initEventAndData() {
        mList = new ArrayList<>();
        mAdapter = new GirlAdapter(mContext,mList);
        mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(SPAN_COUNT,StaggeredGridLayoutManager.VERTICAL);
        mStaggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        mRvGirlContent.setLayoutManager(mStaggeredGridLayoutManager);
        mRvGirlContent.setAdapter(mAdapter);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.getGirlData();
            }
        });
        mRvGirlContent.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int[] visibleItems = mStaggeredGridLayoutManager.findFirstVisibleItemPositions(null);
                int lastItem = Math.max(visibleItems[0],visibleItems[1]);
                if (lastItem > mAdapter.getItemCount() - 5 && !isLoadingMore && dy > 0){
                    isLoadingMore = true;
                    mPresenter.getMoreGirlData();
                }
            }
        });
        mAdapter.setOnItemClickListener(new GirlAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(int position, View shareView) {
                Intent intent = new Intent();
                intent.setClass(mContext, GirlDetailActivity.class);
                intent.putExtra("url",mList.get(position).getUrl());
                intent.putExtra("id",mList.get(position).get_id());
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(mActivity,shareView,"shareView");
                mContext.startActivity(intent,options.toBundle());
            }
        });
        mViewLoading.start();
        mPresenter.getGirlData();
    }

    @Override
    public void showContent(List<GankItemBean> list) {
        if (mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        } else {
            mViewLoading.stop();
        }
        mList.clear();
        mList.addAll(list);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void showMoreContent(List<GankItemBean> list) {
        isLoadingMore = false;
        mViewLoading.stop();
        mList.addAll(list);
        for (int i = mList.size() - GirlPresenter.NUM_OF_PAGE; i < mList.size(); i++) {  //使用notifyDataSetChanged已加载的图片会有闪烁，遂使用inserted逐个插入
            mAdapter.notifyItemChanged(i);
        }
    }

    @Override
    public void showError(String msg) {
        if (mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        } else {
            mViewLoading.stop();
        }
        SnackbarUtil.showShort(mRvGirlContent,msg);
    }

}
