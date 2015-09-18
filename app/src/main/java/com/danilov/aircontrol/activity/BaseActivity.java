package com.danilov.aircontrol.activity;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by Semyon on 18.09.2015.
 */
public class BaseActivity extends AppCompatActivity {

    public <T extends View> T view(final int id) {
        return (T) findViewById(id);
    }

}
