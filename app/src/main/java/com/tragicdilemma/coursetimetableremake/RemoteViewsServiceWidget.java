package com.tragicdilemma.coursetimetableremake;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class RemoteViewsServiceWidget extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactoryWidget(this.getApplicationContext(), intent);
    }
}
