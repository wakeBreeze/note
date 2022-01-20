package zx.normal.函数式编程.optional;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @Description: zx.normal.函数式编程.optional
 * @version: 1.0
 */
public class demo01 {
    static class Subject{

        private int score;
        private String subjectName;

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public void setSubjectName(String subjectName) {
            this.subjectName = subjectName;
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

        System.out.println("================");
        Student student1 = new Student();
        Student student2 = new Student();
        List<Student> StudentList = new ArrayList<>();
        StudentList.add(student);
        StudentList.add(student1);
        StudentList.add(student2);

        Optional.ofNullable(StudentList).ifPresent(list -> {
            for (Student stu : list) {

                Integer integer = Optional.ofNullable(stu)
                        .map(Student::getSubject)
                        .map(Subject::getScore)
                        .orElse(null);
                System.out.println(integer);
//                System.out.println(stu.subject.score);
            }
        });
    }
}
