package compartidos.atracciones;

import activos.Visitante;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

public class MontaniaRusa {

    private final BlockingQueue<Visitante> colaParaSubir;
    private final Semaphore habilitado; // controla 5 por tanda
    private final Semaphore todosSentados; // barrera de 5
    private final Semaphore esperarViaje; // espera fin del viaje
    private final Semaphore todosBajaron;

    public MontaniaRusa() {
        this.colaParaSubir = new ArrayBlockingQueue<>(10); // espacio de espera
        this.habilitado = new Semaphore(1);
        this.todosSentados = new Semaphore(0);
        this.esperarViaje = new Semaphore(0);
        this.todosBajaron = new Semaphore(0);
    }

    // Intenta entrar a la cola
    public boolean entrar(Visitante v) {
        return colaParaSubir.offer(v); // si está llena → se va
    }

    // Subir al vagón
    public int subirAlVagon(int id) throws InterruptedException {
        colaParaSubir.take();
        habilitado.acquire();
        System.out.println("Visitante " + id + " se sentó en el vagon");
        todosSentados.release();
        esperarViaje.acquire();
        bajar(id);
        return 16; // puntos que gana por subir a la montaña rusa
    }

    // Hilo de la montaña
    public void iniciarViaje() throws InterruptedException {
        todosSentados.acquire(1);

    }

    public void terminarViaje() throws InterruptedException {
        esperarViaje.release(1); // libera pasajeros
        todosBajaron.acquire(1); // espera que los 5 bajen
        habilitado.release(1); // recién ahora habilita nueva tanda
    }

    private void bajar(int id) {
        System.out.println("Visitante " + id + " se bajo del vagon");
        todosBajaron.release();
    }
}
