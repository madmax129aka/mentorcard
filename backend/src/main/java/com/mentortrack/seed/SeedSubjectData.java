package com.mentortrack.seed;

import java.util.List;

/**
 * The CSE Semester I-VIII subject list exactly as printed on the real Mentor Card template
 * (see /docs/PDF_FIELD_MAP.md "Subject names per semester" for the extraction method and full
 * transcription notes). Subject codes are synthetic (CSxxx) since the template itself does not
 * print subject codes — they only exist in this system to key the CAT-marks Excel import and the
 * semester-marksheet OCR confirm flow.
 */
public final class SeedSubjectData {

    private SeedSubjectData() {
    }

    public static final List<SeedSubject> ALL = List.of(
            // Semester I
            new SeedSubject(1, "CS101", "Technical English I", 1),
            new SeedSubject(1, "CS102", "Mathematics I", 2),
            new SeedSubject(1, "CS103", "Engg. Physics I", 3),
            new SeedSubject(1, "CS104", "Engg. Chemistry I", 4),
            new SeedSubject(1, "CS105", "Basic Electrical & Electronics Engg.", 5),
            new SeedSubject(1, "CS106", "Basic Mechanical & Civil Engg.", 6),
            new SeedSubject(1, "CS107", "C Programming and MS Office Tools", 7),
            new SeedSubject(1, "CS108", "Orientation to Entrepreneurship & Project Lab", 8),

            // Semester II
            new SeedSubject(2, "CS201", "Mathematics II", 1),
            new SeedSubject(2, "CS202", "Solid State Physics", 2),
            new SeedSubject(2, "CS203", "Technical Chemistry", 3),
            new SeedSubject(2, "CS204", "Engineering Graphics", 4),
            new SeedSubject(2, "CS205", "Fundamentals of Computer Engineering", 5),
            new SeedSubject(2, "CS206", "Communicative English Lab", 6),
            new SeedSubject(2, "CS207", "Python Programming", 7),
            new SeedSubject(2, "CS208", "Environmental Science (Audit Course)", 8),

            // Semester III
            new SeedSubject(3, "CS301", "Data Structures", 1),
            new SeedSubject(3, "CS302", "Database Management System", 2),
            new SeedSubject(3, "CS303", "Digital Principles and System Design", 3),
            new SeedSubject(3, "CS304", "Basic Electrical Engineering", 4),
            new SeedSubject(3, "CS305", "Universal Human Values: Understanding Harmony", 5),
            new SeedSubject(3, "CS306", "Data Structures Lab", 6),
            new SeedSubject(3, "CS307", "Database Management System Lab", 7),
            new SeedSubject(3, "CS308", "Digital Systems Lab", 8),
            new SeedSubject(3, "CS309", "Object Oriented Programming with C++", 9),

            // Semester IV
            new SeedSubject(4, "CS401", "Statistics for Computer Engineers", 1),
            new SeedSubject(4, "CS402", "Design and Analysis of Algorithms", 2),
            new SeedSubject(4, "CS403", "Operating System", 3),
            new SeedSubject(4, "CS404", "Microprocessor and Microcontrollers", 4),
            new SeedSubject(4, "CS405", "The Indian Constitution / Traditional Knowledge (Audit Course)", 5),
            new SeedSubject(4, "CS406", "Microprocessor and Microcontrollers Lab", 6),
            new SeedSubject(4, "CS407", "Design and Analysis of Algorithms Lab", 7),
            new SeedSubject(4, "CS408", "Operating System Lab", 8),
            new SeedSubject(4, "CS409", "Java Programming", 9),
            new SeedSubject(4, "CS410", "Technical Skill I", 10),
            new SeedSubject(4, "CS411", "Soft Skill I - Employability Skills", 11),

            // Semester V
            new SeedSubject(5, "CS501", "Computer Organization and Architecture", 1),
            new SeedSubject(5, "CS502", "Computer Networks", 2),
            new SeedSubject(5, "CS503", "Principles of Compiler Design", 3),
            new SeedSubject(5, "CS504", "Program Elective I", 4),
            new SeedSubject(5, "CS505", "Open Elective I", 5),
            new SeedSubject(5, "CS506", "Online Course (NPTEL/SWAYAM/AICTE-UGC approved MOOC)", 6),
            new SeedSubject(5, "CS507", "Network Programming Lab", 7),
            new SeedSubject(5, "CS508", "Compiler Design Lab", 8),
            new SeedSubject(5, "CS509", "User Experience Design", 9),
            new SeedSubject(5, "CS510", "Technical Skill II", 10),

            // Semester VI
            new SeedSubject(6, "CS601", "Object Oriented Software Engineering", 1),
            new SeedSubject(6, "CS602", "Web Design using PHP & MySQL", 2),
            new SeedSubject(6, "CS603", "Artificial Intelligence", 3),
            new SeedSubject(6, "CS604", "Program Elective II", 4),
            new SeedSubject(6, "CS605", "Open Elective II", 5),
            new SeedSubject(6, "CS606", "Object Oriented Software Engineering Lab", 6),
            new SeedSubject(6, "CS607", "Web Design using PHP & MySQL Lab", 7),
            new SeedSubject(6, "CS608", "Soft Skill II - Qualitative and Quantitative Skills", 8),
            new SeedSubject(6, "CS609", "Technical Skill III", 9),
            new SeedSubject(6, "CS610", "Mini Project / Internship", 10),

            // Semester VII
            new SeedSubject(7, "CS701", "Big Data Analytics", 1),
            new SeedSubject(7, "CS702", "Program Elective III", 2),
            new SeedSubject(7, "CS703", "Connected Business (Elective)", 3),
            new SeedSubject(7, "CS704", "Cloud Computing", 4),
            new SeedSubject(7, "CS705", "Machine Learning", 5),
            new SeedSubject(7, "CS706", "Open Lab", 6),
            new SeedSubject(7, "CS707", "Data Analytics Lab using Machine Learning Algorithms", 7),
            new SeedSubject(7, "CS708", "Cloud Computing Lab", 8),
            new SeedSubject(7, "CS709", "Project Phase I", 9),
            new SeedSubject(7, "CS710", "Foreign Language", 10),

            // Semester VIII
            new SeedSubject(8, "CS801", "Principles of Management and Behavioral Science", 1),
            new SeedSubject(8, "CS802", "Program Elective IV", 2),
            new SeedSubject(8, "CS803", "Program Elective V", 3),
            new SeedSubject(8, "CS804", "Project Phase II", 4)
    );
}
