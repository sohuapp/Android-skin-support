package skin.support.load;

import android.content.Context;

import skin.support.SkinCompatManager;
import skin.support.SkinCompatManager.SkinLoaderStrategy;
import skin.support.content.res.SkinCompatResources;

public class SkinBuildInLoader implements SkinLoaderStrategy {
    @Override
    public String loadSkinInBackground(Context context, String skinName) {
        SkinCompatResources.getInstance(context).addSkinEntry(
                new SkinCompatResources.SkinEntry(
                        context.getApplicationContext(),
                        context.getResources(),
                        context.getPackageName(),
                        skinName,
                        this));
        return skinName;
    }

    @Override
    public String getTargetResourceEntryName(Context context, String skinName,
                                             int resId, int affixesType, String affixesStr) {
        switch (affixesType) {
            case PREFIX:
                return affixesStr + "_" + context.getResources().getResourceEntryName(resId);
            case SUFFIX:
                return context.getResources().getResourceEntryName(resId) + "_" + affixesStr;
            case NONE:
            default:
                return null;
        }
    }

    @Override
    public int getType() {
        return SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN;
    }
}
