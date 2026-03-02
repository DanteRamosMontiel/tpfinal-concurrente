package compartidos.atracciones;

public class AutosChocadores {

    private int esperando = 0; // personas sentadas
    private int bajaron = 0; // personas que bajaron
    private int asientos = 0; // para asignar autos
    private boolean enCurso = false;

    // indicador de apertura/cierre de la atracción
    private boolean abierto = true;

    public synchronized int entrarAutosChocadores(int id) throws InterruptedException {
        // si la atracción está cerrada no hacemos fila
        if (!abierto) {
            System.out.println("AutosChocadores cerrado: visitante " + id + " deambula");
            return 0;
        }

        // Espera si ya hay 20 personas o si cierran mientras espera
        while (esperando == 20 && abierto) {
            wait();
        }
        if (!abierto) {
            System.out.println("AutosChocadores cerró mientras " + id + " esperaba, se va a deambular");
            return 0;
        }

        int autoAsignado = asientos / 2;
        asientos++;
        esperando++;

        System.out.println("Visitante " + id
                + " se sentó en el auto " + autoAsignado);

        // Si es el número 20, despierta al encargado
        if (esperando == 20) {
            notifyAll();
        }

        // Espera a que inicie la atracción (o cierre)
        while (!enCurso && abierto) {
            wait();
        }
        if (!abierto) {
            // se cerró antes de comenzar el viaje
            // ajustar contadores
            asientos--;
            esperando--;
            return 0;
        }

        // Espera a que termine
        while (enCurso && abierto) {
            wait();
        }
        if (!abierto) {
            // se cerró en medio del viaje, forzamos salida rápida
            bajar(id, autoAsignado);
            return 0;
        }

        bajar(id, autoAsignado);

        return 13; // puntos ganados por el visitante al jugar a los autos chocadores
    }

    public synchronized void iniciarAutos() throws InterruptedException {
        if (abierto) {

            // Espera hasta que haya 20 personas
            while (esperando < 20 && abierto) {
                wait();
            }

            enCurso = true;
            notifyAll(); // despierta pasajeros
                  System.out.println("AUTOS CHOCADORES LLENOS, INICIANDO ATRACCION...");
        }
    }

    public synchronized void terminarAutos() throws InterruptedException {
        enCurso = false;
        notifyAll(); // permite que bajen

        // Espera a que bajen los 20 o hasta que se cierre
        while (bajaron < 20 && abierto) {
            wait();
        }

        // Reset
        esperando = 0;
        bajaron = 0;
        asientos = 0;

        notifyAll(); // habilita nueva tanda
    }

    private synchronized void bajar(int id, int autoAsignado) {
        // en caso de cierre abrupto la persona puede bajar igualmente

        System.out.println("Visitante " + id
                + " se bajo del auto " + autoAsignado);

        bajaron++;

        if (bajaron == 20) {
            notifyAll();
        }
    }

    /**
     * Marca la atracción como cerrada y despierta a todos los hilos
     */
    public synchronized void cerrar() {
        abierto = false;
        notifyAll();
    }

    /**
     * Vuelve a abrir la atracción y permite operaciones normales
     */
    public synchronized void abrir() {
        abierto = true;
        notifyAll();
    }

    /**
     * Indica si actualmente no hay ningún visitante en espera o en viaje
     */
    public synchronized boolean estaVacio() {
        return !enCurso && esperando == 0;
    }
}