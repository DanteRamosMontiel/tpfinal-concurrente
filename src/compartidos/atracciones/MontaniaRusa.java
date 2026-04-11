package compartidos.atracciones;

import activos.Visitante;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class MontaniaRusa {

    private final BlockingQueue<Visitante> colaParaSubir;
    private final Semaphore habilitado;
    private final Semaphore todosSentados;
    private final Semaphore esperarViaje;
    private final Semaphore todosBajaron;
    private final Semaphore mutex;
    private final Semaphore condition;
    private final AtomicInteger sentados = new AtomicInteger(0);

    private volatile boolean abierto = true;

    public MontaniaRusa() {
        this.colaParaSubir = new ArrayBlockingQueue<>(10); // espacio de espera
        this.habilitado = new Semaphore(5); // cinco permisos para una tanda
        this.todosSentados = new Semaphore(0);
        this.esperarViaje = new Semaphore(0);
        this.todosBajaron = new Semaphore(0);
        this.mutex = new Semaphore(1);
        this.condition = new Semaphore(0);
    }

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

        if (abierto) {
            if (colaParaSubir.size() == 10) {
                System.out.println("[MONTAÑA RUSA] La cola de la montaña rusa está llena");
            }
            habilitado.acquire();

            mutex.acquire();
            try {
                if (abierto) {
                    colaParaSubir.poll();
                    sentados.incrementAndGet();
                    procesoExitoso = true;
                } else {
                    habilitado.release();
                }
            } finally {
                mutex.release();
            }
        }

        if (procesoExitoso) {
            System.out.println("[MONTAÑA RUSA]Visitante " + id + " se sentó en el vagon");
            todosSentados.release();

            try {
                esperarViaje.acquire();
                bajar(id);
                puntosGanados = 16;
            } catch (InterruptedException e) {
                sentados.decrementAndGet();
            }
        } else {
            System.out.println("[MONTAÑA RUSA] Montania cerrada, el visitante " + id + " no pudo subir.");
        }
        return puntosGanados;
    }

    public void iniciarViaje() throws InterruptedException {
        boolean puedeIniciar = false;
        mutex.acquire();
        try {
            while (!abierto) {
                mutex.release();
                try {
                    condition.acquire();
                } finally {
                    mutex.acquire();
                }
            }
        } finally {
            mutex.release();
        }
        try {
            todosSentados.acquire(5);
            if (abierto) {
                puedeIniciar = true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (puedeIniciar) {
            System.out.println("[MONTAÑA RUSA] MONTAÑA RUSA LLENA. INICIANDO VIAJE..." + " (Visitantes: " + sentados.get() + ")"
                    + " Visitantes en espera: " + colaParaSubir.size());
        } else {
            System.out.println("[MONTAÑA RUSA]El viaje no pudo iniciar (atracción cerrada o interrupción).");
        }
    }

    public void terminarViaje() throws InterruptedException {
        if (abierto) {
            esperarViaje.release(5);
            todosBajaron.acquire(5);
            habilitado.release(5);
            sentados.set(0);
            System.out.println("[MONTAÑA RUSA] VIAJE TERMINADO ...");
        } else {
            System.out.println("[MONTAÑA RUSA] Finalizando ciclo: la montaña ya se encuentra cerrada.");
        }
    }

    private void bajar(int id) {
        System.out.println("[MONTAÑA RUSA] Visitante " + id + " se bajo del vagon");
        sentados.decrementAndGet();
        todosBajaron.release();
    }

    public void cerrar() throws InterruptedException {
        mutex.acquire();
        try {
            abierto = false;
            habilitado.release(10);
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
            // removed notifyAll()
        } finally {
            mutex.release();
        }
    }

    public void abrir() throws InterruptedException {
        mutex.acquire();
        try {
            abierto = true;
            condition.release();
        } finally {
            mutex.release();
        }
    }

    public boolean estaVacio() throws InterruptedException {
        mutex.acquire();
        try {
            return colaParaSubir.isEmpty();
        } finally {
            mutex.release();
        }
    }
}
