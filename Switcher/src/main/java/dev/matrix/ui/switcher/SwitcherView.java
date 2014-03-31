package dev.matrix.ui.switcher;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Scroller;

public class SwitcherView extends ViewGroup implements ValueAnimator.AnimatorUpdateListener, ViewTreeObserver.OnPreDrawListener {

	public static final int SCROLL_DURATION = 300;
	public static final int RESIZE_DURATION = 1000;

	private int mCurrentIndex = -1;
	private boolean mViewsUpdated = false;
	private Scroller mScroll;

	public SwitcherView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mScroll = new Scroller(context);

		setFocusable(true);
		setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);

		getViewTreeObserver().addOnPreDrawListener(this);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int widthMeasureSpec = MeasureSpec.makeMeasureSpec(r - l, MeasureSpec.EXACTLY);
		int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		for (int index = getChildCount() - 1; index >= 0; --index) {
			View view = getChildAt(index);
			view.measure(widthMeasureSpec, heightMeasureSpec);
			view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		}
		mViewsUpdated = false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_UP:
				return selectPrevious();
			case KeyEvent.KEYCODE_DPAD_DOWN:
				return selectNext();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public View focusSearch(View focused, int direction) {
		View view = super.focusSearch(focused, direction);
		if (view != null) {
			return view;
		}
		switch (direction) {
			case FOCUS_UP:
				selectPrevious();
				break;
			case FOCUS_DOWN:
				selectNext();
				break;
		}
		return null;
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		View view = ((MyValueAnimator) animation).view;
		view.setBottom(view.getTop() + (Integer) animation.getAnimatedValue());
		mViewsUpdated = false;
	}

	@Override
	public boolean onPreDraw() {
		if (!mScroll.computeScrollOffset() && mViewsUpdated) {
			return true;
		}
		mViewsUpdated = true;

		int offset = -mScroll.getCurrY();
		for (int index = 0; index < getChildCount(); ++index) {
			View view = getChildAt(index);
			view.setTranslationY(offset);
			offset += view.getHeight();
		}

		ensureVisible(mCurrentIndex);
		return true;
	}

	private void ensureVisible(int index) {
		View view = getChildAt(index);
		if (view == null) {
			return;
		}

		int scrollY = mScroll.getCurrY();
		int translation = (int) view.getTranslationY();
		int top = view.getTop() + translation;
		int bottom = view.getBottom() + translation;
		int screenHeight = getHeight();

		int scrollTo = mScroll.getFinalY();
		if (top < 0) {
			scrollTo = mScroll.getCurrY() + top;
		} else if (bottom > screenHeight) {
			scrollTo = mScroll.getCurrY() + bottom - screenHeight;
		}

		if (scrollTo != mScroll.getFinalY()) {
			mScroll.startScroll(0, scrollY, 0, scrollTo - scrollY, SCROLL_DURATION);
			postInvalidate();
		}
	}

	public boolean selectNext() {
		return setSelection(mCurrentIndex + 1);
	}

	public boolean selectPrevious() {
		return setSelection(mCurrentIndex - 1);
	}

	public boolean setSelection(int index) {
		index = Math.max(Math.min(index, getChildCount() - 1), 0);
		if (index == mCurrentIndex || index < 0 || index >= getChildCount()) {
			return false;
		}
		selectView(mCurrentIndex, false);
		selectView(mCurrentIndex = index, true);
		return true;
	}

	private MyValueAnimator getAnimator(View view) {
		Object tag = view.getTag();
		if (tag instanceof MyValueAnimator) {
			return (MyValueAnimator) tag;
		}
		MyValueAnimator animator = new MyValueAnimator(view);
		animator.setDuration(RESIZE_DURATION);
		animator.addUpdateListener(this);
		view.setTag(animator);
		return animator;
	}

	private void selectView(int index, boolean is) {
		View view = getChildAt(index);
		if (view == null) {
			return;
		}

		ValueAnimator valueAnimator = getAnimator(view);
		valueAnimator.cancel();

		int heightSrc = view.getHeight();

		view.setSelected(is);
		if (is) {
			view.requestFocus();
		}

		int widthMeasureSpec = MeasureSpec.makeMeasureSpec(view.getWidth(), MeasureSpec.EXACTLY);
		int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		view.forceLayout();
		view.measure(widthMeasureSpec, heightMeasureSpec);

		int heightDst = view.getMeasuredHeight();
		if (heightSrc == heightDst) {
			return;
		}

		valueAnimator.setIntValues(heightSrc, heightDst);
		valueAnimator.start();
	}

	class MyValueAnimator extends ValueAnimator {
		public final View view;

		MyValueAnimator(View view) {
			this.view = view;
		}
	}
}
