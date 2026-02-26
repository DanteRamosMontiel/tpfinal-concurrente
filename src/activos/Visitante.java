package activos;

import compartidos.Parque;
import java.util.Random;

public class Visitante extends Thread {

    private final Parque parque;
    private final int id;
    private int puntosDisponibles;
    private int puntos;
    private int i = 0;
    private final Random rand;

    public Visitante(int xId, Parque xParque) {
        this.parque = xParque;
        this.id = xId;
        this.puntosDisponibles = 0;
        this.rand = new Random();
        this.puntos = 0;
    }

    @Override
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

            while (i < 5) {
                //int random = rand.nextInt(3);

                switch (i) {
                    case 0:
                        // MONTANIA 
                        boolean montania = parque.entrarMontania(this);
                        if (montania) {
                            System.out.println("El visitante N°" + id + " entró a la fila de la montania rusa");
                            puntos = parque.subirVagonMontania(id);
                            this.puntosDisponibles += puntos;
                            System.out.println("El visitante N°" + id + " ganó " + puntos + " puntos, ahora tiene:" + this.puntosDisponibles);
                        } else {
                            System.out.println("El visitante N°" + id + "No pudo entrar a la montaña rusa, abandona el juego");
                        }
                        break;
                    case 1:
                        // AUTOS CHOCADORES PADRE
                        System.out.println("El visitante N°" + id + " entró a la fila de los autos chocadores");
                        puntos = parque.entrarAutosChocadores(id);
                        this.puntosDisponibles += puntos;
                        System.out.println("El visitante N°" + id + " ganó " + puntos + " puntos, ahora tiene:" + this.puntosDisponibles);
                        break;
                    case 2:
                        // REALIDAD VIRTUAL
                        parque.entrarRealidadVirtual(id);
                        Thread.sleep(2000);
                        puntos = parque.salirRealidadVirtual(id);
                        this.puntosDisponibles += puntos;
                        System.out.println("El visitante N°" + id + " ganó " + puntos + " puntos, ahora tiene:" + this.puntosDisponibles);
                        break;
                    case 3:
                        // GOMONES
                       if (rand.nextBoolean()) {
                            System.out.println("El visitante N°" + id + " quiere usar una bicicleta");
                            parque.usarBicicleta(id);
                        } else {
                            System.out.println("El visitante N°" + id + " quiere tomar el tren");
                            parque.subirTren(id); 
                        }
                         
                        System.out.println("El visitante N°" + id + " entró a la fila de los gomones");
                        parque.usarGomon(id);
                        break;
                    case 4:
                        // 1. Entrega sus puntos y espera que el asistente los reciba
                        parque.entrarTiendaPremios(id, this.puntosDisponibles);

                        // 2. Espera a que el asistente le devuelva el saldo procesado
                        // Enviamos 0 porque lo que nos interesa es lo que RECIBIMOS del asistente
                        this.puntosDisponibles = parque.entrarTiendaPremios(-1, 0);

                        System.out.println("El visitante N°" + id + " salió de la tienda. Saldo final: " + this.puntosDisponibles);
                        break;
                    
                    default:
                        break;
                }

                i++;
            }

        } catch (InterruptedException e) {
        }
    }
}
