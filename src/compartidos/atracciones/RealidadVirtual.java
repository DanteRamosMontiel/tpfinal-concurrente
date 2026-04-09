package compartidos.atracciones;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RealidadVirtual {

    // Cantidad disponible de cada componente del equipo de realidad virtual
    private int visores;
    private int manoplas;         // Hay que devolver 2 manoplas por visitante
    private int bases;

    // Indica si la atracción está abierta o cerrada
    private boolean abierto = true;

    // Lock y condition para sincronizar el acceso al equipo
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition equipoDisponible = lock.newCondition();

    // Nota: Se podría agregar otro hilo para simular a un encargado que reparte el equipo,
    // pero no tendría sentido ya que con el Lock es suficiente 
    // Constructor que inicializa la cantidad de equipo disponible
    public RealidadVirtual(int visores, int manoplas, int bases) {
        this.visores = visores;
        this.manoplas = manoplas;
        this.bases = bases;
    }

    // Visitante intenta entrar a la atracción y obtiene el equipo necesario
    public void entrar(int id) throws InterruptedException {
        lock.lock();
        try {
            // Esperamos a que haya equipo completo disponible (1 visor, 2 manoplas, 1 base)
            // Si la atracción está cerrada, salimos del bucle
            while ((visores < 1 || manoplas < 2 || bases < 1) && abierto) {
                equipoDisponible.await();
            }
            // Si cerraron la atracción mientras esperábamos, nos vamos
            if (!abierto) {
                System.out.println("RealidadVirtual cerrada, visitante " + id + " deambula");
                throw new InterruptedException();
            }

            // Tomamos el equipo disponible
            visores--;
            manoplas -= 2;
            bases--;

            System.out.println("Visitante " + id + " recibió equipo completo por el encargado");
        } finally {
            lock.unlock();
        }
    }

    // Visitante termina, devuelve el equipo y gana puntos
    public int salir(int id) {
        lock.lock();
        try {
            // Devolvemos el equipo que usó
            visores++;
            manoplas += 2;
            bases++;

            System.out.println("Visitante " + id + " devolvió el equipo y recibe fichas RV");

            // Notificamos a otros visitantes que hay equipo disponible
            equipoDisponible.signalAll();
        } finally {
            lock.unlock();
        }
        // Puntos ganados por jugar a la realidad virtual
        return 19;
    }

    // Cierra la atracción y despierta a cualquiera que esté esperando
    public void cerrar() {
        lock.lock();
        try {
            abierto = false;
            // Despertamos a los que estaban esperando para que se vayan
            equipoDisponible.signalAll();
        } finally {
            lock.unlock();
        }
    }

    // Abre la atracción de nuevo
    public void abrir() {
        lock.lock();
        try {
            abierto = true;
            // Notificamos a los que estaban esperando que ya pueden entrar
            equipoDisponible.signalAll();
        } finally {
            lock.unlock();
        }
    }

    // Verifica si la atracción está vacía (todo el equipo disponible)
    public boolean estaVacio() {
        lock.lock();
        try {
            return visores == 0 && manoplas == 0 && bases == 0;
        } finally {
            lock.unlock();
        }
    }
}
