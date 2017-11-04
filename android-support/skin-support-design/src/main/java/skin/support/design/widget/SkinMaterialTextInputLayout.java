package skin.support.design.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.StyleRes;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.TintTypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import skin.support.content.res.SkinCompatResources;
import skin.support.design.R;
import skin.support.widget.SkinCompatBackgroundHelper;
import skin.support.widget.SkinCompatEditText;
import skin.support.widget.SkinCompatHelper;
import skin.support.widget.SkinCompatSupportable;

import static skin.support.widget.SkinCompatHelper.INVALID_ID;

/**
 * Created by ximsfei on 17-3-2.
 */

public class SkinMaterialTextInputLayout extends TextInputLayout implements SkinCompatSupportable {
    private static final String TAG = SkinMaterialTextInputLayout.class.getSimpleName();
    private SkinCompatBackgroundHelper mBackgroundTintHelper;
    private int mCounterTextColorResId = INVALID_ID;
    private int mErrorTextColorResId = INVALID_ID;
    private int mFocusedTextColorResId = INVALID_ID;
    private int mDefaultTextColorResId = INVALID_ID;

    public SkinMaterialTextInputLayout(Context context) {
        this(context, null);
    }

    public SkinMaterialTextInputLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SkinMaterialTextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBackgroundTintHelper = new SkinCompatBackgroundHelper(this);
        mBackgroundTintHelper.loadFromAttributes(attrs, defStyleAttr);

        final TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs,
                R.styleable.TextInputLayout, defStyleAttr, R.style.Widget_Design_TextInputLayout);
        if (a.hasValue(R.styleable.TextInputLayout_android_textColorHint)) {
            mDefaultTextColorResId = mFocusedTextColorResId =
                    a.getResourceId(R.styleable.TextInputLayout_android_textColorHint, INVALID_ID);
            applyFocusedTextColorResource();
        }

        int counterTextAppearance = a.getResourceId(R.styleable.TextInputLayout_counterTextAppearance, INVALID_ID);
        loadCounterTextColorResFromAttributes(counterTextAppearance);
        a.recycle();
    }

    private void loadCounterTextColorResFromAttributes(@StyleRes int resId) {
        if (resId != INVALID_ID) {
            TypedArray counterTA = getContext().obtainStyledAttributes(resId, skin.support.R.styleable.SkinTextAppearance);
            if (counterTA.hasValue(skin.support.R.styleable.SkinTextAppearance_android_textColor)) {
                mCounterTextColorResId = counterTA.getResourceId(skin.support.R.styleable.SkinTextAppearance_android_textColor, INVALID_ID);
            }
            counterTA.recycle();
        }
        applyCounterTextColorResource();
    }

    @Override
    public void setCounterEnabled(boolean enabled) {
        super.setCounterEnabled(enabled);
        if (enabled) {
            applyCounterTextColorResource();
        }
    }

    private void applyCounterTextColorResource() {
        mCounterTextColorResId = SkinCompatHelper.checkResourceId(mCounterTextColorResId);
        if (mCounterTextColorResId != INVALID_ID) {
            TextView counterView = getCounterView();
            if (counterView != null) {
//                SkinLog.e(TAG, "mCounterTextColor = " +Integer.toHexString(SkinCompatResources.getInstance().getColor(mCounterTextColorResId)));
                counterView.setTextColor(SkinCompatResources.getInstance().getColor(mCounterTextColorResId));
                updateEditTextBackground();
            }
        }
    }

    private TextView getCounterView() {
        try {
            Field counterView = TextInputLayout.class.getDeclaredField("mCounterView");
            counterView.setAccessible(true);
            return (TextView) counterView.get(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setErrorEnabled(boolean enabled) {
        super.setErrorEnabled(enabled);
        if (enabled) {
            applyErrorTextColorResource();
        }
    }

    private void applyErrorTextColorResource() {
        mErrorTextColorResId = SkinCompatHelper.checkResourceId(mErrorTextColorResId);
        if (mErrorTextColorResId != INVALID_ID) {
            TextView errorView = getErrorView();
            if (errorView != null) {
                errorView.setTextColor(SkinCompatResources.getInstance().getColor(mErrorTextColorResId));
                updateEditTextBackground();
            }
        }
    }

    private TextView getErrorView() {
        try {
            Field errorView = TextInputLayout.class.getDeclaredField("mErrorView");
            errorView.setAccessible(true);
            return (TextView) errorView.get(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateEditTextBackground() {
        try {
            Method updateEditTextBackground = TextInputLayout.class.getDeclaredMethod("updateEditTextBackground");
            updateEditTextBackground.setAccessible(true);
            updateEditTextBackground.invoke(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDefaultTextColor(ColorStateList colors) {
        try {
            Field defaultTextColor = TextInputLayout.class.getDeclaredField("mDefaultTextColor");
            defaultTextColor.setAccessible(true);
            defaultTextColor.set(this, colors);
            updateLabelState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyFocusedTextColorResource() {
        mFocusedTextColorResId = SkinCompatHelper.checkResourceId(mFocusedTextColorResId);
        if (mFocusedTextColorResId != INVALID_ID) {
            setFocusedTextColor(SkinCompatResources.getInstance().getColorStateList(mFocusedTextColorResId));
        } else if (getEditText() != null) {
            int textColorResId = INVALID_ID;
            if (getEditText() instanceof SkinCompatEditText) {
                textColorResId = ((SkinCompatEditText) getEditText()).getTextColorResId();
            } else if (getEditText() instanceof SkinMaterialTextInputEditText) {
                textColorResId = ((SkinMaterialTextInputEditText) getEditText()).getTextColorResId();
            }
            textColorResId = SkinCompatHelper.checkResourceId(textColorResId);
            if (textColorResId != INVALID_ID) {
                ColorStateList colors = SkinCompatResources.getInstance().getColorStateList(textColorResId);
                setFocusedTextColor(colors);
            }
        }
    }

    private void setFocusedTextColor(ColorStateList colors) {
        try {
            Field focusedTextColor = TextInputLayout.class.getDeclaredField("mFocusedTextColor");
            focusedTextColor.setAccessible(true);
            focusedTextColor.set(this, colors);
            updateLabelState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateLabelState() {
        try {
            Method updateLabelState = TextInputLayout.class.getDeclaredMethod("updateLabelState", boolean.class);
            updateLabelState.setAccessible(true);
            updateLabelState.invoke(this, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void applySkin() {
        applyErrorTextColorResource();
        applyCounterTextColorResource();
        applyFocusedTextColorResource();
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.applySkin();
        }
    }

}
