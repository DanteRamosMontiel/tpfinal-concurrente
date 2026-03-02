package compartidos.shopping;

import compartidos.extras.Mesa;
import java.util.concurrent.Semaphore;

public class Comedor {

    private Mesa mesas[];
    private Semaphore mutex;

    public Comedor(int cantMesas) {
        this.mesas = new Mesa[cantMesas];
        for (int i = 0; i < mesas.length; i++) {
            mesas[i] = new Mesa(i);
        }
        this.mutex = new Semaphore(1);
    }

    /**
     * Intenta entrar al comedor y sentarse en una mesa. Retorna true si pudo
     * sentarse y comer, false si el comedor está lleno.
     */
    public Object[] entrar() throws Exception {
        Object[] resultado = new Object[2];
        boolean sentado = false;
        int mesaAsignada = -1;

        mutex.acquire();
        try {
            for (int i = 0; i < mesas.length && !sentado; i++) {
                if (!mesas[i].estaOcupada()) {
                    mutex.release();
                    sentado = mesas[i].usar();
                    if (sentado) {
                        mesaAsignada = i; // Guardamos el índice real
                    } else {
                        mutex.acquire();
                    }
                }
            }
        } finally {
            if (mutex.availablePermits() == 0) {
                mutex.release();
            }
        }
        resultado[0] = sentado;
        resultado[1] = mesaAsignada; // Retornamos el índice correcto
        return resultado;
    }

    public void salir(int idMesa) throws InterruptedException {
        if (idMesa >= 0 && idMesa < mesas.length) {
            mesas[idMesa].dejar();
        }
    }
}
