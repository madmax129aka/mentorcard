package com.mentortrack.seed;

import com.mentortrack.config.SeedProperties;
import com.mentortrack.domain.*;
import com.mentortrack.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

/**
 * Seeds demo data on startup so the full import -> upload -> download flow is demoable
 * immediately, per the spec's build instructions (step 12). Controlled by
 * mentortrack.seed.enabled (default true); set to false for a real deployment.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final SeedProperties seedProperties;
    private final StudentRepository studentRepository;
    private final AdminUserRepository adminUserRepository;
    private final SubjectRepository subjectRepository;
    private final MarkRepository markRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(SeedProperties seedProperties,
                       StudentRepository studentRepository,
                       AdminUserRepository adminUserRepository,
                       SubjectRepository subjectRepository,
                       MarkRepository markRepository,
                       PasswordEncoder passwordEncoder) {
        this.seedProperties = seedProperties;
        this.studentRepository = studentRepository;
        this.adminUserRepository = adminUserRepository;
        this.subjectRepository = subjectRepository;
        this.markRepository = markRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedProperties.isEnabled()) {
            log.info("Seed data disabled (mentortrack.seed.enabled=false)");
            return;
        }

        seedSubjects();
        seedAdmin();
        List<Student> students = seedStudents();
        seedSampleCatMarks(students);

        log.info("Demo seed data loaded: {} subjects, {} students, admin account 'admin'/'admin123'",
                subjectRepository.count(), students.size());
    }

    private void seedSubjects() {
        if (subjectRepository.count() > 0) {
            return;
        }
        for (SeedSubject s : SeedSubjectData.ALL) {
            Subject subject = new Subject();
            subject.setSemesterNumber(s.semester());
            subject.setSubjectCode(s.subjectCode());
            subject.setName(s.name());
            subject.setDisplayOrder(s.displayOrder());
            subjectRepository.save(subject);
        }
    }

    private void seedAdmin() {
        if (adminUserRepository.findByUsername("admin").isPresent()) {
            return;
        }
        AdminUser admin = new AdminUser();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        adminUserRepository.save(admin);
    }

    private List<Student> seedStudents() {
        if (studentRepository.count() > 0) {
            return studentRepository.findAll();
        }
        String[] names = {
                "Aarav Sharma", "Priya Nair", "Rohan Iyer", "Sneha Reddy", "Karthik Menon",
                "Ananya Rao", "Vikram Singh", "Divya Krishnan", "Arjun Pillai", "Meera Suresh",
                "Nikhil Varma", "Lakshmi Narayanan", "Siddharth Kumar", "Pooja Desai", "Rahul Bose"
        };
        Random random = new Random(42); // deterministic demo DOBs
        List<Student> saved = new java.util.ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            String regNo = "21CSE" + String.format("%03d", i + 1);
            Student student = new Student();
            student.setRegNo(regNo);
            student.setName(names[i]);
            student.setPasswordHash(passwordEncoder.encode(regNo)); // default password = reg no
            student.setForcePasswordChange(true);
            student.setDob(LocalDate.of(2003, 1 + random.nextInt(12), 1 + random.nextInt(28)));
            student.setBloodGroup(new String[]{"A+", "B+", "O+", "AB+", "O-"}[random.nextInt(5)]);
            student.setHobbies("Reading");
            student.setGames("Cricket");
            student.setLiterary("Debate");
            student.setCommunity("NCC");
            student.setMentorName("Dr. Faculty Mentor");
            student.setAdmittedOn("07/2021");
            saved.add(studentRepository.save(student));
        }
        return saved;
    }

    /** Pre-populate a small amount of CAT-marks data for Semester I so the dashboard has something
     * to show immediately, without requiring the admin to import an Excel file first. The admin
     * import flow (with its own match/unmatched summary) remains the primary path for real use. */
    private void seedSampleCatMarks(List<Student> students) {
        if (markRepository.count() > 0) {
            return;
        }
        List<Subject> sem1Subjects = subjectRepository.findBySemesterNumberOrderByDisplayOrderAsc(1);
        Random random = new Random(7);
        for (Student student : students) {
            for (Subject subject : sem1Subjects) {
                Mark mark = new Mark();
                mark.setStudent(student);
                mark.setSubject(subject);
                mark.setCat1((double) (12 + random.nextInt(9))); // 12-20
                mark.setCat2((double) (12 + random.nextInt(9)));
                mark.setCat3((double) (12 + random.nextInt(9)));
                mark.setSource(MarkSource.EXCEL_IMPORT);
                markRepository.save(mark);
            }
        }
    }
}
