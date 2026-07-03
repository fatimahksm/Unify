package com.university.unify.network;

public class ApiConfig {

    public static final String BASE_URL = "http://collegeapp.atwebpages.com/";

    public static final String GET_FACULTIES = BASE_URL + "faculties/get_faculties.php";
    public static final String GET_MAJORS_BY_FACULTY = BASE_URL + "majors/get_majors_by_faculty.php";

    public static final String REGISTER_USER = BASE_URL + "users/register_user.php";

    public static final String GET_USER_BY_EMAIL = BASE_URL + "users/get_user_by_email.php";

    public static final String LOGIN_USER = BASE_URL + "users/login_user.php";

    public static final String ADD_FACULTY = BASE_URL + "faculties/add_faculty.php";
    public static final String UPDATE_FACULTY = BASE_URL + "faculties/update_faculty.php";
    public static final String DELETE_FACULTY = BASE_URL + "faculties/delete_faculty.php";

    public static final String GET_FACULTY_ADMINS = BASE_URL + "faculty_admins/get_faculty_admins.php";

    public static final String DELETE_FACULTY_ADMIN = BASE_URL + "faculty_admins/delete_faculty_admin.php";
    public static final String UPDATE_FACULTY_ADMIN = BASE_URL + "faculty_admins/update_faculty_admin.php";
    public static final String GET_ADMIN_DASHBOARD_COUNTS = BASE_URL + "dashboard/get_admin_dashboard_counts.php";

    public static final String GET_COURSES = BASE_URL + "courses/get_courses.php";
    public static final String ADD_COURSE = BASE_URL + "courses/add_course.php";

    public static final String CREATE_FACULTY_ADMIN = BASE_URL + "users/create_faculty_admin.php";

    public static final String COMPLETE_FACULTY_ADMIN_PROFILE =
            BASE_URL + "users/complete_faculty_admin_profile.php";

    public static final String UPDATE_FIREBASE_UID =
            BASE_URL + "users/update_firebase_uid.php";




    public static final String ADD_MAJOR = BASE_URL + "majors/add_major.php";
    public static final String DELETE_MAJOR = BASE_URL + "majors/delete_major.php";

    public static final String GET_INSTRUCTORS_BY_FACULTY = BASE_URL + "instructors/get_instructors_by_faculty.php";
    public static final String ADD_INSTRUCTOR = BASE_URL + "instructors/add_instructor.php";
    public static final String DELETE_INSTRUCTOR = BASE_URL + "instructors/delete_instructor.php";

    public static final String GET_PENDING_STUDENTS_BY_FACULTY = BASE_URL + "students/get_pending_students_by_faculty.php";
    public static final String APPROVE_STUDENT = BASE_URL + "students/approve_student.php";
    public static final String REJECT_STUDENT = BASE_URL + "students/reject_student.php";

    public static final String GET_USER_PROFILE = BASE_URL + "users/get_user_profile.php";

    public static final String UPDATE_MAJOR = BASE_URL + "majors/update_major.php";

    public static final String COMPLETE_INSTRUCTOR_PROFILE = BASE_URL + "instructors/complete_instructor_profile.php";

    public static final String GET_INSTRUCTOR_COURSES = BASE_URL + "instructors/get_instructor_courses.php";

    public static final String GET_COURSE_STUDENTS = BASE_URL + "instructors/get_course_students.php";

    public static final String UPDATE_COURSE =
            BASE_URL + "courses/update_course.php";

    public static final String UPDATE_COURSE_SCHEDULE =
            BASE_URL + "courses/update_course_schedule.php";

    public static final String CREATE_ANNOUNCEMENT = BASE_URL + "announcements/create_announcement.php";
    public static final String GET_ANNOUNCEMENTS = BASE_URL + "announcements/get_course_announcements.php";

    public static final String DELETE_ANNOUNCEMENT = BASE_URL + "announcements/delete_announcement.php";

    public static final String GET_INSTRUCTOR_HOME_STATS = BASE_URL + "instructors/get_instructor_home_stats.php";

    public static final String UPDATE_STUDENT_GRADE = BASE_URL + "instructors/update_student_grade.php";

    public static final String GET_STUDENT_COURSES =
            BASE_URL + "students/get_student_courses.php";

    public static final String GET_COURSE_PARTICIPANTS =
            BASE_URL + "students/get_course_participants.php";

    public static final String GET_STUDENT_COURSE_GRADE =
            BASE_URL + "students/get_student_course_grade.php";

    public static final String GET_STUDENT_PROFILE =
            BASE_URL + "students/get_student_profile.php";
    public static final String GET_STUDENT_ANNOUNCEMENTS =
            BASE_URL + "students/get_student_announcements.php";

    public static final String GET_AVAILABLE_COURSES =
            BASE_URL + "students/get_available_courses.php";

    public static final String ENROLL_COURSE =
            BASE_URL + "students/enroll_course.php";

    public static final String ADD_COURSE_SCHEDULE =
            BASE_URL + "courses/add_course_schedule.php";

    public static final String GET_COURSE_SCHEDULES =
            BASE_URL + "courses/get_course_schedules.php";

    public static final String DELETE_COURSE_SCHEDULE =
            BASE_URL + "courses/delete_course_schedule.php";

    public static final String ASSIGN_INSTRUCTOR_TO_COURSE =
            BASE_URL + "courses/assign_instructor_to_course.php";

    public static final String GET_COURSE_INSTRUCTORS =
            BASE_URL + "courses/get_course_instructors.php";

    public static final String REMOVE_COURSE_INSTRUCTOR =
            BASE_URL + "courses/remove_course_instructor.php";

    public static final String ADD_COURSE_MATERIAL =
            BASE_URL + "materials/add_material.php";

    public static final String GET_COURSE_MATERIALS =
            BASE_URL + "materials/get_course_materials.php";

    public static final String DELETE_COURSE_MATERIAL =
            BASE_URL + "materials/delete_material.php";

    public static final String UPLOAD_MATERIAL_PDF =
            BASE_URL + "materials/upload_material_pdf.php";

    public static final String UPLOAD_PROFILE_IMAGE =
            BASE_URL + "profile/upload_profile_image.php";





    public static final String CREATE_STUDENT_POST =
            BASE_URL + "social/create_post.php";

    public static final String GET_FACULTY_POSTS =
            BASE_URL + "social/get_faculty_posts.php";

    public static final String TOGGLE_POST_LIKE =
            BASE_URL + "social/toggle_like.php";

    public static final String GET_POST_COMMENTS =
            BASE_URL + "social/get_post_comments.php";

    public static final String ADD_POST_COMMENT =
            BASE_URL + "social/add_comment.php";

    public static final String STUDENT_AI_CHATBOT =
            BASE_URL + "chatbot/student_ai_chatbot.php";

    public static final String STUDENT_AI_CONTEXT =
            BASE_URL + "chatbot/student_ai_context.php";

}
