package com.example.linker.paulkhunaadmin.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.linker.paulkhunaadmin.R;
import com.example.linker.paulkhunaadmin.activity.MainActivity;
import com.example.linker.paulkhunaadmin.utils.PermissionUtils;
import com.example.linker.paulkhunaadmin.utils.RequestHandler;
import com.example.linker.paulkhunaadmin.utils.URLs;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LocationForm.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LocationForm#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocationForm extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    // get fields

    TextView txtLocationName, txtLocationDesc, txtLocationLat, txtLocationLong;
    Button btnGetLatLong, btnSaveLocation;

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    static Location mLastLocation;

    PermissionUtils permissionUtils;

    public LocationForm() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LocationForm.
     */
    // TODO: Rename and change types and number of parameters
    public static LocationForm newInstance(String param1, String param2) {
        LocationForm fragment = new LocationForm();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        permissionUtils = new PermissionUtils(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_location_form, container, false);

        txtLocationName = rootView.findViewById(R.id.txt_location_name);
        txtLocationDesc = rootView.findViewById(R.id.txt_location_desc);
        txtLocationLat = rootView.findViewById(R.id.txt_location_lat);
        txtLocationLong = rootView.findViewById(R.id.txt_location_long);

        btnGetLatLong = rootView.findViewById(R.id.btn_get_lat_long);
        btnSaveLocation = rootView.findViewById(R.id.btn_save_location);

        btnGetLatLong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strLat, strLong;

                if(isPermissionGranted()){
                    if(checkForLocationAndNetwork()){
                        mLastLocation = getLocation();

                        strLat = String.valueOf(mLastLocation.getLatitude());
                        strLong = String.valueOf(mLastLocation.getLongitude());

                        txtLocationLat.setText(strLat);
                        txtLocationLong.setText(strLong);
                    }
                }else{
                    permissionUtils.checkForPermissions(getActivity());
                }

            }
        });

        btnSaveLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkForErrors()){
                    //if it passes all the validations
                    final String strName = txtLocationName.getText().toString();
                    final String strDesc = txtLocationDesc.getText().toString();
                    final String strLat = txtLocationLat.getText().toString();
                    final String strLong = txtLocationLong.getText().toString();

                    class RegisterUser extends AsyncTask<Void, Void, String> {

                        private ProgressBar progressBar;

                        @Override
                        protected String doInBackground(Void... voids) {
                            //creating request handler object
                            RequestHandler requestHandler = new RequestHandler();

                            //creating request parameters
                            HashMap<String, String> params = new HashMap<>();
                            params.put("title", strName);
                            params.put("description", strDesc);
                            params.put("lat", strLat);
                            params.put("lon", strLong);

                            //returing the response
                            return requestHandler.sendPostRequest(URLs.URL_ADD_LOCATION, params);
                        }

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            //displaying the progress bar while data saves on the server
                            progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
                            progressBar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        protected void onPostExecute(String s) {
                            super.onPostExecute(s);
                            //hiding the progressbar after completion
                            progressBar.setVisibility(View.GONE);

                            try {
                                //converting response to json object
                                JSONObject obj = new JSONObject(s);

                                //if no error in response
                                if (obj.getInt("success") == 1) {

                                    Toast.makeText(getContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Some error occurred", Toast.LENGTH_SHORT).show();
                                }

                                txtLocationName.setText("");
                                txtLocationDesc.setText("");
                                txtLocationLat.setText("");
                                txtLocationLong.setText("");

                                txtLocationName.requestFocus();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    //executing the async task
                    RegisterUser ru = new RegisterUser();
                    ru.execute();
                }
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    // Error Checker

    public boolean checkForErrors(){
        txtLocationName.setError(null);
        txtLocationDesc.setError(null);
        txtLocationLat.setError(null);
        txtLocationLong.setError(null);

        String strName = txtLocationName.getText().toString();
        String strDesc = txtLocationDesc.getText().toString();
        String strLat = txtLocationLat.getText().toString();
        String strLong = txtLocationLong.getText().toString();

        boolean flag = true;

        if(TextUtils.isEmpty(strName)){
            txtLocationName.setError("The name can not be empty");
            flag = false;
            txtLocationName.requestFocus();
        }else if(TextUtils.isEmpty(strDesc)){
            txtLocationDesc.setError("The Description can not be empty");
            flag = false;
            txtLocationDesc.requestFocus();
        }else if(TextUtils.isEmpty(strLat) || !isFloat(strLat)){
            txtLocationLat.setError("The Latitude is either empty or invalid");
            flag = false;
            txtLocationLat.requestFocus();
        }else if(TextUtils.isEmpty(strLong) || !isFloat(strLong)){
            txtLocationLong.setError("The longitude is either empty or invalid");
            txtLocationLong.requestFocus();
            flag = false;
        }

        return flag;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLastLocation = getLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "+ connectionResult.getErrorCode());
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Method to display the location on UI
     * */

    public Location getLocation() {

        try
        {
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

            return mLastLocation;
        }
        catch (SecurityException e)
        {
            e.printStackTrace();

        }

        return null;

    }

    private boolean isPermissionGranted()
    {
        String permission = Manifest.permission.ACCESS_COARSE_LOCATION;
        int res = getContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public boolean checkForLocationAndNetwork() {
        final Context context = getContext();
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        if(!gps_enabled && !network_enabled) {
            // notify user
            final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage(context.getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(context.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(myIntent);
                    //get gps
                    //checkForLocationAndNetwork();
                }
            });
            dialog.setNegativeButton(context.getString(R.string.Cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Toast.makeText(context, "Location or GPS is not enabled.", Toast.LENGTH_LONG).show();
                }
            });
            dialog.show();
        }

        return gps_enabled && network_enabled;
    }

    //check for float

    public boolean isFloat(String number){
        try{
            Double n = Double.parseDouble(number);
        }catch (NumberFormatException n){
            return false;
        }

        return true;
    }
}
