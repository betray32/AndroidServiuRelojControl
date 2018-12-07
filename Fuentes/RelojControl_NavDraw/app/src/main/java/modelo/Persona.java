package modelo;

/**
 * Created by soportev on 26-02-2015.
 */
public class Persona
{
    /* Variables de persona */
    private String nombre;
    private String apellido_materno;
    private String apellido_paterno;
    private String fecha;
    private String hora;
    private int codigo_marca;

    // BOB EL CONSTRUCTOR
    public Persona()
    {
        this.nombre             = "";
        this.apellido_paterno   = "";
        this.apellido_materno   = "";
        this.fecha              = "";
        this.hora               = "";
        this.codigo_marca       = 0;
    }

    // Datos Personales
    public Persona( String nombre, String apellido_materno, String apellido_paterno )
    {
        this.nombre = nombre;
        this.apellido_materno = apellido_materno;
        this.apellido_paterno = apellido_paterno;
    }

    // Marcaciones
    public Persona( String fecha, String hora, int codigo_marca )
    {
        this.fecha = fecha;
        this.hora = hora;
        this.codigo_marca = codigo_marca;
    }

    // Hora de entrada
    public Persona( String hora )
    {
        this.hora = hora;
    }

    /**
     * Gets y Sets
     */

    public String getNombre()
    {
        return nombre;
    }

    public void setNombre( String nombre )
    {
        this.nombre = nombre;
    }

    public String getApellido_paterno()
    {
        return apellido_paterno;
    }

    public void setApellido_paterno( String apellido_paterno )
    {
        this.apellido_paterno = apellido_paterno;
    }

    public void setApellido_materno( String apellido_materno )
    {
        this.apellido_materno = apellido_materno;
    }

    public String getFecha()
    {
        return fecha;
    }

    public void setFecha( String fecha )
    {
        this.fecha = fecha;
    }

    public String getHora()
    {
        return hora;
    }

    public void setHora( String hora )
    {
        this.hora = hora;
    }

    public int getCodigo_marca()
    {
        return codigo_marca;
    }

    public void setCodigo_marca( int codigo_marca )
    {
        this.codigo_marca = codigo_marca;
    }

}

