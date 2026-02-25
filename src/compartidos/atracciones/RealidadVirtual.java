package compartidos.atracciones;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RealidadVirtual {

    private int visores;
    private int manoplas;
    private int bases;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition equipoDisponible = lock.newCondition();


    //SE PUEDE AGREGAR OTRO HILO PARA QUE SIMULE SER EL ADMINISTRADOR O ENCARGADO , PERO NO TENDRIA SENTIDO 
    public RealidadVirtual(int visores, int manoplas, int bases) {
        this.visores = visores;
        this.manoplas = manoplas;
        this.bases = bases;
    }

    public void entrar(int id) throws InterruptedException {
        lock.lock();
        try {
            while (visores < 1 || manoplas < 2 || bases < 1) {
                equipoDisponible.await();
            }

            visores--;
            manoplas -= 2;
            bases--;

            System.out.println("Visitante " + id + " recibió equipo completo por el encargado");
        } finally {
            lock.unlock();
        }
    }

    public void salir(int id) {
        lock.lock();
        try {
            visores++;
            manoplas += 2;
            bases++;

            System.out.println("Visitante " + id + " devolvió el equipo y recibe fichas RV");

            equipoDisponible.signalAll();
        } finally {
            lock.unlock();
        }
    }
}