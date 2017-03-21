package corposign.corposign;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import geofence.GeofenceTransitionsIntentService;
import geofence.SimpleGeofence;
import geofence.SimpleGeofenceStore;

public class Mapa extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback {

    List<Geofence> mGeofenceList;

    public static final String TAG = "CorpoSign";
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public static final long GEOFENCE_EXPIRATION_TIME = Geofence.NEVER_EXPIRE;
    public static final String ANDROID_BUILDING_ID = "1";
    public static final double ANDROID_BUILDING_LATITUDE = -8.058929;
    public static final double ANDROID_BUILDING_LONGITUDE = -34.951243;
    public static final float ANDROID_BUILDING_RADIUS_METERS = 40.0f;

    //public static final double ANDROID_BUILDING_LATITUDE = -8.083720;
    //public static final double ANDROID_BUILDING_LONGITUDE = -34.946909;

    public static String login;

    LatLng lat;

    private SimpleGeofence mAndroidBuildingGeofence;

    // Persistent storage for geofences.
    private SimpleGeofenceStore mGeofenceStorage;

    private LocationServices mLocationService;
    // Stores the PendingIntent used to request geofence monitoring.
    private PendingIntent mGeofenceRequestIntent;
    private GoogleApiClient mGoogleApiClient;
    private GeofencingRequest mGeofenceRequest;
    private static GoogleMap mMap;
    protected FirebaseDatabase database;
    static List<String> adapter;
    DatabaseReference myRef;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onResult(Result result) {

    }

    @Override
    public void onStop() {
        super.onStop();

        client.disconnect();
    }

    private enum REQUEST_TYPE {
        ADD
    }

    private REQUEST_TYPE mRequestType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        adapter = new ArrayList<>();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("geofence");

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                String value = dataSnapshot.getValue(String.class);
                adapter.add(value);
            }

            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            }

            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            public void onCancelled(DatabaseError error) {
            }

        });

        if (!isGooglePlayServicesAvailable()) {
            Log.e(TAG, "Google Play services indisponível.");
            finish();
            return;
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        login = bundle.getString("login");

        lat = new LatLng(ANDROID_BUILDING_LATITUDE, ANDROID_BUILDING_LONGITUDE);

        buildGoogleApiClient();

        mGeofenceStorage = new SimpleGeofenceStore(this);
        mGeofenceList = new ArrayList<Geofence>();

        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.fragmentoMapa)).getMap();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        createGeofences();

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        mGeofenceRequest = builder.build();

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void addMarkerForFence(SimpleGeofence fence) {
        if (fence == null) return;

        //BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.corposign);

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(fence.getLatitude(), fence.getLongitude()))
                .title("CorpoSign")
                //.icon(icon)
                .snippet("Raio: " + fence.getRadius() + "m")).showInfoWindow();

        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(fence.getLatitude(), fence.getLongitude()))
                .radius(fence.getRadius())
                .fillColor(0x40ff0000)
                .strokeColor(Color.TRANSPARENT)
                .strokeWidth(2);

        mMap.addCircle(circleOptions);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        //builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        builder.addGeofences(mGeofenceList);

        return builder.build();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();

        client.connect();
        mGoogleApiClient.connect();

        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Mapa Page", // TODO: Define a title for the content shown.
                Uri.parse("http://host/path"),
                Uri.parse("android-app://corposign.corposign/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofenceRequestIntent != null) {
            return mGeofenceRequestIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);

        Bundle bundle = new Bundle();
        bundle.putString("login",login);
        intent.putExtras(bundle);

        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static List<String> loginDataEvento(String banco){
        List<String> lista = new ArrayList<>();
        String parcial = "";
        for(int i=0; i<banco.length(); i++){
            if(banco.charAt(i) != ';') parcial += banco.charAt(i);
            else{
                lista.add(parcial);
                parcial = "";
            }
        }
        lista.add(parcial);

        return lista;
    }

    public static String jaEvento(String evento){
        String data = "dd/MM/yyyy";
        String hora = "h:mm a";
        String data1, hora1;
        java.util.Date agora = new java.util.Date();;
        SimpleDateFormat formata = new SimpleDateFormat(data);
        data1 = formata.format(agora);
        formata = new SimpleDateFormat(hora);
        hora1 = formata.format(agora);

        List<String> parcial;

        for(int i=0; i<adapter.size(); i++) {
            parcial = Mapa.loginDataEvento(adapter.get(i));
            if (parcial.get(0).equals(login) && parcial.get(1).equals(data1) && parcial.get(3).equals(evento)) {
                return "Calma, " + login + "! Você já " + evento.toLowerCase() +  " seu expediente às " + hora1 + "!";
            }
        }

        return "";
    }

    public void encerrarExpediente(View view){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("geofence");

        String data = "dd/MM/yyyy";
        String hora = "h:mm a";
        String data1, hora1;
        java.util.Date agora = new java.util.Date();;
        SimpleDateFormat formata = new SimpleDateFormat(data);
        data1 = formata.format(agora);
        formata = new SimpleDateFormat(hora);
        hora1 = formata.format(agora);

        if(!Mapa.jaEvento("Encerrou").equals("")) Toast.makeText(this,Mapa.jaEvento("Encerrou"), Toast.LENGTH_SHORT).show();
        else{
            DatabaseReference childRef = myRef.push();
            Toast.makeText(this,"Expediente encerrado às " + hora1 + "!", Toast.LENGTH_SHORT).show();
            childRef.setValue(login + ";" + data1 + ";" + hora1 + ";" + "Encerrou");
        }
    }

    public void iniciarExpediente(View view) throws SecurityException{
        List<String> parcial;

        String data = "dd/MM/yyyy";
        String hora = "h:mm a";
        String data1, hora1;
        java.util.Date agora = new java.util.Date();;
        SimpleDateFormat formata = new SimpleDateFormat(data);
        data1 = formata.format(agora);
        formata = new SimpleDateFormat(hora);
        hora1 = formata.format(agora);

        try{
        if(!jaEvento("Iniciou").equals("")) Toast.makeText(this,jaEvento("Iniciou"), Toast.LENGTH_SHORT).show();
        else{
                Toast.makeText(this, "Tentando entrar às " + hora1 + " do dia " + data1, Toast.LENGTH_SHORT).show();
                LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                        getGeofencingRequest(),
                        getGeofencePendingIntent()
                ).setResultCallback(this); // Result processed in onResult().
            if(jaEvento("Iniciou").equals("")) Toast.makeText(this,"Você está fora da GeoCerca, jovem!", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException securityException) {
            Toast.makeText(this, "Você não tem permissão de acessar a localização do dispositivo!", Toast.LENGTH_SHORT).show();
        }
    }

    public void createGeofences() {
        // Create internal "flattened" objects containing the geofence data.
        mAndroidBuildingGeofence = new SimpleGeofence(
                ANDROID_BUILDING_ID,                // geofenceId.
                ANDROID_BUILDING_LATITUDE,
                ANDROID_BUILDING_LONGITUDE,
                ANDROID_BUILDING_RADIUS_METERS,
                GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER// | Geofence.GEOFENCE_TRANSITION_EXIT
        );

        // Store these flat versions in SharedPreferences and add them to the geofence list.
        mGeofenceStorage.setGeofence(ANDROID_BUILDING_ID, mAndroidBuildingGeofence);
        mGeofenceList.add(mAndroidBuildingGeofence.toGeofence());

        addMarkerForFence(mAndroidBuildingGeofence);

        CameraPosition INIT =
                new CameraPosition.Builder()
                        .target(lat)
                        .zoom(17.5f)
                        .bearing(300F) // orientation
                        .tilt(50F) // viewing angle
                        .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(INIT));

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Exceção enquanto resolvendo conexão.", e);
            }
        } else {
            int errorCode = connectionResult.getErrorCode();
            Log.e(TAG, "Conexão com o Google Play services falhou com código de erro " + errorCode);
        }
    }

    /**
     * Once the connection is available, send a request to add the Geofences.
     */
    @Override
    public void onConnected(Bundle connectionHint) throws SecurityException {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, "Erro na conexão com a API do Google.", Toast.LENGTH_SHORT).show();
            return;
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        if (null != mGeofenceRequestIntent) {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, mGeofenceRequestIntent);
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        String resultCode = GooglePlayServicesUtil.GMS_ERROR_DIALOG;
        if (!resultCode.equals("")) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Google Play services is available.");
            }
            return true;
        } else {
            Log.e(TAG, "Google Play services is unavailable.");
            return false;
        }
    }


}
