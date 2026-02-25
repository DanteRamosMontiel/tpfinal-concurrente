package compartidos.atracciones;

import activos.Visitante;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

public class MontaniaRusa {

    private BlockingQueue<Visitante> colaParaSubir;
    private Semaphore habilitado; // controla 5 por tanda
    private Semaphore todosSentados; // barrera de 5
    private Semaphore esperarViaje; // espera fin del viaje
    private Semaphore todosBajaron = new Semaphore(0);

    public MontaniaRusa() {
        this.colaParaSubir = new ArrayBlockingQueue<>(10); // espacio de espera
        this.habilitado = new Semaphore(5);
        this.todosSentados = new Semaphore(0);
        this.esperarViaje = new Semaphore(0);
    }

    // Intenta entrar a la cola
    public boolean entrar(Visitante v) {
        return colaParaSubir.offer(v); // si está llena → se va
    }

    // Subir al vagón
    public void subirAlVagon(int id) throws InterruptedException {
        colaParaSubir.take();
        habilitado.acquire();
        System.out.println("Visitante " + id + " se sentó en el vagon");
        todosSentados.release();
        esperarViaje.acquire();
        bajar(id);
    }

    // Hilo de la montaña
    public void iniciarViaje() throws InterruptedException {
        todosSentados.acquire(5);
   
    }

    public void terminarViaje() throws InterruptedException {
        esperarViaje.release(5); // libera pasajeros
        todosBajaron.acquire(5); // espera que los 5 bajen
        habilitado.release(5); // recién ahora habilita nueva tanda
    }

    private void bajar(int id) {
        System.out.println("Visitante " + id + " se bajo del vagon");
        todosBajaron.release();
    }
}