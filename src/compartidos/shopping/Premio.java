package compartidos.shopping;

public class Premio {
    private String nombre;
    private int precio;

    public Premio(String xNombre, int xPrecio) {
        this.nombre = xNombre;
        this.precio = xPrecio;
    }

    public String getNombre(){
        return this.nombre;
    }

    public int getPrecio(){
        return this.precio;
    }
}
