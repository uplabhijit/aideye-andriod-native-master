package com.gocubetech.aideye;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class NfcReaderService extends Service
{
    public NfcReaderService()
    {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
//        Toast.makeText(this, "Start service ", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
