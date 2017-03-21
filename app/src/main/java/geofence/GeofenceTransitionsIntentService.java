package geofence;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import corposign.corposign.Mapa;
import corposign.corposign.R;


public class GeofenceTransitionsIntentService extends IntentService {

    protected static final String TAG = "GeofenceTransitionsIS";
    protected FirebaseDatabase database;
    DatabaseReference myRef;

    public GeofenceTransitionsIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("geofence");

        if (geofencingEvent.hasError()) {
            String errorMessage = "Algum erro ocorreu!";
            Log.e(TAG, errorMessage);
            return;
        }

        Bundle bundle = intent.getExtras();
        String login = bundle.getString("login");

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER/* || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT*/) {

            DatabaseReference childRef = myRef.push();

            String data = "dd/MM/yyyy";
            String hora = "h:mm a";
            String data1, hora1;
            java.util.Date agora = new java.util.Date();;
            SimpleDateFormat formata = new SimpleDateFormat(data);
            data1 = formata.format(agora);
            formata = new SimpleDateFormat(hora);
            hora1 = formata.format(agora);

            if(Mapa.jaEvento("Iniciou").equals("")){
                childRef.setValue(login + ";" + data1 + ";" + hora1 + ";" + "Iniciou");
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

                String geofenceTransitionDetails = getGeofenceTransitionDetails(
                        this, geofenceTransition, triggeringGeofences
                );

                sendNotification(geofenceTransitionDetails);
                Log.i(TAG, geofenceTransitionDetails);
            }
        } else {
            // Log the error.
            Log.e(TAG, "Tipo inválido!");
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param context               The app context.
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
    private String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    private void sendNotification(String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), Mapa.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(Mapa.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.corposign)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.corposign))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "Entrou!";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "Saiu!";
            default:
                return "Não sabemos se entrou ou saiu!";
        }
    }
}