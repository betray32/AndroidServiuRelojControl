package com.example.camilo.relojcontrol_navdraw;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Calendar;

import alarmas.AlarmaAutoBroad;

/**
 * Created by Camilo on 12/03/2015.
 */
public class F_Configuracion extends Fragment
{
    /**
     * el rootView es la vista con la cual se manejan los datos
     */
    public View rootView;
    public SharedPreferences pref;
    public SharedPreferences.Editor editor;
    public ToggleButton toggleAlarma;
    public ToggleButton toggleNotf;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        rootView = inflater.inflate( R.layout.f_configuracion, container, false );

        // Las preferencias
        pref = getActivity().getSharedPreferences( "configs", Context.MODE_PRIVATE );
        editor = pref.edit();

        // Setear el rut guardado en el label
        TextView lbRut = (TextView) rootView.findViewById( R.id.lbRutIngresado );
        lbRut.setText( pref.getString( "rut_in", "rut_no_seteado" ) );

        rootView.findViewById( R.id.btnCambiarRutConfig ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                cambiarRutConfig();
            }
        } );

        rootView.findViewById( R.id.btnResetAyudas ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                btnResetAyudas();
            }
        } );

        // ToggleAlarma
        toggleAlarma = (ToggleButton) rootView.findViewById( R.id.toggleAlarma );
        toggleAlarma.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                btnToggleSincro();
            }
        } );

        // Toggle Notificaciones
        toggleNotf = (ToggleButton) rootView.findViewById( R.id.toggleNotificaciones );
        toggleNotf.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                btnToggleNotificaciones();
            }
        } );

        checkAlarmaAuto();
        checkNotfs();

        // Devolver la vista
        return rootView;
    }

    /**
     * Comprueba si las notificaciones están activas desde antes
     */
    private void checkNotfs()
    {
        if ( pref.getBoolean( "notf_error_activas", true ) )
            toggleNotf.setChecked( true );
        else
            toggleNotf.setChecked( false );
    }

    /**
     * El boton para activar o desactivar las notificaciones en caso de que falle las sincronización
     * automática: esta puede fallar por ej; en caso de que cuando intente sincronizar a la hora
     * seleccionada el teléfono no esté conectado a internet o si la persona en la mañana cuando
     * tubo que marcar no lo hizo
     */
    private void btnToggleNotificaciones()
    {
        if ( pref.getBoolean( "primera_notifica_ac", false ) )
        {
            // Comprueba si está activo
            if ( toggleNotf.isChecked() )
            {
                editor.putBoolean( "notf_error_activas", true );
                Toast.makeText( getActivity(), "Notificaciones de error activas", Toast.LENGTH_SHORT ).show();
            }
            else
            {
                editor.putBoolean( "notf_error_activas", false );
                Toast.makeText( getActivity(), "Notificaciones de error desactivadas", Toast.LENGTH_SHORT ).show();
            }

            editor.commit();
        }
        else
        {
            popupNoticaciones();
        }

    }

    private void popupNoticaciones()
    {
        // Get prompts.xml view
        LayoutInflater li = LayoutInflater.from( getActivity() );
        View promptsView = li.inflate( R.layout.popupnotfauto, null );

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity() );

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView( promptsView );

        // Setear el mensaje
        alertDialogBuilder
                .setCancelable( false )
                .setPositiveButton( "Aceptar",
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick( DialogInterface dialog, int id )
                            {
                                editor.putBoolean( "primera_notifica_ac", true );
                                editor.commit();
                                btnToggleNotificaciones();
                            }
                        } );

        // Crear el dialogo de alerta
        AlertDialog alertDialog = alertDialogBuilder.create();

        // Mostrar dialogo
        alertDialog.show();
    }

    /**
     * Comprobar si la alarma automática
     */
    private void checkAlarmaAuto()
    {
        if ( pref.getInt( "alarma_auto", 0 ) == 1 )
            toggleAlarma.setChecked( true );
        else
            toggleAlarma.setChecked( false );
    }

    /**
     * Botón para poder resetear las ayudas
     */
    private void btnResetAyudas()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
        builder.setTitle( R.string.config_popupResetear );
        builder.setIcon( R.drawable.ic_navigation_cancel_manual );
        builder.setPositiveButton( android.R.string.yes, new DialogInterface.OnClickListener()
        {
            // Si la respuesta es SI
            public void onClick( DialogInterface dialog, int id )
            {
                // LA sincronizacion
                editor.putInt( "sincro_on_primera_a", 0 );

                // Popup de la sincronización
                editor.putInt( "sincro_on_primera", 0 );

                // Popup alarma manual
                editor.putInt( "manual_primera", 0 );

                // Popup notificaciones alarma
                editor.putBoolean( "primera_notifica_ac", false );

                editor.commit();

                AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
                builder.setTitle( "Aviso" );
                builder.setIcon( R.drawable.ic_action_action_account_box );
                builder.setMessage( "Se ha reseteado los mensajes de ayuda, active la alarma automática nuevamente en caso de haberla tenido activa" );
                builder.setPositiveButton( "Aceptar", null );
                builder.create();
                builder.show();
            }
        } );
        builder.setNegativeButton( android.R.string.cancel, new DialogInterface.OnClickListener()
        {
            // Si la respuesta es NO
            public void onClick( DialogInterface dialog, int id )
            {
                dialog.dismiss();
            }
        } );
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * DEMO
     */
    private void demo()
    {
        AlarmManager manager = (AlarmManager) getActivity().getSystemService( Context.ALARM_SERVICE );
        Intent intSincroAuto = new Intent( getActivity(), AlarmaAutoBroad.class );
        PendingIntent pdIntAutomatico = PendingIntent.getBroadcast( getActivity(), 0, intSincroAuto, 0 );

        manager.cancel( pdIntAutomatico );
        manager.setRepeating( AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_DAY, pdIntAutomatico );
    }

    /**
     * Comprueba si la Sincronización Automática está funcionando
     */
    private void btnToggleSincro()
    {
        // Este se activa si le ha aparecido el popup
        if ( pref.getInt( "sincro_on_primera_a", 0 ) == 1 )
        {
            // Si esta activada
            if ( toggleAlarma.isChecked() )
            {
                // Valida que tenga un rut ingresado
                if ( pref.getInt( "rut_validado", 0 ) == 1 )
                {
                    activarSincro();
                    Toast.makeText( getActivity(), "Sincronización Automática Activada", Toast.LENGTH_SHORT ).show();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
                    builder.setTitle( "Error" );
                    builder.setMessage( "Debe tener asociado un rut a la aplicación" );
                    builder.setPositiveButton( "Aceptar", null );
                    builder.create();
                    builder.show();
                }
            }
            else
            {
                desactivarSincro();
                Toast.makeText( getActivity(), "Sincronización Automática Desactivada", Toast.LENGTH_SHORT ).show();
            }
        }
        else
        {
            sincroAutoCheck();
        }
    }

    /**
     * El principal problema que resuelve este método esque si la persona recien activa
     * la sincronización por ejemplo a las 14:00hr y está seteado que todos los dias esta se
     * actualize a las 10:45, este calcula que es una hora que ha pasado entonces se setea directamente
     */
    private void activarSincro()
    {
        int hora_sincro = 10;
        int min_sincro  = 45;

        AlarmManager manager = (AlarmManager) getActivity().getSystemService( Context.ALARM_SERVICE );
        Intent intSincroAuto = new Intent( getActivity(), AlarmaAutoBroad.class );
        PendingIntent pdIntAutomatico = PendingIntent.getBroadcast( getActivity(), 0, intSincroAuto, 0 );

        Calendar calendar = Calendar.getInstance();
        // Si es menor a 0 es una hora que ha pasado; entonces se setea para el proximo día
        if ( difTiempo() < 0 )
        {
            calendar.setTimeInMillis( System.currentTimeMillis() + DateUtils.DAY_IN_MILLIS );
            calendar.set( Calendar.HOUR_OF_DAY, 10 );
            calendar.set( Calendar.MINUTE, 16 );

            // calendar.setTimeInMillis( System.currentTimeMillis() ); Prueba
            AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
            builder.setTitle( "Mensaje de aviso" );
            builder.setIcon( R.drawable.ic_action_action_account_box );
            builder.setMessage( "La alarma automática comenzará a funcionar desde mañana; " +
                    "si desea usar la alarma hoy activela manualmente en el menú de la aplicación" );
            builder.setPositiveButton( "Aceptar", null );
            builder.create();
            builder.show();
        }
        else
        {
            // Sino es una hora que aun falta entonces se setea directamente
            calendar.setTimeInMillis( System.currentTimeMillis() );
            calendar.set( Calendar.HOUR_OF_DAY, hora_sincro );
            calendar.set( Calendar.MINUTE, min_sincro );
        }

        manager.cancel( pdIntAutomatico );
        manager.setInexactRepeating (AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pdIntAutomatico );
        editor.putInt( "alarma_auto", 1 );
        editor.commit();
        //manager.set( AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pdIntAutomatico ); pruebas
    }

    /**
     * Compara las horas de la sincronización y la actual
     *
     * @return
     */
    private long difTiempo()
    {
        int hora_sincro = 10;
        int min_sincro = 45;

        long horaTarget = 0;
        long tiempoActual = 0;
        long diffTiempo = 0;

        // Obtengo la instancia y seteo mi hora de destino
        Calendar calendar = Calendar.getInstance();
        calendar.set( Calendar.HOUR_OF_DAY, hora_sincro );
        calendar.set( Calendar.MINUTE, min_sincro );

        // Obtengo en milisegundos la hora de destino
        horaTarget = calendar.getTimeInMillis();

        // Obtengo en milisegundos la hora actual
        tiempoActual = System.currentTimeMillis();

        // Se calcula la diferencia entre la hora de destino y la actual
        diffTiempo = horaTarget - tiempoActual;

        return diffTiempo;
    }

    /**
     * Desactiva la alarma
     */
    private void desactivarSincro()
    {
        Intent intSincroAuto = new Intent( getActivity(), AlarmaAutoBroad.class );
        PendingIntent pdIntAutomatico = PendingIntent.getBroadcast( getActivity(), 0, intSincroAuto, 0 );
        pdIntAutomatico.cancel();
        editor.putInt( "alarma_auto", 0 );
        editor.commit();
    }

    private void cambiarRutConfig()
    {
        // Get prompts.xml view
        LayoutInflater li = LayoutInflater.from( getActivity() );
        View promptsView = li.inflate( R.layout.popupcambiarut, null );

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity() );

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView( promptsView );

        final EditText userInput = (EditText) promptsView
                .findViewById( R.id.txtRutInConfig );

        // Setear el mensaje
        alertDialogBuilder
                .setCancelable( false )
                .setPositiveButton( "Guardar",
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick( DialogInterface dialog, int id )
                            {
                                // Si está vacio
                                if ( ( userInput.getText().toString() ).equals( "" ) )
                                {
                                    Toast.makeText( getActivity(), "Campo vacio, debe ingresar un rut", Toast.LENGTH_SHORT ).show();
                                    cambiarRutConfig();
                                }
                                else
                                {
                                    // Si el largo del input es menor que 8
                                    if ( ( ( userInput.getText().toString() ).length() ) < 7 )
                                    {
                                        Toast.makeText( getActivity(), "El Rut ingresado es inválido", Toast.LENGTH_SHORT ).show();
                                        cambiarRutConfig();
                                    }
                                    else
                                    {
                                        editor = pref.edit();

                                        // Se guarda el rut ingresado
                                        editor.putString( "rut_in", userInput.getText().toString() );
                                        editor.commit();

                                        // Poner el rut ingresado en el label
                                        TextView rut_in = (TextView) rootView.findViewById( R.id.lbRutIngresado );
                                        rut_in.setText( userInput.getText().toString() );
                                        Toast.makeText( getActivity(), "Rut cambiado exitosamente", Toast.LENGTH_SHORT ).show();
                                    }
                                }
                            }
                        } )

                .setNegativeButton( "Cancelar",
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick( DialogInterface dialog, int id )
                            {
                                dialog.cancel();
                            }
                        } );

        // Crear el dialogo de alerta
        AlertDialog alertDialog = alertDialogBuilder.create();

        // Mostrar dialogo
        alertDialog.show();
    }

    /**
     * Comprueba si tiene activada la configuración automática
     */
    private void sincroAutoCheck()
    {
        // Si es primera ves que le pregunta por la sincro y si además esta desactivada la sincro
        if ( pref.getInt( "sincro_on_primera", 0 ) == 0 )
        {
            // Get prompts.xml view
            LayoutInflater li = LayoutInflater.from( getActivity() );
            View promptsView = li.inflate( R.layout.popupalarmaauto, null );

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    getActivity() );

            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView( promptsView );

            // Setear el mensaje
            alertDialogBuilder
                    .setCancelable( false )
                    .setIcon( R.drawable.ic_action_alarm_manual )
                    .setPositiveButton( "Activar",
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick( DialogInterface dialog, int id )
                                {
                                    SharedPreferences.Editor editor = pref.edit();
                                    // Se guarda el rut ingresado
                                    editor.putInt( "sincro_on_primera_a", 1 );
                                    editor.putInt( "sincro_on_primera", 1 );
                                    editor.commit();
                                    btnToggleSincro();
                                }
                            } )

                    .setNegativeButton( "Cancelar",
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick( DialogInterface dialog, int id )
                                {
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putInt( "sincro_on_primera_a", 1 );
                                    editor.putInt( "sincro_on_primera", 1 );
                                    editor.commit();

                                    // APAGAR BOTON
                                    toggleAlarma.setChecked( false );
                                    dialog.cancel();
                                }
                            } );

            // Crear el dialogo de alerta
            AlertDialog alertDialog = alertDialogBuilder.create();

            // Mostrar dialogo
            alertDialog.show();
        }
    }

}
