package compartidos;

import activos.Visitante;
import compartidos.atracciones.*;
import compartidos.extras.recorridoAGomones;
import compartidos.shopping.*;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class Parque {

    // Semaforo
    private Semaphore[] molinetes;
    private Semaphore chequeoHorario;

    // Horarios
    private boolean abierto; // indica si el parque permite nuevos accesos por molinete
    private int horaActual;
    private boolean expulsarVisitantes; // a las 23 los visitantes deben irse
    private boolean atraccionesAbiertas; // control de estado de las atracciones
    private Object lockEspectaculo = new Object(); // Lock para sincronización de espectáculo

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
    // lock para que visitantes esperen la reapertura sin busy-wait
    private final Object aperturaLock = new Object();

    // Fichas CG pendientes por visitante (otorgadas al ganador de gomones)
    private final ConcurrentHashMap<Integer, Integer> fichasCGPendientes = new ConcurrentHashMap<>();

    // Indica que el parque cerró definitivamente: los hilos de visitante deben terminar
    private volatile boolean cerradoDefinitivamente = false;

    public boolean parqueCerradoDefinitivamente() {
        return cerradoDefinitivamente;
    }

    // Constructor
    public Parque(int cantMolinetes, int cantGomones) {
        this.molinetes = new Semaphore[cantMolinetes];
        for (int i = 0; i < molinetes.length; i++) {
            molinetes[i] = new Semaphore(1);
        }
        this.chequeoHorario = new Semaphore(1);
        this.abierto = true;
        this.horaActual = 9;
        this.expulsarVisitantes = false;
        this.atraccionesAbiertas = true;
        this.autosChocadores = new AutosChocadores();
        this.montaniaRusa = new MontaniaRusa();
        this.rand = new Random();
        this.realidadVirtual = new RealidadVirtual(5, 10, 5);
        this.areaPremios = new AreaPremios();
        this.comedor = new Comedor(5);
        this.espectaculo = new Espectaculo();
        this.carreraGomones = new CarreraGomones(cantGomones);
        this.recorrido = new recorridoAGomones();
    }

    public int tomarMolinete() throws InterruptedException {
        int indiceFinal = 0;
        chequeoHorario.acquire();
        if (!abierto || expulsarVisitantes) {
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
        if (horaActual == 24) {
            horaActual = 0;
        }
        System.out.println(">> [PARQUE] Hora actual: " + horaActual + ":00");
        switch (horaActual) {
            case 9:
                this.abierto = true;
                this.expulsarVisitantes = false;
                System.out.println(">> [PARQUE] EL PARQUE ABRIO");
                abrirAtracciones();
                // Inicia el primer espectáculo
                espectaculo.iniciarEspectaculo();
                synchronized (lockEspectaculo) {
                    lockEspectaculo.notifyAll();
                }
                // notificar a visitantes que estaban esperando la reapertura
                synchronized (aperturaLock) {
                    aperturaLock.notifyAll();
                }
                chequeoHorario.release();
                break;

            case 12:
                System.out.println(">> [PARQUE] ¡SE ABREN LAS PUERTAS DEL ESPECTACULO!");
                espectaculo.iniciarEspectaculo();
                synchronized (lockEspectaculo) {
                    lockEspectaculo.notifyAll();
                }
                chequeoHorario.release();
                break;
            case 13:
                System.out.println("[PARQUE] SE INICIA EL ESPECTACULO");
                espectaculo.empezarEspectaculo();
                chequeoHorario.release();
                break;
            case 14:
                System.out.println("[PARQUE] FINALIZO EL ESPECTACULO");
                espectaculo.finEspectaculo();
                chequeoHorario.release();
                break;
            case 15:
                System.out.println(">> [PARQUE] ¡SE ABREN LAS PUERTAS DEL ESPECTACULO!");
                espectaculo.iniciarEspectaculo();
                synchronized (lockEspectaculo) {
                    lockEspectaculo.notifyAll();
                }
                chequeoHorario.release();
                break;
            case 16:
                System.out.println("[PARQUE] SE INICIA EL ESPECTACULO");
               espectaculo.empezarEspectaculo();
                chequeoHorario.release();
                break;
            case 17:
                System.out.println("[PARQUE] FINALIZO EL ESPECTACULO");
                espectaculo.finEspectaculo();
                chequeoHorario.release();
                break;
            case 18:
                this.abierto = false;
                System.out.println(">> [PARQUE] EL PARQUE CERRO PARA NUEVOS INGRESOS");
                // Pero ocurre el espectáculo de las 18
                System.out.println(">> [PARQUE] ¡SE ABREN LAS PUERTAS DEL ESPECTACULO!");
                espectaculo.iniciarEspectaculo();
                synchronized (lockEspectaculo) {
                    lockEspectaculo.notifyAll();
                }
                chequeoHorario.release();
                break;

            case 21:
                System.out.println(">> [PARQUE] ¡SE ABREN LAS PUERTAS DEL ESPECTACULO!");
                espectaculo.iniciarEspectaculo();
                synchronized (lockEspectaculo) {
                    lockEspectaculo.notifyAll();
                }
                chequeoHorario.release();
                break;
            case 20:
                System.out.println("[PARQUE] FINALIZO EL ESPECTACULO");
                espectaculo.finEspectaculo();
                chequeoHorario.release();
                break;

            case 19:
                System.out.println(">> [PARQUE] LAS ATRACCIONES CERRARON");
                chequeoHorario.release();
                cerrarAtracciones();
                System.out.println("[PARQUE] SE INICIA EL ESPECTACULO");
                espectaculo.empezarEspectaculo();
                break;
            case 22:
                System.out.println("[PARQUE] SE INICIA EL ESPECTACULO");
                espectaculo.empezarEspectaculo();
                chequeoHorario.release();
                break;
            case 23:
                System.out.println(">> [PARQUE] ES HORA DE SACAR A TODOS LOS VISITANTES");
                expulsarVisitantes = true;
                hecharGente();
                System.out.println("[PARQUE] FINALIZO EL ESPECTACULO");
                espectaculo.finEspectaculo();
                chequeoHorario.release();
                break;
            default:
                chequeoHorario.release();
                break;
        }
    }

    private void cerrarAtracciones() throws InterruptedException {
        atraccionesAbiertas = false;
        autosChocadores.cerrar();
        montaniaRusa.cerrar();
        realidadVirtual.cerrar();
        carreraGomones.cerrar();
        // cerrar recursos de recorrido (tren/bicicletas)
        recorrido.cerrar();
        comedor.cerrar();
        areaPremios.cerrar();

        // No esperamos activo por completo; los visitantes dentro detectarán el cierre
        // y saldrán pronto. Evitamos bloquear la hora indefinidamente.
        System.out.println("[PARQUE]Atracciones marcadas como cerradas. Los visitantes serán desalojados.");
    }

    private void hecharGente() throws InterruptedException {
        cerradoDefinitivamente = true;
        System.out.println("[PARQUE] Se ha marcado la orden de expulsión; los visitantes terminarán su ciclo pronto.");
        // Despertar visitantes bloqueados en esperarApertura()
        synchronized (aperturaLock) {
            aperturaLock.notifyAll();
        }
        // Despertar visitantes/asistentes bloqueados dentro del espectáculo
        espectaculo.cerrarDefinitivamente();
        // Despertar asistentes bloqueados esperando el próximo espectáculo
        synchronized (lockEspectaculo) {
            lockEspectaculo.notifyAll();
        }
        // Dar tiempo suficiente para que los visitantes detecten la señal y finalicen
        Thread.sleep(5000);
    }

    /**
     * Bloquea el visitante hasta que el parque deje de expulsar visitantes
     * (reapertura). Si el parque cerró definitivamente, retorna de inmediato.
     */
    public void esperarApertura() throws InterruptedException {
        synchronized (aperturaLock) {
            while (expulsarVisitantes && !cerradoDefinitivamente) {
                aperturaLock.wait();
            }
        }
    }

    private void abrirAtracciones() {
        atraccionesAbiertas = true;
        autosChocadores.abrir();
        montaniaRusa.abrir();
        realidadVirtual.abrir();
        carreraGomones.abrir();
        recorrido.abrir();
        comedor.abrir();
        areaPremios.abrir();
        System.out.println("[PARQUE]Las atracciones volvieron a abrir en la mañana.");
    }

    public boolean estanAtraccionesAbiertas() {
        return atraccionesAbiertas;
    }

    public boolean debeExpulsarVisitantes() {
        return expulsarVisitantes;
    }

    // -----------------------Métodos de montania rusa
    // ---------------------------------
    // Para visitantes
    public boolean entrarMontania(Visitante v) throws InterruptedException {
        return montaniaRusa.entrar(v);
    }

    public int subirVagonMontania(int id) throws InterruptedException {
        return montaniaRusa.subirAlVagon(id);
    }

    // Para monitoreo
    public void iniciarViajeMontania() throws InterruptedException {
        montaniaRusa.iniciarViaje();
    }

    public void terminarViajeMontania() throws InterruptedException {
        montaniaRusa.terminarViaje();
    }

    // -----------------------Métodos de recorrido en gomones (tren + bicis)
    // --------
    // visitante usa el tren
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

    // -----------------------Métodos de autos chocadores
    // ---------------------------------
    // Para visitantes

    public int entrarAutosChocadores(int id) throws InterruptedException {
        return autosChocadores.entrarAutosChocadores(id);
    }

    // Para monitoreo
    public void iniciarViajeAutosC() throws InterruptedException {
        autosChocadores.iniciarAutos();
    }

    public void terminarViajeAutosC() throws InterruptedException {
        autosChocadores.terminarAutos();
    }

    // -----------------------Métodos de realidad virtual
    // ---------------------------------
    // Para visitantes
    public void entrarRealidadVirtual(int id) throws InterruptedException {
        realidadVirtual.entrar(id);
    }

    public int salirRealidadVirtual(int id) {
        return realidadVirtual.salir(id);
    }

    // ----------------------Metodos para Gomones---------------------------------
    public void usarGomon(int id) throws InterruptedException {
        carreraGomones.usarGomon(id);
    }

    public int[] CicloGomon(int gomonId, int cantVisitantes) throws InterruptedException {
        return carreraGomones.cicloGomones(gomonId, cantVisitantes);
    }

    public boolean finCicloGomon(int gomonId, int[] visitantes) throws InterruptedException {
        return carreraGomones.finCicloGomones(gomonId, visitantes);
    }

    /** El hilo Gomones llama a este método cuando su gomón ganó la carrera */
    public void otorgarFichasCG(int[] visitantes, int fichas) {
        for (int idVisitante : visitantes) {
            fichasCGPendientes.merge(idVisitante, fichas, Integer::sum);
            System.out.println("[GOMONES] Visitante " + idVisitante + " recibe " + fichas + " fichas CG por ganar la carrera");
        }
    }

    /** El visitante llama a este método al salir de gomones para retirar sus fichas CG ganadas */
    public int retirarFichasCG(int idVisitante) {
        Integer fichas = fichasCGPendientes.remove(idVisitante);
        return fichas != null ? fichas : 0;
    }

    public ConcurrentHashMap<Integer, Object> esperarBolsosCamion() throws InterruptedException {
        return carreraGomones.esperarBolsosCamion();
    }

    public void finCamion() throws InterruptedException {
        carreraGomones.finViajeCamion();
    }

    // -----------------------Métodos de tienda de premios
    // ---------------------------------
    // Para visitantes
    public int entrarTiendaPremios(int id, int n) throws InterruptedException {
        return areaPremios.canjearPremio(id, n);
    }

    // Para el asistente de premios: recibe la función de cálculo de saldo
    public void atenderTiendaPremios(java.util.function.IntUnaryOperator calcularSaldo) throws InterruptedException {
        areaPremios.atenderVisitante(calcularSaldo);
    }

    // -----------------------Métodos de comedor ---------------------------------
    public Object[] entrarComedor() throws Exception {
        return comedor.entrar();
    }

    public void salirComedor(int id) throws InterruptedException {
        comedor.salir(id);
    }

    // -----------------------Métodos de espectáculo
    // ---------------------------------
    /**
     * Verifica si hay espectáculo disponible para que entren los visitantes
     */
    public boolean hayEspectaculoParaEntrar() {
        return espectaculo.hayEspectaculoDisponible();
    }

    /**
     * Los visitantes intentan entrar al espectáculo
     */
    public void entrarEspectaculo(int idVisitante) throws InterruptedException {
        espectaculo.entrarVisitante(idVisitante);
    }

    /**
     * Los asistentes esperan a que los visitantes estén listos dentro del
     * espectáculo
     */
    public void asistenteEsperaVisitantes() throws InterruptedException {
        espectaculo.asistenteEsperaVisitantes();
    }

    /**
     * Los asistentes entran al espectáculo
     */
    public void entrarAsistentesEspectaculo(int idAsistente) throws InterruptedException {
        espectaculo.entrarAsistentes();
    }

    /**
     * Simula el espectáculo durante 1 hora
     */
    public void simularEspectaculo() throws InterruptedException {
        espectaculo.simularEspectaculo();
    }

    /**
     * Los asistentes esperan hasta que sea hora del próximo espectáculo.
     * Retorna false si el parque cerró definitivamente (el asistente debe terminar).
     */
    public boolean esperarProximoEspectaculo() throws InterruptedException {
        synchronized (lockEspectaculo) {
            while (!espectaculo.hayEspectaculoDisponible() && !cerradoDefinitivamente) {
                lockEspectaculo.wait();
            }
        }
        return !cerradoDefinitivamente;
    }

    public void finalizarEspectaculo() throws InterruptedException {
        espectaculo.finEspectaculo();
    }

    public void iniciarEspectaculo() throws InterruptedException {
        espectaculo.iniciarEspectaculo();
    }
}