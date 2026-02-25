package main;

import activos.Visitante;
import activos.extras.Hora;
import activos.simulaciones.*;
import compartidos.Parque;

public class Main {
    public static void main(String[] args) {
        Parque parque = new Parque(5);
        Visitante[] visitantes = new Visitante[25];
        //Montaña rusa
        simulacionMontania sim = new simulacionMontania(parque);
        sim.start();

        //Autos chocadores
        simulacionAutosC simAC = new activos.simulaciones.simulacionAutosC(parque);
        simAC.start();


        //Hora
        Hora h = new Hora(parque);
        for (int i = 0; i < visitantes.length; i++) {
            visitantes[i] = new Visitante(i+900, parque);
        }
        for (int i = 0; i < visitantes.length; i++) {
            visitantes[i].start();
        }
        h.start();

    }
}