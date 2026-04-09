package compartidos.shopping;

import java.util.concurrent.Exchanger;

public class AreaPremios {

    // Primer Exchanger: visitante envía sus puntos, asistente los recibe
    // Es como un buzón donde ambos dejan datos simultáneamente
    private Exchanger<Integer> exchangerPuntos;

    // Segundo Exchanger: asistente devuelve el saldo de puntos restantes
    // Después de que el asistente calcula qué puntos quedan
    private Exchanger<Integer> exchangerSaldo;

    // Indica si la tienda de premios está abierta
    private volatile boolean abierto = true;

    public AreaPremios() {
        this.exchangerPuntos = new Exchanger<>();
        this.exchangerSaldo = new Exchanger<>();
    }

    // Visitante llega a canjear sus puntos por premios
    public int canjearPremio(int id, int puntosEntregados) throws InterruptedException {
        // Si la tienda cerró, nos vamos sin hacer nada
        if (!abierto) {
            System.out.println("AreaPremios cerrada, visitante " + id + " se va");
            return puntosEntregados;
        }
        System.out.println("El visitante N°" + id + " entró a la tienda de premios con " + puntosEntregados + " puntos");
        try {
            // Primeramente nviamos nuestros puntos al asistente y esperamos que nos reciba
            // Si el asistente no aparece en 3 segundos, nos vamos (esto puede 'salvar' la concurrencia si el area de premios cierra mientras esperamos)
            exchangerPuntos.exchange(puntosEntregados, 3, java.util.concurrent.TimeUnit.SECONDS);

            // Si cerraron durante el intercambio, nos vamos
            if (!abierto) {
                return puntosEntregados;
            }

            // Luego speramos que el asistente nos diga cuántos puntos nos quedan
            // Los puntos que enviamos (0 es solo un placeholder, el asistente envía el saldo real)
            return exchangerSaldo.exchange(0, 3, java.util.concurrent.TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            // El asistente no vino en el tiempo permitido, conservamos nuestros puntos
            System.out.println("AreaPremios: timeout esperando asistente, visitante " + id + " conserva sus puntos");
            return puntosEntregados;
        }
    }

    // El asistente atiende visitantes, calcula cuántos puntos quedan después de los premios
    public void atenderVisitante(java.util.function.IntUnaryOperator calcularSaldo) throws InterruptedException {
        // Si la tienda cerró, salimos
        if (!abierto) {
            return;
        }
        try {
            // Fase 1: Esperamos a que un visitante nos envíe sus puntos
            // Si ninguno aparece en 3 segundos, ignoramos este ciclo
            int puntosCliente = exchangerPuntos.exchange(0, 3, java.util.concurrent.TimeUnit.SECONDS);

            // Calculamos cuántos puntos quedan después de intercambiar por premios
            // La función recibe los puntos del cliente y devuelve lo que sobra
            int saldoDevuelto = calcularSaldo.applyAsInt(puntosCliente);

            // Fase 2: Devolvemos al visitante cuántos puntos le quedan
            exchangerSaldo.exchange(saldoDevuelto, 3, java.util.concurrent.TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            // No había visitante en este ciclo, es normal, continuamos
        }
    }

    // Cierra la tienda de premios inmediatamente
    public void cerrar() {
        abierto = false;
        // Creamos nuevos Exchangers para desbloquear a cualquiera que estuviera esperando
        // (Los threads que estaban en exchange() van a recibir TimeoutException)
        exchangerPuntos = new Exchanger<>();
        exchangerSaldo = new Exchanger<>();
    }

    // Reabre la tienda de premios
    public void abrir() {
        abierto = true;
        // Reseteamos los Exchangers para empezar de cero
        exchangerPuntos = new Exchanger<>();
        exchangerSaldo = new Exchanger<>();
    }

    // Verifica si la tienda está vacía (sin visitantes esperando)
    public boolean estaVacio() {
        return abierto;
    }
}
