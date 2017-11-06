package skin.support.content.res;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import skin.support.SkinCompatManager;
import skin.support.utils.SkinPreference;

public class SkinCompatResources {
    private static final Map<Context, SkinCompatResources> sInstanceMap = new HashMap<>();
    private final Context mAppContext;
    private List<SkinEntry> mSkinEntries = new ArrayList<>();

    public static class SkinEntry {
        private Context mAppContext;
        private Resources mResources;
        private String mSkinPkgName;
        private String mSkinName;

        private SkinCompatManager.SkinLoaderStrategy mStrategy;

        public SkinEntry(Context appContext, Resources resources, String pkgName,
                         String skinName, SkinCompatManager.SkinLoaderStrategy strategy) {
            mAppContext = appContext;
            mResources = resources;
            mSkinPkgName = pkgName;
            mSkinName = skinName;
            mStrategy = strategy;
        }

        public int getTargetResId(int resId) {
            try {
                String resName = null;
                if (mStrategy != null) {
                    resName = mStrategy.getTargetResourceEntryName(mAppContext, mSkinName, resId,
                            SkinPreference.getInstance(mAppContext).getAffixesType(),
                            SkinPreference.getInstance(mAppContext).getAffixesStr());
                }
                if (TextUtils.isEmpty(resName)) {
                    resName = mAppContext.getResources().getResourceEntryName(resId);
                }
                String type = mAppContext.getResources().getResourceTypeName(resId);
                return mResources.getIdentifier(resName, type, mSkinPkgName);
            } catch (Exception e) {
                // 换肤失败不至于应用崩溃.
                return 0;
            }
        }
    }

    private SkinCompatResources(Context context) {
        mAppContext = context.getApplicationContext();
        reset();
    }

    public static void init(Context context) {
        SkinCompatResources instance = sInstanceMap.get(context.getApplicationContext());
        if (instance == null) {
            synchronized (SkinCompatResources.class) {
                instance = sInstanceMap.get(context.getApplicationContext());
                if (instance == null) {
                    instance = new SkinCompatResources(context);
                    sInstanceMap.put(context.getApplicationContext(), instance);
                }
            }
        }
    }

    public static SkinCompatResources getInstance(Context context) {
        return sInstanceMap.get(context.getApplicationContext());
    }

    public void reset() {
        mSkinEntries.clear();
    }

    public void addSkinEntry(SkinEntry entry) {
        mSkinEntries.add(entry);
    }

    public int getColor(int resId) {
        if (!mSkinEntries.isEmpty()) {
            for (SkinEntry entry : mSkinEntries) {
                int targetResId = entry.getTargetResId(resId);
                if (targetResId != 0) {
                    return entry.mResources.getColor(targetResId);
                }
            }
        }
        return ContextCompat.getColor(mAppContext, resId);
    }

    public Drawable getDrawable(int resId) {
        if (!mSkinEntries.isEmpty()) {
            for (SkinEntry entry : mSkinEntries) {
                int targetResId = entry.getTargetResId(resId);
                if (targetResId != 0) {
                    return entry.mResources.getDrawable(targetResId);
                }
            }
        }
        return ContextCompat.getDrawable(mAppContext, resId);
    }

    public ColorStateList getColorStateList(int resId) {
        if (!mSkinEntries.isEmpty()) {
            for (SkinEntry entry : mSkinEntries) {
                int targetResId = entry.getTargetResId(resId);
                if (targetResId != 0) {
                    return entry.mResources.getColorStateList(targetResId);
                }
            }
        }
        return ContextCompat.getColorStateList(mAppContext, resId);
    }
}
