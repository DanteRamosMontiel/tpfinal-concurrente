package activos.simulaciones;

import compartidos.Parque;

public class simulacionAutosC extends Thread {

    private Parque parque;

    public simulacionAutosC(Parque xParque) {
        this.parque = xParque;
    }

    public void run() {
        while (true) {
            try {
                parque.iniciarViajeAutosC();
                Thread.sleep(9000);
                parque.terminarViajeAutosC();
            } catch (InterruptedException e) {
            }
        }
    }
}
