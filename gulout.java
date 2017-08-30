import java.io.*;
import java.util.*;
import java.lang.ProcessBuilder.*;
import java.nio.*;


public class gulout
{
    private static boolean hasReceivedDummy(InputStream pis, int lastEventId) throws IOException{
        byte[] eventHeader =  new byte[8];
        byte[] sample =  new byte[8];
        int currentEventId;
        int sidx;
        boolean gotData = false;
        
        while ((pis.read(eventHeader)) != -1){
            currentEventId = ByteBuffer.wrap(eventHeader).order(ByteOrder.LITTLE_ENDIAN).getInt();
            System.err.println("lastEventId = " + lastEventId);
            System.err.println("currentEventId = " + currentEventId);
            
            if(currentEventId == lastEventId){gotData = true;}  //make sure we have the data we were expecting
            
            if(currentEventId == -2 && gotData){return true;}
            
            
            while ((pis.read(sample)) != -1){
                
                sidx = ByteBuffer.wrap(sample).order(ByteOrder.LITTLE_ENDIAN).getInt();
                
                if(sidx == 0){  //break for null sample, indicates end of sample data.
                    break;
                }
                
                if((currentEventId != -2) && (currentEventId != -3)){   //print the output of fm except the dummies.
                    System.err.println(currentEventId + "  " + sidx);
                }
                
            }
        }
      
        return false;
    }
    
    public static void main(String [] args){
       
        try
        {
            
            ProcessBuilder pb = new ProcessBuilder("fmcalc");
            pb.redirectError(Redirect.INHERIT);
            Process p = pb.start();
            OutputStream pos = p.getOutputStream();
            InputStream pis = p.getInputStream();
            
            byte[] stream_id = new byte[8];
            
            if ((System.in.read(stream_id)) != -1){
                pos.write(stream_id);
            }
            
            pos.flush();
            
            if ((pis.read(stream_id)) == -1){System.err.println("error reading fm stream_id & no samples");}
            
            
            byte[] eventHeader =  new byte[8];
            byte[] sample =  new byte[8];
            
            ByteBuffer bb = ByteBuffer.allocate(32);
            bb.order(ByteOrder.LITTLE_ENDIAN).putInt(-2).putInt(1).putInt(-3).putInt(0).putInt(-1).putInt(0).putInt(0).putInt(0);
            byte[] dummy = bb.array();
            
            ByteBuffer bb2 = ByteBuffer.allocate(32);
            bb2.order(ByteOrder.LITTLE_ENDIAN).putInt(-3).putInt(1).putInt(-3).putInt(0).putInt(-1).putInt(0).putInt(0).putInt(0);
            byte[] dummy2 = bb2.array();
            

            int lastEventId = 0;
            int currentEventId;
            int sidx;
            boolean firstRun = true;
            
            while ((System.in.read(eventHeader)) != -1){
                
                currentEventId = ByteBuffer.wrap(eventHeader).order(ByteOrder.LITTLE_ENDIAN).getInt();
                if(firstRun){lastEventId = currentEventId;}
                
                
                if (currentEventId != lastEventId && !firstRun){    
                    pos.write(dummy);
                    pos.write(dummy2);  
                    pos.flush();        //we need a flush after almost every write.
                    
                    if(hasReceivedDummy(pis, lastEventId)){
                        Thread.sleep(1000);     //sleep for 1 sec after each event.
                    }
                    
                    
                    lastEventId = currentEventId;
                }
                
                
                pos.write(eventHeader);
                pos.flush();
                
                while ((System.in.read(sample)) != -1) {
                    
                    pos.write(sample);
                    pos.flush();
                    
                    sidx = java.nio.ByteBuffer.wrap(sample).order(ByteOrder.LITTLE_ENDIAN).getInt();
                    if (sidx == 0){
                        break;
                    }
                    
                }
                
                if (firstRun){firstRun = false;}
            }
        }
        
        catch (FileNotFoundException e)
        {
            System.out.println ("File not found");
        }
        
        catch (IOException e)
        {
            System.out.println ("I/O Problem: " + e.getMessage ());
        }
        
        catch(InterruptedException ex) 
        {
            Thread.currentThread().interrupt();
        }
    }
}

