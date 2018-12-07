package com.example.camilo.relojcontrol_navdraw;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import alarmas.AlarmaAutoBroad;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, I_PasarDatos, I_vPantallaInicio
{

    private PendingIntent pdIntAutomatico;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /**
     * El contexto
     */
    private Context context;

    /**
     * Variables del usuario
     */
    private int numDiaActual;
    private AlarmManager manager;

    /**
     * Las preferencias
     */
    public SharedPreferences pref;
    public SharedPreferences.Editor editor;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        context = this;

        // Manejo del Action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.mipmap.ic_launcher );
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Color del navigation
        //ColorDrawable color_actionBar = new ColorDrawable( Color.parseColor("#002233") );
        //actionBar.setBackgroundDrawable( color_actionBar );

        // Asignar el navigation drawer al activity
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById( R.id.navigation_drawer );
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById( R.id.drawer_layout ) );

        // Intents
        manager = (AlarmManager) this.getSystemService( Context.ALARM_SERVICE );
        Intent intSincroAuto = new Intent( this, AlarmaAutoBroad.class );
        pdIntAutomatico = PendingIntent.getBroadcast( this, 0, intSincroAuto, 0 );

        // Las preferencias
        pref = this.getSharedPreferences( "configs", Context.MODE_PRIVATE );

        popupGuardarRut();
    }

    /**
     * El popup inicial que permite guardar el rut de la persona
     */
    private void popupGuardarRut()
    {
        // Si es la primera ves que se hace ingreso a la aplicación se despliega el popup para guardar el rut
        if ( pref.getInt( "primeraVez", 0 ) == 0 )
        {

            // Get prompts.xml view
            LayoutInflater li = LayoutInflater.from( context );
            View promptsView = li.inflate( R.layout.popuprutinicial, null );

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    context );

            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView( promptsView );

            final EditText userInput = (EditText) promptsView
                    .findViewById( R.id.editTextDialogUserInput );

            // Setear el mensaje
            alertDialogBuilder
                    .setCancelable( false )
                    .setPositiveButton( "Guardar",
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick( DialogInterface dialog, int id )
                                {
                                    // Si está vacio
                                    if ( (userInput.getText().toString()).equals( "" ) )
                                    {
                                        Toast.makeText( getApplicationContext(), "Campo vacio, debe ingresar un rut", Toast.LENGTH_SHORT ).show();
                                        popupGuardarRut();
                                    }
                                    else
                                    {
                                        // Si el largo del input es menor que 8
                                        if ( ((userInput.getText().toString()).length()) < 7 )
                                        {
                                            Toast.makeText( getApplicationContext(), "El Rut ingresado es inválido", Toast.LENGTH_SHORT ).show();
                                            popupGuardarRut();
                                        }
                                        else
                                        {
                                            // Si se validó correctamente se ingresa el rut
                                            editor = pref.edit();
                                            editor.putInt( "primeraVez", 1 );

                                            // Se guarda el rut ingresado
                                            editor.putString( "rut_in", userInput.getText().toString() );
                                            editor.putInt( "rut_validado", 1 );
                                            editor.commit();

                                            // Poner el rut ingresado en el label
                                            TextView rut_in = ( TextView) findViewById( R.id.lbRutIN );
                                            rut_in.setText( userInput.getText().toString() );
                                            Toast.makeText( MainActivity.this, "Rut guardado correctamente", Toast.LENGTH_SHORT ).show();
                                        }
                                    }
                                }
                            } );

            // Crear el dialogo de alerta
            AlertDialog alertDialog = alertDialogBuilder.create();

            // Mostrar dialogo
            alertDialog.show();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected( int position )
    {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;

        switch ( position )
        {
            case 0:
                // RelojControl
                fragment = new F_RelojControl();
                break;
            case 1:
                // Historial
                fragment = new F_Alarmas();
                break;
            case 2:
                // Configuración
                fragment = new F_Configuracion();
        }

        // Se le pasa el fragment para que muestre la vista seleccionada
        getSupportFragmentManager().beginTransaction()
                .replace( R.id.container, fragment )
                .commit();
    }

    public void onSectionAttached( int number )
    {
        /**
         * Acá se crea la instancia para que tome el array desde los resources con los titulos
         */
        String[] stringArray = getResources().getStringArray( R.array.section_titulos );
        if ( number >= 1 )
        {
            mTitle = stringArray[number - 1];
        }
    }

    public void restoreActionBar()
    {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode( ActionBar.NAVIGATION_MODE_STANDARD );
        actionBar.setDisplayShowTitleEnabled( true );
        actionBar.setTitle( mTitle );
    }


    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        if ( !mNavigationDrawerFragment.isDrawerOpen() )
        {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate( R.menu.main, menu );
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu( menu );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if ( id == R.id.action_settings )
        {
            return true;
        }

        return super.onOptionsItemSelected( item );
    }

    /**
     * Metodos mios
     */

    /**
     * Este método permite que se le pasen los datos obtenidos desde el Fragment A
     * hacia el Fragmento B
     */
    @Override
    public void pasarDatos(
            String anio_ini, String mes_ini, String dia_ini,
            String anio_fin, String mes_fin, String dia_fin
    )
    {
        Fragment mostrar_data = new F_RelojMostrar();

        Bundle args = new Bundle();

        args.putString( "anio_ini", anio_ini );
        args.putString( "mes_ini", mes_ini );
        args.putString( "dia_ini", dia_ini );

        args.putString( "anio_fin", anio_fin );
        args.putString( "mes_fin", mes_fin );
        args.putString( "dia_fin", dia_fin );

        mostrar_data.setArguments( args );

        // El Fragment manager se llama para que se muestre el nuevo fragmento
        getSupportFragmentManager().beginTransaction()
                .replace( R.id.container, mostrar_data )
                .commit();
    }

    /**
     * Muestra la pantalla de inicio predeterminada
     */
    @Override
    public void I_vMethodPantallaInicio()
    {
        Fragment pantalla_inicio = new F_RelojControl();
        getSupportFragmentManager().beginTransaction()
                .replace( R.id.container, pantalla_inicio )
                .commit();
    }
}
