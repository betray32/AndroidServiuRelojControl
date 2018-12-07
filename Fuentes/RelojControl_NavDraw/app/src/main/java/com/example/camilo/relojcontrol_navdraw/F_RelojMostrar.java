package com.example.camilo.relojcontrol_navdraw;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import modelo.Persona;

/**
 * Created by Camilo on 01/03/2015.
 */
public class F_RelojMostrar extends Fragment
{
    /**
     * Variables para las fechas
     */
    private String anio_ini;
    private String mes_ini;
    private String dia_ini;

    private String anio_fin;
    private String mes_fin;
    private String dia_fin;

    /**
     * La variable con el campo que se setearán los parametros
     */
    private String rut;
    public View rootView;

    /**
     * Instancia de la interfaz que permite volver a mostrar el fragment principal
     */
    public I_vPantallaInicio i_vPantallaInicio;

    /**
     * Parametro que se setea en 0 si el usuario ha ingresado una fecha o un rut incorrecto
     * y el sistema no le entrega valores de retorno
     */
    private int consulta_valida;
    private boolean conexion_correcta;

    /**
     * Dialogo para mostrar cuando se están cargando los datos
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        rootView = inflater.inflate( R.layout.f_relojmostrar, container, false );


        Button btn_volver = (Button) rootView.findViewById( R.id.btnVolver );
        btn_volver.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                volverPantallaInicio( v );
            }
        } );

        // Devolver la vista
        return rootView;
    }

    @Override
    public void onAttach( Activity activity )
    {
        super.onAttach( activity );

        try
        {
            i_vPantallaInicio = (I_vPantallaInicio) getActivity();
        }
        catch ( ClassCastException ex )
        {
            throw new ClassCastException( activity.toString() + "Debe implementar la clase I_vPantallaInicio" );
        }
    }

    /**
     * Si se va a recibir un dato es necesario implementar este método
     */
    @Override
    public void onStart()
    {
        super.onStart();

        /**
         * Se obtienen los parametros enviados desde el Activity que lo llamó
         * en este caso 'MainActivity'
         */
        Bundle args = getArguments();

        if ( args != null )
        {
            // Desde
            anio_ini = args.getString( "anio_ini" );
            mes_ini  = args.getString( "mes_ini" );
            dia_ini  = args.getString( "dia_ini" );

            // Hasta
            anio_fin = args.getString( "anio_fin" );
            mes_fin  = args.getString( "mes_fin" );
            dia_fin  = args.getString( "dia_fin" );

            // EL shared preference de donde se obtienen los datos del rut
            SharedPreferences sh = getActivity().getSharedPreferences( "configs", Context.MODE_PRIVATE );
            rut = sh.getString( "rut_in", "rut_no_seteado" );

            // Se buscan las marcaciones
            new marcacionesAsync().execute();
        }
    }

    /**
     * Muestra la pantalla inicial del sistema
     * @param v Parámetro incial de la vista
     */
    public void volverPantallaInicio( View v )
    {
        i_vPantallaInicio.I_vMethodPantallaInicio();
    }

    /**
     * Permite obtener los datos personales de la persona a partir del rut
     */
    private class datosPersonalesAsync extends AsyncTask< Void, Void, Void >
    {
        ProgressDialog pDialog;
        Persona[] listaPersona;
        TextView nombre_lb  = (TextView) rootView.findViewById( R.id.lbNombre );
        TextView fechaTit   = (TextView) rootView.findViewById( R.id.lbFechaTitulo );

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            pDialog = new ProgressDialog( getActivity() );
            pDialog = ProgressDialog.show( getActivity(), "Consultando Datos Personales", "Espere porfavor");
            pDialog.setCancelable( false );
        }

        @Override
        protected Void doInBackground( Void... params )
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
            String METHOD_NAME	= "datosPersonales";
            String SOAP_ACTION 	= "http://serviu.android.cl/relojcontrol/datosPersonales";

            SoapObject request = new SoapObject( NAMESPACE, METHOD_NAME );

            // Variables que se mandan al WS
            request.addProperty( "rut"      , rut );

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
                consulta_valida           = resSoap.getPropertyCount();

                /**
                 * Si este valor es 0 quiere decir que el resultado no entregó nada
                 */
                if ( consulta_valida > 0 )
                {
                    for ( int i = 0; i < listaPersona.length; i++ )
                    {
                        SoapObject ic           = (SoapObject) resSoap.getProperty( i );

                        Persona obj             = new Persona();
                        obj.setNombre           ( ic.getProperty( 0 ).toString() );
                        obj.setApellido_paterno ( ic.getProperty( 1 ).toString() );
                        obj.setApellido_materno ( ic.getProperty( 2 ).toString() );

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

            return null;
        }

        @Override
        protected void onPostExecute( Void aVoid )
        {
            super.onPostExecute( aVoid );

            // Si la conexión entró
            if ( conexion_correcta )
            {
                // Si la consulta devolvio registros
                if ( consulta_valida > 0 )
                {
                    // Nombres de los clientes en funcion RUN
                    ArrayList<String> lista = new ArrayList<String>();
                    ArrayAdapter<String> adaptador
                            = new ArrayAdapter<String>( getActivity(), android.R.layout.simple_spinner_item, lista );

                    String nombre = (( listaPersona[0].getNombre().toUpperCase() ).substring( 0, 1 ) + ( listaPersona[0].getNombre().toLowerCase() ).substring( 1 ));
                    String ap_pat = (( listaPersona[0].getApellido_paterno().toUpperCase() ).substring( 0, 1 ) ) + ( listaPersona[0].getApellido_paterno().toLowerCase() ).substring( 1 );

                    // Setear los datos de los labels
                    nombre_lb.setText( "Nombre: " + nombre + " " + ap_pat );
                    fechaTit.setText( "Tramo: " + dia_ini + "/" + ( mes_ini ) + "/" + anio_ini + " - " + dia_fin + "/" + ( mes_fin ) + "/" + anio_fin
                    );
                }
            }

            if ( pDialog.isShowing() )
            {
                pDialog.dismiss();
            }
        }
    }

    /**
     * Permite obtener los datos de la marcación de la persona
     */
    private class marcacionesAsync extends AsyncTask< Void, Void, Void >
    {
        ProgressDialog pDialog;
        Persona[] listaPersona;
        GridView gv         = (GridView) rootView.findViewById( R.id.gridMarcacion );
        TextView nombre_lb  = (TextView) rootView.findViewById( R.id.lbNombre );
        TextView fechaTit   = (TextView) rootView.findViewById( R.id.lbFechaTitulo );

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            pDialog = new ProgressDialog( getActivity() );
            pDialog = ProgressDialog.show( getActivity(), "Consultando Marcaciones", "Espere porfavor");
            pDialog.setCancelable( false );
        }

        @Override
        protected Void doInBackground( Void... params )
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
            String METHOD_NAME	= "marcaciones";
            String SOAP_ACTION 	= "http://serviu.android.cl/relojcontrol/marcaciones";

            SoapObject request = new SoapObject( NAMESPACE, METHOD_NAME );

            // Variables que se mandan al WS
            request.addProperty( "rut"      , rut );
            request.addProperty( "anio_ini" , anio_ini );
            request.addProperty( "mes_ini"  , mes_ini );
            request.addProperty( "dia_ini"  , dia_ini );
            request.addProperty( "anio_fin" , anio_fin );
            request.addProperty( "mes_fin"  , mes_fin );
            request.addProperty( "dia_fin"  , dia_fin );

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

                /**
                 * Si este valor es 0 quiere decir que el resultado no entregó nada
                 */
                if ( consulta_valida > 0 )
                {
                    for ( int i = 0; i < listaPersona.length; i++ )
                    {
                        SoapObject ic           = (SoapObject) resSoap.getProperty( i );

                        Persona obj             = new Persona();

                        obj.setFecha            ( ic.getProperty( 0 ).toString() );
                        obj.setHora             ( ic.getProperty( 1 ).toString() );
                        obj.setCodigo_marca     ( Integer.parseInt( ic.getProperty( 2 ).toString() ) );

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

            return null;
        }

        @Override
        protected void onPostExecute( Void aVoid )
        {
            super.onPostExecute( aVoid );

            // Si la conexión entró
            if ( conexion_correcta )
            {
                // Si la consulta devolvio registros
                if ( consulta_valida > 0 )
                {
                    // Nombres de los clientes en funcion RUN
                    ArrayList<String> lista = new ArrayList<String>();
                    ArrayAdapter<String> adaptador
                            = new ArrayAdapter<String>( getActivity(), android.R.layout.simple_spinner_item, lista );

                    for ( int i = 0; i < listaPersona.length; i++ )
                    {
                        // Dar vuelta los numeros
                        String fecha = listaPersona[i].getFecha();
                        String[] tokens = fecha.split("\\s*/\\s*");
                        String mes     = tokens[0];
                        String dia     = tokens[1];
                        String ano     = tokens[2];
                        fecha = dia + "/" + mes + "/" + ano;

                        lista.add ( fecha );
                        lista.add( listaPersona[i].getHora() );
                        //lista.add( Integer.toString( listaPersona[i].codigo_marca ) );

                        if ( listaPersona[i].getCodigo_marca() == 1 )
                            lista.add( "Entrada" );
                        else
                            lista.add( "Salida" );

                        gv.setAdapter( adaptador );
                    }

                    fechaTit.setText( "Tramo: " + dia_ini + "/" + ( mes_ini ) + "/" + anio_ini
                                    + " - " + dia_fin + "/" + ( mes_fin ) + "/" + anio_fin
                    );

                    // Si ok se buscan los datos personales
                    new datosPersonalesAsync().execute();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
                    builder.setTitle( "Error de registros" );
                    builder.setMessage( "No se han encontrado incidencias, porfavor intente con otros datos" );
                    builder.setPositiveButton( "Aceptar", null );
                    builder.create();
                    builder.show();

                    // Se devuelve el control a la pantalla de inicio
                    i_vPantallaInicio.I_vMethodPantallaInicio();
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

                // Se devuelve el control a la pantalla de inicio
                i_vPantallaInicio.I_vMethodPantallaInicio();
            }

            if ( pDialog.isShowing() )
            {
                pDialog.dismiss();
            }
        }
    }
}
