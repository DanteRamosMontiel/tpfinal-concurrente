package compartidos.atracciones;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class CarreraGomones {

    // Objeto usado para sincronizar acceso a los bolsos
    private final Object Bolso = new Object();

    // Semáforos de interacción entre gomones y camión
    private final Semaphore Camion = new Semaphore(0); // Señal para que el camión inicie su viaje
    private final Semaphore EsperarCamion = new Semaphore(0); // Los visitantes esperan a recuperar sus bolsos

    // Almacena los bolsos de cada visitante (id -> bolso)
    private final ConcurrentHashMap<Integer, Object> Bolsos = new ConcurrentHashMap<>();
    // Semáforo individual para cada visitante (para esperar que su gomón termine)
    private final ConcurrentHashMap<Integer, Semaphore> semGomon = new ConcurrentHashMap<>();
    // Cola donde los visitantes se ponen en orden para subir a los gomones
    private final BlockingQueue<Integer> colaGomones = new ArrayBlockingQueue<>(50);

    // Semáforo para proteger los contadores compartidos
    private final Semaphore mutex = new Semaphore(1);

    // Cantidad de gomones necesarios para llenar una carrera
    private int cantGomonesNecesarios;

    // Barreras cíclicas para sincronizar los gomones entre sí
    private final Semaphore Largada = new Semaphore(0);           // Esperar a que todos los gomones estén listos
    private final Semaphore LargadaCarrera = new Semaphore(0);    // Esperar a que el camión inicie su viaje
    private final Semaphore ReunionFinal = new Semaphore(0);      // Esperar a que todos terminen

    // Contadores para la sincronización del ciclo actual
    private int cantGomonesEsperando = 0;      // Gomones que ya se cargaron
    private int gomonesListosParaPartir = 0;   // Gomones que informaron estar cargados
    private int visitantesEnEstaCarrera = 0;   // Total de visitantes en esta carrera
    private int bolsosEnTransito = 0;          // Bolsos que viajan en el camión

    // Indica si hay una carrera en progreso
    private boolean vueltaEnProgreso = false;
    // Indica si la atracción está abierta
    private volatile boolean abierto = true;

    // Flag atómico para decidir un único ganador por carrera
    private final AtomicBoolean ganadorDeTandaDecidido = new AtomicBoolean(false);
    public static final int FICHAS_CG = 20; // Puntos que gana el ganador 

    public CarreraGomones(int cantGomones) {
        this.cantGomonesNecesarios = cantGomones;
    }

    // Visitante usa un gomón: deja su bolso disponible y luego espera a que la carrera termine
    public void usarGomon(int id) throws InterruptedException {
        // Si la atracción cerró, nos vamos
        if (!abierto) {
            System.out.println("Gomones cerrados, visitante " + id + " deambula");
            return;
        }

        // Dejamos nuestro bolso en un lugar seguro para que el camión lo recoja
        synchronized (Bolso) {
            Bolsos.put(id, new Object());
            System.out.println("Visitante " + id + " tomó un bolso y lo dejó para la camioneta");
        }

        // Creamos un semáforo personal para este visitante
        Semaphore sem = new Semaphore(0);
        semGomon.put(id, sem);
        // Nos metemos en la cola para subir al gomón
        colaGomones.put(id);

        // Esperamos a que nuestro gomón termine la carrera
        sem.acquire();

        // Si la carrera terminó normalmente, esperamos a que el camión devuelva nuestro bolso
        if (abierto) {
            System.out.println("Visitante " + id + " terminó la carrera, esperando el camión...");
            EsperarCamion.acquire();

            // Recuperamos nuestro bolso
            synchronized (Bolso) {
                if (Bolsos.containsKey(id)) {
                    Bolsos.remove(id);
                    System.out.println("Visitante " + id + " recuperó su bolso");
                }
            }
        } else {
            // Si cerraron durante la carrera, nos evacuamos sin bolso
            System.out.println("Visitante " + id + " evacuado de gomones.");
        }
    }

    // El gomón busca visitantes de la cola y carga a los que necesita para llenar
    public int[] cicloGomones(int gomonId, int cantVisitantes) throws InterruptedException {
        // Incrementamos el contador de gomones esperando
        mutex.acquire();
        cantGomonesEsperando++;

        // Cuando todos los gomones están listos, iniciamos la carrera
        if (cantGomonesEsperando == cantGomonesNecesarios) {
            vueltaEnProgreso = true;
            ganadorDeTandaDecidido.set(false);

            gomonesListosParaPartir = 0;
            visitantesEnEstaCarrera = 0;

            // Liberamos a todos los gomones para que comiencen a cargar visitantes
            Largada.release(cantGomonesNecesarios);
        }
        mutex.release();

        // Esperamos a que todos los gomones lleguen a este punto
        Largada.acquire();

        // Tomamos visitantes de la cola según nuestra capacidad
        int[] visitantes = new int[cantVisitantes];
        for (int i = 0; i < cantVisitantes; i++) {
            visitantes[i] = colaGomones.take();
        }
        System.out.println("Gomon " + gomonId + " lleno con " + cantVisitantes + " pasajeros.");

        // Reportamos que estamos listos para partir
        mutex.acquire();
        gomonesListosParaPartir++;
        visitantesEnEstaCarrera += cantVisitantes;

        // Cuando todos los gomones están cargados, enviamos el camión y arrancamos
        if (gomonesListosParaPartir == cantGomonesNecesarios) {
            System.out.println(">> [GOMONES] Todos cargados. ¡Arranca el Camión y la Carrera!");
            bolsosEnTransito = visitantesEnEstaCarrera;
            Camion.release(); // Camión puede recoger los bolsos
            LargadaCarrera.release(cantGomonesNecesarios); // Gomones pueden empezar a correr
        }
        mutex.release();

        // Esperamos a que el camión inicie su viaje
        LargadaCarrera.acquire();

        return visitantes;
    }

    // El gomón reporta que terminó la carrera y sus visitantes pueden bajar
    public boolean finCicloGomones(int GomonId, int[] visitantes) throws InterruptedException {
        boolean esGanador = false;

        if (visitantes.length != 0) {
            // Solo el primer gomón en cruzar la meta es ganador
            if (ganadorDeTandaDecidido.compareAndSet(false, true)) {
                esGanador = true;
                System.out.println("-- [GOMONES " + GomonId + "] ¡GANADOR! Visitantes: "
                        + java.util.Arrays.toString(visitantes)
                        + " reciben " + FICHAS_CG + " fichas CG");
            } else {
                System.out.println("-- [GOMONES " + GomonId + "] cruzó la meta");
            }

            // Liberamos a todos los visitantes de este gomón para que comiencen a bajar
            for (int visitor : visitantes) {
                Semaphore sem = semGomon.remove(visitor);
                if (sem != null) {
                    sem.release();
                }
            }

            // Reportamos que este gomón terminó
            mutex.acquire();
            cantGomonesEsperando--;
            // Cuando todos los gomones terminan, finalizamos la vuelta
            if (cantGomonesEsperando == 0) {
                vueltaEnProgreso = false;
                ReunionFinal.release(cantGomonesNecesarios); // Todos pueden empezar una nueva vuelta
            }
            mutex.release();

            // Esperamos a que todos terminen
            ReunionFinal.acquire();
        }

        return esGanador;
    }

    // El camión espera a que los gomones terminen su carga y se va a recoger los bolsos
    public ConcurrentHashMap<Integer, Object> esperarBolsosCamion() throws InterruptedException {
        // Esperamos a que todos los gomones estén listos y la carrera inicie
        Camion.acquire();
        System.out.println("Camioneta: Iniciando viaje con " + bolsosEnTransito + " bolsos.");
        return Bolsos;
    }

    // El camión termina su viaje y devuelve los bolsos a los visitantes
    public void finViajeCamion() {
        System.out.println(">> [CAMION] Viaje finalizado, dejando bolsos en destino");

        // Determinamos cuántos bolsos tenemos que liberar
        int liberar;
        try {
            mutex.acquire();
            liberar = bolsosEnTransito;
            mutex.release();
        } catch (InterruptedException e) {
            liberar = bolsosEnTransito;
        }

        // Liberamos a los visitantes para que recuperen sus bolsos
        if (liberar > 0) {
            System.out.println(">> [CAMION] Libera " + liberar + " permisos para visitantes");
            EsperarCamion.release(liberar);
        }
    }

    // Cierra la atracción y libera a todos los hilos bloqueados
    public synchronized void cerrar() {
        abierto = false;
        // Limpiamos la cola de visitantes esperando
        colaGomones.clear();
        // Liberamos a todos los visitantes que estaban esperando el final de la carrera
        for (Semaphore s : semGomon.values()) {
            s.release();
        }
        semGomon.clear();
        // Liberamos a los visitantes esperando sus bolsos
        EsperarCamion.release(1000);
    }

    // Reabre la atracción
    public synchronized void abrir() {
        abierto = true;
    }

    // Verifica si hay visitantes o gomones esperando
    public synchronized boolean estaVacio() {
        return colaGomones.isEmpty() && semGomon.isEmpty();
    }
}
