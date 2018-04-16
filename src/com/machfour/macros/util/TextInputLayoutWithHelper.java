package com.machfour.macros.util;

/**
 * Copyright 2015 Roman Donchenko
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// extends TextInputLayout with helper text functionality

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.TextViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.TextView;
import com.machfour.macros.R;


/**
 * TextInputLayout temporary workaround for helper text showing
 */
public class TextInputLayoutWithHelper extends TextInputLayout {

    static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();

    private CharSequence mHelperText;
    private ColorStateList mHelperTextColor;
    private boolean mHelperTextEnabled = false;
    private boolean mErrorEnabled = false;
    private TextView mHelperView;
    private int mHelperTextAppearance = R.style.HelperTextAppearance;

    public TextInputLayoutWithHelper(Context _context) {
        super(_context);
    }

    public TextInputLayoutWithHelper(Context _context, AttributeSet _attrs) {
        super(_context, _attrs);

        final TypedArray a = getContext().obtainStyledAttributes(
            _attrs,
            R.styleable.TextInputLayoutWithHelper, 0, 0);
        try {
            mHelperTextColor = a.getColorStateList(R.styleable.TextInputLayoutWithHelper_helperTextColor);
            mHelperText = a.getText(R.styleable.TextInputLayoutWithHelper_helperText);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (child instanceof EditText) {
            if (!TextUtils.isEmpty(mHelperText)) {
                setHelperText(mHelperText);
            }
        }
    }

    @Override
    public void setErrorEnabled(boolean _enabled) {
        if (mErrorEnabled == _enabled) return;
        mErrorEnabled = _enabled;
        if (_enabled && mHelperTextEnabled) {
            setHelperTextEnabled(false);
        }

        super.setErrorEnabled(_enabled);

        if (!(_enabled || TextUtils.isEmpty(mHelperText))) {
            setHelperText(mHelperText);
        }
    }

    public int getHelperTextAppearance() {
        return mHelperTextAppearance;
    }

    public void setHelperTextAppearance(int _helperTextAppearanceResId) {
        mHelperTextAppearance = _helperTextAppearanceResId;
    }

    public void setHelperTextColor(ColorStateList _helperTextColor) {
        mHelperTextColor = _helperTextColor;
    }

    public void setHelperTextEnabled(boolean _enabled) {
        if (mHelperTextEnabled == _enabled) return;
        if (_enabled && mErrorEnabled) {
            setErrorEnabled(false);
        }
        if (this.mHelperTextEnabled != _enabled) {
            if (_enabled) {
                this.mHelperView = new TextView(this.getContext());
                //this.mHelperView.setTextAppearance(this.getContext(), this.mHelperTextAppearance);
                TextViewCompat.setTextAppearance(this.mHelperView, this.getHelperTextAppearance());
                if (mHelperTextColor != null) {
                    this.mHelperView.setTextColor(mHelperTextColor);
                }

                /* Correction by 'Eajy' - article at
                 * https://medium.com/@elye.project/material-login-with-helper-text-232472400c15
                 * Source code:
                 * https://github.com/elye/demo_materiallogin/blob/master/app/src/main/java
                 *     /com/elyeproj/materiallogin/CustomTextInputLayout.java
                 *
                 */

                this.mHelperView.setText(mHelperText);
                this.mHelperView.setVisibility(VISIBLE);
                /*
                 * This was in the original
                this.mHelperView.setVisibility(INVISIBLE);
                */
                this.addView(this.mHelperView);
                if (this.mHelperView != null) {
                    ViewCompat.setPaddingRelative(
                        this.mHelperView,
                        ViewCompat.getPaddingStart(getEditText()),
                        0, ViewCompat.getPaddingEnd(getEditText()),
                        getEditText().getPaddingBottom());
                }
                /*
                 * alternative correction posted on original page (not by original author)
                 * Fixes issue with character counter?
                 * https://gist.github.com/drstranges/1a86965f582f610244d6
                 * Snippet starts from the commented code line above
                 *
                this.mHelperView.setVisibility(INVISIBLE);
                View view = getChildAt(1);
                if (view instanceof LinearLayout){
                    ((ViewGroup) view).addView(mHelperView, 0);
                } else {
                    this.addView(this.mHelperView);
                    if (this.mHelperView != null) {
                        ViewCompat.setPaddingRelative(
                                this.mHelperView,
                                ViewCompat.getPaddingStart(getEditText()),
                                0, ViewCompat.getPaddingEnd(getEditText()),
                                getEditText().getPaddingBottom());
                    }
                }
                */
            } else {
                this.removeView(this.mHelperView);
                this.mHelperView = null;
            }

            this.mHelperTextEnabled = _enabled;
        }
    }

    public void setHelperText(CharSequence _helperText) {
        mHelperText = _helperText;
        if (!this.mHelperTextEnabled) {
            if (TextUtils.isEmpty(mHelperText)) {
                return;
            }
            this.setHelperTextEnabled(true);
        }

        if (!TextUtils.isEmpty(mHelperText)) {
            this.mHelperView.setText(mHelperText);
            this.mHelperView.setVisibility(VISIBLE);
            //ViewCompat.setAlpha(this.mHelperView, 0.0F);
            this.mHelperView.setAlpha(0.0F);
            ViewCompat.animate(this.mHelperView)
                .alpha(1.0F).setDuration(200L)
                .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                .setListener(null).start();
        } else if (this.mHelperView.getVisibility() == VISIBLE) {
            ViewCompat.animate(this.mHelperView)
                .alpha(0.0F).setDuration(200L)
                .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(View view) {
                        mHelperView.setText(null);
                        mHelperView.setVisibility(INVISIBLE);
                    }
                })
                .start();
        }
        this.sendAccessibilityEvent(2048);
    }

}
