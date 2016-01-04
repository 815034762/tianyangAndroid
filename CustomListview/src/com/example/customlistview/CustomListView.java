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
	private int firstItem;// ��ǹ���������

	private boolean isMove;// ���ͷ���Ƿ�����ƶ�

	private int startY;// �տ�ʼ��Y����OnScrollListener

	private int scrollState;// ���listview�Ĺ����¼�

	private int headHeight;// ��¼�տ�ʼ��ʱ������ĸ߶ȣ���ΪmeasureHeight�ǲ��̶��ġ�����Ҫ��һ������������ԭ����ֵ

	private int status = 0;// ��ʾlistivew��ǰ��״̬

	private final int ORIGINAL = 0;// ԭ����״̬����Ҫ�����������ϻ�ʱ���µ�padding�仯

	private final int MOVE = 1;// ��ʾ���Ի��������ڻ���

	private final int FRESHING = 2;// ��ʾ����ˢ�µ�״̬

	private final int RELEASE = 3;// �ͷ�״̬

	private final int CANCEL = 4;// ȡ��ˢ�£������»�

	private onFreshListener onFreshListeners;

	private int totalResutl;// ����

	private int dy = -1;// ��ֱ��λ��

	private int MAX_EDGE = 50;// ���������������

	private boolean isLoading = false;// �ײ��Ƿ���ʾ����,��Ҫ��Ϊ�˷�ֹ�ظ�����

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

		setSelection(getLastVisiblePosition());//������׶�
		
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
	 * ���ݴ�ֱ�ľ�����ʾ ��ͬ��״̬
	 * 
	 * @param status
	 */
	private void changeStateByStatus(int status) {
		switch (status) {

		case MOVE:

			TextView tv = (TextView) headerView.findViewById(R.id.tv);
			tv.setText("������ˢ��");
			break;
		case RELEASE:

			tv = (TextView) headerView.findViewById(R.id.tv);
			tv.setText("�ͷ�����ˢ��");
			break;
		case FRESHING:

			tv = (TextView) headerView.findViewById(R.id.tv);
			tv.setText("����ˢ��");
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
	 * ��
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollStates) {

		this.scrollState = scrollStates;
		if (scrollStates == OnScrollListener.SCROLL_STATE_IDLE) {
			
			// getAdapter().getCount()�����ײ�footer��
			if (view.getLastVisiblePosition() == (view.getCount() - 1)
					&& (view.getAdapter().getCount()) < (getTotalResutl())) {

				if (status == ORIGINAL && !isLoading && dy == 0) {
					showFooter();
				}
			}
			
			if(view.getAdapter().getCount() >= getTotalResutl())
			{
					Toast.makeText(getContext(), "�Ѿ�û�и���������", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		int tempY;

		switch (ev.getAction()) {

		case MotionEvent.ACTION_DOWN:

			if (0 == firstItem) {

				// ��ԭ��ʼ״̬
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

					if (dy > 0)/** ��ֹ�����ϻ������ **/
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
				// headerView.getMeasuredHeight());//��������ʹ�ã������ζ�����ΪgetMeasuredHeight���ص�ֵ�ǲ�ͬ��
			}
			break;
		case MotionEvent.ACTION_UP:

			if (status == MOVE) {

				isMove = false;
				status = FRESHING;// ����״̬
			}
			if (status == FRESHING && dy >= headHeight + MAX_EDGE) {
				/** �����ľ������һ���ĳ̶Ƚ���ˢ�� **/
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
			 * �Ѿ���ˢ���ˣ��ٴ������ָ�ԭ״
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
	 * ˢ�����
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