package alarmas;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.DateUtils;

import com.example.camilo.relojcontrol_navdraw.MainActivity;
import com.example.camilo.relojcontrol_navdraw.R;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Calendar;

import modelo.Persona;

/**
 * Created by soportev on 16-03-2015.
 */
public class CalcularHorasService extends IntentService
{
    public int horaR;
    public int minR;

    // La hora final
    public int hFinal;
    public int mFinal;
    public int minAntes;

    /**
     * Las preferencias
     */
    public SharedPreferences pref;
    public SharedPreferences pref_config;

    public CalcularHorasService(){ super("CalcularHorasService"); }

    @Override
    protected void onHandleIntent( Intent intent )
    {
        if  ( android.os.Debug.isDebuggerConnected() )
        {
            android.os.Debug.waitForDebugger();
        }

        pref_config = getSharedPreferences( "configs", MODE_PRIVATE );
        pref        = getSharedPreferences( "alarma", MODE_PRIVATE );
        int minAntesG = pref.getInt( "min_antes", 35 );

        // Si se seteo desde la alarma manual cuantos minutos le gusta al usuario que avise, se usan esos
        if ( minAntesG == 35 )
        {
            minAntes = 45;
        }
        else
        {
            minAntes = pref.getInt( "min_antes", 35 );
        }


        wsGetHoraEntrada();
    }

    /**
     * Permite cortar la hora inicial que ha ingresado el usuario y restarle el tiempo que
     * la alarma deberá de avisar antes de que esta suene según el radiobutton
     */
    private void calcularSalida()
    {
        long l_hora_inicial;
        long modificador_hora;
        long nueva_hora;

        Calendar cal    = Calendar.getInstance();
        cal.set( Calendar.HOUR_OF_DAY, horaR );
        cal.set( Calendar.MINUTE, minR );

        l_hora_inicial  = cal.getTimeInMillis();

        // Setear la hora para la jornada seleccionada por el usuario
        modificador_hora = DateUtils.HOUR_IN_MILLIS * getJornada();
        nueva_hora       = l_hora_inicial + modificador_hora;

        cal.setTimeInMillis( nueva_hora );
        l_hora_inicial = cal.getTimeInMillis();

        // Setear la hora de salida pero con el retraso seleccionado por el usuario
        modificador_hora    = DateUtils.MINUTE_IN_MILLIS * minAntes;
        nueva_hora          = l_hora_inicial - modificador_hora;
        cal.setTimeInMillis( nueva_hora );

        hFinal = cal.get( Calendar.HOUR_OF_DAY );
        mFinal = cal.get( Calendar.MINUTE );

        SharedPreferences sp = getSharedPreferences( "configs", MODE_PRIVATE );
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt( "min_antes", minAntes );

    }

    private void setAlarmaFinal()
    {
        AlarmManager manager            = (AlarmManager) this.getSystemService( Context.ALARM_SERVICE );
        Intent intSincroAuto            = new Intent( this , AlarmaActivarBroadAuto.class );
        PendingIntent pdIntAutomatico   = PendingIntent.getBroadcast( this , 0, intSincroAuto, 0 );

        /** Setear la alarma a la hora que ha elegido la persona **/
        Calendar calendar = Calendar.getInstance();

        // Se pone la hora final
        calendar.setTimeInMillis( System.currentTimeMillis() );
        calendar.set( Calendar.HOUR_OF_DAY, hFinal );
        calendar.set( Calendar.MINUTE, mFinal );

        manager.set( AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pdIntAutomatico );
        //manager.set( AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pdIntAutomatico );
    }

    /**
     * Permite armar la fecha del dia de hoy
     */
    private String armarFecha()
    {
        /** Setear la alarma a la hora que ha elegido la persona **/
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis( System.currentTimeMillis() );

        // Generar el String con la fecha del dia actual
        int ano_ac = calendar.get( Calendar.YEAR );
        int mes_ac = calendar.get( Calendar.MONTH );
        int dia_ac = calendar.get( Calendar.DAY_OF_MONTH );
        String fecha_actual = String.valueOf( dia_ac + "/" + (mes_ac + 1) + "/" + ano_ac );

        SharedPreferences pref = getSharedPreferences( "configs", MODE_PRIVATE );
        SharedPreferences.Editor editor = pref.edit();
        editor.putString( "fecha_actual", fecha_actual );
        editor.commit();

        return fecha_actual;
    }

    private int diaSemana()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis( System.currentTimeMillis() );

        // Obtener el dia de la semana
        return calendar.get( Calendar.DAY_OF_WEEK );
    }

    /**
     * Obtener la jornada dependiendo del dia en que nos encontremos
     *
     * @return la cantidad de horas de trabajo dependiendo del día de hoy
     */
    private int getJornada()
    {
        // Si es viernes
        if ( diaSemana() == 6 )
            return 8;
        else
        {
            // Si es de lunes a jueves
            if ( diaSemana() > 1 && diaSemana() < 6 )
                return 9;
        }
        return 0;
    }

    /**
     * Se conecta al WebService, se le entrega el rut de la persona y con la fecha del día de hoy
     * se consulta por la hora de ingreso al servicio
     */
    private void wsGetHoraEntrada()
    {
        Thread nt = new Thread()
        {
            // Definición de variables
            private Persona[] listaPersona;
            private int consulta_valida;
            private boolean conexion_correcta;
            private String hora_t;
            private SharedPreferences pref  = getSharedPreferences( "configs", Context.MODE_PRIVATE );
            private String rut              = pref.getString( "rut_in", "rut_no_seteado" );
            private String fecha_hoy;

            @Override
            public void run()
            {
                // Armar la fecha actual
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis( System.currentTimeMillis() );

                // Generar el String con la fecha del dia actual
                int ano_ac = calendar.get( Calendar.YEAR );
                int mes_ac = calendar.get( Calendar.MONTH );
                int dia_ac = calendar.get( Calendar.DAY_OF_MONTH );
                fecha_hoy = String.valueOf( dia_ac + "/" + (mes_ac + 1) + "/" + ano_ac );

                SharedPreferences.Editor editor = pref.edit();
                editor.putString( "fecha_actual", fecha_hoy );
                editor.commit();

                // Parametros de conexión
                String NAMESPACE 	= "http://serviu.android.cl/relojcontrol";
                //Para el emulador es 10.0.2.2
                //String URL 			= "http://10.5.5.247/RelojWS/RelojWS.asmx";
                //String URL 			= "http://desarrollo.serviuv.cl/RelojWS/RelojWS.asmx";
                String URL 			= "http://valpo.serviu.cl/RelojWS/RelojWS.asmx";
                String METHOD_NAME	= "getHoraEntrada";
                String SOAP_ACTION 	= "http://serviu.android.cl/relojcontrol/getHoraEntrada";

                SoapObject request = new SoapObject( NAMESPACE, METHOD_NAME );

                // Variables que se mandan al WS

                request.addProperty( "rut", rut );
                request.addProperty( "fecha_actual", fecha_hoy );

                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope( SoapEnvelope.VER11 );
                envelope.dotNet = true;
                envelope.setOutputSoapObject( request );
                HttpTransportSE transporte = new HttpTransportSE( URL );
                consulta_valida = 0;
                conexion_correcta = false;

                try
                {
                    transporte.call( SOAP_ACTION, envelope );
                    conexion_correcta = true;
                    // Lista para el resultado
                    SoapObject resSoap = (SoapObject) envelope.getResponse();
                    listaPersona = new Persona[resSoap.getPropertyCount()];
                    consulta_valida = resSoap.getPropertyCount();

                    if ( consulta_valida > 0 )
                    {
                        for ( int i = 0; i < listaPersona.length; i++ )
                        {
                            SoapObject ic = (SoapObject) resSoap.getProperty( i );

                            Persona obj = new Persona();
                            obj.setHora( ic.getProperty( 0 ).toString() );
                            listaPersona[i] = obj;
                        }
                    }

                    // Si la conexión es correcta
                    if ( conexion_correcta )
                    {
                        if ( consulta_valida > 0 )
                        {
                            for ( int i = 0; i < listaPersona.length; i++ )
                            {
                                hora_t = listaPersona[i].getHora();
                            }

                            horaR = Integer.parseInt( hora_t.substring( 0, 2 ) );
                            minR  = Integer.parseInt( hora_t.substring( 3, 5 ) );

                            calcularSalida();
                            setAlarmaFinal();
                        }
                        else
                        {
                            // Notificación de que no se ha marcado
                            notf_SinMarcacion();
                        }
                    }
                }
                catch ( IOException e )
                {
                    // Notificación de que no hay conexión a internet
                    notf_SinConexion();
                    e.printStackTrace();
                }
                catch ( XmlPullParserException e )
                {
                    notf_SinConexion();
                    e.printStackTrace();
                }
            }
        };
        nt.start();
    }


    /**
     * Notificaciones para mostrar erorres, este es el modo correcto de llamar a las notificaciones
     * desde los intent service, se reemplazan todos los context con "THIS" y debe de funcionar
     */
    private void notf_SinConexion()
    {
        if ( pref_config.getBoolean( "notf_error_activas", true ) )
        {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder( this )
                            .setSmallIcon( R.drawable.ic_stat_action_alarm )
                            .setContentTitle( "Reloj Control Serviu V" )
                            .setContentText( "Sin conexión a internet, No se pudo sincronizar" );

            Intent resultIntent = new Intent( this, MainActivity.class );
            TaskStackBuilder stackBuilder = TaskStackBuilder.create( this );
            stackBuilder.addParentStack( MainActivity.class );

            stackBuilder.addNextIntent( resultIntent );
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent( resultPendingIntent );
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
            // mId allows you to update the notification later on.
            mNotificationManager.notify( 10, mBuilder.build() );
        }
    }

    private void notf_SinMarcacion()
    {
        if ( pref_config.getBoolean( "notf_error_activas", true ) )
        {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder( this )
                            .setSmallIcon( R.drawable.ic_stat_action_alarm )
                            .setContentTitle( "Reloj Control Serviu V" )
                            .setContentText( "Sin marcacion de entrada, no se pudo sincronizar" );

            Intent resultIntent = new Intent( this, MainActivity.class );
            TaskStackBuilder stackBuilder = TaskStackBuilder.create( this );
            stackBuilder.addParentStack( MainActivity.class );

            stackBuilder.addNextIntent( resultIntent );
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent( resultPendingIntent );
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
            // mId allows you to update the notification later on.
            mNotificationManager.notify( 10, mBuilder.build() );
        }
    }

}
