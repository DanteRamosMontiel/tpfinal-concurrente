package activos.extras;

import compartidos.Parque;
public class Bicicleta extends Thread {
    private final Parque parque;
    private final int id;

    public Bicicleta(int id, Parque parque) {
        this.id = id;
        this.parque = parque;
        setName("Bicicleta-" + id);
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                parque.gestionarBicicleta(id);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

