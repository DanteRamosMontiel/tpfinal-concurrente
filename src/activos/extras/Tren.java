package activos.extras;

import compartidos.Parque;

/**
 * Hilo que representa el único tren del parque. Se queda dormido en
 * {@link Parque#gestionarTren()} hasta que 15 visitantes hayan subido,
 * procesa la tanda y repite indefinidamente.
 */
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

