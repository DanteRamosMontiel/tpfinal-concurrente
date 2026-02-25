package activos.simulaciones;

import compartidos.Parque;

public class simulacionMontania extends Thread{
    private Parque parque;

    public simulacionMontania(Parque xParque){
        this.parque = xParque;
    }

    public void run(){
        while(true){
            try{
               parque.iniciarViajeMontania();;
               System.out.println("MONTAÑA RUSA LLENA. INICIANDO VIAJE...");
               Thread.sleep(7000);
                  System.out.println("VIAJE TERMINADO DE MONTAÑA RUSA...");
               Thread.sleep(2000);
               parque.terminarViajeMontania();
            }catch(InterruptedException e){}
        }
    }
}