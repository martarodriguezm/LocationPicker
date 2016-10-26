package rodriguez.marta.locationpickersample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rodriguez.marta.locationpicker.ui.LocationPickerActivity;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PICKER_CODE = 3843;

    @BindView(R.id.selectedLocation)
    TextView selectedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.selectLocationBt)
    public void selectLocationBtClicked() {
        startActivityForResult(LocationPickerActivity.getCallingIntent(this), LOCATION_PICKER_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if(requestCode == LOCATION_PICKER_CODE) {
                Place place = LocationPickerActivity.getPlace(data);
                selectedLocation.setText(place.getAddress());
            }
        }
    }
}
