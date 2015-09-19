package com.danilov.aircontrol.core.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by Semyon on 19.09.2015.
 */
public class SafeRecyclerView extends RecyclerView {

    private static final String TAG = "SafeRecyclerView";

    public SafeRecyclerView(final Context context) {
        super(context);
    }

    public SafeRecyclerView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public SafeRecyclerView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    //THIS HAPPENS ON ANIMATED LAYOUT TRANSITIONS
    public void scrollTo(int x, int y) {
        Log.e(TAG, "CustomRecyclerView does not support scrolling to an absolute position.");
    }

}
