package activos;

import compartidos.Parque;

public class Visitante extends Thread {

    private final Parque parque;
    private final int id;

    public Visitante(int xId, Parque xParque) {
        this.parque = xParque;
        this.id = xId;
    }

    public void run() {
        try {
            int m;
            do {
                m = parque.tomarMolinete();
                if (m == -1) {
                    System.out.println("PARQUE CERRADO");
                    Thread.sleep(6000);
                }
            } while (m == -1);
            System.out.println("El visitante N°" + id + " tomó el molinete " + m);
            Thread.sleep(2000);
            System.out.println("El visitante N°" + id + " solto el molinete " + m);
            parque.dejarMolinete(m);
            while (true) {
                //Entra a montania rusa
                parque.entrarMontania(this);
                System.out.println("El visitante N°" + id + " entró a la fila de la montania rusa");
                parque.subirVagonMontania();
                System.out.println("El visitante N°" + id + " entró a un asiento de la montania rusa");
                parque.esperarMontania();
                parque.bajarMontania();
                System.out.println("El visitante N°" + id + " bajó de la montania rusa");
            }
        }catch (InterruptedException e){}
    }
}