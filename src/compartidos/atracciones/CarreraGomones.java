package compartidos.atracciones;

import java.util.concurrent.CyclicBarrier;

public class CarreraGomones {
    private CyclicBarrier barrier;
    //private final Map<Visitante,Object> hash; la idea es usar el hash para guardar las duplas visitante-bolso

    public CarreraGomones(int cantGomones){
        this.barrier = new CyclicBarrier(cantGomones);
        //this.hash = new ConcurrentHashMap<>(); importante usar un hash concurrente para evitar problemas de concurrencia
        
    }
}