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

    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && !parque.parqueCerradoDefinitivamente()) {
                int m;
                i = 0;
                do {
                    m = parque.tomarMolinete();
                    if (m == -1) {
                       if (parque.debeExpulsarVisitantes()) {
                            System.out.println("[VISITANTE]El visitante " + id
                                    + " fue expulsado temporalmente del parque, esperando reapertura.");
                            parque.esperarApertura();
                        } else {
                            System.out.println("[PARQUE] CERRADO para nuevos ingresos");
                            Thread.sleep(6000);
                        }
                    }
                } while (m == -1);
                Thread.sleep(2000);
                System.out.println("[VISITANTE]El visitante N°" + id + " entro al parque utilizando el molinete " + m);
                parque.dejarMolinete(m);

                while (true && !parque.debeExpulsarVisitantes()) {
                    Thread.sleep(500);
                    if (!parque.estanAtraccionesAbiertas()) {
                        if (parque.hayEspectaculoParaEntrar()) {
                            try {
                                parque.entrarEspectaculo(id);
                                i++;
                                continue;
                            } catch (InterruptedException e) {
                                System.out
                                        .println("[VISITANTE] El visitante " + id
                                                + " fue interrumpido en el espectáculo");
                            }
                        } else {
                            System.out.println("[VISITANTE]Visitante N°" + id + " deambula por el parque");
                            Thread.sleep(1000);
                            i++;
                        }
                        continue;
                    }

                    if (parque.hayEspectaculoParaEntrar()) {
                        try {
                            parque.entrarEspectaculo(id);
                            System.out.println("[VISITANTE] El visitante N°" + id + " salió del espectáculo");
                            i++;
                            continue;

                        } catch (InterruptedException e) {
                            System.out
                                    .println("[VISITANTE] El visitante " + id + " fue interrumpido en el espectáculo");
                        }
                    }

                    switch (/*rand.nextInt(6)*/6) {
                        case 0:
                            // MONTANIA
                            try {
                                boolean montania = parque.entrarMontania(this);
                                if (montania) {
                                    System.out.println(
                                            "[VISITANTE]visitante N°" + id + " entró a la fila de la montania rusa");
                                    puntos = parque.subirVagonMontania(id);
                                    this.puntosDisponibles += puntos;
                                    System.out.println("[VISITANTE]El visitante N°" + id + " ganó " + puntos
                                            + " puntos, ahora tiene:" + this.puntosDisponibles);
                                }

                            } catch (InterruptedException e) {
                                System.out.println("[VISITANTE]El visitante " + id
                                        + " fue interrumpido mientras estaba en la montaña rusa");
                            }
                            Thread.sleep(500);
                            break;
                        case 1:
                            // AUTOS CHOCADORES
                            try {
                                System.out.println(
                                        "[VISITANTE]El visitante N°" + id + " entró a la fila de los autos chocadores");
                                puntos = parque.entrarAutosChocadores(id);
                                this.puntosDisponibles += puntos;
                                System.out.println("[VISITANTE]El visitante N°" + id + " ganó " + puntos
                                        + " puntos, ahora tiene:" + this.puntosDisponibles);
                            } catch (InterruptedException e) {
                                System.out.println(
                                        "[VISITANTE]El visitante " + id + " fue interrumpido en autos chocadores");
                            }
                            Thread.sleep(500);
                            break;
                        case 2:
                            // REALIDAD VIRTUAL
                            try {
                                parque.entrarRealidadVirtual(id);
                                Thread.sleep(2000);
                                puntos = parque.salirRealidadVirtual(id);
                                this.puntosDisponibles += puntos;
                                System.out.println("[VISITANTE]El visitante N°" + id + " ganó " + puntos
                                        + " puntos, ahora tiene:" + this.puntosDisponibles);
                            } catch (InterruptedException e) {
                                System.out.println(
                                        "[VISITANTE]El visitante " + id + " se movió de la cola de realidad virtual");
                            }
                            Thread.sleep(500);
                            break;
                        case 3:
                            // GOMONES
                            if (rand.nextBoolean()) {
                                System.out.println("[VISITANTE]El visitante N°" + id + " quiere usar una bicicleta");
                                parque.usarBicicleta(id);
                            } else {
                                System.out.println("[VISITANTE]El visitante N°" + id + " quiere tomar el tren");
                                parque.subirTren(id);
                            }
                            Thread.sleep(500);
                            try {
                                System.out
                                        .println("[VISITANTE]El visitante N°" + id + " entró a la fila de los gomones");
                                parque.usarGomon(id);
                                int fichasCG = parque.retirarFichasCG(id);
                                if (fichasCG > 0) {
                                    this.puntosDisponibles += fichasCG;
                                    System.out.println("[VISITANTE]El visitante N°" + id + " ganó " + fichasCG
                                            + " fichas CG, ahora tiene: " + this.puntosDisponibles);
                                }
                            } catch (InterruptedException e) {
                                System.out.println("[VISITANTE]El visitante " + id + " salió de la cola de gomones");
                            }
                            Thread.sleep(500);
                            break;
                        case 4:
                            // TIENDA DE PREMIOS
                            this.puntosDisponibles = parque.entrarTiendaPremios(id, this.puntosDisponibles);
                            System.out.println("[VISITANTE]El visitante N°" + id + " salió de la tienda. Saldo final: "
                                    + this.puntosDisponibles);
                            Thread.sleep(500);
                            break;
                        case 5:
                            // COMEDOR
                            Object[] resultado = parque.entrarComedor();
                            boolean comedor = (boolean) resultado[0];
                            if (comedor) {
                                System.out.println(
                                        "[VISITANTE]El visitante N°" + id + " entró al comedor y se sentó a comer");
                                Thread.sleep(3000); // Simula el tiempo que tarda en comer
                                System.out.println(
                                        "[VISITANTE]El visitante N°" + id + " terminó de comer y salió del comedor");
                                parque.salirComedor((int) resultado[1]);
                            } else {
                                System.out.println(
                                        "[VISITANTE]El visitante N°" + id + " no pudo entrar al comedor, está lleno");
                                Thread.sleep(3000);
                            }
                            Thread.sleep(500);
                            break;

                        default:
                            break;
                    }

                    i++;
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[VISITANTE] Visitante N°" + id + " salió del parque definitivamente.");
        } catch (Exception e) {
            System.out.println("Algo salió mal con el visitante " + id + ": " + e.getMessage());
        }
        System.out.println("[VISITANTE] Visitante N°" + id + " terminó su visita.");
    }
}