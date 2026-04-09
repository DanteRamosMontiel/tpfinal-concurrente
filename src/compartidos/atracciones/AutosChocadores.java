package compartidos.atracciones;

public class AutosChocadores {

    private int esperando = 0; // personas sentadas
    private int bajaron = 0; // personas que bajaron
    private int asientos = 0; // para asignar autos
    private boolean enCurso = false;
    private boolean abierto = true;

    public synchronized int entrarAutosChocadores(int id) throws InterruptedException {
        int retorno = 0;
        if (abierto) {
            while (esperando == 20 && abierto) {
                wait();
            }
            if (abierto) {
                int autoAsignado = asientos / 2;
                asientos++;
                esperando++;
                System.out.println("Visitante " + id + " se sentó en el auto " + autoAsignado);
                if (esperando == 20) {
                    notifyAll();
                }
                while (!enCurso && abierto) {
                    wait();
                }
                if (abierto) {
                    while (enCurso && abierto) {
                        wait();
                    }
                    if (abierto) {
                        bajar(id, autoAsignado);
                        retorno = 13; 
                    }else{
                        bajar(id, autoAsignado);
                        retorno=0;
                        System.out.println("AutosChocadores cerró durante el viaje, " + id + " se va a deambular");
                    }
                }else{

                    asientos--;
                    esperando--;
                    System.out.println("AutosChocadores cerró antes de iniciar el viaje, " + id + " se va a deambular");
                }
            } else {
                System.out.println("AutosChocadores cerró mientras " + id + " esperaba, se va a deambular");
            }
        } else {
            System.out.println("AutosChocadores está cerrado, " + id + " se va a deambular");
        }
        return retorno; 
    }

    public synchronized void iniciarAutos() throws InterruptedException {
        while (esperando < 20 || !abierto) {
            wait();
        }

        enCurso = true;
        notifyAll(); 
        System.out.println("AUTOS CHOCADORES LLENOS, INICIANDO ATRACCION...");

    }

    public synchronized void terminarAutos() throws InterruptedException {
        enCurso = false;
        notifyAll(); 
        while (bajaron < 20 && abierto) {
            wait();
        }
        esperando = 0;
        bajaron = 0;
        asientos = 0;
        notifyAll(); 
        System.out.println("AUTOS CHOCADORES TERMINADOS, LISTOS PARA NUEVA TANDA...");
    }

    private synchronized void bajar(int id, int autoAsignado) {
        System.out.println("Visitante " + id
                + " se bajo del auto " + autoAsignado);
        bajaron++;
        if (bajaron == 20) {
            notifyAll();
        }
    }

     
    public synchronized void cerrar() {
        abierto = false;
        notifyAll();
    }

     
    public synchronized void abrir() {
        abierto = true;
        notifyAll();
    }

    
    public synchronized boolean estaVacio() {
        return !enCurso && esperando == 0;
    }
}