package main;

import activos.Visitante;
import activos.extras.*;
import activos.simulaciones.*;
import compartidos.Parque;
import java.util.Random;



public class Main {

    public static void main(String[] args) {
        int cantidadBicicletas = 5; // por ejemplo, 5 bicicletas en el parque
        Parque parque = new Parque(5);
        Random random = new Random();
        // crear suficientes visitantes para que el tren pueda arrancar
        Visitante[] visitantes = new Visitante[8];

        // Montaña rusa
        simulacionMontania sim = new simulacionMontania(parque);
        sim.start();

        // Autos chocadores
        simulacionAutosC simAC = new activos.simulaciones.simulacionAutosC(parque);
        simAC.start();

        // Asistente premios
        AsistentePremios asistente = new AsistentePremios(parque);
        asistente.start();

        // Hora
        Hora h = new Hora(parque);
        for (int i = 0; i < visitantes.length; i++) {
            visitantes[i] = new Visitante(i + 900, parque);
        }
        for (int i = 0; i < visitantes.length; i++) {
            visitantes[i].start();
        }
        h.start();


        // ----------------------------------Gomones (tren + bicicletas)----------------------
        // INICIAR TREN
        Tren tren = new Tren(parque);
        tren.start();

        // arrancar bicicletas
        for (int i = 0; i < cantidadBicicletas; i++) {
            Bicicleta bici = new Bicicleta(i + 1, parque);
            bici.start();
        }
        // arrancar camioneta
        Camioneta camion = new Camioneta(parque);
        camion.start();

        // arrancar gomones
        int cantidadGomones = 12;
        int i = 0;

        while (i < cantidadGomones) {
            int capacidad = random.nextBoolean() ? 1 : 2;
            Gomones gomon = new Gomones(capacidad, parque,i);
            gomon.start();
            i++;
        }

       
    }
}