package com.example.customlistview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public class CustomListView extends ListView implements OnScrollListener {

	private View headerView, footerView;
	private int firstItem;// 标记滚动到顶部

	private boolean isMove;// 标记头部是否可以移动

	private int startY;// 刚开始的Y坐标OnScrollListener

	private int scrollState;// 标记listview的滚动事件

	private int headHeight;// 记录刚开始的时候测量的高度，因为measureHeight是不固定的。所以要用一个变量来保存原来的值

	private int status = 0;// 表示listivew当前的状态

	private final int ORIGINAL = 0;// 原来的状态，主要是用来区分上滑时导致的padding变化

	private final int MOVE = 1;// 表示可以滑动和正在滑动

	private final int FRESHING = 2;// 表示正在刷新的状态

	private final int RELEASE = 3;// 释放状态

	private final int CANCEL = 4;// 取消刷新，两次下滑

	private onFreshListener onFreshListeners;

	private int totalResutl;// 总数

	private int dy = -1;// 垂直的位移

	private int MAX_EDGE = 50;// 允许拉伸的最大距离

	private boolean isLoading = false;// 底部是否显示加载,主要是为了防止重复加载

	public CustomListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	private void initView(Context mContext) {

		LayoutInflater flater = LayoutInflater.from(mContext);
		headerView = flater.inflate(R.layout.head, null);

		footerView = flater.inflate(R.layout.footer, null);

		measureHead();

		headHeight = headerView.getMeasuredHeight();
		showOrhideHeader(-headHeight);
		addHeaderView(headerView);

		addFooterView(footerView);

		loadMoreComplete();

		setOnScrollListener(this);
	}

	public void showFooter() {

		isLoading = true;
		footerView.findViewById(R.id.load_layout).setVisibility(View.VISIBLE);

		setSelection(getLastVisiblePosition());//跳到最底端
		
		if (null != onFreshListeners) {
			onFreshListeners.onloadMore();
		}
	}

	public void loadMoreComplete() {

		isLoading = false;
		footerView.findViewById(R.id.load_layout).setVisibility(View.GONE);
		// removeFooterView(footerView);
	}

	private void showOrhideHeader(int topping) {

		headerView.setPadding(headerView.getPaddingLeft(), topping,
				headerView.getPaddingRight(), headerView.getPaddingBottom());
		headerView.invalidate();
	}

	/**
	 * 根据垂直的距离显示 不同的状态
	 * 
	 * @param status
	 */
	private void changeStateByStatus(int status) {
		switch (status) {

		case MOVE:

			TextView tv = (TextView) headerView.findViewById(R.id.tv);
			tv.setText("下拉以刷新");
			break;
		case RELEASE:

			tv = (TextView) headerView.findViewById(R.id.tv);
			tv.setText("释放立即刷新");
			break;
		case FRESHING:

			tv = (TextView) headerView.findViewById(R.id.tv);
			tv.setText("正在刷新");
			break;
		default:
			break;
		}

	}

	private void measureHead() {

		int w = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		headerView.measure(w, h);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		firstItem = firstVisibleItem;
	}

	/**
	 * 在
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollStates) {

		this.scrollState = scrollStates;
		if (scrollStates == OnScrollListener.SCROLL_STATE_IDLE) {
			
			// getAdapter().getCount()包括底部footer的
			if (view.getLastVisiblePosition() == (view.getCount() - 1)
					&& (view.getAdapter().getCount()) < (getTotalResutl())) {

				if (status == ORIGINAL && !isLoading && dy == 0) {
					showFooter();
				}
			}
			
			if(view.getAdapter().getCount() >= getTotalResutl())
			{
					Toast.makeText(getContext(), "已经没有更多数据了", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		int tempY;

		switch (ev.getAction()) {

		case MotionEvent.ACTION_DOWN:

			if (0 == firstItem) {

				// 还原初始状态
				status = ORIGINAL;

				startY = (int) ev.getY();
				isMove = true;
			}
			break;
		case MotionEvent.ACTION_MOVE:

			if (isMove && SCROLL_STATE_TOUCH_SCROLL == scrollState) {
				tempY = (int) ev.getY();
				dy = tempY - startY;

				switch (status) {

				case ORIGINAL:// 0

					if (dy > 0) {
						status = MOVE;
					}
					break;
				case CANCEL:// 3

					if (dy > 0)/** 防止出现上滑的情况 **/
					{
						showOrhideHeader(dy);
					}

					break;
				case MOVE:// 1

					if (dy >= headHeight + MAX_EDGE) {
						changeStateByStatus(RELEASE);
					} else {
						changeStateByStatus(MOVE);
					}
					showOrhideHeader(dy - headHeight);

					break;
				case FRESHING:// 2

					status = CANCEL;
					break;

				default:
					break;
				}
				// showOrhideHeader(dy -
				// headerView.getMeasuredHeight());//不能这样使用，否则会晃动，因为getMeasuredHeight返回的值是不同的
			}
			break;
		case MotionEvent.ACTION_UP:

			if (status == MOVE) {

				isMove = false;
				status = FRESHING;// 更改状态
			}
			if (status == FRESHING && dy >= headHeight + MAX_EDGE) {
				/** 拉开的距离大于一定的程度进行刷新 **/
				if (null != onFreshListeners) {
					showOrhideHeader(0);
					changeStateByStatus(FRESHING);
					onFreshListeners.onfresh();
				} else {
					freshOnComplete();
				}
			} else {
				freshOnComplete();
			}
			/**
			 * 已经在刷新了，再次下拉恢复原状
			 */
			if (status == CANCEL && dy >= 0) {
				freshOnComplete();
			}

			break;
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * 刷新完成
	 */
	public void freshOnComplete() {

		dy = 0;
		status = ORIGINAL;
		showOrhideHeader(-headHeight);
	}

	public interface onFreshListener {
		public void onfresh();

		public void onloadMore();
	}

	public onFreshListener getOnFreshListeners() {
		return onFreshListeners;
	}

	public void setOnFreshListeners(onFreshListener onFreshListeners) {
		this.onFreshListeners = onFreshListeners;
	}

	public int getTotalResutl() {
		return totalResutl;
	}

	public void setTotalResutl(int totalResutl) {
		this.totalResutl = totalResutl;
	}

}