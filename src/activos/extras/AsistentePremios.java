package activos.extras;

import compartidos.Parque;
import compartidos.shopping.Premio;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.IntUnaryOperator;

public class AsistentePremios extends Thread {

    private Parque parque;
    private List<Premio> premios;

    public AsistentePremios(Parque xParque) {
        this.parque = xParque;
        premios = List.of(
                new Premio("Sticker del parque", 15),
                new Premio("Llavero de la montaña rusa", 30),
                new Premio("Gorra del parque", 60),
                new Premio("Pulsera de goma", 10),
                new Premio("Auto chocador en miniatura", 25),
                new Premio("Remera del parque", 50),
                new Premio("Sticker VR", 20),
                new Premio("Llavero tecnológico", 40),
                new Premio("Anteojos 3D", 70),
                new Premio("Medalla simbólica", 25),
                new Premio("Toalla del parque", 50),
                new Premio("Mochila impermeable", 100)
        );
    }

    public void run() {
        while (true) {
            try {
                if (parque.debeExpulsarVisitantes()) {
                    System.out.println("Asistente de premios se retira, parque cerró completamente.");
                    break;
                }
                // Calcula de antemano qué premio daría con 0 puntos para no bloquear
                // (el saldo real se calcula DESPUÉS de recibir los puntos)
                // Usamos el método atenderTienda que: recibe puntos, calcula, devuelve saldo
                parque.atenderTiendaPremios(this::entregarPremio);

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private int entregarPremio(int puntos) {
        List<Premio> premiosDisponibles = this.premios;
        List<Premio> premiosAlcanzables = new ArrayList<>();
        int puntosFinal;
        //Primeor filtramos que premios puede pagar
        for (Premio p : premiosDisponibles) {
            if (p.getPrecio() <= puntos) {
                premiosAlcanzables.add(p);
            }
        }

        if (premiosAlcanzables.isEmpty()) {
            System.out.println("El visitante no tiene fichas suficientes.");
            puntosFinal = puntos;
        } else {
            Random random = new Random();
            Premio elegido = premiosAlcanzables.get(random.nextInt(premiosAlcanzables.size()));

            System.out.println("PREMIO ENTREGADO: " + elegido.getNombre());

            puntosFinal = puntos - elegido.getPrecio(); // Devuelve el sobrante
        }

        return puntosFinal;
    }

}
