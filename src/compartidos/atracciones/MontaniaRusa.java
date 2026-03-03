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
    private int sentados = 0;// contadores para saber cuántos pasajeros han sido sentados

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
        boolean retorno = false;
        retorno = colaParaSubir.offer(v); // si está llena → se va
        if (!abierto) {
            retorno = false;
        }

        return retorno;
    }

    public int subirAlVagon(int id) throws InterruptedException {
        int puntosGanados = 0;
        boolean procesoExitoso = false;

        // Solo intentamos adquirir el permiso si la atracción está abierta al llegar
        if (abierto) {
            if (colaParaSubir.size() == 10) {
                System.out.println("[MONTAÑA RUSA] La cola de la montaña rusa está llena");
            }
            // 1. ESPERAMOS NUESTRO TURNO MIENTRAS SEGUIMOS EN LA FILA
            habilitado.acquire();

            // 2. CONSEGUIMOS LUGAR: Verificamos si sigue abierto y nos sacamos de la fila
            synchronized (this) {
                if (abierto) {
                    colaParaSubir.poll(); // Ahora sí salimos de la fila
                    sentados++;
                    procesoExitoso = true;
                } else {
                    // Si el parque cerró mientras esperábamos nuestro turno
                    habilitado.release();
                }
            }
        }

        // 3. FASE DE VIAJE
        if (procesoExitoso) {
            System.out.println("[MONTAÑA RUSA]Visitante " + id + " se sentó en el vagon");
            todosSentados.release();

            synchronized (this) {
                notifyAll(); // Notificamos al simulador
            }

            try {
                esperarViaje.acquire(); // Esperamos a que el viaje termine
                bajar(id);
                puntosGanados = 16;
            } catch (InterruptedException e) {
                // Revertimos cambios si hay interrupción del sistema
                synchronized (this) {
                    if (sentados > 0)
                        sentados--;
                }
                throw e; // Relanzar la excepción real capturada es válido y necesario
            }
        } else {
            // Mensaje unificado: cae aquí si nunca entró al primer if, o si no pasó el
            // segundo
            System.out.println("[MONTAÑA RUSA] Montania cerrada, el visitante " + id + " no pudo subir.");
        }

        // Único punto de retorno en todo el método
        return puntosGanados;
    }

    // --------------HILO SIMULADOR DE LA MONTANIA------------
    public void iniciarViaje() throws InterruptedException {
        boolean puedeIniciar = false;
        // 1. Esperar a que la atracción abra
        synchronized (this) {
            while (!abierto) {
                wait();
            }
        }
        // 2. Intentar reunir a los pasajeros
        try {
            todosSentados.acquire(5);
            // Solo si después de adquirir los permisos la montaña sigue abierta
            if (abierto) {
                puedeIniciar = true;
            }
        } catch (InterruptedException e) {
            // Si se interrumpe (por cierre), simplemente no marcamos 'puedeIniciar' y
            // dejamos que el método termine
            Thread.currentThread().interrupt();
        }
        // 3. Ejecución del viaje
        if (puedeIniciar) {
            System.out.println("[MONTAÑA RUSA] MONTAÑA RUSA LLENA. INICIANDO VIAJE..." + " (Visitantes: " + sentados + ")"
                    + "Visitantes en espera: " + colaParaSubir.size());
        } else {
            System.out.println("[MONTAÑA RUSA]El viaje no pudo iniciar (atracción cerrada o interrupción).");
        }
    }

    public void terminarViaje() throws InterruptedException {
        if (abierto) {
            // Liberar a todos los pasajeros sentados
            esperarViaje.release(5);
            // Esperar a que se bajen (barrera de sincronización)
            todosBajaron.acquire(5);
            // Habilitar el vagon para la siguiente tanda
            habilitado.release(5);
            synchronized (this) {
                sentados = 0; // Reiniciamos el contador de forma segura
            }
            System.out.println("[MONTAÑA RUSA] VIAJE TERMINADO ...");
        } else {
            System.out.println("[MONTAÑA RUSA] Finalizando ciclo: la montaña ya se encuentra cerrada.");
        }
    }

    private void bajar(int id) {
        System.out.println("[MONTAÑA RUSA] Visitante " + id + " se bajo del vagon");
        synchronized (this) {
            if (sentados > 0)
                sentados--;
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