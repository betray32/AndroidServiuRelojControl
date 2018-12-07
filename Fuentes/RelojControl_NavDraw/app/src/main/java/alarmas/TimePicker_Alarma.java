package alarmas;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.camilo.relojcontrol_navdraw.R;

import java.util.Calendar;

/**
 * Created by soportev on 09-03-2015.
 */
public class TimePicker_Alarma extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener
{
    private TextView targetHora;
    private TextView targetMin;

    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState )
    {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour         = c.get( Calendar.HOUR_OF_DAY );
        int minute       = c.get( Calendar.MINUTE );

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog( getActivity(), this, hour, minute,
                DateFormat.is24HourFormat( getActivity() ) );
    }

    /**
     * Modificado ayer
     * @param view
     * @param hourOfDay
     * @param minute
     */
    public void onTimeSet( TimePicker view, int hourOfDay, int minute )
    {
        targetHora = (TextView) getActivity().findViewById( R.id.lbHoraTarget );
        targetMin  = (TextView) getActivity().findViewById( R.id.lbMinTarget );

        targetHora.setText( String.valueOf( hourOfDay ) );
        targetMin.setText( String.valueOf( minute ) );

    }
}