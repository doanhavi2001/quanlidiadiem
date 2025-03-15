package com.example.baitap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private MapView map;
    private LocationManager locationManager;
    private EditText searchBar;
    private ImageButton currentLocationButton, favoritesButton, searchButton;
    private List<FavoriteLocation> favoriteLocations;
    private GeoPoint currentLocation;
    private Marker currentMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Khởi tạo OSMDroid
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);

        //Tham chiếu
        searchBar = findViewById(R.id.search_bar);
        currentLocationButton = findViewById(R.id.current_location_button);
        favoritesButton = findViewById(R.id.favorites_button);
        searchButton = findViewById(R.id.search_button);
        map = findViewById(R.id.map);

        //Cấu hình bản đồ
        setupMap();

        //Khởi tạo danh sách địa điểm yêu thích
        favoriteLocations = new ArrayList<>();
        addSampleFavoriteLocations();
        addFavoritesMarkersToMap();

        //Quản lý vị trí
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        checkLocationPermission();

        //Xử lý sự kiện button
        currentLocationButton.setOnClickListener(v -> goToCurrentLocation());
        favoritesButton.setOnClickListener(v -> showFavoriteLocations());
        searchButton.setOnClickListener(v -> searchLocation());
    }

    private void setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);
        map.getController().setCenter(new GeoPoint(21.028511, 105.804817)); // Hà Nội
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                onLocationChanged(lastKnownLocation);
            }
        }
    }

    private void goToCurrentLocation() {
        if (currentLocation != null) {
            // Di chuyển bản đồ đến vị trí hiện tại
            map.getController().animateTo(currentLocation);

            // Xóa marker cũ của vị trí hiện tại (nếu có)
            if (currentMarker != null) {
                map.getOverlays().remove(currentMarker);
            }

            // Tạo marker mới cho vị trí hiện tại
            currentMarker = new Marker(map);
            currentMarker.setPosition(currentLocation);
            currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            currentMarker.setTitle("Vị trí của bạn");
            currentMarker.setSnippet("Đây là vị trí hiện tại của bạn");

            // Thêm marker mới vào bản đồ
            map.getOverlays().add(currentMarker);
            map.invalidate(); // Cập nhật lại bản đồ
        } else {
            Toast.makeText(this, "Vị trí hiện tại chưa sẵn sàng", Toast.LENGTH_SHORT).show();
        }
    }
    private void searchLocation() {
        String query = searchBar.getText().toString().trim();
        if (!query.isEmpty()) {
            Toast.makeText(this, "Đang tìm kiếm: " + query, Toast.LENGTH_SHORT).show();
            map.getController().animateTo(new GeoPoint(21.028511, 105.804817));
        }
    }

    private void showFavoriteLocations() {
        if (!favoriteLocations.isEmpty()) {
            String[] locationNames = new String[favoriteLocations.size()];
            for (int i = 0; i < favoriteLocations.size(); i++) {
                locationNames[i] = favoriteLocations.get(i).getName();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Chọn địa điểm yêu thích")
                    .setItems(locationNames, (dialog, which) -> {
                        FavoriteLocation selectedLocation = favoriteLocations.get(which);
                        moveToLocation(selectedLocation);
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

            builder.create().show();
        } else {
            Toast.makeText(this, "Không có địa điểm yêu thích nào!", Toast.LENGTH_SHORT).show();
        }

    }
    private void moveToLocation(FavoriteLocation location) {
        GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
        map.getController().animateTo(point);
        map.getController().setZoom(17.0);
    }
    private void addFavoritesMarkersToMap() {
        for (FavoriteLocation favorite : favoriteLocations) {
            Marker marker = new Marker(map);
            marker.setPosition(new GeoPoint(favorite.getLatitude(), favorite.getLongitude()));
            marker.setTitle(favorite.getName());

            //Xử lý sự kiện khi nhấn vào marker
            marker.setOnMarkerClickListener((m, mapView) -> {
                showLocationDialog(favorite);
                return true;
            });

            map.getOverlays().add(marker);
        }
    }
    private void showLocationDialog(FavoriteLocation favorite) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_location_detail, null);
        builder.setView(view);

        ImageView imageView = view.findViewById(R.id.imageView);
        TextView textName = view.findViewById(R.id.location_name);
        TextView textAddress = view.findViewById(R.id.location_address);
        TextView textDescription = view.findViewById(R.id.location_description);
        //Hien thi thong tin dia diem
        textName.setText(favorite.getName());
        textAddress.setText(favorite.getAddress());
        textDescription.setText(favorite.getDescription());
        imageView.setImageResource(favorite.getImageUrl());

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }


    //Them thong tin dia diem thong tin yeu thich
    private void addSampleFavoriteLocations() {
        favoriteLocations.add(new FavoriteLocation("Nhà", "Hà Nội", "Nhà của tôi", 21.028511, 105.804817, R.drawable.nha_map));
        favoriteLocations.add(new FavoriteLocation("Công viên", "Hà Nội", "Công viên to nhất ", 22.028511, 106.804817, R.drawable.nha_map));
        favoriteLocations.add(new FavoriteLocation("Rạp", "Hà Nội", "Rạp coi Conan", 23.028511, 103.804817, R.drawable.nha_map));
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
    }
}
