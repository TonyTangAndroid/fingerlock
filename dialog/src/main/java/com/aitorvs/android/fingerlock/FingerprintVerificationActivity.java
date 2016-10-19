/*
 * Copyright (c) 2016 Aitor Viana Sanchez
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.aitorvs.android.fingerlock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.aitorvs.android.fingerlock.dialog.R;


/**
 * A dialog which uses fingerprint APIs to authenticate the user, and falls back to password
 * authentication if fingerprint is not available.
 */
@SuppressWarnings("ResourceType")
public class FingerprintVerificationActivity extends Activity implements FingerLockResultCallback {
    public static final String KEY_RESULT_CODE = "keyResultCode";
    public static final String ARG_KEY_NAME = "key_name";
    static final long ERROR_TIMEOUT_MILLIS = 1600;
    static final long SUCCESS_DELAY_MILLIS = 1300;
    static final String TAG = FingerprintVerificationActivity.class.getSimpleName();
    private FingerLockApi.FingerLockImpl mFingerLock;
    private ImageView mFingerprintIcon;
    private TextView mFingerprintStatus;
    Runnable mResetErrorTextRunnable = new Runnable() {
        @Override
        public void run() {
            mFingerprintStatus.setTextColor(ColorAttr.getColor(FingerprintVerificationActivity.this, android.R.attr.textColorSecondary));
            mFingerprintStatus.setText(getResources().getString(R.string.fingerprint_hint));
            mFingerprintIcon.setImageResource(R.drawable.ic_fp_40px);
        }
    };
    private String fingerKey;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvitity_fingerprint_verification);
        this.setFinishOnTouchOutside(false);
        fingerKey = getIntent().getStringExtra(ARG_KEY_NAME);
        if (TextUtils.isEmpty(fingerKey)) {
            throw new IllegalStateException("FingerprintDialog must be shown with show(Activity, String, int).");
        }
        // create the FingerLock library instance
        mFingerLock = FingerLockApi.create();
        mFingerprintIcon = (ImageView) findViewById(R.id.fingerprint_icon);
        mFingerprintStatus = (TextView) findViewById(R.id.fingerprint_status);
        mFingerprintStatus.setText(R.string.initializing);
    }


    @Override
    public void onResume() {
        super.onResume();
        mFingerLock.register(this, fingerKey, this);
        if (BuildConfig.DEBUG) Log.d(TAG, "onResume: called");
    }

    @Override
    public void onPause() {
        super.onPause();
        mFingerLock.unregister(this);
        if (BuildConfig.DEBUG) Log.d(TAG, "onPause: called");
    }


    private void showError(CharSequence error) {
        mFingerprintIcon.setImageResource(R.drawable.ic_fingerprint_error);
        mFingerprintStatus.setText(error);
        mFingerprintStatus.setTextColor(ContextCompat.getColor(FingerprintVerificationActivity.this, R.color.warning_color));
        mFingerprintStatus.removeCallbacks(mResetErrorTextRunnable);
        mFingerprintStatus.postDelayed(mResetErrorTextRunnable, ERROR_TIMEOUT_MILLIS);
    }

    @Override
    public void onFingerLockError(@FingerLock.FingerLockErrorState int errorType, Exception e) {

        switch (errorType) {

            case FingerLock.FINGERPRINT_NOT_RECOGNIZED:
                showError(getResources().getString(R.string.fingerprint_not_recognized));
                break;
            case FingerLock.FINGERPRINT_ERROR_HELP:
                showError(e.getMessage());
                break;
            case FingerLock.FINGERPRINT_REGISTRATION_NEEDED:
            case FingerLock.FINGERPRINT_NOT_SUPPORTED:
            case FingerLock.FINGERPRINT_PERMISSION_DENIED:
            case FingerLock.FINGERPRINT_UNRECOVERABLE_ERROR:
            default:
                onVerifiedError(errorType, e);
                break;
        }
    }

    private void onVerifiedError(int errorType, Exception e) {
        if (e != null) {
            e.printStackTrace();
        }
        verificationFailure(errorType);
    }

    private void verificationFailure(int errorType) {
        Intent data = new Intent();
        data.putExtra(KEY_RESULT_CODE, errorType);
        setResult(RESULT_CANCELED, data);
        finish();
    }

    private void verificationSuccess() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onFingerLockAuthenticationSucceeded() {
        mFingerprintStatus.removeCallbacks(mResetErrorTextRunnable);
        mFingerprintIcon.setImageResource(R.drawable.ic_fingerprint_success);
        mFingerprintStatus.setTextColor(ContextCompat.getColor(FingerprintVerificationActivity.this, R.color.success_color));
        mFingerprintStatus.setText(getResources().getString(R.string.fingerprint_success));
        mFingerprintIcon.postDelayed(new Runnable() {
            @Override
            public void run() {
                verificationSuccess();
            }
        }, SUCCESS_DELAY_MILLIS);
    }

    // FingerLock callbacks

    @Override
    public void onFingerLockReady() {
        mFingerLock.start();
    }

    @Override
    public void onFingerLockScanning(boolean invalidKey) {
        mFingerprintStatus.setText(R.string.fingerprint_hint);
        if (invalidKey) {
            verificationFailure(FingerLock.FINGERPRINT_UNRECOVERABLE_ERROR);
        }
    }


}