package compartidos;

import activos.Visitante;
import compartidos.atracciones.*;
import compartidos.shopping.*;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Parque {

    // Semaforo
    private Semaphore[] molinetes;
    private Semaphore chequeoHorario;

    // Horarios
    private boolean abierto;
    private int horaActual;

    // Atracciones
    private AutosChocadores autosChocadores;
    private MontaniaRusa montaniaRusa;
    private RealidadVirtual realidadVirtual;

    // Shopping
    private AreaPremios areaPremios;
    private Comedor comedor;
    private Espectaculo espectaculo;

    // Extras
    private Random rand;

    // Constructor
    public Parque(int cantMolinetes) {
        this.molinetes = new Semaphore[cantMolinetes];
        for (int i = 0; i < molinetes.length; i++) {
            molinetes[i] = new Semaphore(1);
        }
        this.chequeoHorario = new Semaphore(1);
        this.abierto = true;
        this.horaActual = 9;
        this.autosChocadores = new AutosChocadores();
        this.montaniaRusa = new MontaniaRusa();
        this.realidadVirtual = new RealidadVirtual();
        this.areaPremios = new AreaPremios();
        this.comedor = new Comedor();
        this.espectaculo = new Espectaculo();
        this.rand = new Random();
    }

    public int tomarMolinete() throws InterruptedException {
        int indiceFinal = 0;
        chequeoHorario.acquire();
        if (!abierto) {
            chequeoHorario.release();
            indiceFinal = -1;
        } else {
            int i = 0;
            boolean pudoTomarMolinete = false;
            while (i < molinetes.length && !pudoTomarMolinete) {
                if (molinetes[i].tryAcquire()) {
                    pudoTomarMolinete = true;
                    indiceFinal = i;
                }
                i++;
            }
            if (!pudoTomarMolinete) {
                int random = rand.nextInt((molinetes.length) - 1);
                molinetes[random].acquire();
                indiceFinal = random;
            }
            chequeoHorario.release();
        }

        return indiceFinal;
    }

    public void dejarMolinete(int i) {
        molinetes[i].release();
    }

    // Métodos
    public void cambiarHora() throws InterruptedException {
        chequeoHorario.acquire();
        horaActual++;
        switch (horaActual) {
            case 9:
                this.abierto = true;
                System.out.println("EL PARQUE ABRIO");
                chequeoHorario.release();
                break;

            case 18:
                this.abierto = false;
                System.out.println("EL PARQUE CERRO");
                chequeoHorario.release();
                break;

            case 19:
                System.out.println("LAS ATRACCIONES CERRARON");
                chequeoHorario.release();
                // cerrarAtracciones();
                break;
                
            default:
                chequeoHorario.release();
                break;
        }
    }

    private void cerrarAtracciones() throws InterruptedException {
        // autosChocadores.cerrar();
        // montaniaRusa.cerrar();
        // realidadVirtual.cerrar();
    }

    private void hecharGente() throws InterruptedException {

    }

    //Métodos de montania rusa
    //Para visitantes
    public boolean entrarMontania(Visitante v) throws InterruptedException{
        return montaniaRusa.entrar(v);
    }

    public Visitante subirVagonMontania() throws InterruptedException{
        return montaniaRusa.subirAlVagon();
    }

    public void esperarMontania() throws InterruptedException{
        montaniaRusa.esperarMontania();
    }

    public void bajarMontania() throws InterruptedException{
        montaniaRusa.bajar();
    }

    // //Para monitoreo
    // public void iniciarViajeMontania() throws InterruptedException{
    //     montaniaRusa.iniciarViaje();
    // }

    // public void terminarViajeMontania(){
    //     montaniaRusa.terminarViaje();   
    // }














}