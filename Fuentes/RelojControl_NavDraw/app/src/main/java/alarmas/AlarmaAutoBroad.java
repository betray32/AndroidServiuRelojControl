package alarmas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Created by Camilo on 11-03-2015.
 * Permite sincronizarse automaticamente para setear las variables
 */
public class AlarmaAutoBroad extends BroadcastReceiver
{

    @Override
    public void onReceive( final Context context, Intent intent )
    {
        if ( diaHoy() > 1 && diaHoy() < 7 )
        {
            // Se llama al IntentService para poder calcular la hora final y setear la alarma
            Intent i = new Intent( context, CalcularHorasService.class );
            context.startService( i );
        }
        else
        {
            // ¿A quién le gustan las alarmas los fin de semanas?
        }
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
    private int diaHoy()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis( System.currentTimeMillis() );
        return calendar.get( Calendar.DAY_OF_WEEK );
    }

}
