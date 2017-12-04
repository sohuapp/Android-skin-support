package skin.support.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.LayoutInflaterCompat;
import android.view.LayoutInflater;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import skin.support.SkinCompatManager;
import skin.support.content.res.SkinCompatResources;
import skin.support.observe.SkinObservable;
import skin.support.observe.SkinObserver;
import skin.support.widget.SkinCompatSupportable;
import skin.support.widget.SkinThemeUtils;

import static skin.support.widget.SkinCompatHelper.INVALID_ID;
import static skin.support.widget.SkinCompatHelper.checkResourceId;

public class SkinActivityLifecycle implements Application.ActivityLifecycleCallbacks {
    private static final Map<Context, SkinActivityLifecycle> sInstanceMap = new HashMap<>();
    private WeakHashMap<Context, SkinDelegate> mSkinDelegateMap;
    private WeakHashMap<Context, SkinObserver> mSkinObserverMap;

    public static SkinActivityLifecycle init(Application application) {
        SkinActivityLifecycle instance = sInstanceMap.get(application);
        if (instance == null) {
            synchronized (SkinActivityLifecycle.class) {
                instance = sInstanceMap.get(application);
                if (instance == null) {
                    instance = new SkinActivityLifecycle(application);
                    sInstanceMap.put(application, instance);
                }
            }
        }
        return instance;
    }

    private SkinActivityLifecycle(Application application) {
        application.registerActivityLifecycleCallbacks(this);
        installLayoutFactory(application);
        SkinCompatManager.getInstance(application).addObserver(getObserver(application));
    }

    private void installLayoutFactory(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        try {
            Field field = LayoutInflater.class.getDeclaredField("mFactorySet");
            field.setAccessible(true);
            field.setBoolean(layoutInflater, false);
            LayoutInflaterCompat.setFactory(layoutInflater, getSkinDelegate(context));
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private SkinDelegate getSkinDelegate(Context context) {
        if (mSkinDelegateMap == null) {
            mSkinDelegateMap = new WeakHashMap<>();
        }

        SkinDelegate mSkinDelegate = mSkinDelegateMap.get(context);
        if (mSkinDelegate == null) {
            mSkinDelegate = SkinDelegate.create(context);
        }
        mSkinDelegateMap.put(context, mSkinDelegate);
        return mSkinDelegate;
    }

    private SkinObserver getObserver(final Context context) {
        if (mSkinObserverMap == null) {
            mSkinObserverMap = new WeakHashMap<>();
        }
        SkinObserver observer = mSkinObserverMap.get(context);
        if (observer == null) {
            observer = new SkinObserver() {
                @Override
                public void updateSkin(SkinObservable observable, Object o) {
                    getSkinDelegate(context).applySkin();
                    if (context instanceof Activity && isContextSkinEnable((Activity) context)) {
                        updateWindowBackground((Activity) context);
                    }
                    if (context instanceof SkinCompatSupportable) {
                        ((SkinCompatSupportable) context).applySkin();
                    }
                }
            };
        }
        mSkinObserverMap.put(context, observer);
        return observer;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (isContextSkinEnable(activity)) {
            installLayoutFactory(activity);
            updateWindowBackground(activity);
            if (activity instanceof SkinCompatSupportable) {
                ((SkinCompatSupportable) activity).applySkin();
            }
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (isContextSkinEnable(activity)) {
            SkinCompatManager.getInstance(activity).addObserver(getObserver(activity));
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (isContextSkinEnable(activity)) {
            SkinCompatManager.getInstance(activity).deleteObserver(getObserver(activity));
            mSkinObserverMap.remove(activity);
            mSkinDelegateMap.remove(activity);
        }
    }

    private boolean isContextSkinEnable(Context context) {
        return SkinCompatManager.getInstance(context).isSkinAllActivityEnable() || context instanceof SkinCompatSupportable;
    }

    private void updateWindowBackground(Activity activity) {
        if (SkinCompatManager.getInstance(activity).isSkinWindowBackgroundEnable()) {
            int windowBackgroundResId = SkinThemeUtils.getWindowBackgroundResId(activity);
            if (checkResourceId(windowBackgroundResId) != INVALID_ID) {
                Drawable drawable = SkinCompatResources.getInstance(activity).getDrawable(windowBackgroundResId);
                if (drawable != null) {
                    activity.getWindow().setBackgroundDrawable(drawable);
                }
            }
        }
    }
}