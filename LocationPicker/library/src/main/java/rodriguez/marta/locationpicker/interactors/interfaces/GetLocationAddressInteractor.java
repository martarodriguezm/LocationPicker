package rodriguez.marta.locationpicker.interactors.interfaces;

import android.content.Context;

import com.google.android.gms.location.places.Place;

/**
 * Created by martarodriguez on 4/10/16.
 */

public interface GetLocationAddressInteractor extends Interactor {

    interface Callbacks {
        void onComplete(Place place);

        void onError();
    }

    void execute(Context context, double latitude, double longitude, Callbacks callbacks);
}
