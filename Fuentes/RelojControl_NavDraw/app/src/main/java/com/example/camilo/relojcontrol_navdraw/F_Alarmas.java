package com.example.camilo.relojcontrol_navdraw;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import alarmas.AlarmaManualBroad;
import modelo.Persona;

/**
 * Created by Camilo on 28/02/2015.
 * Esta clase corresponde a la
 */
public class F_Alarmas extends Fragment
{
    /**
     * El shared preferences para guardar la hora elegida por el usuario para la alarma
     */
    public SharedPreferences prefConfig;
    public SharedPreferences prefAlarma;

    /**
     * Captura de variables de los elementos del layout
     */
    public TextView horaInicio;
    public TextView minInicio;
    public TextView horaFinal;
    public TextView minFinal;
    public RadioGroup jornadaRadioG;
    public View rootView;

    /**
     * Las elecciones del usuario
     */
    private int hrInicio;
    private int mnInicio;
    private int hrFin;
    private int minFin;

    private int numDiaActual;

    /**
     * Intent para la alarma y dejarla corriendo en segundo plano
     */
    private PendingIntent pdIntAlarmaManual;
    private AlarmManager manager;

    /**
     * Dialogo para mostrar cuando se esté actualizando los datos
     */
    public ProgressDialog pDialog;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState )
    {
        rootView = inflater.inflate( R.layout.f_alarmas, container, false );

        // Desactiva el RadioButtonGroup
        jornadaRadioG = (RadioGroup) rootView.findViewById( R.id.radioGroupJornada );
        for ( int i = 0; i < jornadaRadioG.getChildCount(); i++ )
            jornadaRadioG.getChildAt( i ).setEnabled( false );

        // Setea los radiobuttons dependiendo del dia
        RadioButton jornada_9 = (RadioButton) rootView.findViewById( R.id.raJornada9 );
        RadioButton jornada_8 = (RadioButton) rootView.findViewById( R.id.raJornada8 );
        getNumeroDiaActual();

        // Si el día es viernes
        if ( numDiaActual == 6 )
            jornada_8.setChecked( true );
        else
        {
            // Si es de lunes a jueves
            if ( numDiaActual > 1 && numDiaActual < 6 )
                jornada_9.setChecked( true );

            // Si es fin de semanas
            else if ( numDiaActual == 1 || numDiaActual == 7 )
            {
                Toast.makeText( getActivity(), "La alarma no funciona los fin de semanas", Toast.LENGTH_LONG ).show();
            }
        }

        horaInicio = (TextView) rootView.findViewById( R.id.lbHoraTarget );
        minInicio  = (TextView) rootView.findViewById( R.id.lbMinTarget );
        horaFinal  = (TextView) rootView.findViewById( R.id.lbTargetHoraSalida );
        minFinal   = (TextView) rootView.findViewById( R.id.lbTargetMinutoSalida );

        // Seteo de dias
        TextView diaActual = (TextView) rootView.findViewById( R.id.lbDiaActual );
        diaActual.setText( setDiaActual() );

        // Setear las horas iniciales guardadas
        prefHoraElegida();

        // Intent para la alarma con botón
        Intent intAlarmaManual = new Intent( getActivity(), AlarmaManualBroad.class );
        pdIntAlarmaManual = PendingIntent.getBroadcast( getActivity(), 0, intAlarmaManual, PendingIntent.FLAG_CANCEL_CURRENT );
        manager = (AlarmManager) getActivity().getSystemService( Context.ALARM_SERVICE );
        /************************************************************************/

        // Dialogo para mostrar
        pDialog = new ProgressDialog( getActivity() );

        // Enviar y setear alarma
        rootView.findViewById( R.id.btnSetAlarma ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                btnEnviarAlarm( v );
            }
        } );

        // Cambiar la hora
        rootView.findViewById( R.id.btnActualizarHora ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                btnUpdateHora();
            }
        } );


        rootView.findViewById( R.id.btnCancelAlarma ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                btnCancelarAlarm( v );
           }
        } );

        comprobarPOPUP();

        // Devolver el control a la interfaz
        return rootView;
    }

    /**
     * Le permite al usuario mostrar el popup si es la primera ves que abre esta pestaña
     */
    private void comprobarPOPUP()
    {
        prefConfig = getActivity().getSharedPreferences( "configs", Context.MODE_PRIVATE );

        // Si es la primera ves que se hace ingreso a la aplicación se despliega el popup para guardar el rut
        if ( prefConfig.getInt( "manual_primera", 0 ) == 0 )
        {

            // Get prompts.xml view
            LayoutInflater li = LayoutInflater.from( getActivity() );
            View promptsView = li.inflate( R.layout.popupalarmamanual, null );

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    getActivity() );

            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView( promptsView );

            final EditText userInput = (EditText) promptsView
                    .findViewById( R.id.editTextDialogUserInput );

            // Setear el mensaje
            alertDialogBuilder
                    .setCancelable( false )
                    .setPositiveButton( "Aceptar",
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick( DialogInterface dialog, int id )
                                {
                                    SharedPreferences.Editor editor = prefConfig.edit();
                                    editor.putInt( "manual_primera", 1 );
                                    editor.commit();
                                }
                            } );

            // Crear el dialogo de alerta
            AlertDialog alertDialog = alertDialogBuilder.create();

            // Mostrar dialogo
            alertDialog.show();
        }
    }

    /**
     * Setea el dia actual en el txtDiaActual de la interfaz
     */
    private String setDiaActual()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis( System.currentTimeMillis() );

        // Obtener el nombre del dia
        String dia_actual = calendar.getDisplayName( Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault() );

        // Obtener la fecha y darle el formato
        SimpleDateFormat df = new SimpleDateFormat( "dd-MMM-yyyy" );
        String fechaFormateada = df.format( calendar.getTime() );

        return dia_actual + " " + fechaFormateada;
    }

    /**
     * Obtiene el dia actual sobre el cual el sistema esta trabajando en modo de entero
     *
     * Dias en numeros
     * SUNDAY =1, MONDAY =2, TUESDAY =3, WEDNESDAY =4, THURSDAY =5, FRIDAY =6, and SATURDAY =7.
     *
     * Recordatorio
     * Dias en letras
     * Sunday - (Domingo) Monday - (Lunes) Tuesday - (Martes)  Wednesday - (Miércoles)
     * Thursday - (Jueves) Friday - (Viernes) Saturday - (Sábado)
     * @return
     */
    private void getNumeroDiaActual()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis( System.currentTimeMillis() );
        numDiaActual = calendar.get( Calendar.DAY_OF_WEEK );
    }

    /**
     * Acción para el botón de enviar la alarma de la interfaz
     *
     * @param v
     */
    public void btnEnviarAlarm( View v )
    {
        // Si es dentro de la semana
        if ( numDiaActual > 1 && numDiaActual < 7 )
        {
            new asyncSetAlarma().execute();
        }
        else
        {
            // Error de conexión
            AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
            builder.setTitle( "Mensaje de aviso" );
            builder.setMessage( "La alarma del Reloj Control no funciona los finde semanas" );
            builder.setPositiveButton( "Aceptar", null );
            builder.create();
            builder.show();
        }
    }

    /**
     * Esta aclase AsyncTask permite al usuario obtener los datos de la hora que marco al inicio
     * de la mañana y si esque marcó setea la alarma dependiendo del día (la cantidad de horas que
     * tiene la jornada) y los minutos antes
     */
    private class asyncSetAlarma extends AsyncTask< Void, Void, Boolean>
    {
        private ProgressDialog dialogo;

        // Definición de variables
        private Persona[] listaPersona;
        private int consulta_valida;
        private boolean conexion_correcta;

        private String hora_t;
        private String rut;
        private String fecha_hoy;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            // Modo privado
            prefConfig = getActivity().getSharedPreferences( "configs", Context.MODE_PRIVATE );
            rut = prefConfig.getString( "rut_in", "rut_no_ingresado" );
            fecha_hoy = armarFecha();

            // Permite guardar las preferencias que ha seteado el usuario anteriormente
            prefAlarma = getActivity().getSharedPreferences( "alarma", Context.MODE_PRIVATE );
            SharedPreferences.Editor editor = prefAlarma.edit();

            // Hora de entrada
            editor.putString( "hora_entrada", horaInicio.getText().toString() );
            editor.putString( "min_entrada", minInicio.getText().toString() );

            // Hora de salida
            editor.putString( "hora_salida", horaFinal.getText().toString() );
            editor.putString( "min_salida"  , minFinal.getText().toString() );

            editor.commit();

            dialogo = new ProgressDialog( getActivity() );
            dialogo = ProgressDialog.show( getActivity(), "Consultando Registros", "Espere porfavor");
            dialogo.setCancelable( false );
        }

        @Override
        protected Boolean doInBackground( Void... params )
        {
            if (android.os.Debug.isDebuggerConnected())
            {
                android.os.Debug.waitForDebugger();
            }

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
            request.addProperty( "rut"      , rut );
            request.addProperty( "fecha_actual", fecha_hoy );

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope( SoapEnvelope.VER11 );
            envelope.dotNet = true;
            envelope.setOutputSoapObject( request );
            HttpTransportSE transporte = new HttpTransportSE( URL );
            consulta_valida = 0;

            try
            {
                transporte.call( SOAP_ACTION, envelope );
                conexion_correcta = true;
                // Lista para el resultado
                SoapObject resSoap  = (SoapObject)envelope.getResponse();
                listaPersona        = new Persona[resSoap.getPropertyCount()];
                consulta_valida     = resSoap.getPropertyCount();

                if ( consulta_valida > 0 )
                {
                    for ( int i = 0; i < listaPersona.length; i++ )
                    {
                        SoapObject ic           = (SoapObject) resSoap.getProperty( i );

                        Persona obj             = new Persona();
                        obj.setHora( ic.getProperty( 0 ).toString() );
                        listaPersona[i]         = obj;
                    }
                }
            }
            catch ( IOException e )
            {
                conexion_correcta = false;
                e.printStackTrace();
            }
            catch ( XmlPullParserException e )
            {
                conexion_correcta = false;
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute( Boolean aBoolean )
        {
            if ( conexion_correcta )
            {
                if ( consulta_valida > 0 )
                {
                    for ( int i = 0; i < listaPersona.length; i++ )
                    {
                        hora_t = listaPersona[i].getHora();
                    }

                    horaInicio.setText( hora_t.substring( 0, 2 ) );
                    minInicio.setText( hora_t.substring( 3, 5 ) );

                    calcularSalida( getRadioAntesAvisar(), getRadioJornada() );

                    hrInicio = Integer.parseInt( horaInicio.getText().toString());
                    mnInicio = Integer.parseInt( minInicio.getText().toString());
                    hrFin    = Integer.parseInt( horaFinal.getText().toString() );
                    minFin   = Integer.parseInt( minFinal.getText().toString() );

                    setearAlarmaManual();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
                    builder.setTitle( "Error de registros" );
                    builder.setMessage( "No se ha encontrado una marcación de entrada para el día de hoy en el servicio" );
                    builder.setPositiveButton( "Aceptar", null );
                    builder.create();
                    builder.show();
                }
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
                builder.setTitle( "Error de Conexión" );
                builder.setMessage( "Sin conexión a internet, No se ha podido setear la alarma manual" );
                builder.setPositiveButton( "Aceptar", null );
                builder.create();
                builder.show();
            }

            if ( dialogo.isShowing() )
            {
                dialogo.dismiss();
            }
        }
    }

    private class asyncUpdateHora extends AsyncTask< Void, Void, Boolean>
    {
        private ProgressDialog dialogo;

        // Definición de variables
        private Persona[] listaPersona;
        private int consulta_valida;
        private boolean conexion_correcta;

        private String hora_t;
        private String rut;
        private String fecha_hoy;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            prefConfig = getActivity().getSharedPreferences( "configs", Context.MODE_PRIVATE );
            rut = prefConfig.getString( "rut_in", "rut_no_ingresado" );
            fecha_hoy = armarFecha();

            dialogo = new ProgressDialog( getActivity() );
            dialogo = ProgressDialog.show( getActivity(), "Consultando Registro", "Espere porfavor");
            dialogo.setCancelable( false );
        }

        @Override
        protected Boolean doInBackground( Void... params )
        {
            if (android.os.Debug.isDebuggerConnected())
            {
                android.os.Debug.waitForDebugger();
            }

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
            request.addProperty( "rut"      , rut );
            request.addProperty( "fecha_actual", fecha_hoy );

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope( SoapEnvelope.VER11 );
            envelope.dotNet = true;
            envelope.setOutputSoapObject( request );
            HttpTransportSE transporte = new HttpTransportSE( URL );
            consulta_valida = 0;

            try
            {
                transporte.call( SOAP_ACTION, envelope );
                conexion_correcta = true;
                // Lista para el resultado
                SoapObject resSoap  = (SoapObject)envelope.getResponse();
                listaPersona        = new Persona[resSoap.getPropertyCount()];
                consulta_valida     = resSoap.getPropertyCount();

                if ( consulta_valida > 0 )
                {
                    for ( int i = 0; i < listaPersona.length; i++ )
                    {
                        SoapObject ic           = (SoapObject) resSoap.getProperty( i );

                        Persona obj             = new Persona();
                        obj.setHora( ic.getProperty( 0 ).toString() );
                        listaPersona[i]         = obj;
                    }
                }
            }
            catch ( IOException e )
            {
                conexion_correcta = false;
                e.printStackTrace();
            }
            catch ( XmlPullParserException e )
            {
                conexion_correcta = false;
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute( Boolean aBoolean )
        {
            if ( conexion_correcta )
            {
                if ( consulta_valida > 0 )
                {
                    for ( int i = 0; i < listaPersona.length; i++ )
                    {
                        hora_t = listaPersona[i].getHora();
                    }

                    horaInicio.setText( hora_t.substring( 0, 2 ) );
                    minInicio.setText( hora_t.substring( 3, 5 ) );
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
                    builder.setTitle( "Error de registros" );
                    builder.setMessage( "No se ha encontrado una marcación de entrada para el día de hoy en el servicio" );
                    builder.setPositiveButton( "Aceptar", null );
                    builder.create();
                    builder.show();
                }
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
                builder.setTitle( "Error de Conexión" );
                builder.setMessage( "Compruebe su conexión a internet y vuelva a intentarlo" );
                builder.setPositiveButton( "Aceptar", null );
                builder.create();
                builder.show();
            }

            if ( dialogo.isShowing() )
            {
                dialogo.dismiss();
            }
        }
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

        return fecha_actual;
    }

    /**
     * Shared preferences para cambiar la hora y guardar la hora elegida en el teléfono
     */
    private void prefHoraElegida()
    {
        prefAlarma = getActivity().getSharedPreferences( "alarma", Context.MODE_PRIVATE );

        // Hora inicial
        horaInicio.setText( prefAlarma.getString( "hora_entrada", "8" ) );
        minInicio.setText ( prefAlarma.getString( "min_entrada", "30" ) );

        // Hora final
        horaFinal.setText( prefAlarma.getString( "hora_salida", "17" ) );
        minFinal.setText ( prefAlarma.getString( "min_salida", "30" ) );
    }

    /**
     * Setea la alarma y envía la conexión al Broadcast Receiver
     */
    public void setearAlarmaManual()
    {
        SharedPreferences sp = getActivity().getSharedPreferences( "alarma", Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt( "min_antes" ,getRadioAntesAvisar() );
        editor.commit();

        manager.cancel( pdIntAlarmaManual );

        long horaTarget     = 0;
        long tiempoActual   = 0;
        long diffTiempo     = 0;

        // Obtengo la instancia y seteo mi hora de destino
        Calendar calendar = Calendar.getInstance();
        calendar.set( Calendar.HOUR_OF_DAY, hrFin );
        calendar.set( Calendar.MINUTE, minFin );

        // Obtengo en milisegundos la hora de destino
        horaTarget      = calendar.getTimeInMillis();

        // Obtengo en milisegundos la hora actual
        tiempoActual    =  System.currentTimeMillis();

        // Se calcula la diferencia entre la hora de destino y la actual
        diffTiempo      =  horaTarget - tiempoActual;

        // Si es menor a 0 es una hora que ha pasado; entonces se muestra un mensaje de error
        if ( diffTiempo < 0 )
        {
            /*calendar.setTimeInMillis( System.currentTimeMillis() + DateUtils.DAY_IN_MILLIS );
            calendar.set( Calendar.HOUR_OF_DAY, hrFin );
            calendar.set( Calendar.MINUTE, minFin );*/

            AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
            builder.setTitle( "Error" );
            builder.setIcon( R.drawable.ic_action_action_account_box );
            builder.setMessage( "La hora de salida calculada ya pasó, no se activará la alarma" );
            builder.setPositiveButton( "Aceptar", null );
            builder.create();
            builder.show();
        }
        else
        {
            // Sino es una hora que aun falta entonces se setea directamente
            calendar.setTimeInMillis( System.currentTimeMillis() );
            calendar.set( Calendar.HOUR_OF_DAY, hrFin );
            calendar.set( Calendar.MINUTE, minFin );

            // Activar la alarma, se pone SET ya que solo será una vez
            manager = (AlarmManager) getActivity().getSystemService( Context.ALARM_SERVICE );
            manager.set( AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pdIntAlarmaManual );
            // Pruebas
            //manager.set( AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pdIntAlarmaManual );

            Toast.makeText( getActivity(), "Alarma Seteada Manualmente a las " + hrFin + ":" + minFin, Toast.LENGTH_SHORT ).show();
        }
    }

    /**
     * Permite cortar la hora inicial que ha ingresado el usuario y restarle el tiempo que
     * la alarma deberá de avisar antes de que esta suene según el radiobutton
     */
    public void calcularSalida( int minAntes, int jornada )
    {
        int hInicio = Integer.parseInt( horaInicio.getText().toString() );
        int mInicio = Integer.parseInt( minInicio .getText().toString() );
        int hFinal;
        int mFinal;
        long l_hora_inicial;
        long modificador_hora;
        long nueva_hora;

        Calendar cal = Calendar.getInstance();
        cal.set( Calendar.HOUR_OF_DAY, hInicio );
        cal.set( Calendar.MINUTE, mInicio );

        l_hora_inicial      = cal.getTimeInMillis();

        // Setear la hora para la jornada seleccionada por el usuario
        modificador_hora    = DateUtils.HOUR_IN_MILLIS * jornada;
        nueva_hora          = l_hora_inicial + modificador_hora;

        cal.setTimeInMillis( nueva_hora );
        l_hora_inicial = cal.getTimeInMillis();

        // Setear la hora de salida pero con el retraso seleccionado por el usuario
        modificador_hora    = DateUtils.MINUTE_IN_MILLIS * minAntes;
        nueva_hora          = l_hora_inicial - modificador_hora;
        cal.setTimeInMillis( nueva_hora );

        hFinal      = cal.get( Calendar.HOUR_OF_DAY );
        mFinal      = cal.get( Calendar.MINUTE );
        horaFinal.setText( String.valueOf( hFinal ) );
        minFinal .setText( String.valueOf( mFinal ) );
    }

    /**
     * Obtiene la selección del usuario en el RadioButtonGroup para saber cuanto tiempo antes desea
     * que la app le notifique que tiene terminará la jornada usa la propiedad TAG que se le asignó
     * en el XML a cada uno de los radios y se obtiene el valor mediante el GetTag
     * @return el numero con la cantidad de minutos antes de salir
     */
    private int getRadioAntesAvisar()
    {
        RadioGroup raSonarAntes         = (RadioGroup) rootView.findViewById( R.id.radioGroupAntesMin );
        int idTiempoSelec               = raSonarAntes.getCheckedRadioButtonId();
        RadioButton raAntesSeleccionado = (RadioButton) rootView.findViewById( idTiempoSelec );

        int resul = 0;
        switch ( raAntesSeleccionado.getTag().toString() )
        {
            case "ra5":
                resul = 5;
                break;
            case "ra10":
                resul = 10;
                break;
            case "ra30":
                resul = 30;
                break;
            case "raUnaHora":
                resul = 60;
                break;
        }

        return resul;
    }

    /**
     * Obtiene la selección de la jornada seleccionada por el usuario, usa la propiedad Tag
     * asignada en el XML a cada uno de los radiobuttons
     * @return la jornada seleccionada que puede ser de 9 u 8 horas
     */
    private int getRadioJornada()
    {
        RadioGroup raJornada        = (RadioGroup) rootView.findViewById( R.id.radioGroupJornada );
        int idJornadaSelec          = raJornada.getCheckedRadioButtonId();
        RadioButton raJornadaSelec  = (RadioButton) rootView.findViewById( idJornadaSelec );

        int resul = 0;
        switch ( raJornadaSelec.getTag().toString() )
        {
            case "raJornada9":
                resul = 9;
                break;
            case "raJornada8":
                resul = 8;
                break;
        }

        return resul;
    }

    /**
     * Cambiar la hora de la alarma asignada
     */
    public void btnUpdateHora()
    {
        // Si es dentro de la semana
        if ( numDiaActual > 1 && numDiaActual < 7 )
        {
            new asyncUpdateHora().execute();
        }
        else
        {
            // Error de conexión
            AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
            builder.setTitle( "Mensaje de aviso" );
            builder.setMessage( "La alarma del Reloj Control no funciona los finde semanas" );
            builder.setPositiveButton( "Aceptar", null );
            builder.create();
            builder.show();
        }
    }

    /**
     * Cancelar la alarma con mensaje de confirmación
     *
     * @param v
     */
    public void btnCancelarAlarm( View v )
    {
        // Le pregunta al usuario con un mensaje emergente si desea cancelar la alarma
        new AlertDialog.Builder( getActivity() )
                .setIcon( android.R.drawable.ic_dialog_alert )
                .setTitle( R.string.alarma_cancTitulo )
                .setMessage( R.string.alarma_cancelarAlarma )
                .setPositiveButton( R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {

                        manager = (AlarmManager) getActivity().getSystemService( Context.ALARM_SERVICE );
                        manager.cancel( pdIntAlarmaManual );
                        Toast.makeText( getActivity(), "Alarma Cancelada", Toast.LENGTH_SHORT ).show();
                    }

                } )
                .setNegativeButton( R.string.no, null )
                .show();
    }
}
