package activos.extras;

import java.util.concurrent.ConcurrentHashMap;

import compartidos.Parque;

public class Camioneta extends Thread {
   
    private ConcurrentHashMap<Integer, Object> bolsosCamion;
    private final Parque parque;

    public Camioneta(Parque parque) {
        setName("CAMIONETA DE GOMONES");
        this.parque = parque;
        this.bolsosCamion = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                // Espera a que los visitantes suban y dejen sus bolsos
                bolsosCamion = parque.esperarBolsosCamion();
              
                Thread.sleep(4000); // Simula el tiempo del viaje
                parque.finCamion(); // Indica que el viaje ha terminado para que los visitantes puedan recuperar sus bolsos
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
}
