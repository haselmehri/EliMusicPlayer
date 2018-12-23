package haselmehri.app.com.elimusicplayer;

import android.app.Application;
import android.graphics.Typeface;

public class BaseApplication extends Application {
    private static BaseApplication mInstance;
    private static Typeface iranianSansFont;
    public static final String TAG = BaseApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        mInstance = this;
        super.onCreate();

        //Fabric.with(this,new Crashlytics());
    }

    public static Typeface getIranianSansFont()
    {
        if (iranianSansFont == null)
            iranianSansFont = Typeface.createFromAsset(mInstance.getAssets(),"fonts/iranian_sans.ttf");

        return iranianSansFont;
    }
}
