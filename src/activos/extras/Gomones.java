package activos.extras;
import compartidos.Parque;
import java.util.Random;

public class Gomones extends Thread {
    private int[] visitantesEnGomon; // Solo 1 o 2 visitantes según capacidad
    private final Parque parque;
    private final int capacidad;
    private final Random rand = new Random();
    private int id;

    public Gomones(int espacioGomon, Parque parque,int id) {
        setName("GOMON-" + id);
        this.id = id;
        this.capacidad = espacioGomon;
        this.visitantesEnGomon = new int[espacioGomon];
        this.parque = parque;
    }

    public void run() {
        try {
            while (!isInterrupted()) {
                // pequeño retardo aleatorio para mezclar hilos de distinto tipo
                Thread.sleep(rand.nextInt(100));

                int[] visitantes = parque.CicloGomon(id, capacidad); // Gomon solicita pasajeros

                Thread.sleep(6000); // Simula el tiempo del recorrido

                parque.finCicloGomon(id, visitantes); // Libera el gomon para que los visitantes puedan salir y recuperar sus bolsos
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
