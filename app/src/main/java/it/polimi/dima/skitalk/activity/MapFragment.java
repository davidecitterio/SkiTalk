package it.polimi.dima.skitalk.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Random;

import it.polimi.dima.model.User;
import it.polimi.dima.skitalk.R;


public class MapFragment extends Fragment {

    private MapView mMapView;
    private GoogleMap googleMap;
    private List<User> membersList;
    private User user;


    public MapFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        int idGroup = args.getInt("groupId");
        int idUser = args.getInt("userId");
        user = new User(idUser, getActivity(), true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                View marker = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.map_pointer, null);

                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                   //mMap.setMyLocationEnabled(true);
                } else {
                    //mMap.setMyLocationEnabled(true);
                }


                // --->
                UpdateMaps um = new UpdateMaps();
                um.execute();
            }
        });

        return rootView;
    }


    // Convert a view to bitmap
    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    public void setMembersList(List<User> membersList) {
        this.membersList = membersList;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private class UpdateMaps extends AsyncTask<String, Void, String> {

        String status, status1, status2;
        String time;


        @Override
        protected String doInBackground(String... params) {
            System.out.println("Mappa creata.");
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            for (int i=0; i< membersList.size(); i++){
                status = "Speed: "+membersList.get(i).getSpeed()+" km/h";
                status1 = "Altitude: "+membersList.get(i).getAltitude()+" m.a.s.l.";
                status2 = "Last Update: "+membersList.get(i).getLastUpdate();
                googleMap.addMarker(new MarkerOptions().position(new LatLng(membersList.get(i).getCoords().getLatitude(), membersList.get(i).getCoords().getLongitude()))
                        .title(membersList.get(i).getName().toUpperCase() + " " + membersList.get(i).getSurname().toUpperCase())
                        .visible(true)
                        .snippet(status+"\n"+status1+"\n"+status2)
                        .icon(BitmapDescriptorFactory.defaultMarker(new Random().nextInt(360)))

                );
                googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                    @Override
                    public View getInfoWindow(Marker arg0) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {

                        LinearLayout info = new LinearLayout(getContext());
                        info.setOrientation(LinearLayout.VERTICAL);

                        TextView title = new TextView(getContext());
                        title.setTextColor(Color.BLACK);
                        title.setGravity(Gravity.CENTER);
                        title.setTypeface(null, Typeface.BOLD);
                        title.setText(marker.getTitle());

                        TextView snippet = new TextView(getContext());
                        snippet.setTextColor(Color.GRAY);
                        snippet.setText(marker.getSnippet());

                        info.addView(title);
                        info.addView(snippet);

                        return info;
                    }
                });

                if (membersList.get(i).getId() == user.getId()){
                    LatLngBounds myplace = new LatLngBounds(
                            new LatLng(membersList.get(i).getCoords().getLatitude()-0.05, membersList.get(i).getCoords().getLongitude()-0.05), new LatLng(membersList.get(i).getCoords().getLatitude()+0.05, membersList.get(i).getCoords().getLongitude()+0.05));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(myplace, 0));
                }

            }

        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }



}
