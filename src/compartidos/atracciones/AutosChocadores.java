package compartidos.atracciones;

public class AutosChocadores {

    private int esperando = 0;     // personas sentadas
    private int bajaron = 0;       // personas que bajaron
    private int asientos = 0;      // para asignar autos
    private boolean enCurso = false;

    public synchronized int entrarAutosChocadores(int id) throws InterruptedException {

        // Espera si ya hay 20 personas
        while (esperando == 20) {
            wait();
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

        // Espera a que inicie la atracción
        while (!enCurso) {
            wait();
        }

        // Espera a que termine
        while (enCurso) {
            wait();
        }

        bajar(id, autoAsignado);

        return 13; //puntos ganados por el visitante al jugar a los autos chocadores
    }

    public synchronized void iniciarAutos() throws InterruptedException {

        // Espera hasta que haya 20 personas
        while (esperando < 20) {
            wait();
        }

        enCurso = true;
        notifyAll(); // despierta pasajeros
    }

    public synchronized void terminarAutos() throws InterruptedException {

        enCurso = false;
        notifyAll(); // permite que bajen

        // Espera a que bajen los 20
        while (bajaron < 20) {
            wait();
        }

        // Reset
        esperando = 0;
        bajaron = 0;
        asientos = 0;

        notifyAll(); // habilita nueva tanda
    }

    private synchronized void bajar(int id, int autoAsignado) {

        System.out.println("Visitante " + id
                + " se bajo del auto " + autoAsignado);

        bajaron++;

        if (bajaron == 20) {
            notifyAll();
        }
    }
}