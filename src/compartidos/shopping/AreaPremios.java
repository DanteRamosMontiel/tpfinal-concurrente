package compartidos.shopping;

import java.util.concurrent.Exchanger;

public class AreaPremios {
    private Exchanger<Integer> puntos;
    private volatile boolean abierto = true;

    public AreaPremios() {
        this.puntos = new Exchanger<>();
    }

    public int canjearPremio(int id,int n) throws InterruptedException{
        if (!abierto) {
            if (id != -1) {
                System.out.println("AreaPremios cerrada, visitante " + id + " se va");
            }
            return 0;
        }
        if (id!=-1) {
            System.out.println("El visitante N°" + id + " entró a la tienda de premios con " + n + " puntos");
        }
        return puntos.exchange(n);
    }

    public void cerrar() {
        abierto = false;
        // crear un nuevo exchanger para que nadie quede esperando
        puntos = new Exchanger<>();
    }

    public void abrir() {
        abierto = true;
        puntos = new Exchanger<>();
    }

    public boolean estaVacio() {
        // no hay forma de saber cuántos esperan, asumimos que si está abierto no hay nadie
        return abierto;
    }
}