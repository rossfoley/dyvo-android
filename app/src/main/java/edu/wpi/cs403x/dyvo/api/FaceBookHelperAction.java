package edu.wpi.cs403x.dyvo.api;

import android.graphics.Bitmap;

/**
 * Created by cdhan_000 on 4/27/2015.
 */
public interface FaceBookHelperAction {


    public void onSuccess(String name, Bitmap bitmap);
    public void onFailure() ;

}
