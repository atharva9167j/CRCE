import java.util.*;
class Thread1 extends Thread{

public void run(){ for(int i=0;i<=5;i++){
System.out.println("The value of i is "+i);
}
}

}
 
class Thread2 extends Thread{ public void run(){
for(int j=0;j<=5;j++){ System.out.println("The value of j is "+j);
}
}

}
public class MT{
public static void main(String[] args){ Thread1 t1 = new Thread1(); Thread2 t2 = new Thread2(); t1.start();
t2.start();
}
}


// ===================================================
import java.util.*;
class Thread1 implements Runnable{ public void run(){
System.out.println("Priority of the thread is "+Thread.currentThread().getPriority()); if(Thread.currentThread().isDaemon()){
System.out.println("Daemon thread work");
}
else{
System.out.println("user thread work");
}

for(int i=0;i<=5;i++){ System.out.println("The value of i is "+i);
}

}
}

class Thread2 implements Runnable{ public void run(){

System.out.println("Priority of the thread is "+Thread.currentThread().getPriority());
 
if(Thread.currentThread().isDaemon()){ System.out.println("Daemon thread work");
}
else{
System.out.println("user thread work");
}

for(int j=0;j<=5;j++){ System.out.println("The value of j is "+j); if(j==2){

try{ Thread.sleep(3000);
}catch(InterruptedException e){ System.out.println(e);
}

}
}

}
}
public class MT2{
public static void main(String[] args){

Thread t3 = new Thread(new Thread1()); Thread t4 = new Thread(new Thread2());

System.out.println(t3.getName()); System.out.println(t4.getName()); t3.setName("ithread"); t4.setName("jthread"); System.out.println(t3.getName()); System.out.println(t4.getName());

t3.setPriority(Thread.MAX_PRIORITY); t4.setPriority(Thread.MIN_PRIORITY);

t3.start(); try{ t4.join();
t4.start();
}catch(InterruptedException e){ System.out.println(e);
 
}

}
}
