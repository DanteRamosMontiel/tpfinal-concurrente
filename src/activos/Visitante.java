package activos;

import java.util.Random;
import compartidos.Parque;

public class Visitante extends Thread {

    private final Parque parque;
    private final int id;
    private final Random rand;

    public Visitante(int xId, Parque xParque) {
        this.parque = xParque;
        this.id = xId;
        this.rand = new Random();
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
            // ACA YO PONDRIA UN SOUT QUE INDIQUE QUE YA ENTRO AL PARQUE AL VISITANTE Y
            // SACARIA LOS TOMO/SOLTO MOLINETE

            while (true) {
                int random = rand.nextInt(3);

                switch (random) {
                    case 0:
                        // MONTANIA 
                        boolean montania = parque.entrarMontania(this);
                        if (montania) {
                            System.out.println("El visitante N°" + id +
                                    " entró a la fila de la montania rusa");
                            parque.subirVagonMontania(id);
                        } else {
                            System.out.println("El visitante N°" + id +
                                    "No pudo entrar a la montaña rusa, abandona el juego");
                        }
                        
                        break;
                    case 1:
                        // AUTOS CHOCADORES PADRE
                        System.out.println("El visitante N°" + id + " entró a la fila de los autos chocadores");
                        parque.entrarAutosChocadores(id);
                        break;
                    case 2:
                        // REALIDAD VIRTUAL
                      
                        parque.entrarRealidadVirtual(id);
                        Thread.sleep(2000);

                        parque.salirRealidadVirtual(id);
                        

                         break;
                    default:
                        break;
                }

            }

        } catch (InterruptedException e) {
        }
    }
}