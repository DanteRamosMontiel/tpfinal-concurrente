package compartidos.shopping;

import compartidos.extras.Mesa;
import java.util.concurrent.Semaphore;

public class Comedor {

    // Array de mesas disponibles en el comedor
    private final Mesa mesas[];
    // Semáforo para proteger el acceso a las mesas (solo un thread a la vez puede buscar mesa)
    private final Semaphore mutex;
    // Indica si el comedor está abierto o cerrado
    private volatile boolean abierto = true;

    public Comedor(int cantMesas) {
        // Creamos el array de mesas según la cantidad especificada
        this.mesas = new Mesa[cantMesas];
        // Inicializamos cada mesa con su ID
        for (int i = 0; i < mesas.length; i++) {
            mesas[i] = new Mesa(i);
        }
        // Semáforo que permite solo un thread buscar mesa a la vez
        this.mutex = new Semaphore(1);
    }

    // Visitante intenta entrar al comedor y sentarse en una mesa libre
    public Object[] entrar() throws Exception {
        // Array para devolver si pudo sentarse y cuál mesa
        Object[] resultado = new Object[2];
        boolean sentado = false;
        int mesaAsignada = -1;

        // Si el comedor cerró, no puede entrar
        if (!abierto) {
            resultado[0] = false;
            resultado[1] = -1;
            return resultado;
        }

        // Tomamos el mutex para buscar mesa sin interferencias
        mutex.acquire();
        try {
            // Recorremos las mesas buscando una libre
            for (int i = 0; i < mesas.length && !sentado; i++) {
                if (!mesas[i].estaOcupada()) {
                    // Liberamos el mutex temporalmente mientras intentamos ocupar la mesa
                    mutex.release();
                    // Intentamos ocupar la mesa (puede fallar si otro thread la tomó)
                    sentado = mesas[i].usar();
                    if (sentado) {
                        mesaAsignada = i; // Guardamos el índice de la mesa asignada
                    } else {
                        // Si no pudimos ocupar, retomamos el mutex
                        mutex.acquire();
                    }
                }
            }
        } finally {
            // Nos aseguramos de liberar el mutex si aún lo tenemos
            if (mutex.availablePermits() == 0) {
                mutex.release();
            }
        }
        // Devolvemos si pudo sentarse y cuál mesa
        resultado[0] = sentado;
        resultado[1] = mesaAsignada;
        return resultado;
    }

    // Visitante termina de comer y libera la mesa
    public void salir(int idMesa) throws InterruptedException {
        // Verificamos que el ID de mesa sea válido
        if (idMesa >= 0 && idMesa < mesas.length) {
            // Liberamos la mesa para que otros puedan usarla
            mesas[idMesa].dejar();
        }
    }

    // Cierra el comedor, no pueden entrar más visitantes
    public void cerrar() {
        abierto = false;
    }

    // Reabre el comedor
    public void abrir() {
        abierto = true;
    }

    // Verifica si todas las mesas están libres (comedor vacío)
    public boolean estaVacio() throws InterruptedException {
        boolean retorno = true;
        // Recorremos todas las mesas
        for (Mesa m : mesas) {
            if (m.estaOcupada()) {
                retorno = false; // Si alguna está ocupada, no está vacío
                break;
            }
        }
        return retorno;
    }
}
