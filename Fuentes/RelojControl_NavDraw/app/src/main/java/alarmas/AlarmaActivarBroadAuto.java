package alarmas;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.example.camilo.relojcontrol_navdraw.MainActivity;
import com.example.camilo.relojcontrol_navdraw.R;

/**
 * Created by Camilo on 11-03-2015.
 */
public class AlarmaActivarBroadAuto extends BroadcastReceiver
{
    private Uri notf_sonido;
    private int minAntes;
    private SharedPreferences pref;

    @Override
    public void onReceive( Context context, Intent intent )
    {
        pref = context.getSharedPreferences( "alarma", Context.MODE_PRIVATE );
        minAntes = pref.getInt( "min_antes", 35 );

        // Sonido y notificación
        notf_sonido = RingtoneManager.getDefaultUri( RingtoneManager.TYPE_NOTIFICATION );
        Ringtone r = RingtoneManager.getRingtone( context, notf_sonido );
        r.play();
        notificacion( context );

        // Se setea que la configuración de la sincronización está lista
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt( "sincro_hoy", 0 );
        editor.commit();
    }

    private void notificacion( Context context )
    {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder( context )
                        .setSmallIcon( R.drawable.ic_stat_action_thumb_up )
                        .setContentTitle( "Reloj Control Serviu V" )
                        .setContentText( "Fin de jornada en: " + minAntes + " min");

        Intent resultIntent = new Intent( context, MainActivity.class );
        TaskStackBuilder stackBuilder = TaskStackBuilder.create( context );
        stackBuilder.addParentStack( MainActivity.class );

        stackBuilder.addNextIntent( resultIntent );
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent( resultPendingIntent );
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE );
        // mId allows you to update the notification later on.
        mNotificationManager.notify( 10, mBuilder.build() );
    }

}
