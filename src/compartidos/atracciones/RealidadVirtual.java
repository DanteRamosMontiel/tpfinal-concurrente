package compartidos.atracciones;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RealidadVirtual {

    private int visores;
    private int manoplas;
    private int bases;

    private boolean abierto = true;

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
            // si cerraron la atracción abortar
            while ((visores < 1 || manoplas < 2 || bases < 1) && abierto) {
                equipoDisponible.await();
            }
            if (!abierto) {
                System.out.println("RealidadVirtual cerrada, visitante " + id + " deambula");
                throw new InterruptedException();
            }

            visores--;
            manoplas -= 2;
            bases--;

            System.out.println("Visitante " + id + " recibió equipo completo por el encargado");
        } finally {
            lock.unlock();
        }
    }

    public int salir(int id) {
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
        return 19; //puntos ganados por el visitante al jugar a la realidad virtual
    }

    public void cerrar() {
        lock.lock();
        try {
            abierto = false;
            equipoDisponible.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void abrir() {
        lock.lock();
        try {
            abierto = true;
            equipoDisponible.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public boolean estaVacio() {
        lock.lock();
        try {
            return visores == 0 && manoplas == 0 && bases == 0;
        } finally {
            lock.unlock();
        }
    }
}