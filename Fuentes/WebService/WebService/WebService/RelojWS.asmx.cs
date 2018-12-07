using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Services;
using System.Data.SqlClient;

namespace WebService
{
    /// <summary>
    /// Summary description for WebService1
    /// </summary>
    [WebService(Namespace = "http://serviu.android.cl/relojcontrol")]
    [WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
    [System.ComponentModel.ToolboxItem(false)]
    // To allow this Web Service to be called from script, using ASP.NET AJAX, uncomment the following line. 
    // [System.Web.Script.Services.ScriptService]
    public class WebService1 : System.Web.Services.WebService
    {
        /**
         * Consulta por los datos personales
         */
        [WebMethod]
        public Persona[] datosPersonales( string rut )
        {
            // Si el rut tiene 7 digitos
            if (rut.Length == 7)
            {
                rut = "0" + rut;
            }

            string urlBD = @"Data Source=valposql;user id=reloj;Password=relojcontrol;Initial Catalog=Personal;";

            SqlConnection con = new SqlConnection(urlBD);
            con.Open();
            string consulta_sql = "SELECT nombre, apellido_paterno, apellido_materno "
                                   + "From Personal.dbo.Funcionario WHERE left(Rut, 8) = '" + rut + "'";

            SqlCommand cmd = new SqlCommand(consulta_sql, con);
            SqlDataReader reader = cmd.ExecuteReader();
            List<Persona> lista = new List<Persona>();

            //Se llama al constructor para consultar por rut y datos personales
            while (reader.Read())
            {
                lista.Add(
                    new Persona( reader.GetString(0), reader.GetString(1), reader.GetString(2) )
                    );
            }
            con.Close();
            return lista.ToArray();
        }

        /**
         * Busca por rut y entre periodos de fechas
         */
        [WebMethod]
        public Persona[] marcaciones(
            string rut,
            string anio_ini, string mes_ini, string dia_ini,
            string anio_fin, string mes_fin, string dia_fin
        )
        {
            string urlBD = @"Data Source=valposql;user id=reloj;Password=relojcontrol;Initial Catalog=Reloj;";
            SqlConnection con = new SqlConnection(urlBD);
            con.Open();


            string fechaDesde = dia_ini + "/" + mes_ini + "/" + anio_ini;
            string fechaHasta = dia_fin + "/" + mes_fin + "/" + anio_fin;

            string sqlQ = "";

            // Si son iguales está preguntando por el dia de hoy
            if (fechaDesde.Equals(fechaHasta))
            {
                sqlQ = "select convert ( varchar(10), fecha_hora, 101) fecha, convert ( varchar(10), fecha_hora, 108) hora, "
                       + "convert (int, codigo_marca)codigo_marca from Marcaciones where numero_tarjeta = '"+rut+"' AND "
                       + "CONVERT(DATETIME, FLOOR(CONVERT(FLOAT, fecha_hora))) = '"+fechaHasta+"'";
            }
            else
            {
                // Caso contrario se obtienen las marcaciones entre los intervalos de fechas
                sqlQ = "SELECT convert ( varchar(10), fecha_hora, 101) fecha, convert ( varchar(10), fecha_hora, 108) hora, "
                                    + "convert (int, codigo_marca)codigo_marca FROM Reloj.dbo.Marcaciones WHERE numero_tarjeta = '" + rut + "' and "
                                    + "fecha_hora between '" + fechaDesde + "' and '" + fechaHasta + "' ORDER BY secuencia DESC";
            }

            SqlCommand cmd = new SqlCommand( sqlQ , con);
            SqlDataReader reader = cmd.ExecuteReader();
            List<Persona> lista = new List<Persona>();

            // Constructor para consultar por Rut y entre fechas
            while (reader.Read())
            {
                lista.Add(
                    new Persona( reader.GetString(0), reader.GetString(1), reader.GetInt32(2) )
                    );
            }

            con.Close();
            return lista.ToArray();
        }

        /**
         * Obtiene la hora de entrada de la persona de acuerdo a su rut
         */
        [WebMethod]
        public Persona[] getHoraEntrada(string rut, string fecha_actual)
        {
            string urlBD = @"Data Source=valposql;user id=reloj;Password=relojcontrol;Initial Catalog=Reloj;";
            SqlConnection con = new SqlConnection(urlBD);
            con.Open();

            string sql_query = "select convert ( varchar(10), fecha_hora, 108) hora from Marcaciones where numero_tarjeta = '" + rut + "' "
                             + "AND CONVERT(DATETIME, FLOOR(CONVERT(FLOAT, fecha_hora))) = '"+fecha_actual+"' AND codigo_marca = 1";

            SqlCommand cmd = new SqlCommand(sql_query, con);
            SqlDataReader reader = cmd.ExecuteReader();
            List<Persona> lista = new List<Persona>();

            while (reader.Read())
            {
                lista.Add( new Persona( reader.GetString(0) ) );
            }

            con.Close();
            return lista.ToArray();
        }
    }
}
