package rodriguez.marta.locationpicker.ui.views;

import com.google.android.gms.location.places.Place;

/**
 * Created by martarodriguez on 4/10/16.
 */

public interface LocationPickerView {
    void showLocationAddress(String address);
    void setPlaceResult(Place place);
    void enableConfirmLocationButton(boolean enabled);
    void showConnectionError();
    void showLoading();
    void hideLoading();
}
