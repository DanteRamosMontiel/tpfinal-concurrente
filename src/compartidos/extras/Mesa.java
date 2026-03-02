package compartidos.extras;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class Mesa {

    private Semaphore mutex;
    private int actual;
    private final int id;
    private boolean ocupada;
    private CyclicBarrier barrier;

    public Mesa(int id) {
        this.mutex = new Semaphore(1);
        this.actual = 0;
        this.id = id;
        this.ocupada = false;
        // La barrera espera a 4 y luego imprime que empiezan a comer
        this.barrier = new CyclicBarrier(4, () -> {
            System.out.println("Mesa llena: Los 4 de la mesa "+this.id+" empiezan a comer.");
        });
    }

    public boolean usar() throws Exception {
        boolean puedeSentarse = false;
        
        mutex.acquire();
        //Una mesa no puede usarse si ya llegó al límite (4) o está marcada como ocupada
        if (!ocupada && actual < 4) {
            actual++;
            if (actual == 4) {
                ocupada = true;
            }
            puedeSentarse = true;
        }
        mutex.release();

        if (puedeSentarse) {
            //El await() DEBE estar fuera del mutex
            //Si estuviera dentro, el hilo 1 bloquea el mutex, llega al await y se duerme
            //El hilo 2 nunca podría entrar a incrementar 'actual' (deadlokc)
            try {
                barrier.await();
            } catch (Exception e) {
                //Si la barrera se rompe, el hilo debería salir
                puedeSentarse = false;
            }
        }
        
        return puedeSentarse;
    }

    public void dejar() throws InterruptedException {
        mutex.acquire();
        try {
            actual--;
            //Si la mesa se vacía por completo, la habilitamos para un nuevo grupo
            if (actual == 0) {
                ocupada = false;
            }
            System.out.println("Un visitante se retiró de la mesa "+this.id+". Quedan: " + actual);
        } finally {
            mutex.release();
        }
    }

    public boolean estaOcupada() throws InterruptedException {
        mutex.acquire();
        try {
            return this.ocupada;
        } finally {
            mutex.release();
        }
    }
}