package activos.extras;

import compartidos.Parque;

public class Hora extends Thread {

    private final Parque elParque;

    public Hora(Parque elParque) {
        this.elParque = elParque;
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(10000); // Simula el paso del tiempo (10 segundo = 1 hora)
                elParque.cambiarHora();
            } catch (InterruptedException e) {
            }
        }
    }

}
