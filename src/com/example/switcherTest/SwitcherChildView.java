package com.example.switcherTest;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class SwitcherChildView extends FrameLayout {

	private View mExpandedView;
	private View mCollapsedView;

	public SwitcherChildView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		mExpandedView = getChildAt(0);
		mCollapsedView = getChildAt(1);
		updateViews(false);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		measureChild(mExpandedView, widthMeasureSpec, heightMeasureSpec);
		measureChild(mCollapsedView, widthMeasureSpec, heightMeasureSpec);

		View view = getActiveView();
		setMeasuredDimension(view.getMeasuredWidth(), view.getMeasuredHeight());
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		drawChild(canvas, mExpandedView, getDrawingTime());
		drawChild(canvas, mCollapsedView, getDrawingTime());
	}

	@Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);
		updateViews(true);
	}

	private View getActiveView() {
		return isSelected() ? mExpandedView : mCollapsedView;
	}

	private void updateViews(boolean animated) {
		if (isSelected()) {
			showView(mExpandedView, animated);
			hideView(mCollapsedView, animated);
		} else {
			showView(mCollapsedView, animated);
			hideView(mExpandedView, animated);
		}
	}

	private void showView(View view, boolean animated) {
		view.animate().cancel();
		view.setVisibility(VISIBLE);
		onShowView(view, animated);
	}

	protected void onShowView(View view, boolean animated) {
		if (animated) {
			view.animate().setDuration(SwitcherView.RESIZE_DURATION).alpha(1).start();
		} else {
			view.setAlpha(1);
		}
	}

	private void hideView(View view, boolean animated) {
		view.animate().cancel();
		view.setVisibility(INVISIBLE);
		onHideView(view, animated);
	}

	protected void onHideView(View view, boolean animated) {
		if (animated) {
			view.animate().setDuration(SwitcherView.RESIZE_DURATION).alpha(0).start();
		} else {
			view.setAlpha(0);
		}
	}
}
