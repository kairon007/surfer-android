/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.frakbot.glowpadbackport;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.Log;

import java.lang.reflect.Method;

public class TargetDrawable {
    private static final String TAG = "TargetDrawable";
    private static final boolean DEBUG = false;

    public static final int[] STATE_ACTIVE =
        {android.R.attr.state_enabled, android.R.attr.state_active};
    public static final int[] STATE_INACTIVE =
        {android.R.attr.state_enabled, -android.R.attr.state_active};
    public static final int[] STATE_FOCUSED =
        {android.R.attr.state_enabled, -android.R.attr.state_active,
         android.R.attr.state_focused};

    // We're using Reflection to access these private APIs on older Android versions
    static Method mGetStateDrawableIndex, mGetStateCount, mGetStateDrawable;

    static {
        // In this static block we initialize all reflected methods (so that we can work around
        // the @hide annotations for those Android Framework methods). This is not ideal, but
        // there's not much we can do about that either.
        try {
            mGetStateCount = StateListDrawable.class.getDeclaredMethod("getStateCount");
            mGetStateCount.setAccessible(true);
        }
        catch (NoSuchMethodException e) {
            Log.e(TAG, "Couldn't access the StateListDrawable#getStateCount() method. " +
                    "Some stuff might break!", e);
        }
        try {
            mGetStateDrawable = StateListDrawable.class.getDeclaredMethod("getStateDrawable", int.class);
            mGetStateDrawable.setAccessible(true);
        }
        catch (NoSuchMethodException e) {
            Log.e(TAG, "Couldn't access the StateListDrawable#getStateDrawable(int) method. " +
                    "Some stuff might break!", e);
        }
        try {
            mGetStateDrawableIndex = StateListDrawable.class.getDeclaredMethod("getStateDrawableIndex", int[].class);
            mGetStateDrawableIndex.setAccessible(true);
        }
        catch (NoSuchMethodException e) {
            Log.e(TAG, "Couldn't access the StateListDrawable#mGetStateDrawableIndex(int[]) method. " +
                    "Some stuff might break!", e);
        }
    }

    private float mTranslationX = 0.0f;
    private float mTranslationY = 0.0f;
    private float mPositionX = 0.0f;
    private float mPositionY = 0.0f;
    private float mScaleX = 1.0f;
    private float mScaleY = 1.0f;
    private float mAlpha = 1.0f;
    private Drawable mDrawable;
    private boolean mEnabled = true;
    private final int mResourceId;

    /* package */ static class DrawableWithAlpha extends Drawable {
        private float mAlpha = 1.0f;
        private Drawable mRealDrawable;

        public DrawableWithAlpha(Drawable realDrawable) {
            mRealDrawable = realDrawable;
        }

        public void setAlphaFloat(float alpha) {
            mAlpha = alpha;
        }

        public int getAlphaFloat() {
            return (int) (mAlpha * 255);
        }

        public void draw(Canvas canvas) {
            mRealDrawable.setAlpha(Math.round(mAlpha * 255f));
            mRealDrawable.draw(canvas);
        }

        @Override
        public void setAlpha(int alpha) {
            mRealDrawable.setAlpha(alpha);
        }

        @Override
        public int getAlpha() {
            return mRealDrawable.getAlpha();
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            mRealDrawable.setColorFilter(cf);
        }

        @Override
        public int getOpacity() {
            return mRealDrawable.getOpacity();
        }
    }

    public TargetDrawable(Resources res, int resId) {
        mResourceId = resId;
        setDrawable(res, resId);
    }

    public void setDrawable(Resources res, int resId) {
        // Note we explicitly don't set mResourceId to resId since we allow the drawable to be
        // swapped at runtime and want to re-use the existing resource id for identification.
        Drawable drawable = resId == 0 ? null : res.getDrawable(resId);
        // Mutate the drawable so we can animate shared drawable properties.
        mDrawable = drawable != null ? drawable.mutate() : null;
        resizeDrawables();
        setState(STATE_INACTIVE);
    }

    public TargetDrawable(TargetDrawable other) {
        mResourceId = other.mResourceId;
        // Mutate the drawable so we can animate shared drawable properties.
        mDrawable = other.mDrawable != null ? other.mDrawable.mutate() : null;
        resizeDrawables();
        setState(STATE_INACTIVE);
    }

    public void setState(int[] state) {
        if (mDrawable instanceof StateListDrawable) {
            StateListDrawable d = (StateListDrawable) mDrawable;
            d.setState(state);
        }
    }

    public boolean hasState(int[] state) {
        if (mDrawable instanceof StateListDrawable) {
            StateListDrawable d = (StateListDrawable) mDrawable;
            // TODO: this doesn't seem to work
            try {
                return (Integer) mGetStateDrawableIndex.invoke(d, state) != -1;
            }
            catch (Exception e) {
                Log.w(TAG, "StateListDrawable#getStateDrawableIndex(int[]) call failed!", e);
            }
        }
        return false;
    }

    /**
     * Returns true if the drawable is a StateListDrawable and is in the focused state.
     *
     * @return Returns true if the drawable is a StateListDrawable and is in the focused state
     */
    public boolean isActive() {
        if (mDrawable instanceof StateListDrawable) {
            StateListDrawable d = (StateListDrawable) mDrawable;
            int[] states = d.getState();
            for (int i = 0; i < states.length; i++) {
                if (states[i] == android.R.attr.state_focused) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if this target is enabled. Typically an enabled target contains a valid
     * drawable in a valid state. Currently all targets with valid drawables are valid.
     *
     * @return Returns true if the target is enabled, false otherwise
     */
    public boolean isEnabled() {
        return mDrawable != null && mEnabled;
    }

    /**
     * Makes drawables in a StateListDrawable all the same dimensions.
     * If not a StateListDrawable, then justs sets the bounds to the intrinsic size of the
     * drawable.
     */
    private void resizeDrawables() {
        if (mDrawable instanceof StateListDrawable) {
            StateListDrawable d = (StateListDrawable) mDrawable;
            int maxWidth = 0;
            int maxHeight = 0;
            Integer stateCount = 0;
            try {
                stateCount = (Integer) mGetStateCount.invoke(d);
            }
            catch (Exception e) {
                Log.w(TAG, "StateListDrawable#getStateCount() call failed!", e);
            }

            for (int i = 0; i < stateCount; i++) {
                Drawable childDrawable;
                try {
                    childDrawable = (Drawable) mGetStateDrawable.invoke(d, i);
                }
                catch (Exception e) {
                    Log.w(TAG, "StateListDrawable#getStateDrawable(int) call failed!", e);
                    continue;
                }

                maxWidth = Math.max(maxWidth, childDrawable.getIntrinsicWidth());
                maxHeight = Math.max(maxHeight, childDrawable.getIntrinsicHeight());
            }
            if (DEBUG) {
                Log.v(TAG, "union of childDrawable rects " + d + " to: "
                        + maxWidth + "x" + maxHeight);
            }
            d.setBounds(0, 0, maxWidth, maxHeight);
            for (int i = 0; i < stateCount; i++) {
                Drawable childDrawable;
                try {
                    childDrawable = (Drawable) mGetStateDrawable.invoke(d, i);
                }
                catch (Exception e) {
                    Log.w(TAG, "StateListDrawable#getStateDrawable(int) call failed!", e);
                    continue;
                }

                if (DEBUG) {
                    Log.v(TAG, "sizing drawable " + childDrawable + " to: "
                            + maxWidth + "x" + maxHeight);
                }
                childDrawable.setBounds(0, 0, maxWidth, maxHeight);
            }
        }
        else if (mDrawable != null) {
            mDrawable.setBounds(0, 0,
                                mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
        }
    }

    public void setX(float x) {
        mTranslationX = x;
    }

    public void setY(float y) {
        mTranslationY = y;
    }

    public void setScaleX(float x) {
        mScaleX = x;
    }

    public void setScaleY(float y) {
        mScaleY = y;
    }

    public void setAlpha(float alpha) {
        mAlpha = alpha;
    }

    public float getX() {
        return mTranslationX;
    }

    public float getY() {
        return mTranslationY;
    }

    public float getScaleX() {
        return mScaleX;
    }

    public float getScaleY() {
        return mScaleY;
    }

    public float getAlpha() {
        return mAlpha;
    }

    public void setPositionX(float x) {
        mPositionX = x;
    }

    public void setPositionY(float y) {
        mPositionY = y;
    }

    public float getPositionX() {
        return mPositionX;
    }

    public float getPositionY() {
        return mPositionY;
    }

    public int getWidth() {
        return mDrawable != null ? mDrawable.getIntrinsicWidth() : 0;
    }

    public int getHeight() {
        return mDrawable != null ? mDrawable.getIntrinsicHeight() : 0;
    }

    public void draw(Canvas canvas) {
        if (mDrawable == null || !mEnabled) {
            return;
        }
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(mScaleX, mScaleY, mPositionX, mPositionY);
        canvas.translate(mTranslationX + mPositionX, mTranslationY + mPositionY);
        canvas.translate(-0.5f * getWidth(), -0.5f * getHeight());
        mDrawable.setAlpha(Math.round(mAlpha * 255f));
        mDrawable.draw(canvas);
        canvas.restore();
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public int getResourceId() {
        return mResourceId;
    }
}
