package fr.fruitice.mail;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * Created by florian on 12/01/2017.
 */

public class TouchyWebView extends WebView {
    public TouchyWebView(Context context) {
        super(context);
    }

    public TouchyWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchyWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //requestDisallowInterceptTouchEvent(true);

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
        Log.d("scrollChanged", "l: " + l + ", t: " + t + ", oldl: "+ oldl + ", oldt: " + oldt);
        //if direction is left or right, requestDisallow
        super.onScrollChanged(l, t, oldl, oldt);
    }
}
