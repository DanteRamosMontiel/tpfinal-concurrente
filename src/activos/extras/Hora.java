package activos.extras;

import compartidos.*;

public class Hora extends Thread {

    private final Parque elParque;

    public Hora(Parque elParque) {
        this.elParque = elParque;
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(5000); // Simula el paso del tiempo (20 segundo = 1 hora)
                elParque.cambiarHora();
            } catch (InterruptedException e) {
            }
        }
    }

}
