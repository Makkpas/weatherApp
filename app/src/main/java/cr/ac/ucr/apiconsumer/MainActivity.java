package cr.ac.ucr.apiconsumer;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.AppBarLayout;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cr.ac.ucr.apiconsumer.api.RetrofitBuilder;
import cr.ac.ucr.apiconsumer.api.WeatherService;
import cr.ac.ucr.apiconsumer.models.Main;
import cr.ac.ucr.apiconsumer.models.Sys;
import cr.ac.ucr.apiconsumer.models.Weather;
import cr.ac.ucr.apiconsumer.models.WeatherResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements LocationListener  {

    private final String TAG = "MainActivity";
    private  final int LOCATION_CODE_REQUEST = 1;
    private  final int REQUEST_CODE = 36;

    private TextView tvGreetings;
    private TextView tvTemperature;
    private TextView tvDescription;
    private TextView tvMinMax;
    private TextView tvCity;

    private ImageView ivImage;

    private ConstraintLayout clContainer;

    private String day;
    private LocationManager locationManager;
    private double latitude;
    private double longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitude = 8.7422451;
        longitude = -82.9468766;

        AppBarLayout ablAppBarLayout = findViewById(R.id.abl_appbar);
        ablAppBarLayout.setOutlineProvider(null);
        ablAppBarLayout.setElevation(0);

        Toolbar tToolbar = findViewById(R.id.t_toolbar);
        tToolbar.setTitle("");
        setSupportActionBar(tToolbar);



        clContainer = findViewById(R.id.cl_container);

        tvGreetings = findViewById(R.id.tv_greeting);
        tvTemperature = findViewById(R.id.tv_temperature);
        tvDescription = findViewById(R.id.tv_description);
        tvMinMax = findViewById(R.id.tv_MinMax);
        tvCity = findViewById(R.id.tv_city);

        ivImage = findViewById(R.id.iv_image);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        checkPermission();

        setBackgroundAndGreeting();

//        getWeather(latitude, longitude);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.im_open_search:
                opemSearchActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void opemSearchActivity() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivityForResult(intent, REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
                String city = data.getStringExtra(SearchActivity.KEY);
                Log.i(TAG, "City: "+city);

                LatLng latlon = getLocationFromAddress(city);

                if(latlon != null){
                    Log.i(TAG, latlon.latitude + "------" + latlon.longitude);
                    getWeather(latlon.latitude, latlon.longitude);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "onActivityResult: Catch" + e.getMessage().toString());
        }
    }

    private LatLng getLocationFromAddress(String city) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> address;
        LatLng latLng = null;

        try {
            address = geocoder.getFromLocationName(city, 5);
            if (address == null){
                return  null;
            }

            Address location = address.get(0);
            latLng = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (IOException e){
            Log.e(TAG, "Error" + e.getMessage().toString());
        }
        return  latLng;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_CODE_REQUEST){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                checkPermission();
            } else {
                getWeather(latitude, longitude);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void checkPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    },
                        LOCATION_CODE_REQUEST
                );
                return;
            }
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        try {
            if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                onLocationChanged(location);
            }else{
                new AlertDialog.Builder(this)
                        .setMessage("Para una mejor funcionalidad active la localización")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                getWeather(latitude, longitude);
//                Log.i(TAG, "No está activado");
            }
        } catch (Exception e){
//            Log.i(TAG, "Hubo un error"+e);
            getWeather(latitude, longitude);
        }
    }


    public void setBackgroundAndGreeting() {
        Calendar calendar = Calendar.getInstance();
        int timeOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay >= 5 && timeOfDay <12){
            tvGreetings.setText(R.string.day);
            clContainer.setBackgroundResource(R.drawable.background_day);
        }else if(timeOfDay > 12  && timeOfDay <19){
            tvGreetings.setText(R.string.afternoon);
            clContainer.setBackgroundResource(R.drawable.background_afternoon);
        }else{
            tvGreetings.setText(R.string.night);
            clContainer.setBackgroundResource(R.drawable.background_night);
        }
        day = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());

    }

    private void getWeather(double latitude, double longitude) {
        WeatherService service = RetrofitBuilder.createService(WeatherService.class);

        Call<WeatherResponse> response = service.getWeatherByCoordinates(latitude, longitude);

        final AppCompatActivity activity = this;

        response.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {

                Log.i(TAG, String.valueOf(call.request().url()));

                if (response.isSuccessful()) {
                    WeatherResponse weatherResponse = response.body();

                    Main main = weatherResponse.getMain();
                    List<Weather> weatherList = weatherResponse.getWeather();
                    Sys sys = weatherResponse.getSys();

                    String temperature = getString(R.string.temperature, String.valueOf(Math.round(main.getTemp())));

                    tvTemperature.setText(temperature);

                    String minmax = getString(R.string.minmax, String.valueOf(Math.round(main.getTemp_min())), String.valueOf(Math.round(main.getTemp_max())));

                    tvMinMax.setText(minmax);

                   if(weatherList.size() > 0){
                       Weather weather = weatherList.get(0);

                       tvDescription.setText(String.format("%s, %s",day.substring(0,1).toUpperCase()+day.substring(1).toLowerCase(), weather.getDescription().substring(0,1).toUpperCase()+weather.getDescription().substring(1).toLowerCase()));

                       String imageUrl = String.format("https://openweathermap.org/img/wn/%s@2x.png", weather.getIcon());

                       RequestOptions options = new RequestOptions()
                               .placeholder(R.mipmap.ic_launcher)
                               .error(R.mipmap.ic_launcher)
                               .centerCrop()
                               .diskCacheStrategy(DiskCacheStrategy.ALL)
                               .priority(Priority.HIGH);

                       Glide.with(activity)
                               .load(imageUrl)
                               .into(ivImage);
                   }

                   tvCity.setText(String.format("%s, %s", weatherResponse.getName(), sys.getCountry()));


                } else {
                    Log.e(TAG, "OnError: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                throw new RuntimeException(t);
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        getWeather(latitude, longitude);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}