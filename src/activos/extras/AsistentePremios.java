package activos.extras;

import compartidos.Parque;
import compartidos.shopping.Premio;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
                // 1. Primer intercambio: Recibe los puntos del cliente
                // El -1 es para que AreaPremios no imprima el mensaje de "Entró visitante"
                int puntosCliente = parque.entrarTiendaPremios(-1, 0);

                // 2. Calcula el premio y cuánto le sobra
                int saldoParaDevolver = this.entregarPremio(puntosCliente);

                // 3. SEGUNDO INTERCAMBIO: Devuelve el saldo al cliente
                // Sin esto, el Visitante nunca sale de su línea 'puntos = parque.entrarTiendaPremios(...)'
                parque.entrarTiendaPremios(-1, saldoParaDevolver);

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
