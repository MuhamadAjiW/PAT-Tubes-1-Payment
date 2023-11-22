package payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        try {
            try{
                AbandonedConnectionCleanupThread.checkedShutdown();
            } catch (Exception e){
                System.out.println("Mysql failed to initialize");
                e.printStackTrace();
            }
            System.out.println("Server is running");
        } catch (Exception e){
            System.out.println("Server error");
            e.printStackTrace();
        }

        SpringApplication.run(Main.class, args);
    }

}
