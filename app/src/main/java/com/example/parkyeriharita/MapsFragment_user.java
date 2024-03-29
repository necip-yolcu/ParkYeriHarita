package com.example.parkyeriharita;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.common.primitives.Doubles;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

public class MapsFragment_user extends Fragment {

    DrawerLayout drawerLayout2;
    NavigationView navigationView2;
    ActionBarDrawerToggle actionBarDrawerToggle2;

    android.widget.SearchView search_bar;

    My_Adapter myAdapter;
    ListView listView;
    List<MyItems> listItems = new ArrayList<>();
    MyItems myItems1, myItems2;
    String park_adi, park_saat;
    int park_bos_sayi, park_fiyat;
    LinkedHashMap<String[], LinkedHashMap<String, Double>> park_list = new LinkedHashMap<>();
    LinkedHashMap<String, LatLng> list_latlng;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            googleMap.setMyLocationEnabled(true);
            UiSettings uis = googleMap.getUiSettings();
            uis.setCompassEnabled(true);
            uis.setMyLocationButtonEnabled(true);
            uis.setZoomControlsEnabled(true);

            final LatLng[] guncel_koord = new LatLng[1];
            //final PolylineOptions[] options = {new PolylineOptions().add(samandıra).add(samandıra).width(5).color(Color.BLUE).visible(true).geodesic(true)};
            //final Polyline[] polylineFinal = {mMap.addPolyline(options[0])};
            final boolean[] once = {true};
            googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    guncel_koord[0] = new LatLng(location.getLatitude(), location.getLongitude());
                    if (once[0]) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(guncel_koord[0], 15));
                        try {
                            for (String str : list_latlng.keySet())
                                googleMap.addMarker(new MarkerOptions().position(list_latlng.get(str)).title(str));
                        } catch (Exception ignored) {}
                        once[0] = false;
                    }

                    //polylineFinal[0].remove();
                    //options[0] = new PolylineOptions().add(samandıra).add(guncel_koord[0]).width(5).color(Color.BLUE).visible(true).geodesic(true);
                    //polylineFinal[0] = mMap.addPolyline(options[0]);


                    int count=0;
                    for (LinkedHashMap<String, Double> i : myItems1.getAll_info().values()) {
                        double[] latlng = Doubles.toArray(i.values());
                        double distance = SphericalUtil.computeDistanceBetween(guncel_koord[0], new LatLng(latlng[0], latlng[1]));

                        listItems.get(count).setKm(distance);
                        count++;
                        myAdapter.setMyItemsList(listItems);
                        listView.setAdapter(myAdapter);
                    }

                }
            });

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps_user, container, false);

        drawerLayout2 = view.findViewById(R.id.drawerlayout2);
        actionBarDrawerToggle2 = new ActionBarDrawerToggle(getActivity(), drawerLayout2, R.string.open, R.string.close);
        navigationView2 = view.findViewById(R.id.navView2);

        search_bar = view.findViewById(R.id.search_bar);
        listView = view.findViewById(R.id.listView);


        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        drawerLayout2.addDrawerListener(actionBarDrawerToggle2);
        actionBarDrawerToggle2.syncState();

        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        navigationView2.setItemIconTintList(null);
        navigationView2.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.item_share) {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_TEXT, "merhaba"); // uyg. adresi (googlePlay)
                    startActivity(Intent.createChooser(share, "Gönderiyi paylaş !! "));

                } else if (item.getItemId() == R.id.item_send_us) {
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ncpyolcu@gmail.com"});
                    emailIntent.setType("text/html");
                    emailIntent.setPackage("com.google.android.gm");
                    startActivity(Intent.createChooser(emailIntent, "E-mail Gönder"));

                } else if (item.getItemId() == R.id.item_rate) {
                    Toast.makeText(getActivity(), "..to Google Play", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

        search_bar.setFocusable(false);
        search_bar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try {
                    myAdapter.getFilter().filter(newText);
                } catch (Exception ignored) {}
                return true;
            }

        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("park")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                for (String yer : document.getData().keySet()) {
                                    System.out.println("çıktı yer " + yer);

                                    Object o1 = document.getData().get(yer);
                                    ObjectMapper om1 = new ObjectMapper();
                                    LinkedHashMap<String, Object> mappedObject1 = om1.convertValue(o1, LinkedHashMap.class);
                                    park_adi = (String) mappedObject1.get(" adı");
                                    park_saat = (String) mappedObject1.get("aktif saatler");
                                    park_bos_sayi = ((Long) mappedObject1.get("boş yer sayısı")).intValue();
                                    park_fiyat = ((Long) mappedObject1.get("0-1 saat")).intValue();
                                    String[] info = new String[]{park_adi, park_saat, String.valueOf(park_bos_sayi), String.valueOf(park_fiyat)};
                                    System.out.println("çıktı info " + Arrays.toString(info));

                                    Object o2 = mappedObject1.get("koordinat");
                                    ObjectMapper om2 = new ObjectMapper();
                                    LinkedHashMap<String, Double> mappedObject2 = om2.convertValue(o2, LinkedHashMap.class);
                                    System.out.println("çıktı koordinat " + mappedObject2);
                                    park_list.put(info, mappedObject2);

                                }
                            }
                            after_database();

                        } else {
                            Log.w("çıktıı", "Error getting documents.", task.getException());
                        }
                    }
                });


        //Window w = getActivity().getWindow(); w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        //getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        super.onViewCreated(view, savedInstanceState);

    }

    public void after_database() {
        myItems1 = new MyItems(null, null, 0, 0, 0, park_list);

        list_latlng = new LinkedHashMap<>();
        listItems.clear();
        for (Map.Entry<String[], LinkedHashMap<String, Double>> entry : myItems1.getAll_info().entrySet()) {
            double[] latlng = Doubles.toArray(entry.getValue().values());
            list_latlng.put(entry.getKey()[0], new LatLng(latlng[0], latlng[1]));
            double distance = SphericalUtil.computeDistanceBetween(new LatLng(latlng[0], latlng[1]), new LatLng(latlng[0], latlng[1]));

            myItems2 = new MyItems(entry.getKey()[0], entry.getKey()[1], Integer.parseInt(entry.getKey()[2]), Integer.parseInt(entry.getKey()[3]), distance, null);
            listItems.add(myItems2);
        }

        myAdapter = new My_Adapter(listItems);
        listView.setAdapter(myAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getActivity(), PagesActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle2.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public class My_Adapter extends BaseAdapter implements Filterable {
        List<MyItems> myItemsList;
        List<MyItems> myItemsListFiltered;

        public My_Adapter(List<MyItems> myItemsList) {
            this.myItemsList = myItemsList;
            this.myItemsListFiltered = myItemsList;
        }

        @Override
        public int getCount() {
            return myItemsListFiltered.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        public void setMyItemsList(List<MyItems> myItemsList) {
            this.myItemsList = myItemsList;
            notifyDataSetChanged();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            @SuppressLint({"ViewHolder", "InflateParams"})
            View view1 = getLayoutInflater().inflate(R.layout.row_items, null);

            TextView tx_yer_ismi = view1.findViewById(R.id.tx_yer_ismi);
            tx_yer_ismi.setText(myItemsListFiltered.get(position).getName());

            TextView tx_bos_yer_sayisi = view1.findViewById(R.id.tx_bos_yer_sayisi);
            tx_bos_yer_sayisi.setText(myItemsListFiltered.get(position).getBos_sayisi()+"");

            TextView tx_km = view1.findViewById(R.id.tx_km);
            tx_km.setText(String.format("%.2f", myItemsListFiltered.get(position).getKm()/1000) + " km");

            TextView tx_fee = view1.findViewById(R.id.tx_fee);
            tx_fee.setText(myItemsListFiltered.get(position).getFiyat()+"₺");

            TextView tx_duration = view1.findViewById(R.id.tx_duration);

            TextView tx_time = view1.findViewById(R.id.tx_time);
            tx_time.setText(myItemsListFiltered.get(position).getAktif_saat());

            ImageView imageView = view1.findViewById(R.id.imageView);
            imageView.setImageResource(R.drawable.park_entry);
            ImageView imageView2 = view1.findViewById(R.id.imageView2);
            imageView2.setImageResource(R.drawable.car_32);
            ImageView imageView3 = view1.findViewById(R.id.imageView3);
            imageView3.setImageResource(R.drawable.car_ph_32);

            return view1;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();

                    if (constraint == null && constraint.length() == 0) {
                        filterResults.count = myItemsList.size();
                        filterResults.values = myItemsList;
                    } else {
                        String searchStr = constraint.toString().toLowerCase();
                        List<MyItems> resultData = new ArrayList<>();

                        for (MyItems i : myItemsList) {
                            if (i.getName().toLowerCase().contains(searchStr))
                                resultData.add(i);

                            filterResults.count = resultData.size();
                            filterResults.values = resultData;
                        }

                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    myItemsListFiltered = (List<MyItems>) results.values;
                    notifyDataSetChanged();
                }
            };
        }
    }
}