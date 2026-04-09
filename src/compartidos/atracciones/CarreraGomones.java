package compartidos.atracciones;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class CarreraGomones {
    private final Object Bolso;
    private final Semaphore Camion = new Semaphore(0); // señal para que la camioneta parta después de G gomones
    private final Semaphore EsperarCamion = new Semaphore(0); // visitantes bloqueados esperando la camioneta
    private final ConcurrentHashMap<Integer, Object> Bolsos = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Semaphore> semGomon = new ConcurrentHashMap<>();
    private final BlockingQueue<Integer> colaGomones = new ArrayBlockingQueue<>(50);
    private final Semaphore mutex = new Semaphore(1); // para proteger contadores y control de vuelta
    private int cantGomonesNecesarios;
    private int cantGomonesEsperando;
    private final Semaphore Largada = new Semaphore(0);
    private int vueltasGomones = 0;
    // contador de visitantes esperando al camión en el destino
    private int visitantesEsperandoCamion = 0;
    // control para evitar vueltas simultaneas
    private boolean vueltaEnProgreso = false;
    private volatile boolean abierto = true;

    // --- Lógica de ganador de carrera ---
    // Marca si ya hubo un ganador en la tanda actual; se resetea al comenzar nueva tanda
    private final AtomicBoolean ganadorDeTandaDecidido = new AtomicBoolean(false);
    public static final int FICHAS_CG = 20; // fichas otorgadas al ganador

    public CarreraGomones(int cantGomones) {
        Bolso = new Object();
        this.cantGomonesEsperando = 0;
        this.cantGomonesNecesarios = cantGomones;
    }

    // -------------------------------------------------Metodo para
    // visitantes---------------------------------------------//
    public void usarGomon(int id) throws InterruptedException {
        if (abierto) {
            synchronized (Bolso) {
                Bolsos.put(id, new Object()); // cada visitante tiene su propio "bolso"
                System.out.println("Visitante " + id + " tomó un bolso");
            }
            // si cierran mientras espera esto se convertirá en un simple retorno
            colaGomones.put(id); // se encola la petición para usar el gomon
            Semaphore sem = new Semaphore(0);
            semGomon.put(id, sem); // se almacena el semáforo para que el gomon lo despierte
            sem.acquire(); // el visitante espera a que el gomon lo libere
            if (abierto) {
                // abandonar despues de despertar
                System.out.println("Visitante " + id + " terminó la carrera en gomon, aguardando el camión");
                // El visitante llega al destino y se cuenta entre los que esperan el camión
                synchronized (this) {
                    visitantesEsperandoCamion++;
                }
                EsperarCamion.acquire(); // espera a que el camión lleve los bolsos al destino
                // El camión llegó, el visitante reduce el contador
                synchronized (this) {
                    visitantesEsperandoCamion--;
                }

                synchronized (Bolso) {
                    if (Bolsos.containsKey(id)) {
                        Bolsos.remove(id); // el visitante recupera su bolso
                        System.out.println("Visitante " + id + " recuperó su bolso");
                    }
                }
            } else {
                System.out.println("Gomones cerrados, visitante " + id + " deambula");
            }
        } else {
            System.out.println("Gomones cerrados, visitante " + id + " deambula");
        }
    }

    // -------------------------------------------------Metodo para
    // gomon---------------------------------------------//
    public int[] cicloGomones(int gomonId, int cantVisitantes) throws InterruptedException {

        // =========================
        // FASE 1 - FORMAR TANDA
        // =========================
        mutex.acquire();

        cantGomonesEsperando++;

        if (cantGomonesEsperando == cantGomonesNecesarios) {
            vueltaEnProgreso = true;
            // Resetear ganador al comenzar nueva tanda
            ganadorDeTandaDecidido.set(false);

            // libera gomones
            Largada.release(cantGomonesNecesarios);

            // despierta al camión
            Camion.release();
        }

        mutex.release();

        // Esperar habilitación de largada
        Largada.acquire();

        // =========================
        // FASE 2 - TOMAR PASAJEROS
        // =========================

        int[] visitantes = new int[cantVisitantes];

        for (int i = 0; i < cantVisitantes; i++) {
            visitantes[i] = colaGomones.take();
        }

        System.out.println("Gomon " + gomonId +
                " sale con " + cantVisitantes + " pasajeros.");

        return visitantes;
    }

    /**
     * Finaliza el ciclo de un gomón. Retorna true si este gomón ganó la carrera
     * (es el primero en llegar de la tanda), false en caso contrario.
     */
    public boolean finCicloGomones(int GomonId, int[] visitantes) throws InterruptedException {

        boolean esGanador = false;

        if (visitantes.length != 0) {

            // Determinar si este gomón es el ganador de la tanda (el primero en llegar)
            if (ganadorDeTandaDecidido.compareAndSet(false, true)) {
                esGanador = true;
                System.out.println("-- [GOMONES " + GomonId + "] ¡GANADOR DE LA CARRERA! Visitantes: "
                        + java.util.Arrays.toString(visitantes)
                        + " reciben " + FICHAS_CG + " fichas CG");
            } else {
                System.out.println("-- [GOMONES " + GomonId + "] carrera finalizada (no ganador)");
            }

            for (int visitor : visitantes) {
                Semaphore sem = semGomon.remove(visitor);
                if (sem != null) {
                    sem.release();
                }
            }

            // Controlar fin de tanda
            mutex.acquire();
            cantGomonesEsperando--;

            if (cantGomonesEsperando == 0) {
                vueltaEnProgreso = false;
            }

            mutex.release();
        }

        return esGanador;
    }

    // -------------------------------------------------Metodo para camion---------------------------------------------//
    public ConcurrentHashMap<Integer, Object> esperarBolsosCamion() throws InterruptedException {

        Camion.acquire(); // SOLO el camión usa esto

        System.out.println("Camioneta: Camion lleno, iniciando viaje.");

        return Bolsos;
    }

    public void finViajeCamion() {
        System.out.println(">> [CAMION] viaje finalizado, dejando a los bolsos en el destino");
        int liberar;
        synchronized (this) {
            liberar = visitantesEsperandoCamion;
        }
        if (liberar > 0) {
            System.out.println(">> [CAMION] libera a " + liberar + " visitantes");
            EsperarCamion.release(liberar); // libera a todos los visitantes que esperaban
        } else {
            System.out.println(">> [CAMION] no había visitantes esperando");
        }
        // marca que la vuelta se completó y los gomones pueden iniciar siguiente
        synchronized (this) {
            vueltaEnProgreso = false;
            notifyAll(); // despierta gomones que estaban esperando
        }
        System.out.println(">> [CAMION] vuelve a dormirse");
    }

    /** marca la atracción cerrada y libera a los visitantes que esperan */
    public synchronized void cerrar() {
        abierto = false;
        colaGomones.clear();
        // liberar todos los semáforos de gomon para que no queden dormidos
        for (Semaphore s : semGomon.values()) {
            s.release();
        }
        semGomon.clear();
        // liberar también a los que esperan al camión
        EsperarCamion.release(1000);
    }

    public synchronized void abrir() {
        abierto = true;
    }

    public synchronized boolean estaVacio() {
        return colaGomones.isEmpty() && semGomon.isEmpty() && visitantesEsperandoCamion == 0;
    }
}