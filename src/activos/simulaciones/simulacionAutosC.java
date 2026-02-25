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
                ;
                System.out.println("AUTOS CHOCADORES LLENOS, INICIANDO ATRACCION...");
                Thread.sleep(7000);
                System.out.println("AUTOS CHOCADORES TERMINADOS...");
                Thread.sleep(2000);
                parque.terminarViajeAutosC();
            } catch (InterruptedException e) {
            }
        }
    }
}
