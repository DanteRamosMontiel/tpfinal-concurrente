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
            while (true) {
                int m;
                // reset contador de actividades por "día"
                i = 0;
                do {
                    m = parque.tomarMolinete();
                    if (m == -1) {
                        if (parque.debeExpulsarVisitantes()) {
                            System.out.println("El visitante " + id + " fue expulsado temporalmente del parque, esperando reapertura.");
                            parque.esperarApertura();
                            // tras la espera, reintenta tomar molinete
                        } else {
                            System.out.println("PARQUE CERRADO para nuevos ingresos");
                            Thread.sleep(6000);
                        }
                    }
                } while (m == -1);
            System.out.println("El visitante N°" + id + " tomó el molinete " + m);
            Thread.sleep(2000);
            System.out.println("El visitante N°" + id + " solto el molinete " + m);
            parque.dejarMolinete(m);
            // ACA YO PONDRIA UN SOUT QUE INDIQUE QUE YA ENTRO AL PARQUE AL VISITANTE Y
            // SACARIA LOS TOMO/SOLTO MOLINETE

            while (i < 50 && !parque.debeExpulsarVisitantes()) {
                // si las atracciones estuvieron cerradas, el visitante sólo deambula
                if (!parque.estanAtraccionesAbiertas()) {
                    System.out.println("El visitante " + id + " deambula por el parque");
                    Thread.sleep(1000);
                    i++;
                    continue;
                }
                //int random = rand.nextInt(3);

                switch (1) {
                    case 0:
                        // MONTANIA 
                        try {
                            boolean montania = parque.entrarMontania(this);
                            if (montania) {
                                System.out.println("El visitante N°" + id + " entró a la fila de la montania rusa");
                                puntos = parque.subirVagonMontania(id);
                                this.puntosDisponibles += puntos;
                                System.out.println("El visitante N°" + id + " ganó " + puntos + " puntos, ahora tiene:" + this.puntosDisponibles);
                            } else {
                                System.out.println("El visitante N°" + id + "No pudo entrar a la montaña rusa, abandona el juego");
                            }
                        } catch (InterruptedException e) {
                            System.out.println("El visitante " + id + " fue interrumpido mientras estaba en la montaña rusa");
                        }
                        break;
                    case 1:
                        // AUTOS CHOCADORES PADRE
                        try {
                            System.out.println("El visitante N°" + id + " entró a la fila de los autos chocadores");
                            puntos = parque.entrarAutosChocadores(id);
                            this.puntosDisponibles += puntos;
                            System.out.println("El visitante N°" + id + " ganó " + puntos + " puntos, ahora tiene:" + this.puntosDisponibles);
                        } catch (InterruptedException e) {
                            System.out.println("El visitante " + id + " fue interrumpido en autos chocadores");
                        }
                        break;
                    case 2:
                        // REALIDAD VIRTUAL
                        try {
                            parque.entrarRealidadVirtual(id);
                            Thread.sleep(2000);
                            puntos = parque.salirRealidadVirtual(id);
                            this.puntosDisponibles += puntos;
                            System.out.println("El visitante N°" + id + " ganó " + puntos + " puntos, ahora tiene:" + this.puntosDisponibles);
                        } catch (InterruptedException e) {
                            System.out.println("El visitante " + id + " se movió de la cola de realidad virtual");
                        }
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
                         
                        try {
                            System.out.println("El visitante N°" + id + " entró a la fila de los gomones");
                            parque.usarGomon(id);
                        } catch (InterruptedException e) {
                            System.out.println("El visitante " + id + " salió de la cola de gomones");
                        }
                        break;
                    case 4:
                        // TIENDA DE PREMIOS
                        // 1. Entrega sus puntos y espera que el asistente los reciba
                        parque.entrarTiendaPremios(id, this.puntosDisponibles);

                        // 2. Espera a que el asistente le devuelva el saldo procesado
                        // Enviamos 0 porque lo que nos interesa es lo que RECIBIMOS del asistente
                        this.puntosDisponibles = parque.entrarTiendaPremios(-1, 0);

                        System.out.println("El visitante N°" + id + " salió de la tienda. Saldo final: " + this.puntosDisponibles);
                        break;
                    case 5:
                        // COMEDOR
                        Object[] resultado = parque.entrarComedor();
                        boolean comedor = (boolean) resultado[0];
                        if (comedor) {
                            System.out.println("El visitante N°" + id + " entró al comedor y se sentó a comer");
                            Thread.sleep(3000); // Simula el tiempo que tarda en comer
                            System.out.println("El visitante N°" + id + " terminó de comer y salió del comedor");
                            parque.salirComedor((int)resultado[1]);
                        } else {
                            System.out.println("El visitante N°" + id + " no pudo entrar al comedor, está lleno");
                            Thread.sleep(3000);
                        }
                        break;
                    default:
                        break;
                }

                i++;
            }
            }

        } catch (Exception e) {
            System.out.println("Algo salió mal.");
        }
    }
}
