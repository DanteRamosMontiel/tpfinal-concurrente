package compartidos.atracciones;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class CarreraGomones {
    private final Object Bolso = new Object();
    
    // Semáforos de interacción con el camión
    private final Semaphore Camion = new Semaphore(0); 
    private final Semaphore EsperarCamion = new Semaphore(0); 
    
    private final ConcurrentHashMap<Integer, Object> Bolsos = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Semaphore> semGomon = new ConcurrentHashMap<>();
    private final BlockingQueue<Integer> colaGomones = new ArrayBlockingQueue<>(50);
    
    private final Semaphore mutex = new Semaphore(1); // Protege contadores
    
    private int cantGomonesNecesarios;
    
    // Barreras cíclicas hechas con semáforos
    private final Semaphore Largada = new Semaphore(0);
    private final Semaphore LargadaCarrera = new Semaphore(0);
    private final Semaphore ReunionFinal = new Semaphore(0);
    
    // Contadores de ciclo
    private int cantGomonesEsperando = 0;
    private int gomonesListosParaPartir = 0;
    private int visitantesEnEstaCarrera = 0;
    private int bolsosEnTransito = 0; 
    
    private boolean vueltaEnProgreso = false;
    private volatile boolean abierto = true;
    
    private final AtomicBoolean ganadorDeTandaDecidido = new AtomicBoolean(false);
    public static final int FICHAS_CG = 20; 

    public CarreraGomones(int cantGomones) {
        this.cantGomonesNecesarios = cantGomones;
    }

    // -------------------------------------------------
    // METODO PARA VISITANTES
    // -------------------------------------------------
    public void usarGomon(int id) throws InterruptedException {
        if (!abierto) {
            System.out.println("Gomones cerrados, visitante " + id + " deambula");
            return;
        }

        synchronized (Bolso) {
            Bolsos.put(id, new Object()); 
            System.out.println("Visitante " + id + " tomó un bolso y lo dejó para la camioneta");
        }

        Semaphore sem = new Semaphore(0);
        semGomon.put(id, sem); 
        colaGomones.put(id); // Ahora sí, el gomón lo puede sacar
        
        sem.acquire(); // Espera a que el gomón termine la carrera
        
        if (abierto) {
            System.out.println("Visitante " + id + " terminó la carrera, esperando el camión...");
            EsperarCamion.acquire(); // Espera los bolsos
            
            synchronized (Bolso) {
                if (Bolsos.containsKey(id)) {
                    Bolsos.remove(id);
                    System.out.println("Visitante " + id + " recuperó su bolso");
                }
            }
        } else {
            System.out.println("Visitante " + id + " evacuado de gomones.");
        }
    }

    // -------------------------------------------------
    // METODO PARA GOMONES
    // -------------------------------------------------
    public int[] cicloGomones(int gomonId, int cantVisitantes) throws InterruptedException {
        
        // =========================
        // FASE 1: LLEGAR A LA ESTACIÓN
        // =========================
        mutex.acquire();
        cantGomonesEsperando++;

        if (cantGomonesEsperando == cantGomonesNecesarios) {
            vueltaEnProgreso = true;
            ganadorDeTandaDecidido.set(false);
            
            // Reseteamos contadores para la siguiente fase
            gomonesListosParaPartir = 0;
            visitantesEnEstaCarrera = 0;
            
            Largada.release(cantGomonesNecesarios); // Libera la barrera 1
        }
        mutex.release();

        Largada.acquire(); // Esperan a que toda la tanda de gomones haya llegado

        // =========================
        // FASE 2: TOMAR PASAJEROS
        // =========================
        int[] visitantes = new int[cantVisitantes];
        for (int i = 0; i < cantVisitantes; i++) {
            visitantes[i] = colaGomones.take(); // Esperan ordenadamente a los visitantes
        }
        System.out.println("Gomon " + gomonId + " lleno con " + cantVisitantes + " pasajeros.");

        // =========================
        // FASE 3: BARRERA DE LARGADA
        // =========================
        mutex.acquire();
        gomonesListosParaPartir++;
        visitantesEnEstaCarrera += cantVisitantes;

        // FIX 2: Solo el ÚLTIMO gomón arranca el camión y la carrera para todos
        if (gomonesListosParaPartir == cantGomonesNecesarios) {
            System.out.println(">> [GOMONES] Todos cargados. ¡Arranca el Camión y la Carrera!");
            bolsosEnTransito = visitantesEnEstaCarrera; // Congelamos la cantidad para el camión
            Camion.release(); 
            LargadaCarrera.release(cantGomonesNecesarios); // Libera la barrera 2
        }
        mutex.release();

        LargadaCarrera.acquire(); // Se alinean y largan TODOS JUNTOS

        return visitantes;
    }

    public boolean finCicloGomones(int GomonId, int[] visitantes) throws InterruptedException {
        boolean esGanador = false;

        if (visitantes.length != 0) {
            // Determinar ganador atómicamente
            if (ganadorDeTandaDecidido.compareAndSet(false, true)) {
                esGanador = true;
                System.out.println("-- [GOMONES " + GomonId + "] ¡GANADOR! Visitantes: "
                        + java.util.Arrays.toString(visitantes)
                        + " reciben " + FICHAS_CG + " fichas CG");
            } else {
                System.out.println("-- [GOMONES " + GomonId + "] cruzó la meta");
            }

            // Liberar a sus visitantes
            for (int visitor : visitantes) {
                Semaphore sem = semGomon.remove(visitor);
                if (sem != null) {
                    sem.release();
                }
            }

            // =========================
            // FASE 4: REUNIÓN FINAL
            // =========================
            mutex.acquire();
            cantGomonesEsperando--; // Usamos el contador a la inversa

            // FIX 3: Nadie empieza otra vuelta hasta que todos hayan cruzado la meta
            if (cantGomonesEsperando == 0) {
                vueltaEnProgreso = false;
                ReunionFinal.release(cantGomonesNecesarios); // Libera la barrera 3
            }
            mutex.release();

            ReunionFinal.acquire(); 
        }

        return esGanador;
    }

    // -------------------------------------------------
    // METODOS PARA LA CAMIONETA
    // -------------------------------------------------
    public ConcurrentHashMap<Integer, Object> esperarBolsosCamion() throws InterruptedException {
        Camion.acquire(); // Se despierta en la Fase 3 de los gomones
        System.out.println("Camioneta: Iniciando viaje con " + bolsosEnTransito + " bolsos.");
        return Bolsos;
    }

    public void finViajeCamion() {
        System.out.println(">> [CAMION] Viaje finalizado, dejando bolsos en destino");
        
        int liberar;
        try {
            mutex.acquire();
            liberar = bolsosEnTransito; 
            mutex.release();
        } catch (InterruptedException e) {
            liberar = bolsosEnTransito; 
        }
        
        if (liberar > 0) {
            System.out.println(">> [CAMION] Libera " + liberar + " permisos para visitantes");
            EsperarCamion.release(liberar); // Deja permisos exactos
        }
    }

    public synchronized void cerrar() {
        abierto = false;
        colaGomones.clear();
        for (Semaphore s : semGomon.values()) {
            s.release(); // Libera atrapados
        }
        semGomon.clear();
        EsperarCamion.release(1000); 
    }

    public synchronized void abrir() {
        abierto = true;
    }

    public synchronized boolean estaVacio() {
        return colaGomones.isEmpty() && semGomon.isEmpty();
    }
}