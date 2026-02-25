package compartidos.atracciones;

import java.util.concurrent.Semaphore;

public class AutosChocadores {

    private Semaphore autosLlenos = new Semaphore(0);
    private Semaphore habilitado = new Semaphore(20);
    private Semaphore esperarInicio = new Semaphore(0);
    private Semaphore todosBajaron = new Semaphore(0);
    private int asientos = 0;
    private final Object lock = new Object();

    public void entrarAutosChocadores(int id) throws InterruptedException {
        habilitado.acquire();

        //Para simular los autos con 2 pasajeros
        int autoAsignado;
        synchronized (lock) {
            autoAsignado = asientos / 2;
            asientos++;
        }

        System.out.println("Visitante " + id +
                " se sentó en el auto " + autoAsignado);

        autosLlenos.release();
        esperarInicio.acquire();
        bajar(id, autoAsignado);
    }

    public void iniciarAutos() throws InterruptedException {
        autosLlenos.acquire(20);
    }

    public void terminarAutos() throws InterruptedException {
    

        esperarInicio.release(20);
        todosBajaron.acquire(20);
        synchronized (lock) {
            asientos = 0;
        }
        habilitado.release(20);
    }

    private void bajar(int id, int autoAsignado) {
        System.out.println("Visitante " + id + " se bajo del auto " + autoAsignado);
        todosBajaron.release();
    }
}
