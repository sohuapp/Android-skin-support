package skin.support;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import skin.support.app.SkinActivityLifecycle;
import skin.support.app.SkinViewInflater;
import skin.support.app.SkinLayoutInflater;
import skin.support.content.res.SkinCompatResources;
import skin.support.load.SkinAssetsLoader;
import skin.support.load.SkinBuildInLoader;
import skin.support.observe.SkinObservable;
import skin.support.utils.SkinPreference;

public class SkinCompatManager extends SkinObservable {
    public static final int SKIN_LOADER_STRATEGY_ASSETS = 0;
    public static final int SKIN_LOADER_STRATEGY_BUILD_IN = 1;
    private static final String SKIN_SEPARATOR = "&";
    private static final Map<Context, SkinCompatManager> sInstanceMap = new HashMap<>();
    private final Object mLock = new Object();
    private final Context mAppContext;
    private boolean mLoading = false;
    private List<SkinLayoutInflater> mInflaters = new ArrayList<>();
    private List<SkinLayoutInflater> mHookInflaters = new ArrayList<>();
    private Map<Integer, SkinLoaderStrategy> mStrategyMap = new HashMap<>();
    private boolean mSkinAllActivityEnable = true;
    private boolean mSkinWindowBackgroundColorEnable = false;

    /**
     * 皮肤包加载监听.
     */
    public interface SkinLoaderListener {
        /**
         * 开始加载.
         */
        void onStart();

        /**
         * 加载成功.
         */
        void onSuccess();

        /**
         * 加载失败.
         *
         * @param errMsg 错误信息.
         */
        void onFailed(String errMsg);
    }

    /**
     * 皮肤包加载策略.
     */
    public interface SkinLoaderStrategy {
        int NONE = -1;
        int PREFIX = 0;
        int SUFFIX = 1;

        /**
         * 加载皮肤包.
         *
         * @param context  {@link Context}
         * @param skinName 皮肤包名称.
         * @return 加载成功，返回皮肤包名称；失败，则返回空。
         */
        String loadSkinInBackground(Context context, String skinName);

        /**
         * 根据应用中的资源ID，获取皮肤包相应资源的资源名.
         *
         * @param context     {@link Context}
         * @param skinName    皮肤包名称.
         * @param resId       应用中需要换肤的资源ID.
         * @param affixesType {@link #NONE} {@link #PREFIX} {@link #SUFFIX}
         * @param affixesStr  资源名，词缀字符串
         * @return 皮肤包中相应的资源名.
         */
        String getTargetResourceEntryName(Context context, String skinName, int resId, int affixesType, String affixesStr);

        /**
         * {@link #SKIN_LOADER_STRATEGY_ASSETS}
         * {@link #SKIN_LOADER_STRATEGY_BUILD_IN}
         *
         * @return 皮肤包加载策略类型.
         */
        int getType();
    }

    /**
     * 初始化换肤框架.
     *
     * @param application
     * @return
     */
    public static SkinCompatManager init(Application application) {
        SkinCompatManager instance = sInstanceMap.get(application);
        if (instance == null) {
            synchronized (SkinCompatManager.class) {
                instance = sInstanceMap.get(application);
                if (instance == null) {
                    instance = new SkinCompatManager(application);
                    sInstanceMap.put(application, instance);
                }
                SkinPreference.init(application);
                SkinCompatResources.init(application);
                SkinActivityLifecycle.init(application);
            }
        }
        return instance;
    }

    public static SkinCompatManager getInstance(Context context) {
        return init((Application) context.getApplicationContext());
    }

    private SkinCompatManager(Application application) {
        mAppContext = application;
        initLoaderStrategy();
    }

    private void initLoaderStrategy() {
        mStrategyMap.put(SKIN_LOADER_STRATEGY_ASSETS, new SkinAssetsLoader());
        mStrategyMap.put(SKIN_LOADER_STRATEGY_BUILD_IN, new SkinBuildInLoader());
    }

    /**
     * 添加皮肤包加载策略.
     *
     * @param strategy 自定义加载策略
     * @return
     */
    public SkinCompatManager addStrategy(SkinLoaderStrategy strategy) {
        mStrategyMap.put(strategy.getType(), strategy);
        return this;
    }

    public Map<Integer, SkinLoaderStrategy> getStrategies() {
        return mStrategyMap;
    }

    /**
     * 自定义View换肤时，可选择添加一个{@link SkinLayoutInflater}
     *
     * @param inflater 在{@link SkinViewInflater#createView(Context, String, String)}方法中调用.
     * @return
     */
    public SkinCompatManager addInflater(SkinLayoutInflater inflater) {
        mInflaters.add(inflater);
        return this;
    }

    public List<SkinLayoutInflater> getInflaters() {
        return mInflaters;
    }


    /**
     * 自定义View换肤时，可选择添加一个{@link SkinLayoutInflater}
     *
     * @param inflater 在{@link SkinViewInflater#createView(Context, String, String)}方法中最先调用.
     * @return
     */
    public SkinCompatManager addHookInflater(SkinLayoutInflater inflater) {
        mHookInflaters.add(inflater);
        return this;
    }

    public List<SkinLayoutInflater> getHookInflaters() {
        return mHookInflaters;
    }

    /**
     * 获取当前皮肤包.
     *
     * @return
     */
    public String getCurSkinName() {
        return SkinPreference.getInstance(mAppContext).getSkinName();
    }

    /**
     * 恢复默认主题，使用应用自带资源.
     */
    public void restoreDefaultSkin() {
        loadSkin(null, null, 0, "", null);
    }

    /**
     * 设置是否所有Activity都换肤.
     *
     * @param enable true: 所有Activity都换肤; false: 实现SkinCompatSupportable的Activity支持换肤.
     * @return
     */
    public SkinCompatManager setSkinAllActivityEnable(boolean enable) {
        mSkinAllActivityEnable = enable;
        return this;
    }

    public boolean isSkinAllActivityEnable() {
        return mSkinAllActivityEnable;
    }

    /**
     * 设置WindowBackground换肤，使用Theme中的{@link android.R.attr#windowBackground}属性.
     *
     * @param enable true: 打开; false: 关闭.
     * @return
     */
    public SkinCompatManager setSkinWindowBackgroundEnable(boolean enable) {
        mSkinWindowBackgroundColorEnable = enable;
        return this;
    }

    public boolean isSkinWindowBackgroundEnable() {
        return mSkinWindowBackgroundColorEnable;
    }

    /**
     * 加载记录的皮肤包，一般在Application中初始化换肤框架后调用.
     *
     * @return
     */
    public void loadSkin() {
        loadSkin(null);
    }

    /**
     * 加载记录的皮肤包，一般在Application中初始化换肤框架后调用.
     *
     * @param listener 皮肤包加载监听.
     * @return
     */
    public void loadSkin(SkinLoaderListener listener) {
        String skin = SkinPreference.getInstance(mAppContext).getSkinName();
        String strategy = SkinPreference.getInstance(mAppContext).getSkinStrategy();
        if (!TextUtils.isEmpty(skin) && !TextUtils.isEmpty(strategy)) {
            try {
                String[] nameArray = skin.split(SKIN_SEPARATOR);
                String[] strategyArray = strategy.split(SKIN_SEPARATOR);
                if (nameArray.length == strategyArray.length) {
                    int count = nameArray.length;
                    List<String> skinNames = new ArrayList<>();
                    List<Integer> strategies = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        skinNames.add(nameArray[i]);
                        strategies.add(Integer.valueOf(strategyArray[i]));
                    }
                    loadSkin(skinNames, strategies, SkinPreference.getInstance(mAppContext).getAffixesType(),
                            SkinPreference.getInstance(mAppContext).getAffixesStr(), listener);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 加载皮肤包.
     *
     * @param skinName 皮肤包名称.
     * @param strategy 皮肤包加载策略.
     * @return
     */
    public void loadSkin(String skinName, int strategy) {
        if (strategy == SKIN_LOADER_STRATEGY_BUILD_IN) {
            loadSkin(skinName, strategy, SkinLoaderStrategy.PREFIX, skinName, null);
        } else {
            loadSkin(skinName, strategy, SkinLoaderStrategy.NONE, "", null);
        }
    }

    /**
     * 加载皮肤包.
     *
     * @param skinName   皮肤包名称.
     * @param strategy   皮肤包加载策略.
     * @param affixesType 词缀类型
     * @param affixesStr 词缀名
     * @return
     */
    public void loadSkin(String skinName, int strategy, int affixesType, String affixesStr) {
        loadSkin(skinName, strategy, affixesType, affixesStr, null);
    }

    /**
     * 加载皮肤包.
     *
     * @param skinName   皮肤包名称.
     * @param listener   皮肤包加载监听.
     * @param strategy   皮肤包加载策略.
     * @param affixesType 词缀类型
     * @param affixesStr 词缀名
     * @param listener   皮肤包加载监听.
     * @return
     */
    public void loadSkin(String skinName, int strategy, int affixesType, String affixesStr, SkinLoaderListener listener) {
        List<String> skinNames = new ArrayList<>();
        skinNames.add(skinName);
        List<Integer> strategies = new ArrayList<>();
        strategies.add(strategy);
        loadSkin(skinNames, strategies, affixesType, affixesStr, listener);
    }

    /**
     * 加载皮肤包.
     *
     * @param skinNames  皮肤包名称list.
     * @param strategies 皮肤包加载策略list.
     * @param affixesType 词缀类型
     * @param affixesStr 词缀名
     * @param listener   皮肤包加载监听.
     * @return
     */
    public void loadSkin(List<String> skinNames, List<Integer> strategies, int affixesType, String affixesStr, SkinLoaderListener listener) {
        new SkinLoadTask(skinNames, strategies, affixesType, affixesStr, listener).execute();
    }

    private class SkinLoadTask extends AsyncTask<Void, Void, Boolean> {
        private final SkinLoaderListener mListener;
        private final List<String> mSkinNames;
        private final List<Integer> mStrategies;
        private final int mAffixesType;
        private final String mAffixesStr;

        SkinLoadTask(List<String> skinNames, List<Integer> strategies,
                     int affixesType, String affixesStr, SkinLoaderListener listener) {
            mListener = listener;
            mSkinNames = skinNames;
            mStrategies = strategies;
            mAffixesType = affixesType;
            mAffixesStr = affixesStr;
        }

        protected void onPreExecute() {
            if (mListener != null) {
                mListener.onStart();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            synchronized (mLock) {
                while (mLoading) {
                    try {
                        mLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mLoading = true;
            }
            boolean success = false;
            try {
                SkinCompatResources.getInstance(mAppContext).reset();
                if (mSkinNames == null || mStrategies == null || mSkinNames.isEmpty()) {
                    return true;
                }
                if (mSkinNames.size() != mStrategies.size()) {
                    return false;
                }

                int skinCount = mSkinNames.size();
                for (int i = skinCount - 1; i >= 0; i--) {
                    String path = mStrategyMap.get(mStrategies.get(i))
                            .loadSkinInBackground(mAppContext, mSkinNames.get(i));
                    success = !TextUtils.isEmpty(path);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!success) {
                SkinCompatResources.getInstance(mAppContext).reset();
            }
            return success;
        }

        protected void onPostExecute(Boolean success) {
            synchronized (mLock) {
                if (success) {
                    if (mSkinNames == null || mSkinNames.isEmpty()) {
                        SkinPreference.getInstance(mAppContext)
                                .setSkinName("")
                                .setSkinStrategy("")
                                .setAffixesType(SkinLoaderStrategy.NONE)
                                .setAffixesStr("")
                                .commitEditor();
                    } else {
                        StringBuilder skinName = new StringBuilder();
                        StringBuilder skinStrategy = new StringBuilder();
                        int skinCount = mSkinNames.size();
                        for (int i = 0; i < skinCount; i++) {
                            if (i != 0) {
                                skinName.append(SKIN_SEPARATOR);
                                skinStrategy.append(SKIN_SEPARATOR);
                            }
                            skinName.append(mSkinNames.get(i));
                            skinStrategy.append(mStrategies.get(i));
                        }
                        SkinPreference.getInstance(mAppContext)
                                .setSkinName(skinName.toString())
                                .setSkinStrategy(skinStrategy.toString())
                                .setAffixesType(mAffixesType)
                                .setAffixesStr(mAffixesStr)
                                .commitEditor();
                    }
                    notifyUpdateSkin();
                    if (mListener != null) mListener.onSuccess();
                } else {
                    SkinPreference.getInstance(mAppContext)
                            .setSkinName("")
                            .setSkinStrategy("")
                            .setAffixesType(SkinLoaderStrategy.NONE)
                            .setAffixesStr("")
                            .commitEditor();
                    if (mListener != null) mListener.onFailed("皮肤资源获取失败");
                }
                mLoading = false;
                mLock.notifyAll();
            }
        }
    }

    /**
     * 获取皮肤包包名.
     *
     * @param skinPkgPath sdcard中皮肤包路径.
     * @return
     */
    public String getSkinPackageName(String skinPkgPath) {
        PackageManager mPm = mAppContext.getPackageManager();
        PackageInfo info = mPm.getPackageArchiveInfo(skinPkgPath, PackageManager.GET_ACTIVITIES);
        return info.packageName;
    }

    /**
     * 获取皮肤包资源{@link Resources}.
     *
     * @param skinPkgPath sdcard中皮肤包路径.
     * @return
     */
    @Nullable
    public Resources getSkinResources(String skinPkgPath) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, skinPkgPath);

            Resources superRes = mAppContext.getResources();
            return new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}