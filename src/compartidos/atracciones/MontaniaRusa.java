package compartidos.atracciones;

import activos.Visitante;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

public class MontaniaRusa {

    private BlockingQueue<Visitante> colaParaSubir;
    private Semaphore asientos;
    private Semaphore esperarViaje;
    private Semaphore esperarMontania;
    private Semaphore mutex;
    private Semaphore habilitado;
    private int actual;

    public MontaniaRusa() {
        this.colaParaSubir = new ArrayBlockingQueue<>(10); // cola de espera
        this.asientos = new Semaphore(5); // asientos del vagón
        this.esperarViaje = new Semaphore(0);
        this.esperarMontania = new Semaphore(0);
        this.habilitado = new Semaphore(5);
        this.mutex = new Semaphore(1);
        this.actual = 0;
    }

    //Método para entrar a la cola de espera
    public boolean entrar(Visitante v) throws InterruptedException {
        boolean puede = colaParaSubir.offer(v);
        return puede;
    }

    //Método para tomar un asiento 
    public Visitante subirAlVagon() throws InterruptedException {
        asientos.acquire(); // espera asiento libre
        habilitado.acquire(); // este semaforos sirve para que suban de a tandas y no se mezclen pasajeros que ya subieron con nuevos
        mutex.acquire();
        actual++;
        System.out.println("ACTUAL = " + actual);
        if (actual == 5) {
            esperarMontania.release();
        }
        mutex.release();
        Visitante v = colaParaSubir.take(); // sale de la cola
        return v;
    }

    //Método que el hilo de simulacion de viaje usara par bloquearse y esperar que este lleno
    public void iniciarViaje() throws InterruptedException {
        this.esperarMontania.acquire();
    }

    //Método para que los visitantes esperen que termine el viaje de la montaña rusa
    public void esperarMontania() throws InterruptedException {
        this.esperarViaje.acquire();
    }

    //Método para liberar a los 5 hilos que se quedaron esperando que el viaje terminara
    public void terminarViaje() {
        this.esperarViaje.release(5);
    }

    public void bajar() throws InterruptedException {
        mutex.acquire();
        actual--;
        if (actual == 0) {
            habilitado.release(5);
        }
        mutex.release();
        asientos.release(); // libera asiento
    }
}
