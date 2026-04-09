package compartidos.shopping;

import java.util.concurrent.Exchanger;
import java.util.function.IntUnaryOperator;

public class AreaPremios {
    // Exchanger de la fase 1: visitante envía sus puntos, asistente los recibe
    private Exchanger<Integer> exchangerPuntos;
    // Exchanger de la fase 2: asistente devuelve saldo, visitante lo recibe
    private Exchanger<Integer> exchangerSaldo;
    private volatile boolean abierto = true;

    public AreaPremios() {
        this.exchangerPuntos = new Exchanger<>();
        this.exchangerSaldo  = new Exchanger<>();
    }

    /**
     * Método para el VISITANTE.
     * Entrega sus puntos al asistente y espera recibir el saldo restante.
     * Usa timeout para no quedar bloqueado si el asistente ya no está activo.
     */
    public int canjearPremio(int id, int puntosEntregados) throws InterruptedException {
        if (!abierto) {
            System.out.println("AreaPremios cerrada, visitante " + id + " se va");
            return puntosEntregados;
        }
        System.out.println("El visitante N°" + id + " entró a la tienda de premios con " + puntosEntregados + " puntos");
        try {
            // Fase 1: envía puntos al asistente con timeout de 3 segundos
            exchangerPuntos.exchange(puntosEntregados, 3, java.util.concurrent.TimeUnit.SECONDS);
            if (!abierto) return puntosEntregados;
            // Fase 2: espera el saldo de vuelta del asistente con timeout
            return exchangerSaldo.exchange(0, 3, java.util.concurrent.TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            // El asistente ya no está activo (parque cerró), devolver puntos intactos
            System.out.println("AreaPremios: timeout esperando asistente, visitante " + id + " conserva sus puntos");
            return puntosEntregados;
        }
    }

    /**
     * Método para el ASISTENTE.
     * Recibe los puntos del visitante, aplica la función de cálculo de premio,
     * y devuelve el saldo resultante al visitante.
     */
    public void atenderVisitante(java.util.function.IntUnaryOperator calcularSaldo) throws InterruptedException {
        if (!abierto) return;
        try {
            // Fase 1: recibe los puntos del visitante con timeout
            int puntosCliente = exchangerPuntos.exchange(0, 3, java.util.concurrent.TimeUnit.SECONDS);
            // Calcula el saldo a devolver según la lógica de premios
            int saldoDevuelto = calcularSaldo.applyAsInt(puntosCliente);
            // Fase 2: devuelve el saldo al visitante con timeout
            exchangerSaldo.exchange(saldoDevuelto, 3, java.util.concurrent.TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            // No había visitante esperando en este ciclo, continuar
        }
    }

    public void cerrar() {
        abierto = false;
        // Recrear exchangers para desbloquear hilos que pudieran quedar esperando
        exchangerPuntos = new Exchanger<>();
        exchangerSaldo  = new Exchanger<>();
    }

    public void abrir() {
        abierto = true;
        exchangerPuntos = new Exchanger<>();
        exchangerSaldo  = new Exchanger<>();
    }

    public boolean estaVacio() {
        return abierto;
    }
}