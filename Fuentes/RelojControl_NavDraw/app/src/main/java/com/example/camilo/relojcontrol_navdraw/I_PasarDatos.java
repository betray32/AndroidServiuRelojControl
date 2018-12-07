package com.example.camilo.relojcontrol_navdraw;

/**
 * Created by Camilo on 01/03/2015.
 */
public interface I_PasarDatos
{
    /**
     * Este método es necesario para entregarle los datos al fragmento que se encargará de
     * conectarse con el servicio web para las consultas
     * @param rut_in Rut de inicio, corresponde al rut de la persona que desea buscar
     */
    public void pasarDatos (
            String anio_ini, String mes_ini, String dia_ini,
            String anio_fin, String mes_fin, String dia_fin
    );
}
