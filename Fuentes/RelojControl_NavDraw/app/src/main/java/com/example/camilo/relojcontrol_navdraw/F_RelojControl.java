package com.example.camilo.relojcontrol_navdraw;

// Calendarios
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Calendar;

import calendarios.DatePickerFragment_Fin;
import calendarios.DatePickerFragment_Inicio;

/**
 * Created by Camilo on 28/02/2015.
 */
public class F_RelojControl extends Fragment
{
    /**
     * el rootView es la vista con la cual se manejan los datos
     */
    public View rootView;

    /**
     * Se crea una instancia de la interface
     */
    public I_PasarDatos i_pasarDatos;

    /**
     * Campos para setear las fechas elegidas por la persona
     */
    private String anio_ini;
    private String mes_ini;
    private String dia_ini;
    private String anio_fin;
    private String mes_fin;
    private String dia_fin;

    /**
     * Rut Ingresado
     */
    private String strRutIn;

    /**
     * Labels de fechas de inicio
     */
    public TextView lbFecha_ini;
    public TextView lbFecha_fin;
    public TextView rut_in;

    /**
     * Preferencias para guardar el rut
     */
    public SharedPreferences pref;

    /**
     * Este metodo es como el onCreate de las actividades pero en este caso para los fragmentos
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return: una instancia del view
     */
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState )
    {
        rootView = inflater.inflate( R.layout.f_relojcontrol, container, false );

        setLabelsFechas();

        // Setear el rut en el ViewText
        rut_in = (TextView) rootView.findViewById( R.id.lbRutIN );
        pref = getActivity().getSharedPreferences( "configs", Context.MODE_PRIVATE );
        rut_in.setText( pref.getString( "rut_in", "rut_no_seteado" ) );

        /**
         * Se crea el listener para el botón y se le pasa la instancia de v al metodo que
         * lo implementa (enviaRut)
         */
        Button btn_enviar = (Button) rootView.findViewById( R.id.btnCambiar );
        btn_enviar.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                enviaRut( v );
            }
        } );

        /**
         * Boton fecha inicial
         */
        ImageButton btn_setFechaInicio = ( ImageButton ) rootView.findViewById( R.id.btnSetFechaInicio );
        btn_setFechaInicio.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                setFechaInicio( v );
            }
        } );

        /**
         * Boton fecha término
         */
        ImageButton btn_setFechaTermino = (ImageButton) rootView.findViewById( R.id.btnSetFechaTermino );
        btn_setFechaTermino.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                setFechaTermino( v );
            }
        } );

        /**
         * Devolver la instancia de vista
         */
        return rootView;
    }

    /**
     * Setea los controles para ajustar la fecha actual en los calendarios
     */
    private void setLabelsFechas()
    {
        lbFecha_ini = (TextView) rootView.findViewById( R.id.lbFechaIni );
        lbFecha_fin = (TextView) rootView.findViewById( R.id.lbFechaFin );

        final Calendar c = Calendar.getInstance();
        int yy_in = c.get( Calendar.YEAR );
        int mm_in = c.get( Calendar.MONTH );
        int dd_in = c.get( Calendar.DAY_OF_MONTH );


        // Set current date into textview
        lbFecha_ini.setText( new StringBuilder()
                // Month is 0 based, just add 1
                .append( dd_in ).append( " " ).append( "-" ).append( mes_selec( mm_in ) ).append( "-" )
                .append( yy_in ) );

        lbFecha_fin.setText( new StringBuilder()
                // Month is 0 based, just add 1
                .append( dd_in ).append( " " ).append( "-" ).append( mes_selec( mm_in ) ).append( "-" )
                .append( yy_in ) );
    }

    /**
     * Setea los meses dependiendo del mes de entrada que se le entrege
     * @param mm_in el numero del mes
     * @return el mes pero en palabras
     */
    public String mes_selec( int mm_in )
    {
        String mes_s = "";

        switch ( mm_in + 1 )
        {
            case 1:
                mes_s = "Enero";
                break;
            case 2:
                mes_s = "Febrero";
                break;
            case 3:
                mes_s = "Marzo";
                break;
            case 4:
                mes_s = "Abril";
                break;
            case 5:
                mes_s = "Mayo";
                break;
            case 6:
                mes_s = "Junio";
                break;
            case 7:
                mes_s = "Julio";
                break;
            case 8:
                mes_s = "Agosto";
                break;
            case 9:
                mes_s = "Septiembre";
                break;
            case 10:
                mes_s = "Octubre";
                break;
            case 11:
                mes_s = "Noviembre";
                break;
            case 12:
                mes_s = "Diciembre";
                break;
        }

        return mes_s;
    }

    /**
     * Pasar los datos desde este fragmento al MainActivity obligandolo a implementar
     * la interface "I_PasarDatos"
     *
     * @param activity
     */
    @Override
    public void onAttach( Activity activity )
    {
        super.onAttach( activity );

        try
        {
            i_pasarDatos = (I_PasarDatos) getActivity();
        }
        catch ( ClassCastException ex )
        {
            throw new ClassCastException( activity.toString() + "Debe implementar la clase I_Pasar datos" );
        }
    }

    /**
     * Definición de la acción del boton para enviar los datos al MainActivity
     */
    public void enviaRut( View v )
    {
        setFechas();
        i_pasarDatos.pasarDatos(
                anio_ini, mes_ini, dia_ini,
                anio_fin, mes_fin, dia_fin
        );
    }

    /**
     * Este metodo setea todas las fechas obtenidas desde los TextView en variables para enviarlas
     */
    private void setFechas()
    {
        // Variable para ir formateando
        String formato_fecha = null;

        // Ingreso de datos cortados para la fecha de inicio
        formato_fecha  = ((TextView) rootView.findViewById( R.id.lbFechaIni )).getText().toString();
        String[] tokens = formato_fecha.split("\\s*-\\s*");
        dia_ini     = tokens[0];
        mes_ini     = mes_a_numero( tokens[1] );
        anio_ini    = tokens[2];

        // Ingreso para la fecha de fin
        formato_fecha = ((TextView) rootView.findViewById( R.id.lbFechaFin )).getText().toString();
        tokens = formato_fecha.split("\\s*-\\s*");
        dia_fin     = tokens[0];
        mes_fin     = mes_a_numero( tokens[1] );
        anio_fin    = tokens[2];
    }

    private String mes_a_numero( String mes_n )
    {
        String numMes_s = "";

        switch ( mes_n )
        {
            case "Enero":
                numMes_s = "1";
                break;
            case "Febrero":
                numMes_s = "2";
                break;
            case "Marzo":
                numMes_s = "3";
                break;
            case "Abril":
                numMes_s = "4";
                break;
            case "Mayo":
                numMes_s = "5";
                break;
            case "Junio":
                numMes_s = "6";
                break;
            case "Julio":
                numMes_s = "7";
                break;
            case "Agosto":
                numMes_s = "8";
                break;
            case "Septiembre":
                numMes_s = "9";
                break;
            case "Octubre":
                numMes_s = "10";
                break;
            case "Noviembre":
                numMes_s = "11";
                break;
            case "Diciembre":
                numMes_s = "12";
                break;
        }

        return numMes_s;
    }

    /**
     * Setear la fecha de inicio del calendario
     */
    public void setFechaInicio( View v )
    {
        new DatePickerFragment_Inicio().show( getFragmentManager(),"Fecha inicio" );
    }

    /**
     * Setear la fecha de termino en calendario
     */
    public void setFechaTermino( View v )
    {
        new DatePickerFragment_Fin().show( getFragmentManager(), "Fecha Fin" );
    }
}
