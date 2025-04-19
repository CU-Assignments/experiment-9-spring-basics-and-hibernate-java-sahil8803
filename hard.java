<hibernate-configuration>
    <session-factory>
        <property name="hibernate.connection.driver_class">com.mysql.cj.jdbc.Driver</property>
        <property name="hibernate.connection.url">jdbc:mysql://localhost:3306/bankdb</property>
        <property name="hibernate.connection.username">root</property>
        <property name="hibernate.connection.password">your_password</property>

        <property name="hibernate.dialect">org.hibernate.dialect.MySQL8Dialect</property>
        <property name="hibernate.hbm2ddl.auto">update</property>
        <property name="show_sql">true</property>

        <mapping class="Account"/>
        <mapping class="TransactionRecord"/>
    </session-factory>
</hibernate-configuration>


import jakarta.persistence.*;

@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String holderName;
    private double balance;

    public Account() {}

    public Account(String holderName, double balance) {
        this.holderName = holderName;
        this.balance = balance;
    }

    // Getters and setters...
    public int getId() { return id; }
    public String getHolderName() { return holderName; }
    public double getBalance() { return balance; }

    public void setHolderName(String name) { this.holderName = name; }
    public void setBalance(double balance) { this.balance = balance; }
}


import jakarta.persistence.*;
import java.util.Date;

@Entity
public class TransactionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int fromAccount;
    private int toAccount;
    private double amount;
    private Date date;

    public TransactionRecord() {}

    public TransactionRecord(int from, int to, double amount) {
        this.fromAccount = from;
        this.toAccount = to;
        this.amount = amount;
        this.date = new Date();
    }

    // Getters and setters...
}


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class AccountDao {
    private SessionFactory factory;

    public AccountDao() {
        factory = new Configuration().configure().buildSessionFactory();
    }

    public Session getSession() {
        return factory.getCurrentSession();
    }

    public Account getAccount(int id) {
        return getSession().get(Account.class, id);
    }

    public void saveTransaction(TransactionRecord tr) {
        getSession().persist(tr);
    }

    public void updateAccount(Account acc) {
        getSession().update(acc);
    }

    public void createAccount(Account acc) {
        Session session = factory.openSession();
        session.beginTransaction();
        session.save(acc);
        session.getTransaction().commit();
        session.close();
    }
}


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    @Autowired
    private AccountDao dao;

    @Transactional
    public void transferMoney(int fromId, int toId, double amount) {
        Account from = dao.getAccount(fromId);
        Account to = dao.getAccount(toId);

        if (from.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance!");
        }

        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);

        dao.updateAccount(from);
        dao.updateAccount(to);

        TransactionRecord tr = new TransactionRecord(fromId, toId, amount);
        dao.saveTransaction(tr);
    }
}


import org.springframework.context.annotation.*;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

@Configuration
@ComponentScan(basePackages = "com.example")
@EnableTransactionManagement
public class AppConfig {

    @Bean
    public SessionFactory sessionFactory() {
        return new Configuration().configure().buildSessionFactory();
    }

    @Bean
    public HibernateTransactionManager transactionManager() {
        return new HibernateTransactionManager(sessionFactory());
    }

    @Bean
    public AccountDao accountDao() {
        return new AccountDao();
    }

    @Bean
    public AccountService accountService() {
        return new AccountService();
    }
}


import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainApp {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        AccountDao dao = context.getBean(AccountDao.class);
        AccountService service = context.getBean(AccountService.class);

        // Uncomment below to add initial accounts
        /*
        dao.createAccount(new Account("Avneet", 1000));
        dao.createAccount(new Account("Karan", 500));
        */

        try {
            service.transferMoney(1, 2, 300);
            System.out.println("Transaction successful!");
        } catch (Exception e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        context.close();
    }
}
