package compartidos.atracciones;

import compartidos.Parque;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import activos.extras.Camioneta;
import activos.extras.Gomones;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import java.util.HashMap;

public class CarreraGomones {
    private final Object Bolso;
    private final Semaphore Camion = new Semaphore(0); // señal para que la camioneta parta después de G gomones
    private final Semaphore EsperarCamion = new Semaphore(0); // visitantes bloqueados esperando la camioneta
    private final ConcurrentHashMap<Integer, Object> Bolsos = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Semaphore> semGomon = new ConcurrentHashMap<>();
    private final BlockingQueue<Integer> colaGomones = new ArrayBlockingQueue<>(50);
    private int cantGomonesNecesarios;
    private int cantGomonesEsperando;
    private int vueltasGomones = 0;
    // contador de visitantes esperando al camión en el destino
    private int visitantesEsperandoCamion = 0;

    public CarreraGomones(int cantGomones) {
        Bolso = new Object();
        this.cantGomonesEsperando = 0;
        this.cantGomonesNecesarios = cantGomones;

    }

    // -------------------------------------------------Metodo para visitantes---------------------------------------------//
    public void usarGomon(int id) throws InterruptedException {
        synchronized (Bolso) {
            Bolsos.put(id, new Object()); // cada visitante tiene su propio "bolso"
            System.out.println("Visitante " + id + " tomó un bolso");
        }
        colaGomones.put(id); // se encola la petición para usar el gomon
        Semaphore sem = new Semaphore(0);
        semGomon.put(id, sem); // se almacena el semáforo para que el gomon lo despierte
        sem.acquire(); // el visitante espera a que el gomon lo libere
        System.out.println("Visitante " + id + " terminó la carrera en gomon, aguardando el camión");

        // El visitante llega al destino y se cuenta entre los que esperan el camión
        synchronized (this) {
            visitantesEsperandoCamion++;
        }

        EsperarCamion.acquire(); // espera a que el camión lleve los bolsos al destino

        // El camión llegó, el visitante reduce el contador
        synchronized (this) {
            visitantesEsperandoCamion--;
        }

        synchronized (Bolso) {
            if (Bolsos.containsKey(id)) {
                Bolsos.remove(id); // el visitante recupera su bolso
                System.out.println("Visitante " + id + " recuperó su bolso");
            }
        }
    }

    // -------------------------------------------------Metodo para gomon---------------------------------------------//

    public int[] cicloGomones(int GomonId, int cant) throws InterruptedException {
        int visitor = colaGomones.take();

        int[] visitantes = new int[cant];
        visitantes[0] = visitor;
        if (cant == 2) {
            int visitor2 = colaGomones.take();
            visitantes[1] = visitor2;
        }
        synchronized (this) {
            int miVuelta = vueltasGomones;
            cantGomonesEsperando++;
            System.out.println("-- [GOMONES " + GomonId + " cap:" + cant + "] esperando a que se completen " + cantGomonesNecesarios + " para iniciar la carrera. Actualmente esperando: " + cantGomonesEsperando);
            if (cantGomonesEsperando == cantGomonesNecesarios) {
                vueltasGomones++;
                cantGomonesEsperando = 0;
                Camion.release();
                
                notifyAll();
            } else {
                while (miVuelta == vueltasGomones) {
                    wait();
                }
            }
        }

        return visitantes;
    }

    public void finCicloGomones(int GomonId, int[] visitantes) throws InterruptedException {
        System.out.println("-- [GOMONES " + GomonId + "] carrera finalizada con visitantes " + visitantes[0]+ (visitantes.length > 1 ? " y " + visitantes[1] : ""));
        for (int visitor : visitantes) {
            Semaphore sem = semGomon.remove(visitor);
            if (sem != null) {
                sem.release(); // libera al visitante
            }
        }
    }

    // -------------------------------------------------Metodo para camion---------------------------------------------//
    public ConcurrentHashMap<Integer, Object> esperarBolsosCamion() throws InterruptedException {
        Camion.acquire(); // espera a que el camion esté lleno (esto no cambia)
        return Bolsos;
    }

    public void finViajeCamion() {
        System.out.println(">> [CAMION] viaje finalizado, dejando a los bolsos en el destino");
        int liberar;
        synchronized (this) {
            liberar = visitantesEsperandoCamion;
        }
        if (liberar > 0) {
            System.out.println(">> [CAMION] libera a " + liberar + " visitantes");
            EsperarCamion.release(liberar); // libera a todos los visitantes que esperaban
        } else {
            System.out.println(">> [CAMION] no había visitantes esperando");
        }
        System.out.println(">> [CAMION] vuelve a dormirse");
    }

}