public class Course {
    private String courseName;
    private String duration;

    public Course(String courseName, String duration) {
        this.courseName = courseName;
        this.duration = duration;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getDuration() {
        return duration;
    }
}
    	 	 		 								public class Student {
    private String name;
    private Course course;

    public Student(String name, Course course) {
        this.name = name;
        this.course = course;
    }

    public void displayInfo() {
        System.out.println("Student Name: " + name);
        System.out.println("Course Name: " + course.getCourseName());
        System.out.println("Course Duration: " + course.getDuration());
    }
}
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public Course course() {
        return new Course("Java Programming", "3 Months");
    }

    @Bean
    public Student student() {
        return new Student("Avneet", course());
    }
}
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainApp {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        Student student = context.getBean(Student.class);
        student.displayInfo();
    }
}
