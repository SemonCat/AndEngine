package org.andengine.ui.supportFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import org.andengine.util.ActivityUtils;
import org.andengine.util.DialogUtils;
import org.andengine.util.call.AsyncCallable;
import org.andengine.util.call.Callable;
import org.andengine.util.call.Callback;
import org.andengine.util.progress.ProgressCallable;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga Inc.
 *
 * @author Nicolas Gramlich
 * @since 18:35:28 - 29.08.2009
 */
public abstract class BaseSupportFragment extends Fragment {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================


    // ===========================================================
	// Methods
	// ===========================================================

	public void toastOnUiThread(final CharSequence pText) {
		this.toastOnUiThread(pText, Toast.LENGTH_SHORT);
	}

	public void toastOnUiThread(final CharSequence pText, final int pDuration) {
		if (ActivityUtils.isOnUiThread()) {
			Toast.makeText(getActivity(), pText, pDuration).show();
		} else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), pText, pDuration).show();
                }
            });
		}
	}

	@Deprecated
	public void showDialogOnUiThread(final int pDialogID) {
		DialogUtils.showDialogOnUiThread(getActivity(), pDialogID);
	}

	@Deprecated
	public void showDialogOnUiThread(final int pDialogID, final Bundle pBundle) {
		DialogUtils.showDialogOnUiThread(getActivity(), pDialogID, pBundle);
	}

	/**
	 * Performs a task in the background, showing a {@link android.app.ProgressDialog},
	 * while the {@link org.andengine.util.call.Callable} is being processed.
	 *
	 * @param <T>
	 * @param pTitleResourceID
	 * @param pMessageResourceID
	 * @param pErrorMessageResourceID
	 * @param pCallable
	 * @param pCallback
	 */
	protected <T> void doAsync(final int pTitleResourceID, final int pMessageResourceID, final Callable<T> pCallable, final Callback<T> pCallback) {
		this.doAsync(pTitleResourceID, pMessageResourceID, pCallable, pCallback, null);
	}

	/**
	 * Performs a task in the background, showing a indeterminate {@link android.app.ProgressDialog},
	 * while the {@link org.andengine.util.call.Callable} is being processed.
	 *
	 * @param <T>
	 * @param pTitleResourceID
	 * @param pMessageResourceID
	 * @param pErrorMessageResourceID
	 * @param pCallable
	 * @param pCallback
	 * @param pExceptionCallback
	 */
	protected <T> void doAsync(final int pTitleResourceID, final int pMessageResourceID, final Callable<T> pCallable, final Callback<T> pCallback, final Callback<Exception> pExceptionCallback) {
		ActivityUtils.doAsync(getActivity(), pTitleResourceID, pMessageResourceID, pCallable, pCallback, pExceptionCallback);
	}

	/**
	 * Performs a task in the background, showing a {@link android.app.ProgressDialog} with an ProgressBar,
	 * while the {@link org.andengine.util.call.AsyncCallable} is being processed.
	 *
	 * @param <T>
	 * @param pTitleResourceID
	 * @param pMessageResourceID
	 * @param pErrorMessageResourceID
	 * @param pAsyncCallable
	 * @param pCallback
	 */
	protected <T> void doProgressAsync(final int pTitleResourceID, final int pIconResourceID, final ProgressCallable<T> pCallable, final Callback<T> pCallback) {
		this.doProgressAsync(pTitleResourceID, pIconResourceID, pCallable, pCallback, null);
	}

	/**
	 * Performs a task in the background, showing a {@link android.app.ProgressDialog} with a ProgressBar,
	 * while the {@link org.andengine.util.call.AsyncCallable} is being processed.
	 *
	 * @param <T>
	 * @param pTitleResourceID
	 * @param pMessageResourceID
	 * @param pErrorMessageResourceID
	 * @param pAsyncCallable
	 * @param pCallback
	 * @param pExceptionCallback
	 */
	protected <T> void doProgressAsync(final int pTitleResourceID, final int pIconResourceID, final ProgressCallable<T> pCallable, final Callback<T> pCallback, final Callback<Exception> pExceptionCallback) {
		ActivityUtils.doProgressAsync(getActivity(), pTitleResourceID, pIconResourceID, pCallable, pCallback, pExceptionCallback);
	}

	/**
	 * Performs a task in the background, showing an indeterminate {@link android.app.ProgressDialog},
	 * while the {@link org.andengine.util.call.AsyncCallable} is being processed.
	 *
	 * @param <T>
	 * @param pTitleResourceID
	 * @param pMessageResourceID
	 * @param pErrorMessageResourceID
	 * @param pAsyncCallable
	 * @param pCallback
	 * @param pExceptionCallback
	 */
	protected <T> void doAsync(final int pTitleResourceID, final int pMessageResourceID, final AsyncCallable<T> pAsyncCallable, final Callback<T> pCallback, final Callback<Exception> pExceptionCallback) {
		ActivityUtils.doAsync(getActivity(), pTitleResourceID, pMessageResourceID, pAsyncCallable, pCallback, pExceptionCallback);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
