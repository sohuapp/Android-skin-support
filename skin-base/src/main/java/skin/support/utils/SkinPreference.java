package skin.support.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

import skin.support.SkinCompatManager;

public class SkinPreference {
    private static final String FILE_NAME = "meta-data";

    private static final String KEY_SKIN_NAME = "skin-name";
    private static final String KEY_SKIN_STRATEGY = "skin-strategy";
    private static final String KEY_AFFIXES_TYPE = "skin-affixes-type";
    private static final String KEY_AFFIXES_STR = "skin-affixes-str";
    private static final Map<Context, SkinPreference> sInstanceMap = new HashMap<>();
    private final Context mApp;
    private final SharedPreferences mPref;
    private final SharedPreferences.Editor mEditor;

    public static void init(Context context) {
        SkinPreference instance = sInstanceMap.get(context.getApplicationContext());
        if (instance == null) {
            synchronized (SkinPreference.class) {
                instance = sInstanceMap.get(context.getApplicationContext());
                if (instance == null) {
                    instance = new SkinPreference(context.getApplicationContext());
                    sInstanceMap.put(context.getApplicationContext(), instance);
                }
            }
        }
    }

    public static SkinPreference getInstance(Context context) {
        return sInstanceMap.get(context);
    }

    private SkinPreference(Context applicationContext) {
        mApp = applicationContext;
        mPref = mApp.getSharedPreferences(mApp.getPackageName() + FILE_NAME, Context.MODE_PRIVATE);
        mEditor = mPref.edit();
    }

    public SkinPreference setSkinName(String skinName) {
        mEditor.putString(KEY_SKIN_NAME, skinName);
        return this;
    }

    public String getSkinName() {
        return mPref.getString(KEY_SKIN_NAME, "");
    }

    public SkinPreference setSkinStrategy(String strategy) {
        mEditor.putString(KEY_SKIN_STRATEGY, strategy);
        return this;
    }

    public String getSkinStrategy() {
        return mPref.getString(KEY_SKIN_STRATEGY, "");
    }

    public SkinPreference setAffixesType(int strategy) {
        mEditor.putInt(KEY_AFFIXES_TYPE, strategy);
        return this;
    }

    public int getAffixesType() {
        return mPref.getInt(KEY_AFFIXES_TYPE, SkinCompatManager.SkinLoaderStrategy.NONE);
    }

    public SkinPreference setAffixesStr(String strategy) {
        mEditor.putString(KEY_AFFIXES_STR, strategy);
        return this;
    }

    public String getAffixesStr() {
        return mPref.getString(KEY_AFFIXES_STR, "");
    }

    public void commitEditor() {
        mEditor.apply();
    }
}
