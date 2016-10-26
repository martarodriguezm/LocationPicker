package rodriguez.marta.locationpicker.interactors;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.location.places.Place;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import rodriguez.marta.locationpicker.executors.PostExecutionThread;
import rodriguez.marta.locationpicker.executors.ThreadExecutor;
import rodriguez.marta.locationpicker.interactors.interfaces.GetLocationAddressInteractor;
import rodriguez.marta.locationpicker.models.AppPlace;
import timber.log.Timber;

/**
 * Created by martarodriguez on 4/10/16.
 */

public class GetLocationAddressInteractorImpl implements GetLocationAddressInteractor {
    private double latitude;
    private double longitude;
    private Callbacks callbacks;
    private Context context;

    private ThreadExecutor threadExecutor;
    private PostExecutionThread postExecutionThread;

    public GetLocationAddressInteractorImpl(ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        this.threadExecutor = threadExecutor;
        this.postExecutionThread = postExecutionThread;
    }

    @Override
    public void execute(Context context, double latitude, double longitude, Callbacks callbacks) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.callbacks = callbacks;
        this.context = context;

        threadExecutor.execute(this);
    }

    @Override
    public void run() {
        try {
            String address = getCompleteAddressString(latitude, longitude);
            final Place place = new AppPlace(latitude, longitude, address);
            postExecutionThread.post(new Runnable() {
                @Override
                public void run() {
                    callbacks.onComplete(place);
                }
            });
        } catch (Exception e) {
            Timber.e("GetLocationAddressInteractorImpl", e);
            postExecutionThread.post(new Runnable() {
                @Override
                public void run() {
                    callbacks.onError();
                }
            });
        }
    }

    private String getCompleteAddressString(double latitude, double longitude) throws IOException {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
            } else {
            }
        } catch (Exception e) {
            Timber.e("getCompleteAddressString", e);
            throw e;
        }
        return strAdd;
    }
}