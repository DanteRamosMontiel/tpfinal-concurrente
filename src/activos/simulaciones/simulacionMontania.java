package activos.simulaciones;

import compartidos.atracciones.MontaniaRusa;

public class simulacionMontania extends Thread{
    private MontaniaRusa montania;

    public simulacionMontania(MontaniaRusa xMontania){
        this.montania = xMontania;
    }

    public void run(){
        while(true){
            try{
               montania.iniciarViaje();
               System.out.println("MONTAÑA RUSA LLENA. INICIANDO VIAJE...");
               Thread.sleep(7000);
               System.out.println("VIAJE TERMINADO.");
               Thread.sleep(2000);
               montania.terminarViaje();
            }catch(InterruptedException e){}
        }
    }
}