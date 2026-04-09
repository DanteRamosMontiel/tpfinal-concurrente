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
        colaGomones.put(id); 
        
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


    public int[] cicloGomones(int gomonId, int cantVisitantes) throws InterruptedException {

        mutex.acquire();
        cantGomonesEsperando++;

        if (cantGomonesEsperando == cantGomonesNecesarios) {
            vueltaEnProgreso = true;
            ganadorDeTandaDecidido.set(false);
            
            gomonesListosParaPartir = 0;
            visitantesEnEstaCarrera = 0;
            
            Largada.release(cantGomonesNecesarios); 
        }
        mutex.release();

        Largada.acquire();


        int[] visitantes = new int[cantVisitantes];
        for (int i = 0; i < cantVisitantes; i++) {
            visitantes[i] = colaGomones.take(); 
        }
        System.out.println("Gomon " + gomonId + " lleno con " + cantVisitantes + " pasajeros.");


        mutex.acquire();
        gomonesListosParaPartir++;
        visitantesEnEstaCarrera += cantVisitantes;


        if (gomonesListosParaPartir == cantGomonesNecesarios) {
            System.out.println(">> [GOMONES] Todos cargados. ¡Arranca el Camión y la Carrera!");
            bolsosEnTransito = visitantesEnEstaCarrera; 
            Camion.release(); 
            LargadaCarrera.release(cantGomonesNecesarios); 
        }
        mutex.release();

        LargadaCarrera.acquire(); 

        return visitantes;
    }

    public boolean finCicloGomones(int GomonId, int[] visitantes) throws InterruptedException {
        boolean esGanador = false;

        if (visitantes.length != 0) {
            if (ganadorDeTandaDecidido.compareAndSet(false, true)) {
                esGanador = true;
                System.out.println("-- [GOMONES " + GomonId + "] ¡GANADOR! Visitantes: "
                        + java.util.Arrays.toString(visitantes)
                        + " reciben " + FICHAS_CG + " fichas CG");
            } else {
                System.out.println("-- [GOMONES " + GomonId + "] cruzó la meta");
            }
            for (int visitor : visitantes) {
                Semaphore sem = semGomon.remove(visitor);
                if (sem != null) {
                    sem.release();
                }
            }
            mutex.acquire();
            cantGomonesEsperando--;
            if (cantGomonesEsperando == 0) {
                vueltaEnProgreso = false;
                ReunionFinal.release(cantGomonesNecesarios); 
            }
            mutex.release();

            ReunionFinal.acquire(); 
        }

        return esGanador;
    }

    public ConcurrentHashMap<Integer, Object> esperarBolsosCamion() throws InterruptedException {
        Camion.acquire(); 
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
            EsperarCamion.release(liberar); 
        }
    }

    public synchronized void cerrar() {
        abierto = false;
        colaGomones.clear();
        for (Semaphore s : semGomon.values()) {
            s.release(); 
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