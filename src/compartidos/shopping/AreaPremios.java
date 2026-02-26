package compartidos.shopping;

import java.util.concurrent.Exchanger;

public class AreaPremios {
    private Exchanger<Integer> puntos;

    public AreaPremios() {
        this.puntos = new Exchanger<>();
    }

    public int canjearPremio(int id,int n) throws InterruptedException{
        if (id!=-1) {
            System.out.println("El visitante N°" + id + " entró a la tienda de premios con " + n + " puntos");
        }
        return puntos.exchange(n);
    }
}