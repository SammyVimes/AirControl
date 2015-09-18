package com.danilov.aircontrol.task;

import android.support.annotation.NonNull;

/**
 * Created by Semyon on 18.09.2015.
 */
public class Task<Return> {

    private TaskListener<Return> listener;

    public Task(final TaskListener<Return> listener) {
        this.listener = listener;
    }

    public void onTaskComplete(final Return result) {
        listener.onTaskComplete(result);
    }

    public void onError(final Throwable t) {
        listener.onError(t);
    }

    public static interface TaskListener<TaskType> {

        void onTaskComplete(@NonNull final TaskType result);

        void onError(@NonNull final Throwable t);

    }

}
