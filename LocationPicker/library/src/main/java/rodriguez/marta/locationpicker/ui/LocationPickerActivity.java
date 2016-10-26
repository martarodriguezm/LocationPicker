package rodriguez.marta.locationpicker.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import rodriguez.marta.locationpicker.R;
import rodriguez.marta.locationpicker.executors.JobExecutor;
import rodriguez.marta.locationpicker.executors.PostExecutionThread;
import rodriguez.marta.locationpicker.executors.ThreadExecutor;
import rodriguez.marta.locationpicker.executors.UIThread;
import rodriguez.marta.locationpicker.interactors.GetLocationAddressInteractorImpl;
import rodriguez.marta.locationpicker.models.AppPlace;
import rodriguez.marta.locationpicker.ui.presenters.LocationPickerPresenter;
import rodriguez.marta.locationpicker.ui.views.LocationPickerView;
import timber.log.Timber;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMyLocationButtonClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationPickerView {
    public static final int LOCATION_PICKER_CODE = 3843;

    private static final String LATITUDE_KEY = "latitude_key";
    private static final String LONGITUDE_KEY = "longitude_key";
    private static final String ADDRESS_KEY = "address_key";

    private GoogleMap googleMap;
    private Marker position;
    private TextView locationTextView;
    private Button confirmLocationBt;
    private ContentLoadingProgressBar loading;

    private GoogleApiClient googleApiClient;
    private Location lastLocation;

    private LatLng selectedLocation;

    private LocationPickerPresenter presenter;

    public static Intent getCallingIntent(Context context) {
        Intent callingIntent = new Intent(context, LocationPickerActivity.class);
        return callingIntent;
    }

    public static Intent getCallingIntent(Context context, String address, double latitude, double longitude) {
        Intent callingIntent = new Intent(context, LocationPickerActivity.class);
        callingIntent.putExtra(LATITUDE_KEY, latitude);
        callingIntent.putExtra(LONGITUDE_KEY, longitude);
        callingIntent.putExtra(ADDRESS_KEY, address);
        return callingIntent;
    }

    public static Place getPlace(Intent data) {
        return new AppPlace(data.getDoubleExtra(LATITUDE_KEY, 0.0), data.getDoubleExtra(LONGITUDE_KEY, 0.0), data.getStringExtra(ADDRESS_KEY));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        this.selectedLocation = new LatLng(getIntent().getDoubleExtra(LATITUDE_KEY, 0.0), getIntent().getDoubleExtra(LONGITUDE_KEY, 0.0));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        locationTextView = (TextView) findViewById(R.id.locationTextView);
        confirmLocationBt = (Button) findViewById(R.id.confirmLocationBt);
        loading = (ContentLoadingProgressBar) findViewById(R.id.loading);

        setConfirmLocationBtListener();
        if(getIntent().getStringExtra(ADDRESS_KEY) != null) {
            showLocationAddress(getIntent().getStringExtra(ADDRESS_KEY));
        }

        ThreadExecutor threadExecutor = JobExecutor.getInstance();
        PostExecutionThread postExecutionThread = UIThread.getInstance();
        presenter = new LocationPickerPresenter(new GetLocationAddressInteractorImpl(threadExecutor, postExecutionThread));
        presenter.attachView(this);

        // Create an instance of GoogleAPIClient.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }

    private void setConfirmLocationBtListener() {
        confirmLocationBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.confirmLocationButtonClicked();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        this.googleMap.getUiSettings().setCompassEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        } else {
            this.googleMap.setMyLocationEnabled(true);
        }
        this.googleMap.setOnMapClickListener(this);
        this.googleMap.setOnMyLocationButtonClickListener(this);

        if(selectedLocation.latitude != 0.0 || selectedLocation.longitude != 0.0) {
            onMapClick(selectedLocation);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        this.selectedLocation = latLng;
        presenter.locationSelected(this, selectedLocation);
        if (position == null) {
            position = googleMap.addMarker(new MarkerOptions().position(latLng)
                    .title(getString(R.string.selected_position)));
        } else {
            position.setPosition(latLng);
        }
        if(googleMap.getCameraPosition().zoom < 7.0f) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
        } else {
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        refreshLocation();
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(selectedLocation.latitude == 0.0 || selectedLocation.longitude == 0.0) {
            refreshLocation();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Timber.e("onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.e("onConnectionFailed");
    }

    private void refreshLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        onMapClick(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
    }

    @Override
    public void showLocationAddress(String address) {
        locationTextView.setText(address.trim());
    }

    @Override
    public void setPlaceResult(Place place) {
        Intent data = new Intent();
        data.putExtra(ADDRESS_KEY, place.getAddress().toString());
        data.putExtra(LATITUDE_KEY, place.getLatLng().latitude);
        data.putExtra(LONGITUDE_KEY, place.getLatLng().longitude);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void enableConfirmLocationButton(boolean enabled) {
        confirmLocationBt.setEnabled(enabled);
    }

    @Override
    public void showConnectionError() {
        Snackbar.make(findViewById(android.R.id.content), R.string.connection_error, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                })
                .show();
    }

    @Override
    public void showLoading() {
        loading.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        loading.setVisibility(View.GONE);
    }
}
