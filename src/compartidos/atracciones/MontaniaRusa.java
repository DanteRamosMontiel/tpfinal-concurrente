package compartidos.atracciones;

import activos.Visitante;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

public class MontaniaRusa {

    private final BlockingQueue<Visitante> colaParaSubir;
    private final Semaphore habilitado; // controla 5 por tanda (permite hasta 5 visitantes sentarse simultáneamente)
    private final Semaphore todosSentados; // barrera de 5
    private final Semaphore esperarViaje; // espera fin del viaje
    private final Semaphore todosBajaron;

    private boolean abierto = true;

    public MontaniaRusa() {
        this.colaParaSubir = new ArrayBlockingQueue<>(10); // espacio de espera
        this.habilitado = new Semaphore(5); // cinco permisos para una tanda
        this.todosSentados = new Semaphore(0);
        this.esperarViaje = new Semaphore(0);
        this.todosBajaron = new Semaphore(0);
    }

    // Intenta entrar a la cola
    public boolean entrar(Visitante v) {
        if (!abierto) {
            return false;
        }
        return colaParaSubir.offer(v); // si está llena → se va
    }

    // Subir al vagón
    // contadores para saber cuántos pasajeros han sido sentados
    private int sentados = 0;

    public int subirAlVagon(int id) throws InterruptedException {
        // si la atracción cerró antes de subir, devolvemos 0
        if (!abierto) {
            System.out.println("Montania cerrada, visitante " + id + " no sube");
            throw new InterruptedException();
        }
        // esperar a salir de la cola; usamos poll para poder despertar cuando cierren
        while (true) {
            if (!abierto) {
                System.out.println("Montania cerrada mientras " + id + " esperaba, vuelve a deambular");
                throw new InterruptedException();
            }
            Visitante v = colaParaSubir.poll(500, java.util.concurrent.TimeUnit.MILLISECONDS);
            if (v != null) {
                break;
            }
            // timeout, revalidar condición y seguir esperando
        }
        habilitado.acquire();
        synchronized (this) {
            if (!abierto) {
                habilitado.release();
                throw new InterruptedException();
            }
            sentados++;
        }
        System.out.println("Visitante " + id + " se sentó en el vagon");
        todosSentados.release();
        // notificar al hilo simulador que hay un nuevo pasajero
        synchronized (this) {
            notifyAll();
        }
        try {
            esperarViaje.acquire();
        } catch (InterruptedException e) {
            // si se interrumpe por cierre, salimos
            synchronized (this) {
                sentados--; // ajustamos contador
            }
            throw e;
        }
        bajar(id);
        return 16; // puntos que gana por subir a la montaña rusa
    }

    // Hilo de la montaña
    public void iniciarViaje() throws InterruptedException {
        // registrar hilo simulador para interrupciones
        synchronized (this) {
            // incluso si ya está en uso, sobrescribir no afecta
        }

        // esperar que la atracción esté abierta
        synchronized (this) {
            while (!abierto) {
                wait();
            }
        }

        // bloquear hasta que llegue un grupo completo (5 pasajeros sentados) o se cierre
        try {
            todosSentados.acquire(5);
        } catch (InterruptedException e) {
            // llegada de cierre, abandonar
            return;
        }

        // si se cerró mientras esperábamos, abandonamos sin iniciar
        if (!abierto) {
            return;
        }

        System.out.println("MONTAÑA RUSA LLENA. INICIANDO VIAJE...");
    }

    public void terminarViaje() throws InterruptedException {
        if (!abierto) {
            // si el parque cerró, los pasajeros ya fueron liberados en cerrar()
            return;
        }
        // liberar a todos los pasajeros sentados
        esperarViaje.release(5);
        // esperar a que se bajen (puede que algunos ya lo hayan hecho en cierre)
        todosBajaron.acquire(5);
        habilitado.release(5); // permite nueva tanda completa
        synchronized (this) {
            sentados = 0; // reiniciamos contador
        }
        System.out.println("VIAJE TERMINADO DE MONTAÑA RUSA...");
    }

    private void bajar(int id) {
        System.out.println("Visitante " + id + " se bajo del vagon");
        synchronized (this) {
            if (sentados > 0) sentados--;
        }
        todosBajaron.release();
    }

    public synchronized void cerrar() {
        abierto = false;
        // liberar posibles hilos bloqueados
        habilitado.release(10); // liberar suficientes permisos en cierre
        colaParaSubir.clear();

        // forzamos que cualquier visitante ya sentado salga
        esperarViaje.drainPermits();
        esperarViaje.release(5);

        // facilitamos que el simulador salga del acquire
        todosSentados.drainPermits();
        todosSentados.release(5);

        // también liberamos cualquier espera de bajada para no bloquear
        todosBajaron.drainPermits();
        todosBajaron.release(5);

        // notificar a cualquiera que esté esperando en el objeto
        notifyAll();
    }

    public synchronized void abrir() {
        abierto = true;
    }

    public synchronized boolean estaVacio() {
        return colaParaSubir.isEmpty();
    }
}