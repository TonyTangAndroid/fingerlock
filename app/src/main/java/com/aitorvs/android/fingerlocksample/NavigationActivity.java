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

package com.aitorvs.android.fingerlocksample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.aitorvs.android.fingerlock.FingerprintVerificationActivity;


public class NavigationActivity extends AppCompatActivity {

    private static final int REQUEST_VERIFY = 1002;
    private TextView tvStatus;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        findViewById(R.id.btn_verify_fingerprint);
        doVerification();
    }

    public void verify(View view) {
        doVerification();
    }

    private void doVerification() {
        Intent intent = new Intent(this, FingerprintVerificationActivity.class);
        intent.putExtra(FingerprintVerificationActivity.ARG_KEY_NAME, getString(R.string.app_name));
        startActivityForResult(intent, REQUEST_VERIFY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            tvStatus.setText(R.string.verified_successfully);
        } else {
            if (data != null && data.hasExtra(FingerprintVerificationActivity.KEY_RESULT_CODE)) {
                final int intExtra = data.getIntExtra(FingerprintVerificationActivity.KEY_RESULT_CODE, -1);
                Log.d("error", "error code: " + intExtra);
                tvStatus.setText(getString(R.string.verified_failed)
                        + " with error code : " + intExtra);
            } else {
                tvStatus.setText(R.string.verified_cancelled);
            }
        }
    }

    public void verifyOriginal(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
