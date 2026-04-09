package activos.extras;

import compartidos.Parque;
public class Tren extends Thread {
    private final Parque parque;

    public Tren(Parque parque) {
        this.parque = parque;
        setName("Hilo-Tren");
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                parque.gestionarTren();
            }
        } catch (InterruptedException e) {
            // interrumpido, termina
            Thread.currentThread().interrupt();
        }
    }
}

