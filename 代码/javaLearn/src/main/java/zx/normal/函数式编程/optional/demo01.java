package zx.normal.函数式编程.optional;

import javax.security.auth.Subject;
import java.util.Optional;

/**
 * @Description: zx.normal.函数式编程.optional
 * @version: 1.0
 */
public class demo01 {
    static class Subject{
        private int score;

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }
    }
    static class Student{
        private Subject subject;

        public Subject getSubject() {
            return subject;
        }

        public void setSubject(Subject subject) {
            this.subject = subject;
        }
    }

    /**
     * 普通方式判空
     * @param student
     * @return
     */
    public static Integer getScoreNormal(Student student){
        if (student != null){       // 第一层 null判空
            Subject subject = student.getSubject();
            if (subject != null){   // 第二层 null判空
                return subject.score;
            }
        }
        return null;
    }

    /**
     * Optional方式判空
     * @param student
     * @return
     */
    public static Integer getScoreOptional(Student student){
        return Optional.ofNullable(student)
                .map(Student::getSubject)
                .map(Subject::getScore)
                .orElse(null);
    }
    public static void main(String[] args) {
        Subject subject = new Subject();
        Student student = new Student();
        subject.score = 100;
        student.subject = subject;

        System.out.println(getScoreNormal(student));
        System.out.println(getScoreOptional(student));
    }
}
