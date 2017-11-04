package skin.support.widget;

import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.SeekBar;

import skin.support.content.res.SkinCompatResources;

/**
 * Created by ximsfei on 17-1-21.
 */
public class SkinCompatSeekBarHelper extends SkinCompatProgressBarHelper {
    private static final int[] TINT_ATTRS = {
            android.R.attr.thumb
    };

    private final SeekBar mView;

    private int mThumbResId = INVALID_ID;

    public SkinCompatSeekBarHelper(SeekBar view) {
        super(view);
        mView = view;
    }

    @Override
    void loadFromAttributes(AttributeSet attrs, int defStyleAttr) {
        super.loadFromAttributes(attrs, defStyleAttr);

        TypedArray a = mView.getContext().obtainStyledAttributes(attrs, TINT_ATTRS, defStyleAttr, 0);
        mThumbResId = a.getResourceId(0, INVALID_ID);

        a.recycle();

        applySkin();
    }

    @Override
    public void applySkin() {
        super.applySkin();
        mThumbResId = checkResourceId(mThumbResId);
        if (mThumbResId != INVALID_ID) {
            mView.setThumb(SkinCompatResources.getInstance().getDrawable(mThumbResId));
        }
    }
}
