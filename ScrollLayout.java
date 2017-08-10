package com.rytong.bankqd.view.dragviewhorizontal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.rytong.bankqd.MainActivity;
import com.rytong.bankqd.R;
import com.rytong.bankqd.view.dragviewhorizontal.State.StateMode;

/**
 * 拖拽控件采用横向滚动的形式，现定为每页8个元素，两行展示，由gridview展示。父布局为scrolllayout，用于控制滚动。
 * 
 * @author mu
 *
 */

public class ScrollLayout extends ViewGroup {
	/**
	 * 用于储存每页的gridview
	 */
	private List<MyGridView> mViewList;
	/**
	 * 用于保存每页gridview的adapter
	 */
	private List<MyGridViewAdapter> mViewAdapterList;
	/**
	 * 用于观测滑动速率
	 */
	private VelocityTracker mVelocityTracker;
	/**
	 * 判断触摸滑动的阀门
	 */
	private int mTouchSlop = 0;
	/**
	 * 拉断阀门
	 */
	private static final int SNAP_VELOCITY = 600;
	/**
	 * 用于控制滚动
	 */
	private Scroller mScroller;
	/**
	 * scrollayout中页面数，从1计数
	 */
	private int mPageNum = 0;

	/**
	 * 每个gridview的列数
	 */
	private int mColNum = Config.DEFAULT_HOME_COL_NUM;

	/**
	 * 每个gridview的行数
	 */
	private int mRow = Config.DEFAULT_HOME_ROW_NUM;
	/**
	 * 每个gridview的元素数
	 */
	private int mNumPerPage;
	/**
	 * 从报文解析出的总的数据存放处,文件夹的总数据由
	 */
	private DataBean mSCDataBean;
	/**
	 * 滚动时底部的动态更新点
	 */
	private LinearLayout mPointsLayout;

	private Context mContext;
	/**
	 * 本类子view的宽度，即gridview的宽度，用于滚动时计算距离
	 */
	private int mChildWidth = 0;
	/**
	 * 用于处理拖动的跨页滚动
	 */
	private Handler myHandler = new Handler();
	/**
	 * 记录按下时触摸的位置x
	 */
	private int mDownX = 0;
	/**
	 * 记录按下时触摸的位置y
	 */
	private int mDownY = 0;
	/**
	 * 记录上一次按下的位置x
	 */
	private int mLastX = 0;
	/**
	 * 记录上一次按下的位置y
	 */
	private int mLastY = 0;
	/**
	 * 记录当前触摸事件的位置x
	 */
	private int mCurrentX;
	/**
	 * 记录当前触摸事件的位置y
	 */
	private int mCurrentY;
	/**
	 * 控件当前状态
	 */
	private StateMode mMode = StateMode.REST;
	/**
	 * 需要隐藏的元素序号，从0开始
	 */
	private int mHideItem = -1;
	/**
	 * 当前scrolllayout是第几个gridview
	 */
	public int mCurrentIndex = 0;
	/**
	 * 储存底部更新点
	 */
	private List<ImageView> points;
	/**
	 * 拖拽后其他方格动画是否结束
	 */
	public boolean mAnimationEnd = false;
	/**
	 * 如果是文件夹可从从这个变量得到是点击第几项展开的文件夹
	 */
	private int mParentIndex = -1;
	/**
	 * 如果为文件夹会有父级滚动布局，即首页的滚动布局
	 */
	private ScrollLayout mParentScrollLayout;
	/**
	 * 文件夹滚动布局
	 */
	private ScrollLayout mFolderScrollLayout;
	/**
	 * 是否为首页拖拽控件
	 */
	private boolean mIsHomeDragList = true;

	public ScrollLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	/**
	 * 初始化
	 */
	public void init() {
		mNumPerPage = Config.DEFAULT_HOME_ROW_NUM * Config.DEFAULT_HOME_COL_NUM;
		mScroller = new Scroller(getContext());
		mVelocityTracker = VelocityTracker.obtain();
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();// 24
		mViewList = new ArrayList<MyGridView>();
		mViewAdapterList = new ArrayList<MyGridViewAdapter>();

	}

	/**
	 * 初始化数据
	 * 
	 * @param dataBean
	 *            展示在scrollayout中的数据
	 * @param pointsLayout
	 *            scrollayout中的更新点布局
	 * @param isHomeDragList
	 *            是否为首页展示，还是为文件夹展示
	 */
	public void initialDatas(DataBean dataBean, LinearLayout pointsLayout,
			boolean isHomeDragList) {
		mIsHomeDragList=isHomeDragList;
		if (isHomeDragList) {
			mColNum = QDDragGridView.mColumn;
			mRow = QDDragGridView.mRow;
			mNumPerPage = mColNum * mRow;
		} else {
			mColNum = Config.FOLDER_COL_NUM;
			mRow = Config.FOLDER_ROW_NUM;
			mNumPerPage = mColNum * mRow;
		}

		mSCDataBean = dataBean;
		mPointsLayout = pointsLayout;
		int size = mSCDataBean.getItemBeans().size();
		mPageNum = size / mNumPerPage + ((size % mNumPerPage) > 0 ? 1 : 0);
		addPageViews(isHomeDragList);
		initPoints();

	}

	/**
	 * 加载gridview页面到scrolllayout
	 * 
	 * @param isHomeDragList
	 *            是否为首页展示，还是为文件夹展示
	 */
	public void addPageViews(boolean isHomeDragList) {
		for (int i = 0; i < mPageNum; i++) {
			if(mViewList==null){
				mViewList = new ArrayList<MyGridView>();
			}
			if(mViewAdapterList==null){
				mViewAdapterList = new ArrayList<MyGridViewAdapter>();
			}
			MyGridViewAdapter myGridViewAdapter = new MyGridViewAdapter(
					mContext, mSCDataBean, mColNum, mRow, isHomeDragList, this,
					i);
			MyGridView myGridView = new MyGridView(mContext, mSCDataBean,
					mColNum,mRow, isHomeDragList, this, myHandler, myGridViewAdapter,i);
			myGridView.setAdapter(myGridViewAdapter);
			addView(myGridView, new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));

			mViewList.add(myGridView);
			mViewAdapterList.add(myGridViewAdapter);
		}

	}

	/**
	 * 初始化底部更新点，默认选中第一页
	 */
	private void initPoints() {
		points = new ArrayList<ImageView>();
		for (int i = 0; i < mPageNum; i++) {
			ImageView point = new ImageView(getContext());
			/**
			 * 更新点之间间隙
			 */
			ImageView pointSpace = new ImageView(getContext());
			if (i == 0) {
				/**
				 * 默认选中第一页
				 */
				point.setImageResource(R.drawable.viewpager_point_red);
			} else {
				point.setImageResource(R.drawable.viewpager_point_white);
			}
			points.add(point);
			LayoutParams para = new LayoutParams(
					MainActivity.mSizeUtil.getScaledValue(10),
					MainActivity.mSizeUtil.getScaledValueH(10));
			mPointsLayout.addView(point, para);
			if (i < mPageNum - 1) {
				
				mPointsLayout.addView(pointSpace, para);
			}
		}
	}
	boolean allowScroll=false;
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// 处理滑动冲突,也就是什么时候返回true的问题
		// 规则:开始滑动时水平距离超过TouchSlop的时候
		allowScroll=false;
		mCurrentX = (int) ev.getX();
		mCurrentY = (int) ev.getY();
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownX = mCurrentX;
			mDownY = mCurrentY;
			mLastX = mDownX;
			mLastY = mDownY;
			if (!mScroller.isFinished()) { // 如果动画还没有执行完成,则打断
				mScroller.abortAnimation();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			int deltaX = mCurrentX - mDownX;
			if (Math.abs(deltaX) - mTouchSlop > 0
					&& (this.mMode == StateMode.REST||mMode ==StateMode.DELETE)) { // 水平方向距离长
				// MOVE中返回true一次,后续的MOVE和UP都不会收到此请求
				// mMode = StateMode.SROLLING;
				//setMode(StateMode.SROLLING);
				allowScroll=true;
				
			}

			break;

		case MotionEvent.ACTION_UP:
			break;
		}

		return allowScroll;

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 得到本次触摸的位置
		mCurrentX = (int) event.getX();
		mCurrentY = (int) event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:

			break;
		case MotionEvent.ACTION_MOVE:

			mVelocityTracker.addMovement(event);
			int deltaX = mCurrentX - mLastX; // 手指滑动的距离
			// 调用该方法让View也对应的移动指定的距离，这样就实现了跟随手指滑动的效果，垂直方向不移动

			scrollBy(-deltaX, 0);

			break;
		case MotionEvent.ACTION_UP:
			// 释放手指以后开始自动滑动到目标位置
			int distance = getScrollX() - mCurrentIndex * mChildWidth; // 相对于当前View滑动的距离,正为向左,负为向右
			if (Math.abs(distance) > mChildWidth / 2) {// 必须滑动的距离要大于1/2个宽度,否则不会切换到其他页面
				if (distance > 0) { // 切换到下一个页面
					mCurrentIndex++;
				} else { // 切换到上一个页面
					mCurrentIndex--;
				}
			} else {// 滑动速率是否超过SNAP_VELOCITY

				mVelocityTracker.computeCurrentVelocity(1000);
				float xV = mVelocityTracker.getXVelocity();

				if (Math.abs(xV) > SNAP_VELOCITY) {
					if (xV > 0) {
						mCurrentIndex--;
					} else {
						mCurrentIndex++;
					}
				}
			}
			mVelocityTracker.clear();
			mCurrentIndex = mCurrentIndex < 0 ? 0
					: mCurrentIndex > getChildCount() - 1 ? getChildCount() - 1
							: mCurrentIndex; // 这里保证边界值
			smoothScrollTo(mCurrentIndex * mChildWidth, 0); // 滑动到指定位置
			updatePoints(mCurrentIndex);
			// mMode = StateMode.REST;
//			if(mMode==StateMode.DELETE){
//				
//			}else{
//				setMode(StateMode.REST);
//			}
			
			break;
		}

		mLastX = mCurrentX; // 存储当前位置为上一次位置
		mLastY = mCurrentY;
		return super.onTouchEvent(event);

	}
	/**
	 * 页面滚动时更新滚动更新点
	 * @param currentPosition
	 */
	public void updatePoints(int currentPosition) {
		for (int i = 0; i < mPageNum; i++) {

			if (i == currentPosition) {
				points.get(i).setImageResource(R.drawable.viewpager_point_red);
			} else {
				points.get(i)
						.setImageResource(R.drawable.viewpager_point_white);
			}

		}
	}

	// 这个是工具方法，弹性滑动到指定位置
	public void smoothScrollTo(int destX, int destY) {

		mScroller.startScroll(getScrollX(), getScrollY(), destX - getScrollX(),
				destY - getScrollY(), 1000);
		// 刷新
		invalidate();
	}

	@Override
	public void computeScroll() {

		super.computeScroll();
		// 先计算当前Scroller的偏移
		if (mScroller.computeScrollOffset()) {
			// 然后调用我们熟悉的scrollTo将View移动到getCurrX,getCurrY的位置

			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			// 通知刷新界面
			postInvalidate();
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childCount = getChildCount(); // 子元素的个数
		int left = 0; // 左边的距离
		View child;
		// 遍历布局子元素
		for (int i = 0; i < childCount; i++) {
			child = getChildAt(i);
			int width = child.getMeasuredWidth();
			mChildWidth = width;
			// 调用每个子元素的layout方法去布局这个子元素，这里相当于默认第一个子元素占满了屏幕，后面的子元素就是在第一个屏幕后面紧挨着和屏幕一样大小的后续元素，所以left是一直累加的，top保持0，bottom保持第一个元素的高度，right就是left+元素的宽度
			child.layout(left, 0, left + width, child.getMeasuredHeight());
			left += width;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (getChildCount() != 0) {
			measureChildren(widthMeasureSpec, heightMeasureSpec);
		}

	}

	/**
	 * 更新srcollayout内所有的gridview的数据
	 */
	public void notifyDataSetChangedForAll() {
		for (int i = 0; i < mViewAdapterList.size(); i++) {
			if(mViewAdapterList.get(i).getCount()!=0){

				mViewAdapterList.get(i).notifyDataSetChanged();
			}else{

				updatePageNum();
			}
		}

	}

	public StateMode getMode() {
		return mMode;
	}

	public void setHideItem(int hideItem) {
		mHideItem = hideItem;

		notifyDataSetChangedForAll();
	}

	public int getHideItem() {
		return mHideItem;
	}

	/**
	 * 排序scrolllayout的数据
	 * 
	 * @param oldPosition
	 *            旧位置
	 * @param newPosition
	 *            新位置
	 */
	public void reorderItems(int oldPosition, int newPosition) {

		int index = Math.max(oldPosition, newPosition);
		if (index < mSCDataBean.getItemBeans().size()) {
			ItemBean temp = mSCDataBean.getItemBeans().get(oldPosition);
			if (oldPosition < newPosition) {
				for (int i = oldPosition; i < newPosition; i++) {
					Collections.swap(mSCDataBean.getItemBeans(), i, i + 1);
				}

			} else if (oldPosition > newPosition) {
				for (int i = oldPosition; i > newPosition; i--) {

					Collections.swap(mSCDataBean.getItemBeans(), i, i - 1);
				}

			}
			mSCDataBean.getItemBeans().set(newPosition, temp);
		}

		notifyDataSetChangedForAll();

	}

	/**
	 * 获取每页的宽度
	 * 
	 * @return
	 */
	public int getChildWidth() {
		return mChildWidth;
	}

	/**
	 * 获取页面数
	 * 
	 * @return
	 */
	public int getPageNum() {
		return mPageNum;
	}

	/**
	 * 宫格交换动画
	 * 
	 * @param oldPosition
	 * @param newPosition
	 */
	@SuppressLint("NewApi")
	public void animateReorder(final int oldPosition, final int newPosition) {

		try {
			/**
			 * 是否向前移动
			 */
			boolean isForward = newPosition > oldPosition;

			List<Animator> resultList = new LinkedList<Animator>();
			int column = mViewList.get(mCurrentIndex).getColumn();
			int firstVisiblePosition = mViewList.get(mCurrentIndex)
					.getFirstVisiblePosition();
			if (isForward) {

				for (int pos = oldPosition; pos < newPosition; pos++) {
					/**
					 * 得到操作对象
					 */
					View view = mViewList.get(mCurrentIndex).getChildAt(
							pos - firstVisiblePosition);
					/**
					 * 是否换行
					 */
					if ((pos + 1) % column == 0) {
						resultList.add(createTranslationAnimations(view,
								-view.getWidth() * (column - 1), 0,
								view.getHeight(), 0));
					} else {
						resultList.add(createTranslationAnimations(view,
								view.getWidth(), 0, 0, 0));
					}
				}

			} else {

				for (int pos = oldPosition; pos > newPosition; pos--) {
					View view = mViewList.get(mCurrentIndex).getChildAt(
							pos - firstVisiblePosition);
					if ((pos + column) % column == 0) {
						resultList.add(createTranslationAnimations(view,
								view.getWidth() * (column - 1), 0,
								-view.getHeight(), 0));
					} else {
						resultList.add(createTranslationAnimations(view,
								-view.getWidth(), 0, 0, 0));
					}
				}
			}

			AnimatorSet resultSet = new AnimatorSet();
			resultSet.playTogether(resultList);
			resultSet.setDuration(300);
			resultSet.setInterpolator(new AccelerateDecelerateInterpolator());
			resultSet.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationStart(Animator animation) {

					mAnimationEnd = false;
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					mAnimationEnd = true;

				}
			});

			resultSet.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("NewApi")
	private AnimatorSet createTranslationAnimations(View view, float startX,
			float endX, float startY, float endY) {
		ObjectAnimator animX = ObjectAnimator.ofFloat(view, "translationX",
				startX, endX);

		ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY",
				startY, endY);
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(animX, animY);
		return animSetXY;
	}

	public int pointToPosition(int x, int y) {
		return mViewList.get(mCurrentIndex).pointToPosition(x, y);
	}

	/**
	 * 设置父级点击序号
	 * 
	 * @param parentScrollLayout
	 */
	public void setParentIndex(int parentIndex) {
		mParentIndex = parentIndex;
	}

	/**
	 * 获取父级点击序号
	 * 
	 * @return
	 */
	public int getParentIndex() {
		return mParentIndex;
	}

	/**
	 * 设置父级ScrollLayout
	 * 
	 * @param parentScrollLayout
	 */
	public void setParentScrollLayout(ScrollLayout parentScrollLayout) {
		mParentScrollLayout = parentScrollLayout;
	}

	/**
	 * 获取父级ScrollLayout
	 * 
	 * @return
	 */
	public ScrollLayout getParentScrollLayout() {
		return mParentScrollLayout;
	}

	/**
	 * 设置文件夹ScrollLayout
	 * 
	 * @param
	 */
	public void setFolderScrollLayout(ScrollLayout folderScrollLayout) {
		mFolderScrollLayout = folderScrollLayout;
	}

	/**
	 * 获取文件夹ScrollLayout
	 * 
	 * @return
	 */
	public ScrollLayout getFolderScrollLayout() {
		return mFolderScrollLayout;
	}

	public void setMode(StateMode mode) {
		switch (mode) {
		case REST:
			StateMode modeFolder=null;
			StateMode modeParent=null;
			if(mFolderScrollLayout!=null){
				modeFolder=mFolderScrollLayout.getMode();
			}
			if(mParentScrollLayout!=null){
				modeParent=mParentScrollLayout.getMode();
			}
			if (mMode == StateMode.DELETE&&
					(modeFolder== null||(modeFolder!=null&&modeFolder != StateMode.DELETE))&&
					(modeParent== null||(modeParent!=null&&modeParent != StateMode.DELETE))) {
				StringBuilder folder1 = new StringBuilder();
				StringBuilder folder2 = new StringBuilder();
				StringBuilder other = new StringBuilder();
				DataBean tempDataBean = null;
				if (mIsHomeDragList) {
					tempDataBean = mSCDataBean;
				} else {
					tempDataBean = mParentScrollLayout.mSCDataBean;
				}
				for (int i = 0; i < tempDataBean.getItemBeans().size(); i++) {
					ItemBean curItemBean = (ItemBean) tempDataBean
							.getItemBeans().get(i);
					if (i == 0) {
						DataBean childDataBean = (DataBean) curItemBean
								.getAttributes().get("list");
						for (int j = 0; j < childDataBean.getItemBeans().size(); j++) {
							String channelId = (String) childDataBean
									.getItemBeans().get(j).getAttributes()
									.get("ID");
							folder1.append(channelId).append(",");

						}

					} else if (i == 1) {
						DataBean childDataBean = (DataBean) curItemBean
								.getAttributes().get("list");
						for (int j = 0; j < childDataBean.getItemBeans().size(); j++) {
							String channelId = (String) childDataBean
									.getItemBeans().get(j).getAttributes()
									.get("ID");
							folder2.append(channelId).append(",");

						}

					} else {
						String channelId = (String) curItemBean.getAttributes()
								.get("ID");
						other.append(channelId).append(",");

					}

				}
				if (folder1.length() > 0) {
					folder1.deleteCharAt(folder1.length() - 1);

				}
				if (folder2.length() > 0) {
					folder2.deleteCharAt(folder2.length() - 1);

				}
				if (other.length() > 0) {
					other.deleteCharAt(other.length() - 1);
				}

				MainActivity.getEmpRender().doLua(
						QDDragGridView.mLuaonReOrderSaveMethod.concat("("
								+ "\"" + folder1 + "\"" + "," + "\"" + folder2
								+ "\"" + "," + "\"" + other + "\"" + ")"));
			}
			mMode = mode;
			if(mIsHomeDragList){
				if (mFolderScrollLayout != null&&mFolderScrollLayout.getMode()!=StateMode.REST) {
					mFolderScrollLayout.setMode(StateMode.REST);
					mFolderScrollLayout.notifyDataSetChangedForAll();
				}
			}else{
				if (mParentScrollLayout != null&&mParentScrollLayout.getMode()!=StateMode.REST) {
					mParentScrollLayout.setMode(StateMode.REST);
					mParentScrollLayout.notifyDataSetChangedForAll();
				}
			}
			notifyDataSetChangedForAll();
			
			break;
		case DELETE:
			if (QDDragGridView.mAllowEidt) {
				mMode = mode;
				if(mIsHomeDragList){
					if (mFolderScrollLayout != null&&mFolderScrollLayout.getMode()!=StateMode.DELETE) {
						mFolderScrollLayout.setMode(StateMode.DELETE);
						mFolderScrollLayout.notifyDataSetChangedForAll();
					}
				}else{
					if (mParentScrollLayout != null&&mParentScrollLayout.getMode()!=StateMode.DELETE) {
						mParentScrollLayout.setMode(StateMode.DELETE);
						mParentScrollLayout.notifyDataSetChangedForAll();

					}
				}
				notifyDataSetChangedForAll();
			}
			break;
		case DRAG:

			if (QDDragGridView.mAllowEidt&&mMode!=StateMode.DRAG) {

				mMode = mode;
				notifyDataSetChangedForAll();
			}
			break;
		
		default:
			break;
		}

	}
	/**
	 * 用于删除宫格到需要减少页面时调用
	 */
	public void updatePageNum(){
		int size = mSCDataBean.getItemBeans().size();
		mPageNum = size / mNumPerPage + ((size % mNumPerPage) > 0 ? 1 : 0);
		removeAllViews();
		mPointsLayout.removeAllViews();
		int listSize=mViewAdapterList.size();

		mViewAdapterList.clear();
		mViewList.clear();
		addPageViews(mIsHomeDragList);
		initPoints();
		updatePoints(mCurrentIndex);
	}

}
