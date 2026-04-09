package activos.extras;
import compartidos.Parque;
import compartidos.atracciones.CarreraGomones;
import java.util.Random;

public class Gomones extends Thread {
    private int[] visitantesEnGomon; // Solo 1 o 2 visitantes según capacidad
    private final Parque parque;
    private final int capacidad;
    private final Random rand = new Random();
    private int id;

    public Gomones(int espacioGomon, Parque parque, int id) {
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

                // Simular recorrido con tiempo aleatorio (competencia real entre gomones)
                int tiempoRecorrido = 4000 + rand.nextInt(4000); // entre 4 y 8 segundos
                Thread.sleep(tiempoRecorrido);

                // finCicloGomon retorna true si este gomón fue el primero en llegar (ganador)
                boolean esGanador = parque.finCicloGomon(id, visitantes);

                if (esGanador) {
                    // Entregar fichas CG a los visitantes ganadores
                    parque.otorgarFichasCG(visitantes, CarreraGomones.FICHAS_CG);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
