package compartidos.extras;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class recorridoAGomones {

    // ---------- tren --------------------------------------------------
    private final Semaphore visitantesEnTren = new Semaphore(0);
    private final Semaphore trenListo = new Semaphore(0);

    // ---------- bicicletas --------------------------------------------
    private final BlockingQueue<Integer> colaBici = new ArrayBlockingQueue<>(50);
    private final HashMap<Integer, Semaphore> semBici = new HashMap<>();

    // ----------METODOS PARA VISITANTES ---------------------------------------------//
    public void subirTren(int id) throws InterruptedException {
        System.out.println("Visitante " + id + " subió al tren y espera que arranque");
        visitantesEnTren.release();              // indica que hay un pasajero más
        trenListo.acquire();                     // queda esperando a que el viaje finalice
        System.out.println("Visitante " + id + " se bajó del tren");
    }


    public void usarBicicleta(int visitorId) throws InterruptedException {
        System.out.println("Visitante " + visitorId + " solicita una bicicleta");
        Semaphore sem = new Semaphore(0);
        semBici.put(visitorId, sem);        // se almacenará para que la bici lo despierte
        colaBici.put(visitorId);           // encola la petición
        sem.acquire();                     // espera a que la bici libere
    }
    //-----------------------------------------------------------------------------------

    //MANEJO DEL TREN
    public void cicloTren() throws InterruptedException {
        visitantesEnTren.acquire(15);// haya 15 liberaciones efectuadas por los visitantes
        System.out.println(">> [TREN] 15 visitantes a bordo, el tren despierta y arranca");
        Thread.sleep(3000); // simulación del trayecto
        System.out.println(">> [TREN] recorrido terminado, dejando a los pasajeros");
        trenListo.release(15); // despierta a los 15 visitantes que esperaban
    }


    //MANEJO DE LAS BICICLETAS
    public void cicloBicicleta(int bikeId) throws InterruptedException {
        int visitor = colaBici.take(); // espera hasta que un visitante solicite
        System.out.println("-- [BICI " + bikeId + "] despierta para el visitante " + visitor);
        Thread.sleep(2000); // paseo simulado
        System.out.println("-- [BICI " + bikeId + "] paseo finalizado con visitante " + visitor);
        Semaphore sem = semBici.remove(visitor);
        if (sem != null) {
            sem.release();          // libera al visitante
        }
    }
}

