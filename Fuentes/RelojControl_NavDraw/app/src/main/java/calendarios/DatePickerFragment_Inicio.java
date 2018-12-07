package calendarios;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;

import com.example.camilo.relojcontrol_navdraw.R;

import java.util.Calendar;

/**
 * Created by Camilo on 02-03-2015.
 */
public class DatePickerFragment_Inicio extends DialogFragment implements DatePickerDialog.OnDateSetListener
{

    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState )
    {
        // Use the current date as the default date in the picker
        final Calendar c    = Calendar.getInstance();
        int year            = c.get( Calendar.YEAR );
        int month           = c.get( Calendar.MONTH );
        int day             = c.get( Calendar.DAY_OF_MONTH );

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog( getActivity(), this, year, month, day );
    }

    /**
     * Funcion para cuando la persona setea los campos
     * @param view
     * @param year
     * @param month
     * @param day
     */
    public void onDateSet( DatePicker view, int year, int month, int day )
    {
        TextView lbFechaIn = (TextView) getActivity().findViewById( R.id.lbFechaIni );
        lbFechaIn.setText( day + "-" + (setNombre_Meses( month )) + "-" + year);
    }

    private String setNombre_Meses( int mes )
    {
        String mes_s = "";

        switch ( mes + 1 )
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

}
