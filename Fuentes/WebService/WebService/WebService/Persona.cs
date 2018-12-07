using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace WebService
{
    /*
     * Esta clase será creada para encapsular los metodos y variables de la tabla "Persona"
     */
    public class Persona
    {
        /* Variables de persona */
        public string nombre                { get; set; }
        public string apellido_paterno      { get; set; }
        public string apellido_materno      { get; set; }
        public string fecha                 { get; set; }
        public string hora                  { get; set; }
        public int codigo_marca             { get; set; }

        // Bob el constructor vacio
        public Persona()
        {
            this.nombre             = "";
            this.apellido_paterno   = "";
            this.apellido_materno   = "";
            this.fecha              = "";
            this.hora               = "";
            this.codigo_marca       = 0;
        }

        // Para obtener los datos personales de la persona
        public Persona(string nombre, string apellido_paterno, string apellido_materno)
        {
            this.nombre             = nombre;
            this.apellido_paterno   = apellido_paterno;
            this.apellido_materno   = apellido_materno;
        }


        // Marcaciones entre fechas
        public Persona( string fecha, string hora, int codigo_marca )
        {
            this.fecha              = fecha;
            this.hora               = hora;
            this.codigo_marca       = codigo_marca;
        } 

        // Hora de ingreso de la persona
        public Persona(string hora)
        {
            this.hora = hora;
        }

    }
}