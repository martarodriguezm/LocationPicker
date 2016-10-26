package rodriguez.marta.locationpicker.ui.presenters;

import android.content.Context;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

import rodriguez.marta.locationpicker.interactors.interfaces.GetLocationAddressInteractor;
import rodriguez.marta.locationpicker.ui.views.LocationPickerView;
import timber.log.Timber;

/**
 * Created by martarodriguez on 4/10/16.
 */

public class LocationPickerPresenter extends BasePresenter<LocationPickerView> {

    private GetLocationAddressInteractor getLocationAddressInteractor;
    private Place place;

    public LocationPickerPresenter(GetLocationAddressInteractor getLocationAddressInteractor) {
        this.getLocationAddressInteractor = getLocationAddressInteractor;
    }

    @Override
    public void onViewAttached() {

    }

    public void locationSelected(Context context, LatLng latLng) {
        if(getView() != null) {
            getView().showLoading();
            getView().enableConfirmLocationButton(false);
        }
        GetLocationAddressInteractor.Callbacks callbacks = new GetLocationAddressInteractor.Callbacks() {
            @Override
            public void onComplete(Place place) {
                LocationPickerPresenter.this.place = place;
                if(getView() != null) {
                    getView().hideLoading();
                    getView().enableConfirmLocationButton(true);
                    getView().showLocationAddress(place.getAddress().toString());
                }
            }

            @Override
            public void onError() {
                Timber.e("locationSelected error");
                if(getView() != null) {
                    getView().hideLoading();
                    getView().showConnectionError();
                }
            }
        };
        getLocationAddressInteractor.execute(context, latLng.latitude, latLng.longitude, callbacks);
    }

    public void confirmLocationButtonClicked() {
        if(getView() != null) {
            getView().setPlaceResult(place);
        }
    }
}
