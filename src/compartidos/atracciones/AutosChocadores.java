package compartidos.atracciones;

public class AutosChocadores {

    // Lleva la cuenta de cuántos visitantes están esperando en sus autos para que inicie la atracción
    private int esperando = 0;
    // Cuenta cuántos visitantes ya bajaron de la atracción
    private int bajaron = 0;
    // Se usa para asignar autos. Cada auto ocupa 2 asientos, así que dividiendo entre 2 obtenemos el número del auto
    private int asientos = 0;
    // Indica si la atracción está en movimiento (viaje en curso)
    private boolean enCurso = false;
    // Indica si la atracción está abierta al público o cerrada
    private boolean abierto = true;

    public synchronized int entrarAutosChocadores(int id) throws InterruptedException {
        int retorno = 0;
        // Primero verificamos que la atracción esté abierta
        if (abierto) {
            // Si hay 20 visitantes esperando (lleno), este se queda esperando
            while (esperando == 20 && abierto) {
                wait();
            }
            // Si la atracción no cerró mientras esperábamos, continuamos
            if (abierto) {
                // Asignamos el auto, cada auto tiene 2 asientos, así que dividimos los asientos
                int autoAsignado = asientos / 2;
                asientos++;
                esperando++;
                System.out.println("Visitante " + id + " se sentó en el auto " + autoAsignado);
                // Si llegamos a 20 visitantes, notificamos al hilo que inicia la atracción
                if (esperando == 20) {
                    notifyAll();
                }
                // Esperamos a que inicie el viaje
                while (!enCurso && abierto) {
                    wait();
                }
                // Si la atracción sigue abierta, esperamos a que termine el viaje
                if (abierto) {
                    // Esperamos a que termine el viaje (enCurso se vuelve false)
                    while (enCurso && abierto) {
                        wait();
                    }
                    // Si la atracción sigue abierta, bajamos normalmente
                    if (abierto) {
                        bajar(id, autoAsignado);
                        retorno = 13;
                    } else {
                        // Si cerró durante el viaje, aún así bajamos pero sin puntos
                        bajar(id, autoAsignado);
                        retorno = 0;
                        System.out.println("AutosChocadores cerró durante el viaje, " + id + " se va a deambular");
                    }
                } else {
                    // Si cerró antes de iniciar, cancelamos el viaje
                    asientos--;
                    esperando--;
                    System.out.println("AutosChocadores cerró antes de iniciar el viaje, " + id + " se va a deambular");
                }
            } else {
                // Si cerró mientras esperábamos en la cola
                System.out.println("AutosChocadores cerró mientras " + id + " esperaba, se va a deambular");
            }
        } else {
            // Si la atracción ya estaba cerrada al intentar entrar
            System.out.println("AutosChocadores está cerrado, " + id + " se va a deambular");
        }
        return retorno;
    }

    public synchronized void iniciarAutos() throws InterruptedException {
        // Esperamos a que haya 20 visitantes o a que cierren la atracción
        while (esperando < 20 || !abierto) {
            wait();
        }
        // Marcamos que el viaje está en curso
        enCurso = true;
        // Notificamos a todos para que sepan que comenzó el viaje
        notifyAll();
        System.out.println("AUTOS CHOCADORES LLENOS, INICIANDO ATRACCION...");
    }

    public synchronized void terminarAutos() throws InterruptedException {
        // Marcamos que el viaje terminó
        enCurso = false;
        // Notificamos a los visitantes que ya pueden bajar
        notifyAll();
        // Esperamos a que los 20 visitantes bajen completamente
        while (bajaron < 20 && abierto) {
            wait();
        }
        // Reseteamos los contadores para la próxima tanda
        esperando = 0;
        bajaron = 0;
        asientos = 0;
        notifyAll();
        System.out.println("AUTOS CHOCADORES TERMINADOS, LISTOS PARA NUEVA TANDA...");
    }

    private synchronized void bajar(int id, int autoAsignado) {
        // Registramos que el visitante se bajó
        System.out.println("Visitante " + id
                + " se bajo del auto " + autoAsignado);
        bajaron++;
        // Si los 20 visitantes ya bajaron, notificamos al simulador que puede continuar
        if (bajaron == 20) {
            notifyAll();
        }
    }

    // Cierra la atracción para que no entren más visitantes
    public synchronized void cerrar() {
        abierto = false;
        notifyAll();
    }

    // Reabre la atracción
    public synchronized void abrir() {
        abierto = true;
        notifyAll();
    }

    // Verifica si la atracción está vacía (sin visitantes ni en movimiento)
    public synchronized boolean estaVacio() {
        return !enCurso && esperando == 0;
    }
}
