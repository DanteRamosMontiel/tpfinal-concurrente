package compartidos;

import activos.Visitante;
import compartidos.atracciones.*;
import compartidos.extras.recorridoAGomones;
import compartidos.shopping.*;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ConcurrentHashMap;

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
    private CarreraGomones carreraGomones;

    // Shopping
    private AreaPremios areaPremios;
    private Comedor comedor;
    private Espectaculo espectaculo;

    // Extras
    private Random rand;

    // recurso para tren y bicis
    private recorridoAGomones recorrido;

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
        this.rand = new Random();
        this.realidadVirtual = new RealidadVirtual(5, 10, 5);
        this.areaPremios = new AreaPremios();
        this.comedor = new Comedor();
        this.espectaculo = new Espectaculo();
        this.carreraGomones = new CarreraGomones(10);        
        // inicializar el recurso compartido para recorridos en gomones
        this.recorrido = new recorridoAGomones();
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

    //-----------------------Métodos de montania rusa ---------------------------------
    // Para visitantes
    public boolean entrarMontania(Visitante v) throws InterruptedException{
        return montaniaRusa.entrar(v);
    }

    public int subirVagonMontania(int id) throws InterruptedException{
       return montaniaRusa.subirAlVagon(id);
    }

    //Para monitoreo
    public void iniciarViajeMontania() throws InterruptedException{
      montaniaRusa.iniciarViaje();
    }

    public void terminarViajeMontania() throws InterruptedException{
     montaniaRusa.terminarViaje();  
    }

    //-----------------------Métodos de recorrido en gomones (tren + bicis) --------
    //visitante usa el tren
    public void subirTren(int id) throws InterruptedException {
        recorrido.subirTren(id);
    }

    // hilo tren llama a este método en bucle para procesar tandas
    public void gestionarTren() throws InterruptedException {
        recorrido.cicloTren();
    }

    // visitante usa una bicicleta
    public void usarBicicleta(int id) throws InterruptedException {
        recorrido.usarBicicleta(id);
    }

    // cada hilo bicicleta llama esto para atender a un visitante
    public void gestionarBicicleta(int bikeId) throws InterruptedException {
        recorrido.cicloBicicleta(bikeId);
    }

    //-----------------------Métodos de autos chocadores ---------------------------------
    // Para visitantes

    public int entrarAutosChocadores(int id) throws InterruptedException{
      return autosChocadores.entrarAutosChocadores(id);
    }

    //Para monitoreo
    public void iniciarViajeAutosC() throws InterruptedException{
      autosChocadores.iniciarAutos();
    }

    public void terminarViajeAutosC() throws InterruptedException{
      autosChocadores.terminarAutos();
    }

    //-----------------------Métodos de realidad virtual ---------------------------------
    // Para visitantes
    public void entrarRealidadVirtual(int id) throws InterruptedException{
      realidadVirtual.entrar(id);
    }

    public int salirRealidadVirtual(int id){
      return realidadVirtual.salir(id);
    }

    
    //----------------------Metodos para Gomones---------------------------------
    public void usarGomon(int id) throws InterruptedException {
        carreraGomones.usarGomon(id);
    }
    public int[] CicloGomon(int gomonId,int cantVisitantes) throws InterruptedException {
        return carreraGomones.cicloGomones(gomonId, cantVisitantes);
    }

    public void finCicloGomon(int gomonId,int[] visitantes) throws InterruptedException {
        carreraGomones.finCicloGomones(gomonId, visitantes);
    }
    public ConcurrentHashMap<Integer, Object> esperarBolsosCamion() throws InterruptedException {
        return carreraGomones.esperarBolsosCamion();
    }

    public void finCamion() throws InterruptedException {
        carreraGomones.finViajeCamion();
    }

    //-----------------------Métodos de tienda de premios ---------------------------------
    //------------------ Para visitantes y asistente de premios----------------------------
    public int entrarTiendaPremios(int id,int n) throws InterruptedException{
      return areaPremios.canjearPremio(id, n);
    }


}